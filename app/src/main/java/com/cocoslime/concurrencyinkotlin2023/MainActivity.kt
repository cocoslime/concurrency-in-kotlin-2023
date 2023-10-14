package com.cocoslime.concurrencyinkotlin2023

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.cocoslime.concurrencyinkotlin2023.adapter.ArticleAdapter
import com.cocoslime.concurrencyinkotlin2023.adapter.ArticleLoader
import com.cocoslime.concurrencyinkotlin2023.databinding.ActivityMainBinding
import com.cocoslime.concurrencyinkotlin2023.producer.ArticleProducer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity(), ArticleLoader {
    private lateinit var binding: ActivityMainBinding

    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        articleAdapter = ArticleAdapter(this)
        binding.articles.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = articleAdapter
        }

        GlobalScope.launch {
            loadMore()
        }
    }

    override suspend fun loadMore() {
        val producer = ArticleProducer.producer

        if (!producer.isClosedForReceive) {
            val articles = producer.receive()

            withContext(Dispatchers.Main) {
                binding.progressBar.isVisible = false
                articleAdapter.add(articles)
            }
        }
    }
}