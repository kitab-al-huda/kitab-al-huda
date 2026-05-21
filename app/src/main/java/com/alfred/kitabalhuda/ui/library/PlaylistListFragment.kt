package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.databinding.FragmentPlaylistListBinding

class PlaylistListFragment : Fragment() {

    private var _binding: FragmentPlaylistListBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LibraryViewModel
    private lateinit var adapter: PlaylistAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaylistListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[LibraryViewModel::class.java]

        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        adapter = PlaylistAdapter(
            onPlaylistClick = { playlist ->
                val fragment = PlaylistDetailFragment.newInstance(playlist.id, playlist.name)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main, fragment)
                    .addToBackStack(null)
                    .commit()
            },
            onPlaylistOptionsClick = { playlist ->
                val anchor = binding.recyclerPlaylists.findViewHolderForAdapterPosition(adapter.currentList.indexOf(playlist))?.itemView?.findViewById<View>(R.id.btn_playlist_options) ?: requireView()
                val popup = androidx.appcompat.widget.PopupMenu(requireContext(), anchor)
                popup.menu.add(0, 1, 0, R.string.edit_playlist)
                popup.menu.add(0, 2, 1, R.string.delete)
                
                popup.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> {
                            val dialog = EditPlaylistDialog(playlist) { newName, newDescription ->
                                viewModel.updatePlaylist(playlist.copy(name = newName, description = newDescription))
                            }
                            dialog.show(parentFragmentManager, EditPlaylistDialog.TAG)
                            true
                        }
                        2 -> {
                            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                                .setTitle(R.string.delete)
                                .setMessage(R.string.delete_playlist_confirmation)
                                .setPositiveButton(R.string.delete) { _, _ ->
                                    viewModel.deletePlaylist(playlist)
                                }
                                .setNegativeButton(R.string.cancel, null)
                                .show()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
        )
        binding.recyclerPlaylists.layoutManager = LinearLayoutManager(context)
        binding.recyclerPlaylists.adapter = adapter
    }

    private fun setupFab() {
        binding.fabCreatePlaylist.setOnClickListener {
            val bottomSheet = CreatePlaylistBottomSheet { name ->
                viewModel.createPlaylist(name)
            }
            bottomSheet.show(parentFragmentManager, CreatePlaylistBottomSheet.TAG)
        }
    }

    private fun observeViewModel() {
        viewModel.allPlaylists.observe(viewLifecycleOwner) { playlists ->
            adapter.submitList(playlists)
            binding.textEmptyState.visibility = if (playlists.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = PlaylistListFragment()
    }
}
