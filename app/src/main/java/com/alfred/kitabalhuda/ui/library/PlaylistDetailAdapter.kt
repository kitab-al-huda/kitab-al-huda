package com.alfred.kitabalhuda.ui.library

import android.view.LayoutInflater
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
            val context = binding.root.context

            // Position count
            binding.textTrackNumber.text = (position + 1).toString()

            // Set Titles (Phonetic & Arabic)
            binding.textTrackTitle.text = track.sourate.nomPhonetique
            binding.textTrackArabic.text = track.sourate.nomArabe

            // Format Metadata: Reciter • x versets • Lieu
            val versesText = if (track.sourate.nombreVersets == 1) "1 verset" else "${track.sourate.nombreVersets} versets"
            val lieuText = when (track.sourate.lieuRevelation.lowercase()) {
                "makkah", "mecquoise" -> "Mecquoise"
                "madinah", "médinoise" -> "Médinoise"
                else -> track.sourate.lieuRevelation
            }
            binding.textTrackArtist.text = "${track.reciteur.nom} • $versesText • $lieuText"

            // Format and display Duration
            binding.textTrackDuration.text = formatDuration(track.audio.duree)

            // Setup now-playing and highlight states
            if (isPlaying) {
                // Show waveform, hide standard number
                binding.iconNowPlaying.visibility = android.view.View.VISIBLE
                binding.textTrackNumber.visibility = android.view.View.GONE

                // Color highlight
                binding.textTrackTitle.setTextColor(context.getColor(com.alfred.kitabalhuda.R.color.huda_teal))
                binding.textTrackArabic.setTextColor(context.getColor(com.alfred.kitabalhuda.R.color.huda_teal))
                binding.textTrackDuration.setTextColor(context.getColor(com.alfred.kitabalhuda.R.color.huda_teal_light))

                // Soft glowing background container
                binding.trackItemContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    context.getColor(com.alfred.kitabalhuda.R.color.surface_elevated)
                )
                binding.trackItemContainer.elevation = 4f
            } else {
                // Hide waveform, show standard number
                binding.iconNowPlaying.visibility = android.view.View.GONE
                binding.textTrackNumber.visibility = android.view.View.VISIBLE

                // Set default colors
                binding.textTrackTitle.setTextColor(context.getColor(com.alfred.kitabalhuda.R.color.white))
                binding.textTrackArabic.setTextColor(context.getColor(com.alfred.kitabalhuda.R.color.white))
                binding.textTrackDuration.setTextColor(context.getColor(com.alfred.kitabalhuda.R.color.gray_500))

                // Default transparent background card
                binding.trackItemContainer.backgroundTintList = android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.TRANSPARENT
                )
                binding.trackItemContainer.elevation = 0f
            }

            binding.root.setOnClickListener { onTrackClick(track) }
            binding.btnItemOptions.setOnClickListener { onDeleteClick(track) }
        }

        /**
         * Converts duration in milliseconds or seconds to MM:SS string.
         */
        private fun formatDuration(duration: Long): String {
            if (duration <= 0) return ""
            // Detect if duration is stored in seconds instead of milliseconds
            val totalSeconds = if (duration > 100000) duration / 1000 else duration
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return String.format(java.util.Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }
}
