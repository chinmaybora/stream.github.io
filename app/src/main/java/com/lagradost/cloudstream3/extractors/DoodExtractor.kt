package com.lagradost.cloudstream3.extractors

import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.Qualities
import kotlinx.coroutines.delay

class DoodToExtractor : DoodLaExtractor() {
    override var mainUrl = "https://dood.to"
}

class DoodSoExtractor : DoodLaExtractor() {
    override var mainUrl = "https://dood.so"
}

class DoodWsExtractor : DoodLaExtractor() {
    override var mainUrl = "https://dood.ws"
}


open class DoodLaExtractor : ExtractorApi() {
    override var name = "DoodStream"
    override var mainUrl = "https://dood.la"
    override val requiresReferer = false

    override fun getExtractorUrl(id: String): String {
        return "$mainUrl/d/$id"
    }

    override suspend fun getUrl(url: String, referer: String?): List<ExtractorLink>? {
        val id = url.removePrefix("$mainUrl/e/").removePrefix("$mainUrl/d/")
        val trueUrl = getExtractorUrl(id)
        val response = app.get(trueUrl).text
        Regex("href=\".*/download/(.*?)\"").find(response)?.groupValues?.get(1)?.let { link ->
            if (link.isEmpty()) return null
            delay(5000) // might need this to not trigger anti bot
            val downloadLink = "$mainUrl/download/$link"
            val downloadResponse = app.get(downloadLink).text
            Regex("onclick=\"window\\.open\\((['\"])(.*?)(['\"])").find(downloadResponse)?.groupValues?.get(2)
                ?.let { trueLink ->
                    return listOf(
                        ExtractorLink(
                            trueLink,
                            this.name,
                            trueLink,
                            mainUrl,
                            Qualities.Unknown.value,
                            false
                        )
                    ) // links are valid in 8h
                }
        }

        return null
    }
}
