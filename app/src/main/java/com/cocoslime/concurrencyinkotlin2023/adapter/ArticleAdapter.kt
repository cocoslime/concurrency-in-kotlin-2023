package com.cocoslime.concurrencyinkotlin2023.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cocoslime.concurrencyinkotlin2023.databinding.ItemArticleBinding
import com.cocoslime.concurrencyinkotlin2023.model.Article
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


interface ArticleLoader {
    suspend fun loadMore()
}

class ArticleAdapter(
    private val articleLoader: ArticleLoader
) : ListAdapter<Article, ArticleAdapter.ArticleViewHolder>(ArticleDiffCallback()) {
    private var loading = false

    fun add(articles: List<Article>) {
        submitList(currentList + articles)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        return ArticleViewHolder(
            ItemArticleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val article = getItem(position)

        holder.binding.feed.text = article.feedName
        holder.binding.title.text = article.title
        holder.binding.summary.text = article.summary

        if (!loading && position >= itemCount - 2) {
            loading = true

            GlobalScope.launch {
                articleLoader.loadMore()
                loading = false
            }
        }
    }

    class ArticleViewHolder(
        val binding: ItemArticleBinding
    ) : RecyclerView.ViewHolder(binding.root)

    class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.title == newItem.title
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}
