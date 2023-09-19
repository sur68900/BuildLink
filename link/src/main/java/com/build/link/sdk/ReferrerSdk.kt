package  com.build.link.sdk

import android.content.Context
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.build.link.builder.Builder.Companion.ACCOUNT_ID
import com.build.link.builder.Builder.Companion.ALGORITHM
import com.build.link.builder.Builder.Companion.DATA
import com.build.link.builder.Builder.Companion.DECODE_ERROR_MESSAGE
import com.build.link.builder.Builder.Companion.NONCE
import com.build.link.builder.Builder.Companion.NULL
import com.build.link.builder.Builder.Companion.SOURCE
import com.build.link.builder.Builder.Companion.UTF8
import com.build.link.builder.Builder.Companion.UTM_CONTENT
import org.json.JSONObject
import java.net.URLDecoder
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReferrerSdk {

    suspend fun getId(context: Context, decrypt: String, logger: (Throwable) -> Unit): String? {
        return suspendCoroutine { continuation ->
            val mReferrerClient = InstallReferrerClient.newBuilder(context).build()
            startReferrerConnection(mReferrerClient, decrypt, logger) { continuation.resume(it) }
        }
    }

    private fun startReferrerConnection(mReferrerClient: InstallReferrerClient, decrypt: String, logger: (Throwable) -> Unit, result: (String?) -> Unit) {
        try {
            mReferrerClient.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            result(getReferrerData(mReferrerClient.installReferrer.installReferrer, decrypt))
                        }
                        else -> result(null)
                    }
                    mReferrerClient.endConnection()
                }
                override fun onInstallReferrerServiceDisconnected() { result(null) }
            })
        } catch (e: Exception) {
            logger(e)
            result(null)
        }

    }

    private fun getReferrerData(str: String, decrypt: String): String? {
        if (!str.contains(UTM_CONTENT)) return null
        return try {
            val urlForDecode = URLDecoder.decode(str.split(UTM_CONTENT)[1], UTF8)
            val jsonUrl = JSONObject(urlForDecode)
            val source = JSONObject(jsonUrl[SOURCE].toString())
            val data = source[DATA]
            val nonce = source[NONCE]
            val message = data.toString().decodeHex()
            val keyFacebook = decrypt.decodeHex()
            val specKey = SecretKeySpec(keyFacebook, ALGORITHM)
            val secretKeyFacebookFromReferrer = nonce.toString().decodeHex()
            val nonceSpec = IvParameterSpec(secretKeyFacebookFromReferrer)
            val c = Cipher.getInstance(ALGORITHM)
            c.init(Cipher.DECRYPT_MODE, specKey, nonceSpec)
            val resultString = JSONObject(String(c.doFinal(message)))
            resultString.get(ACCOUNT_ID).toString()
        } catch (e: Exception) {
            NULL
        }
    }

    private fun String.decodeHex(): ByteArray {
        check(length % 2 == 0) { DECODE_ERROR_MESSAGE }
        return chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}