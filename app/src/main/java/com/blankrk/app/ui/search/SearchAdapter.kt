package com.blankrk.app.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blankrk.app.data.model.ShortVideo
import com.blankrk.app.databinding.ItemSearchResultBinding
import com.bumptech.glide.Glide

class SearchAdapter(
    private val onItemClicked: (ShortVideo) -> Unit
) : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {

    private val items = mutableListOf<ShortVideo>()

    fun submitList(newItems: List<ShortVideo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val binding = ItemSearchResultBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    inner class SearchViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ShortVideo) {
            binding.resultTitle.text = item.title
            binding.resultChannel.text = item.channelTitle
            Glide.with(binding.thumbnail).load(item.thumbnailUrl).into(binding.thumbnail)
            binding.root.setOnClickListener { onItemClicked(item) }
        }
    }
}
