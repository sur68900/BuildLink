package com.build.link.builder

import android.app.Activity
import android.content.Context
import com.appsflyer.AppsFlyerLib
import com.build.link.BuildResult
import com.build.link.repository.SdkRepository
import com.onesignal.OneSignal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder

class Builder {
    private val sdkRepository = SdkRepository()

    private lateinit var fbId: String
    private lateinit var fbToken: String
    private lateinit var fbDecrypt: String
    private lateinit var devKey: String
    private lateinit var domain: String
    private lateinit var notId: String
    private lateinit var sub10: String
    private lateinit var activity: Activity
    private lateinit var logger: (Throwable) -> Unit

    fun activity(activity: Activity) = apply { this.activity = activity }
    fun domain(domain: String) = apply { this.domain = domain }
    fun fbId(fbId: String) = apply { this.fbId = fbId }
    fun fbToken(fbToken: String) = apply { this.fbToken = fbToken }
    fun fbKey(fbDecrypt: String) = apply { this.fbDecrypt = fbDecrypt }
    fun appsKey(devKey: String) = apply { this.devKey = devKey }
    fun sub10(sub10: String) = apply { this.sub10 = sub10 }
    fun notId(notId: String) = apply { this.notId = notId }
    fun logger(logger: (Throwable) -> Unit) = apply { this.logger = logger }
    fun getAppsId(context: Context) = AppsFlyerLib.getInstance().getAppsFlyerUID(context)

    suspend fun buildLink() = try {
        sdkRepository.setupApps(activity, devKey)
        sdkRepository.setDeep(sdkRepository.getDeep(fbId, fbToken, activity, logger))
        val validLink = domain + getDevice() + getAttr()
        sendTag(sdkRepository.getAfUserId(), sdkRepository.getPush())
        BuildResult.Success(validLink)
    } catch (e: Exception) {
        logger(e)
        BuildResult.Error(e)
    }

    private fun sendTag(userId: String, push: String?) {
        val validPush = push?.takeIf { it != NULL } ?: ORGANIC
        OneSignal.setExternalUserId(userId)
        OneSignal.sendTag(SUB_APP, validPush)
    }

    private fun getAttr() = sdkRepository.getAttr(notId)
        .replace(Regex("(?<=$SUB10=)[^&]*"), sub10)
        .replace(Regex("(?<=$NOT_ID=)[^&]*"), notId)

    private suspend fun getDevice() = sdkRepository.getDeviceInfo(
        activity,
        getReferrer(),
        fbId,
        fbToken,
        devKey,
        logger
    )

    private suspend fun getReferrer() = try {
        withContext(Dispatchers.IO) {
            URLEncoder.encode(sdkRepository.getRef(activity, fbDecrypt, logger), UTF8)
        }
    } catch (e: Exception) {
        null
    }

    companion object {
        const val MIN_SUBS = 10
        const val ORGANIC = "organic"
        const val NOT_ID = "notId"
        const val AD_ID = "ad_id"
        const val ADSET = "adset"
        const val ADSET_ID = "adset_id"
        const val AF_STATUS = "af_status"
        const val AF_CHANNEL = "af_channel"
        const val AF_AD = "af_ad"
        const val CAMPAIGN = "campaign"
        const val CAMPAIGN_ID = "campaign_id"
        const val MEDIA_SOURCE = "media_source"
        const val NULL = "null"
        const val NONE = "None"
        const val MYAPP = "myapp://"
        const val SUB = "sub"
        const val UTF8 = "utf-8"
        const val PUSH = "push"
        const val SUB_APP = "sub_app"
        const val ACCOUNT_ID = "account_id"
        const val DECODE_ERROR_MESSAGE = "Must have an even length"
        const val DATA = "data"
        const val NONCE = "nonce"
        const val SOURCE = "source"
        const val UTM_CONTENT = "utm_content="
        const val ALGORITHM = "AES/GCM/NoPadding"
        const val AF_USER_ID = "af_userid"
        const val BATTERY = "battery"
        const val ADB = "adb"
        const val BUNDLE = "bundle"
        const val DEV_KEY_TITLE = "dev_key"
        const val FB_APP_ID_TITLE = "fb_app_id"
        const val FB_AT_TITLE = "fb_at"
        const val SUB10 = "sub10"
        const val GOOGLE_AD_ID = "google_adid"
    }

}