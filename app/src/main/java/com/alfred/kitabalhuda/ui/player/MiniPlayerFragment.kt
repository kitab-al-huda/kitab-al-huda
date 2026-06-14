package com.alfred.kitabalhuda.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.databinding.FragmentMiniPlayerBinding

class MiniPlayerFragment : Fragment() {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!

    private var lastClickTime = 0L
    private lateinit var viewModel: PlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiniPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]

        binding.btnMinPlay.setOnClickListener {
            viewModel.togglePlayPause()
        }

        viewModel.playerUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PlayerUiState.Loading -> {
                    binding.progressMinLoading.visibility = View.VISIBLE
                    binding.btnMinPlay.visibility = View.GONE
                }
                is PlayerUiState.Playing -> {
                    binding.progressMinLoading.visibility = View.GONE
                    binding.btnMinPlay.visibility = View.VISIBLE
                    binding.btnMinPlay.setImageResource(R.drawable.ic_media_pause)
                    binding.textMinTitle.text = state.fullTitle
                    binding.textMinSubtitle.text = state.artist
                }
                is PlayerUiState.Paused -> {
                    binding.progressMinLoading.visibility = View.GONE
                    binding.btnMinPlay.visibility = View.VISIBLE
                    binding.btnMinPlay.setImageResource(R.drawable.ic_media_play)
                    binding.textMinTitle.text = state.fullTitle
                    binding.textMinSubtitle.text = state.artist
                }
                is PlayerUiState.Idle -> {
                    binding.progressMinLoading.visibility = View.GONE
                    binding.btnMinPlay.visibility = View.VISIBLE
                    binding.btnMinPlay.setImageResource(R.drawable.ic_media_play)
                    binding.textMinTitle.text = getString(R.string.ready_to_play)
                    binding.textMinSubtitle.text = ""
                }
                is PlayerUiState.Error -> {
                    binding.progressMinLoading.visibility = View.GONE
                    binding.btnMinPlay.visibility = View.VISIBLE
                    binding.btnMinPlay.setImageResource(R.drawable.ic_media_play)
                }
            }
        }

        binding.root.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime < 1000) return@setOnClickListener
            lastClickTime = currentTime

            val existing = parentFragmentManager.findFragmentByTag("FullPlayer")
            if (existing != null && existing.isAdded) return@setOnClickListener

            val fullPlayer = FullPlayerFragment()
            fullPlayer.show(parentFragmentManager, "FullPlayer")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
