package `in`.programy.spotube.util

import `in`.programy.spotube.R
import `in`.programy.spotube.model.Item
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.list_item.view.*

class VideoListAdapter(): RecyclerView.Adapter<VideoListAdapter.VideoViewHolder>() {
    inner class VideoViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    private val differCallback = object : DiffUtil.ItemCallback<Item>(){
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this,differCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        return VideoViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item,parent,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val currItem = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this).load(currItem.snippet.thumbnails.default.url).into(ivItemImage)
            tvPrimary.text = currItem.snippet.title
            tvSecondary.text = currItem.snippet.channelTitle
            setOnClickListener {
                onItemClickListener?.let { it(currItem) }
            }
        }
    }

    private var onItemClickListener: ((Item) -> Unit)? = null

    fun setOnItemClickListener(listener: (Item)-> Unit){
        onItemClickListener = listener
    }
}