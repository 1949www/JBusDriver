package me.jbusdriver.component.magnet.loader

import me.jbusdriver.component.magnet.bean.Magnet
import org.jsoup.Jsoup

class BtdiggsMagnetLoaderImpl : IMagnetLoader {
    //  key -> page
    private val search = "https://www.btdigg.xyz/search/%s/%s/1/0.html"

    override var hasNexPage: Boolean = true


    override fun loadMagnets(key: String, page: Int): List<Magnet> {
        val doc = Jsoup.connect(search.format(encode(key), page)).initHeaders().get()
        hasNexPage = doc.select(".page-split :last-child[title]").size > 0
        return doc.select(".list dl").map {
            val href = it.select("dt a")
            val title = href.text()
            val url = href.attr("href")

            val realUrl = when {
                url.startsWith("www.") -> "https://$url"
                url.startsWith("/magnet") -> {
                    IMagnetLoader.MagnetFormatPrefix + url.removePrefix("/magnet/").removeSuffix(".html")
                }
                else -> "https://www.btdigg.xyz$url"
            }

            val labels = it.select(".attr span")
            Magnet(title, labels.component2().text(), labels.component1().text(), realUrl).apply {
                if (!realUrl.startsWith(IMagnetLoader.MagnetFormatPrefix)){
                    this.linkLoader = {
                        (IMagnetLoader.MagnetFormatPrefix + Jsoup.connect(realUrl).get().select(".content .infohash").text().trim())
                    }
                }
            }
        }

    }
}