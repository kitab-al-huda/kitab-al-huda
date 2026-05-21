package com.alfred.kitabalhuda.ui.quran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import com.alfred.kitabalhuda.databinding.ItemReciterSelectionBinding
import com.alfred.kitabalhuda.repository.ReciteurRepository
import com.alfred.kitabalhuda.util.ReciterPreferences
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ReciterSelectionBottomSheet : BottomSheetDialogFragment() {
    
    companion object {
        const val TAG = "ReciterSelectionBottomSheet"
        
        fun newInstance(): ReciterSelectionBottomSheet {
            return ReciterSelectionBottomSheet()
        }
    }

    var onReciterSelected: ((ReciteurEntity) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.bottom_sheet_select_reciter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_reciters)
        val currentReciterId = ReciterPreferences.getSelectedReciterId(requireContext())

        val adapter = ReciterSelectionAdapter(currentReciterId) { reciter ->
            ReciterPreferences.setSelectedReciter(requireContext(), reciter.id, reciter.nom)
            onReciterSelected?.invoke(reciter)
            dismiss()
        }
        
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        val app = requireActivity().application as KitabAlHudaApplication
        val repository = ReciteurRepository(app.database.reciteurDao())

        lifecycleScope.launch {
            repository.getAllReciteurs().collectLatest { reciters ->
                adapter.submitList(reciters)
            }
        }
    }

    override fun getTheme(): Int = R.style.Theme_KitabAlHuda_BottomSheet
}

// ─── Adapter ────────────────────────────────────────────

class ReciterSelectionAdapter(
    private val selectedId: Int,
    private val onItemClick: (ReciteurEntity) -> Unit
) : ListAdapter<ReciteurEntity, ReciterSelectionAdapter.ViewHolder>(ReciterDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemReciterSelectionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemReciterSelectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(reciter: ReciteurEntity) {
            binding.textReciterName.text = reciter.nom
            binding.textReciterDesc.text = reciter.description ?: ""
            binding.textReciterDesc.visibility =
                if (reciter.description.isNullOrEmpty()) View.GONE else View.VISIBLE
            
            // Show checkmark for selected reciter
            binding.iconCheck.visibility =
                if (reciter.id == selectedId) View.VISIBLE else View.GONE
            
            binding.root.setOnClickListener { onItemClick(reciter) }
        }
    }
}

class ReciterDiff : DiffUtil.ItemCallback<ReciteurEntity>() {
    override fun areItemsTheSame(a: ReciteurEntity, b: ReciteurEntity) = a.id == b.id
    override fun areContentsTheSame(a: ReciteurEntity, b: ReciteurEntity) = a == b
}
