package com.build.link.sdk

import android.content.Context
import android.content.Context.BATTERY_SERVICE
import android.os.BatteryManager
import android.provider.Settings
import com.appsflyer.AppsFlyerLib
import com.build.link.builder.Builder.Companion.GOOGLE_AD_ID
import com.build.link.builder.Builder.Companion.AF_USER_ID
import com.build.link.builder.Builder.Companion.ACCOUNT_ID
import com.build.link.builder.Builder.Companion.ADB
import com.build.link.builder.Builder.Companion.BATTERY
import com.build.link.builder.Builder.Companion.BUNDLE
import com.build.link.builder.Builder.Companion.FB_APP_ID_TITLE
import com.build.link.builder.Builder.Companion.FB_AT_TITLE
import com.build.link.builder.Builder.Companion.DEV_KEY_TITLE
import com.google.android.gms.ads.identifier.AdvertisingIdClient

class DeviceInfo {
    private lateinit var userId: String

    fun getInfo(
        context: Context,
        referrerResult: Any?,
        fbId: String,
        fbToken: String,
        devKey: String,
        logger: (Throwable) -> Unit
    ): String {
        val result = hashMapOf<String, String?>()
        result[GOOGLE_AD_ID] = getAdId(context, logger).toString()
        result[AF_USER_ID] = createAfUserId(context, logger).toString()
        result[ADB] = getAdb(context, logger).toString()
        result[BATTERY] = getBatteryStatus(context, logger).toString()
        result[BUNDLE] = context.packageName.toString()
        result[FB_APP_ID_TITLE] = fbId
        result[FB_AT_TITLE] = fbToken
        result[DEV_KEY_TITLE] = devKey
        result[ACCOUNT_ID] = referrerResult.toString()

        userId = result[AF_USER_ID].toString()
        return result.entries.joinToString(separator = "&", postfix = "&") { "${it.key}=${it.value}" }
    }

    fun getAfUserId() = userId

    private fun createAfUserId(context: Context, logger: (Throwable) -> Unit) = try {
        AppsFlyerLib.getInstance().getAppsFlyerUID(context)
    } catch (e: Exception) {
        logger(e)
        null
    }

    private fun getAdb(context: Context, logger: (Throwable) -> Unit) = try {
        Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
    } catch (e: Exception) {
        logger(e)
        true
    }

    private fun getAdId(context: Context, logger: (Throwable) -> Unit) = try {
        AdvertisingIdClient.getAdvertisingIdInfo(context).id
    } catch (e: Exception) {
        logger(e)
        null
    }

    private fun getBatteryStatus(context: Context, logger: (Throwable) -> Unit) = try {
        val bm = context.getSystemService(BATTERY_SERVICE) as BatteryManager
        val batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        batLevel.toFloat()
    } catch (e: Exception) {
        logger(e)
        100.0f
    }
}