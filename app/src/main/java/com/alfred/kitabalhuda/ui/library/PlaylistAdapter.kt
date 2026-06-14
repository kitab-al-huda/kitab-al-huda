package com.alfred.kitabalhuda.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.databinding.ItemPlaylistBinding
import com.alfred.kitabalhuda.utils.toArabicIndic

class PlaylistAdapter(
    private val onPlaylistClick: (PlaylistEntity) -> Unit,
    private val onPlaylistOptionsClick: (PlaylistEntity) -> Unit
) : ListAdapter<com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistWithCount, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlistWithCount = getItem(position)
        holder.bind(playlistWithCount)
    }

    inner class PlaylistViewHolder(private val binding: ItemPlaylistBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(playlistWithCount: com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistWithCount) {
            val playlist = playlistWithCount.playlist
            binding.textPlaylistName.text = playlist.name
            binding.textPlaylistCount.text = "${playlistWithCount.trackCount.toArabicIndic()} ${binding.root.context.getString(com.alfred.kitabalhuda.R.string.tracks_count)}"

            // TODO: Load cover image if available

            binding.root.setOnClickListener {
                onPlaylistClick(playlist)
            }

            binding.btnPlaylistOptions.setOnClickListener {
                onPlaylistOptionsClick(playlist)
            }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistWithCount>() {
        override fun areItemsTheSame(
            oldItem: com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistWithCount,
            newItem: com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistWithCount
        ): Boolean {
            return oldItem.playlist.id == newItem.playlist.id
        }

        override fun areContentsTheSame(
            oldItem: com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistWithCount,
            newItem: com.alfred.kitabalhuda.database.dao.PlaylistDao.PlaylistWithCount
        ): Boolean {
            return oldItem == newItem
        }
    }
}
