package  com.build.link.sdk

import android.content.Context
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.build.link.builder.Builder.Companion.ADSET_ID
import com.build.link.builder.Builder.Companion.AF_AD
import com.build.link.builder.Builder.Companion.AF_CHANNEL
import com.build.link.builder.Builder.Companion.AF_STATUS
import com.build.link.builder.Builder.Companion.CAMPAIGN
import com.build.link.builder.Builder.Companion.CAMPAIGN_ID
import com.build.link.builder.Builder.Companion.ADSET
import com.build.link.builder.Builder.Companion.AD_ID
import com.build.link.builder.Builder.Companion.MEDIA_SOURCE
import com.build.link.builder.Builder.Companion.MIN_SUBS
import com.build.link.builder.Builder.Companion.MYAPP
import com.build.link.builder.Builder.Companion.NONE
import com.build.link.builder.Builder.Companion.NOT_ID
import com.build.link.builder.Builder.Companion.NULL
import com.build.link.builder.Builder.Companion.PUSH
import com.build.link.builder.Builder.Companion.SUB
import com.build.link.builder.Builder.Companion.UTF8
import java.net.URLDecoder
import java.net.URLEncoder
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AppsFlyerSdk {
    private var afStatus: String? = null
    private val keys = listOf(CAMPAIGN, MEDIA_SOURCE, AF_CHANNEL, AF_STATUS, AF_AD, CAMPAIGN_ID, ADSET_ID, ADSET, AD_ID)
    private val result = mutableMapOf<String, String>()

    suspend fun setupApps(context: Context, devKey: String) = suspendCoroutine { continuation ->
        val appsConversationListener = MyAppsFlyerConversionListener(continuation)
        AppsFlyerLib.getInstance().init(devKey, appsConversationListener, context).start(context)
    }

    fun setDeep(deepLink: String?) {
        val campaign = deepLink.takeIf { it != "" && it != NULL && it != null } ?: result.getOrDefault(CAMPAIGN, NULL)
        result[CAMPAIGN] = campaign
        setupSubs(campaign)
    }

    fun getAttributes(notId: String?): String {
        val validSb = StringBuilder()
        result[NOT_ID] = notId ?: NULL
        result.forEach { (key, value) -> validSb.append("$key=${URLEncoder.encode(value, UTF8)}&") }
        return validSb.toString().dropLast(1)
    }

    fun getPush() = result[PUSH]

    private fun setupSubs(campaign2: String) {
        var campaign = campaign2
        campaign = URLDecoder.decode(campaign, UTF8)

        var defaultSubs = ArrayList<Sub>()
        var push = Sub(PUSH, null)
        for (index in 1..MIN_SUBS) {
            if (index == 1) defaultSubs.add(Sub("$SUB$index", NULL)) else defaultSubs.add(Sub("$SUB$index", ""))
        }
        if (campaign != NULL && campaign != "" && campaign != NONE) {
            val subsCampaign = campaign.split(MYAPP).last().split("_").mapIndexed { index, s ->
                val subIndex = if (index == 0) "${SUB}1" else if (index == 1) PUSH else SUB + index
                Sub(subIndex, s)
            }.toMutableList()
            if (subsCampaign.size > 1) push = subsCampaign.removeAt(1)
            if (subsCampaign.size >= defaultSubs.size) defaultSubs = ArrayList(subsCampaign) else {
                subsCampaign.forEachIndexed { index, sub -> defaultSubs[index] = sub }
            }
        }
        defaultSubs.add(push)
        defaultSubs.forEach{ result[it.name] = it.value.toString() }
    }

    inner class MyAppsFlyerConversionListener(private val continuation: Continuation<MutableMap<String, String>>) : AppsFlyerConversionListener {
        override fun onConversionDataSuccess(data: MutableMap<String, Any?>?) { returnData(data) }
        override fun onConversionDataFail(error: String?) { returnData() }
        override fun onAppOpenAttribution(data: MutableMap<String, String>?) { returnData() }
        override fun onAttributionFailure(error: String?) { returnData() }

        private fun returnData(data: MutableMap<String, Any?>? = null) {
            if (afStatus == null) {
                afStatus = data?.get(AF_STATUS).toString()
                continuation.resume(getData(data))
            }
        }

        private fun getData(values: MutableMap<String, Any?>?): MutableMap<String, String> {
            keys.forEach { result[it] = values?.getOrDefault(it, NULL).toString() }
            return result
        }
    }

    data class Sub(val name: String, var value: String?)
}

