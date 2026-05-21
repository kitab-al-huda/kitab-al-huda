package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfred.kitabalhuda.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val playerViewModel = ViewModelProvider(requireActivity())[com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java]
        
        adapter = HistoryAdapter { historyItem ->
            // Play from this track
            playerViewModel.playSurah(
                historyItem.sourate.numero,
                historyItem.sourate.nomPhonetique
            )
            Toast.makeText(context, "Playing ${historyItem.sourate.nomPhonetique}", Toast.LENGTH_SHORT).show()
        }
        
        binding.recyclerHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerHistory.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.recentHistory.observe(viewLifecycleOwner) { history ->
            adapter.submitList(history)
            binding.textEmptyHistory.visibility = if (history.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = HistoryFragment()
    }
}
