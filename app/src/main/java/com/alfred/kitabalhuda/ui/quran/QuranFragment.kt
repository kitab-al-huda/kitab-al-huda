package com.alfred.kitabalhuda.ui.quran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.databinding.FragmentQuranBinding
import com.alfred.kitabalhuda.di.ViewModelFactory
import com.alfred.kitabalhuda.repository.SourateRepository

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranFragment : Fragment() {
    // ... (rest of class)


    private var _binding: FragmentQuranBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SourateViewModel
    private lateinit var adapter: SourateAdapter
    private lateinit var playerViewModel: com.alfred.kitabalhuda.ui.player.PlayerViewModel

    private var availableSurahNumbers: Set<Int> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuranBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val application = requireActivity().application as KitabAlHudaApplication
        val database = application.database
        val repository = SourateRepository(database.sourateDao())
        val factory = ViewModelFactory(this, repository)
        viewModel = ViewModelProvider(this, factory).get(SourateViewModel::class.java)
        
        playerViewModel = ViewModelProvider(requireActivity()).get(com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java)

        adapter = SourateAdapter(
            onClick = { sourate ->
                val app = requireActivity().application as KitabAlHudaApplication
                val audioDao = app.database.audioDao()
                lifecycleScope.launch {
                    val selectedReciterId = com.alfred.kitabalhuda.utils.ReciterPreferences.getSelectedReciterId(requireContext())
                    val audioExists = withContext(Dispatchers.IO) {
                        audioDao.getAudioForSurahAndReciter(selectedReciterId, sourate.numero) != null
                    }
                    if (audioExists) {
                        Toast.makeText(context, "Playing: ${sourate.nomArabe}", Toast.LENGTH_SHORT).show()
                        playerViewModel.playSurah(sourate.numero, sourate.nomArabe)
                    } else {
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("سورة غير متوفرة")
                            .setMessage("هذه السورة غير متوفرة حالياً لهذا القارئ بصيغة مجانية (zero-rated). هل تود الاستماع إليها بصوت القارئ مشاري بن راشد العفاسي؟")
                            .setPositiveButton("الاستماع بصوت العفاسي") { _, _ ->
                                playerViewModel.playSurahWithReciter(
                                    sourate.numero,
                                    sourate.nomArabe,
                                    1, // Al-Afasy ID
                                    "مشاري بن راشد العفاسي"
                                )
                            }
                            .setNegativeButton("إلغاء", null)
                            .show()
                    }
                }
            },
            onOptionsClick = { sourate ->
                // Find the Audio ID for this surah + selected reciter
                val app = requireActivity().application as KitabAlHudaApplication
                val audioDao = app.database.audioDao()
                
                lifecycleScope.launch {
                    val selectedReciterId = com.alfred.kitabalhuda.utils.ReciterPreferences.getSelectedReciterId(requireContext())
                    // Fetch ALL parts so multi-part surahs (e.g. Al-Baqara) play fully in playlists
                    val audioParts = withContext(Dispatchers.IO) {
                        audioDao.getAudioPartsForSurah(selectedReciterId, sourate.numero)
                    }
                    if (audioParts.isNotEmpty()) {
                        val sheet = com.alfred.kitabalhuda.ui.library.AddToPlaylistBottomSheet
                            .newInstance(audioParts.map { it.id })
                        sheet.show(parentFragmentManager, com.alfred.kitabalhuda.ui.library.AddToPlaylistBottomSheet.TAG)
                    } else {
                        Toast.makeText(context, getString(R.string.audio_not_found), Toast.LENGTH_SHORT).show()
                    }
                }
            },
            onLongClick = { sourate ->
                // Open reciter selection for this specific sourate
                val sheet = ReciterSelectionBottomSheet.newInstance(
                    requestKey = REQUEST_PLAY_SURAH_WITH_RECITER,
                    sourateNumero = sourate.numero,
                    sourateNom = sourate.nomArabe
                )
                sheet.show(parentFragmentManager, ReciterSelectionBottomSheet.TAG)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        setupSearch()
        setupReciterSelector()
        setupLocationFilters()
        setupFragmentResultListeners()

        viewModel.sourates.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list, availableSurahNumbers)
        }
        updateAvailableSurahs()

        // Observe player state to show "currently playing" indicator
        playerViewModel.playerUiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is com.alfred.kitabalhuda.ui.player.PlayerUiState.Playing -> {
                    adapter.setCurrentlyPlaying(state.surahNumber, true)
                }
                is com.alfred.kitabalhuda.ui.player.PlayerUiState.Paused -> {
                    adapter.setCurrentlyPlaying(state.surahNumber, false)
                }
                else -> {
                    adapter.setCurrentlyPlaying(-1)
                }
            }
            val paddingBottom = if (state !is com.alfred.kitabalhuda.ui.player.PlayerUiState.Idle) {
                resources.getDimensionPixelSize(R.dimen.mini_player_bottom_padding)
            } else {
                0
            }
            binding.recyclerView.setPadding(0, 0, 0, paddingBottom)
        }
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchQuery.value = query ?: ""
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchQuery.value = newText ?: ""
                return true
            }
        })
    }



    private fun setupReciterSelector() {
        // Show the current saved reciter name on the chip
        val currentName = com.alfred.kitabalhuda.utils.ReciterPreferences.getSelectedReciterName(requireContext())
        binding.chipReciter.text = currentName

        binding.chipReciter.setOnClickListener {
            val sheet = ReciterSelectionBottomSheet.newInstance(requestKey = REQUEST_CHANGE_DEFAULT_RECITER)
            sheet.show(parentFragmentManager, ReciterSelectionBottomSheet.TAG)
        }
    }

    private fun setupLocationFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chip_filter_mecca -> "MECCA"
                R.id.chip_filter_medina -> "MEDINA"
                else -> "ALL"
            }
            viewModel.filterType.value = filter
        }
    }



    private fun updateAvailableSurahs() {
        val app = requireActivity().application as KitabAlHudaApplication
        val audioDao = app.database.audioDao()
        lifecycleScope.launch {
            val reciterId = com.alfred.kitabalhuda.utils.ReciterPreferences.getSelectedReciterId(requireContext())
            availableSurahNumbers = withContext(Dispatchers.IO) {
                audioDao.getAudiosByReciteurDirect(reciterId).map { it.sourateNumero }.toSet()
            }
            viewModel.sourates.value?.let { list ->
                adapter.submitList(list, availableSurahNumbers)
            }
        }
    }

    private fun setupFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            REQUEST_CHANGE_DEFAULT_RECITER,
            viewLifecycleOwner
        ) { _, bundle ->
            val name = bundle.getString(ReciterSelectionBottomSheet.RESULT_RECITER_NAME) ?: return@setFragmentResultListener
            binding.chipReciter.text = name
            updateAvailableSurahs()
        }

        parentFragmentManager.setFragmentResultListener(
            REQUEST_PLAY_SURAH_WITH_RECITER,
            viewLifecycleOwner
        ) { _, bundle ->
            val reciterId = bundle.getInt(ReciterSelectionBottomSheet.RESULT_RECITER_ID)
            val reciterName = bundle.getString(ReciterSelectionBottomSheet.RESULT_RECITER_NAME) ?: return@setFragmentResultListener
            val sourateNumero = bundle.getInt(ReciterSelectionBottomSheet.RESULT_SOURATE_NUMERO)
            val sourateNom = bundle.getString(ReciterSelectionBottomSheet.RESULT_SOURATE_NOM) ?: return@setFragmentResultListener

            playerViewModel.playSurahWithReciter(
                sourateNumero,
                sourateNom,
                reciterId,
                reciterName
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CHANGE_DEFAULT_RECITER = "changeDefaultReciterRequest"
        private const val REQUEST_PLAY_SURAH_WITH_RECITER = "playSurahWithReciterRequest"
    }
}
