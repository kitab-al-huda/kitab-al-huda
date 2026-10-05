package com.alfred.kitabalhuda.ui.quran

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.di.ViewModelFactory
import com.alfred.kitabalhuda.repository.SourateRepository
import com.alfred.kitabalhuda.ui.player.PlayerUiState
import com.alfred.kitabalhuda.ui.player.PlayerViewModel
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme
import com.alfred.kitabalhuda.utils.ReciterPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuranFragment : Fragment() {

    private lateinit var viewModel: SourateViewModel
    private lateinit var playerViewModel: PlayerViewModel

    private var availableSurahNumbersState by mutableStateOf<Set<Int>>(emptySet())
    private var selectedReciterNameState by mutableStateOf("")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val application = requireActivity().application as KitabAlHudaApplication
        val database = application.database
        val repository = SourateRepository(database.sourateDao())
        val factory = ViewModelFactory(this, repository)
        viewModel = ViewModelProvider(this, factory)[SourateViewModel::class.java]
        playerViewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]

        selectedReciterNameState = ReciterPreferences.getSelectedReciterName(requireContext())
        updateAvailableSurahs()
        setupFragmentResultListeners()

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KitabAlHudaTheme {
                    val sourates by viewModel.sourates.observeAsState(emptyList())
                    val searchQuery by viewModel.searchQuery.observeAsState("")
                    val filterType by viewModel.filterType.observeAsState("ALL")
                    val playerUiState by playerViewModel.playerUiState.observeAsState(PlayerUiState.Idle)

                    val currentPlayingSurah = when (val s = playerUiState) {
                        is PlayerUiState.Playing -> s.surahNumber
                        is PlayerUiState.Paused -> s.surahNumber
                        else -> -1
                    }
                    val isAudioPlaying = playerUiState is PlayerUiState.Playing
                    val bottomPadding = if (playerUiState !is PlayerUiState.Idle) 64 else 0

                    QuranScreen(
                        sourates = sourates,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.searchQuery.value = it },
                        selectedFilter = filterType,
                        onFilterSelected = { viewModel.filterType.value = it },
                        selectedReciterName = selectedReciterNameState,
                        onReciterChipClick = {
                            val sheet = ReciterSelectionBottomSheet.newInstance(requestKey = REQUEST_CHANGE_DEFAULT_RECITER)
                            sheet.show(parentFragmentManager, ReciterSelectionBottomSheet.TAG)
                        },
                        availableSurahNumbers = availableSurahNumbersState,
                        currentPlayingSurahNumber = currentPlayingSurah,
                        isAudioPlaying = isAudioPlaying,
                        onSurahClick = { sourate ->
                            val app = requireActivity().application as KitabAlHudaApplication
                            val audioDao = app.database.audioDao()
                            lifecycleScope.launch {
                                val selectedReciterId = ReciterPreferences.getSelectedReciterId(requireContext())
                                val audioExists = withContext(Dispatchers.IO) {
                                    audioDao.getAudioForSurahAndReciter(selectedReciterId, sourate.numero) != null
                                }
                                if (audioExists) {
                                    Toast.makeText(context, getString(R.string.playing_surah, sourate.nomArabe), Toast.LENGTH_SHORT).show()
                                    playerViewModel.playSurah(sourate.numero)
                                } else {
                                    AlertDialog.Builder(requireContext())
                                        .setTitle(R.string.surah_unavailable_title)
                                        .setMessage(getString(R.string.surah_unavailable_message, getString(R.string.reciter_default_name)))
                                        .setPositiveButton(getString(R.string.listen_with_reciter, getString(R.string.reciter_default_name))) { _, _ ->
                                            playerViewModel.playSurahWithReciter(
                                                sourate.numero,
                                                1, // Al-Afasy ID
                                                getString(R.string.reciter_default_name)
                                            )
                                        }
                                        .setNegativeButton("إلغاء", null)
                                        .show()
                                }
                            }
                        },
                        onOptionsClick = { sourate ->
                            val app = requireActivity().application as KitabAlHudaApplication
                            val audioDao = app.database.audioDao()
                            lifecycleScope.launch {
                                val selectedReciterId = ReciterPreferences.getSelectedReciterId(requireContext())
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
                        onSurahLongClick = { sourate ->
                            val sheet = ReciterSelectionBottomSheet.newInstance(
                                requestKey = REQUEST_PLAY_SURAH_WITH_RECITER,
                                sourateNumero = sourate.numero,
                                sourateNom = sourate.nomArabe
                            )
                            sheet.show(parentFragmentManager, ReciterSelectionBottomSheet.TAG)
                        },
                        bottomPadding = bottomPadding
                    )
                }
            }
        }
    }

    private fun updateAvailableSurahs() {
        val app = requireActivity().application as KitabAlHudaApplication
        val audioDao = app.database.audioDao()
        lifecycleScope.launch {
            val reciterId = ReciterPreferences.getSelectedReciterId(requireContext())
            availableSurahNumbersState = withContext(Dispatchers.IO) {
                audioDao.getAudiosByReciteurDirect(reciterId).map { it.sourateNumero }.toSet()
            }
        }
    }

    private fun setupFragmentResultListeners() {
        parentFragmentManager.setFragmentResultListener(
            REQUEST_CHANGE_DEFAULT_RECITER,
            this
        ) { _, bundle ->
            val name = bundle.getString(ReciterSelectionBottomSheet.RESULT_RECITER_NAME) ?: return@setFragmentResultListener
            selectedReciterNameState = name
            updateAvailableSurahs()
        }

        parentFragmentManager.setFragmentResultListener(
            REQUEST_PLAY_SURAH_WITH_RECITER,
            this
        ) { _, bundle ->
            val reciterId = bundle.getInt(ReciterSelectionBottomSheet.RESULT_RECITER_ID)
            val reciterName = bundle.getString(ReciterSelectionBottomSheet.RESULT_RECITER_NAME) ?: return@setFragmentResultListener
            val sourateNumero = bundle.getInt(ReciterSelectionBottomSheet.RESULT_SOURATE_NUMERO)

            playerViewModel.playSurahWithReciter(
                sourateNumero,
                reciterId,
                reciterName
            )
        }
    }

    companion object {
        private const val REQUEST_CHANGE_DEFAULT_RECITER = "changeDefaultReciterRequest"
        private const val REQUEST_PLAY_SURAH_WITH_RECITER = "playSurahWithReciterRequest"
    }
}
