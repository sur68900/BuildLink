package com.build.link.sdk

import android.content.Context
import com.facebook.applinks.AppLinkData
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FacebookSdk {

    suspend fun getDeep(id: String, token: String, context: Context, logger: (Throwable) -> Unit) = suspendCoroutine { continuation ->
        com.facebook.FacebookSdk.apply {
            setApplicationId(id)
            setClientToken(token)
            @Suppress("DEPRECATION")
            sdkInitialize(context)
            setAdvertiserIDCollectionEnabled(true)
            setAutoInitEnabled(true)
            fullyInitialize()
        }
        AppLinkData.fetchDeferredAppLinkData(context) {
            try {
                continuation.resume(it?.targetUri.toString())
            } catch (e: Exception) {
                logger(e)
                continuation.resume(null)
            }
        }
    }

}