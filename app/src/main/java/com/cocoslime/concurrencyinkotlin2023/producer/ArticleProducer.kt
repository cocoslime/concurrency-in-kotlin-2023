package com.cocoslime.concurrencyinkotlin2023.producer

import com.cocoslime.concurrencyinkotlin2023.logError
import com.cocoslime.concurrencyinkotlin2023.model.Article
import com.cocoslime.concurrencyinkotlin2023.model.Feed
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.newFixedThreadPoolContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

object ArticleProducer {
    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        // Ref. https://edition.cnn.com/services/rss/
        Feed("cnn","http://rss.cnn.com/rss/edition.rss"),
        Feed("fox", "https://feeds.foxnews.com/foxnews/politics?format=xml"),
//        Feed("inv", "htt://something.wrong") //FIXME: 예외 발생을 위한 잘못된 URL
    )

    private val networkDispatcher = newFixedThreadPoolContext(2, "IO")
    private val factory = DocumentBuilderFactory.newInstance()
    val producer = GlobalScope.produce(networkDispatcher) {
        feeds.forEach {
            try {
                val articles = fetchArticles(it)
                send(articles)
            } catch (e: Exception) {
                //ignore Exception
                e.logError("ArticleProducer")
            }
        }
    }

    private fun fetchArticles(
        feed: Feed
    ) : List<Article> {
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)

        return (0 until news.childNodes.length)
            .asSequence()
            .map { news.childNodes.item(it) }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map { it as Element }
            .filter { it.tagName == "item" }
            .map { it.getElementsByTagName("title").item(0)?.textContent to it.getElementsByTagName("description").item(0)?.textContent }
            .filter { it.first != null && it.second != null }
            .map {
                val title = it.first!!
                val description = it.second!!
                val summary = if (!description.startsWith("<div") && description.contains("<div")) {
                    description.substring(0, description.indexOf("<div"))
                } else {
                    description
                }

                Article(feed.name, title, summary)
            }
            .toList()
    }
}