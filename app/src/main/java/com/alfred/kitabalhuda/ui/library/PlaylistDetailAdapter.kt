package com.alfred.kitabalhuda.ui.library

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alfred.kitabalhuda.database.dao.PlaylistDao
import com.alfred.kitabalhuda.databinding.ItemPlaylistTrackBinding
import java.util.Collections

class PlaylistDetailAdapter(
    private val onTrackClick: (PlaylistDao.PlaylistTrack) -> Unit,
    private val onDeleteClick: (PlaylistDao.PlaylistTrack) -> Unit,
    private val onOrderChanged: (List<PlaylistDao.PlaylistTrack>) -> Unit
) : RecyclerView.Adapter<PlaylistDetailAdapter.TrackViewHolder>() {

    private val items = mutableListOf<PlaylistDao.PlaylistTrack>()
    private var currentlyPlayingUrl: String? = null

    fun submitList(newItems: List<PlaylistDao.PlaylistTrack>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    
    fun setCurrentlyPlaying(url: String?) {
        currentlyPlayingUrl = url
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val binding = ItemPlaylistTrackBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TrackViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size

    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        // Note: Real DB update should happen after drop. For now we notify callback on every move or deferred?
        // Better deferred. But simpler for MVP: Update DB later.
        onOrderChanged(items)
    }

    fun onItemDismiss(position: Int) {
        val item = items[position]
        items.removeAt(position)
        notifyItemRemoved(position)
        onDeleteClick(item)
    }
    
    fun getCurrentTracks(): List<PlaylistDao.PlaylistTrack> {
        return items.toList()
    }

    inner class TrackViewHolder(private val binding: ItemPlaylistTrackBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(track: PlaylistDao.PlaylistTrack, position: Int) {
            val isPlaying = track.audio.urlWeb == currentlyPlayingUrl
            
            binding.textTrackNumber.text = (position + 1).toString()
            // Display real names from joined data
            binding.textTrackTitle.text = track.sourate.nomPhonetique
            binding.textTrackArtist.text = track.reciteur.nom
            
            // Show/hide now playing indicator
            if (isPlaying) {
                binding.iconNowPlaying.visibility = android.view.View.VISIBLE
                binding.textTrackNumber.visibility = android.view.View.GONE
                binding.textTrackTitle.setTextColor(binding.root.context.getColor(com.alfred.kitabalhuda.R.color.huda_teal))
            } else {
                binding.iconNowPlaying.visibility = android.view.View.GONE
                binding.textTrackNumber.visibility = android.view.View.VISIBLE
                binding.textTrackTitle.setTextColor(binding.root.context.getColor(com.alfred.kitabalhuda.R.color.white))
            }
            
            binding.root.setOnClickListener { onTrackClick(track) }
            binding.btnItemOptions.setOnClickListener { onDeleteClick(track) }
            
            // We could attach touch listener to drag handle here if using handle-only drag.
        }
    }
}
