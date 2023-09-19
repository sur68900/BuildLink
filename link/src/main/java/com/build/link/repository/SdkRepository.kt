package com.build.link.repository

import android.app.Activity
import android.content.Context
import com.build.link.sdk.AppsFlyerSdk
import com.build.link.sdk.DeviceInfo
import com.build.link.sdk.FacebookSdk
import com.build.link.sdk.ReferrerSdk

class SdkRepository {
    private val apps = AppsFlyerSdk()
    private val deep = FacebookSdk()
    private val referrer = ReferrerSdk()
    private val device = DeviceInfo()

    suspend fun setupApps(activity: Activity, devKey: String) = apps.setupApps(activity, devKey)
    fun getAttr(notId: String?) = apps.getAttributes(notId)
    fun setDeep(deep: String?) = apps.setDeep(deep)
    fun getPush() = apps.getPush()

    suspend fun getDeep(id: String, token: String, context: Context, logger: (Throwable) -> Unit) =
        deep.getDeep(id, token, context, logger)

    suspend fun getRef(context: Context, decrypt: String, logger: (Throwable) -> Unit) =
        referrer.getId(context, decrypt, logger)

    fun getDeviceInfo(context: Context, ref: Any?, id: String, token: String, devKey: String, logger: (Throwable) -> Unit): String {
        return device.getInfo(context, ref, id, token, devKey, logger)
    }

    fun getAfUserId() = device.getAfUserId()
}