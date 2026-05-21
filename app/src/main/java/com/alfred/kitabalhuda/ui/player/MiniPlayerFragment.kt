package com.alfred.kitabalhuda.ui.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alfred.kitabalhuda.databinding.FragmentMiniPlayerBinding

class MiniPlayerFragment : Fragment() {

    private var _binding: FragmentMiniPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMiniPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var viewModel: PlayerViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[PlayerViewModel::class.java]

        viewModel.player.observe(viewLifecycleOwner) { player ->
            if (player != null) {
                // Update UI when player is ready
                updateMiniPlayerMetadata(player)
                
                binding.btnMinPlay.setOnClickListener {
                    if (player.isPlaying) player.pause() else player.play()
                }
                
                // Add listener for playback state changes to update icon
                player.addListener(object : androidx.media3.common.Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        binding.btnMinPlay.setImageResource(
                            if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
                        )
                    }

                    override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                        updateMiniPlayerMetadata(player)
                    }

                    override fun onEvents(player: androidx.media3.common.Player, events: androidx.media3.common.Player.Events) {
                         if (events.contains(androidx.media3.common.Player.EVENT_MEDIA_METADATA_CHANGED)) {
                             updateMiniPlayerMetadata(player)
                         }
                    }
                })
                
                binding.root.setOnClickListener {
                    parentFragmentManager.beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
                        .add(android.R.id.content, FullPlayerFragment())
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
    }


    private fun updateMiniPlayerMetadata(player: androidx.media3.common.Player) {
        val metadata = player.mediaMetadata
        val title = metadata.title ?: "Ready to Play"
        binding.textMinTitle.text = title
        
        // Update subtitle with reciter's name (artist)
        binding.textMinSubtitle.text = metadata.artist ?: ""
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
