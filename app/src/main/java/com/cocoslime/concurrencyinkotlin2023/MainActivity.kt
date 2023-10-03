package com.cocoslime.concurrencyinkotlin2023

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.view.isVisible
import com.cocoslime.concurrencyinkotlin2023.databinding.ActivityMainBinding
import com.cocoslime.concurrencyinkotlin2023.model.Article
import com.cocoslime.concurrencyinkotlin2023.model.Feed
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.lang.Exception
import java.util.concurrent.atomic.AtomicInteger
import javax.xml.parsers.DocumentBuilderFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val TAG = "MainActivity"
    private val networkDispatcher = newFixedThreadPoolContext(2, "IO")
    private val factory = DocumentBuilderFactory.newInstance()

    private val feeds = listOf(
        Feed("npr", "https://www.npr.org/rss/rss.php?id=1001"),
        // Ref. https://edition.cnn.com/services/rss/
        Feed("cnn","http://rss.cnn.com/rss/edition.rss"),
        Feed("fox", "https://feeds.foxnews.com/foxnews/politics?format=xml"),
        Feed("inv", "htt://something.wrong") //FIXME: 예외 발생을 위한 잘못된 URL
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        asyncLoadNews()
    }

    private fun asyncLoadNews() = GlobalScope.launch {
        val requests = feeds.map { feed ->
            asyncFetchArticles(feed, networkDispatcher)
        }

        //Deferred.getCompleted() 는 experimental API 라서, await() 를 사용하는 코드로 대체함
        val failedCount = AtomicInteger(0)
        val articles = requests.flatMap {
            try {
                it.await()
            } catch (e: Exception) {
                Log.e(TAG, e.localizedMessage, e)
                failedCount.incrementAndGet()
                emptyList()
            }
        }

        launch(Dispatchers.Main) {
            binding.newsCount.text = getString(
                R.string.news_count, articles.size.toString(), (requests.size - failedCount.get()).toString()
            )

            binding.warnings.isVisible = failedCount.get() != 0
            binding.warnings.text = getString(
                R.string.fail_fetch_feed, failedCount.get()
            )
        }
    }

    private fun asyncFetchArticles(
        feed: Feed,
        dispatcher: CoroutineDispatcher
    ) = GlobalScope.async(dispatcher){
        val builder = factory.newDocumentBuilder()
        val xml = builder.parse(feed.url)
        val news = xml.getElementsByTagName("channel").item(0)

        (0 until news.childNodes.length)
            .asSequence()
            .map { news.childNodes.item(it) }
            .filter { Node.ELEMENT_NODE == it.nodeType }
            .map { it as Element }
            .filter { it.tagName == "item" }
            .map {
                val title = it.getElementsByTagName("title").item(0).textContent
                val summary = it.getElementsByTagName("description").item(0).textContent
                Article(feed.name, title, summary)
            }
            .toList()
    }
}