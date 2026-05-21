package com.alfred.kitabalhuda.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import com.alfred.kitabalhuda.databinding.ItemReciteurBinding
import com.bumptech.glide.Glide

class ReciteurAdapter(
    private val onReciteurClick: (ReciteurEntity) -> Unit
) : ListAdapter<ReciteurEntity, ReciteurAdapter.ReciteurViewHolder>(ReciteurDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReciteurViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReciteurBinding.inflate(inflater, parent, false)
        return ReciteurViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReciteurViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ReciteurViewHolder(private val binding: ItemReciteurBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reciteur: ReciteurEntity) {
            binding.name.text = reciteur.nom
            // binding.description.text = reciteur.description ?: "" // Optional if layout has it
            
            Glide.with(binding.image)
                .load(reciteur.imageUrl)
                .placeholder(R.drawable.ic_launcher_foreground) // Use a dummy placeholder for now
                .error(R.drawable.ic_launcher_foreground)
                .into(binding.image)

            binding.root.setOnClickListener { onReciteurClick(reciteur) }
        }
    }
}

class ReciteurDiffCallback : DiffUtil.ItemCallback<ReciteurEntity>() {
    override fun areItemsTheSame(oldItem: ReciteurEntity, newItem: ReciteurEntity): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ReciteurEntity, newItem: ReciteurEntity): Boolean = oldItem == newItem
}
