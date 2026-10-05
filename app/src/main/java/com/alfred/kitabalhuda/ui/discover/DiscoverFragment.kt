package com.alfred.kitabalhuda.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.alfred.kitabalhuda.KitabAlHudaApplication
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.database.dao.ListeningHistoryDao.HistoryItem
import com.alfred.kitabalhuda.di.ViewModelFactory
import com.alfred.kitabalhuda.repository.ReciteurRepository
import com.alfred.kitabalhuda.ui.home.HomeViewModel
import com.alfred.kitabalhuda.ui.player.PlayerUiState
import com.alfred.kitabalhuda.ui.player.PlayerViewModel
import com.alfred.kitabalhuda.ui.theme.KitabAlHudaTheme
import com.alfred.kitabalhuda.utils.HadithManager
import com.alfred.kitabalhuda.utils.ReciterPreferences
import com.alfred.kitabalhuda.utils.TimeOfDayManager
import kotlinx.coroutines.launch

class DiscoverFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var playerViewModel: PlayerViewModel

    private var hadithTextState by mutableStateOf<String?>(null)
    private var hadithRefState by mutableStateOf<String?>(null)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val app = requireActivity().application as KitabAlHudaApplication
        val repository = ReciteurRepository(app.database.reciteurDao())
        val factory = ViewModelFactory(this, repository)
        homeViewModel = ViewModelProvider(this, factory)[HomeViewModel::class.java]
        playerViewModel = ViewModelProvider(requireActivity())[PlayerViewModel::class.java]

        val historyDao = app.database.listeningHistoryDao()

        // Fetch random Hadith asynchronously
        lifecycleScope.launch {
            val hadith = HadithManager.getRandomHadith(requireContext())
            if (hadith != null) {
                hadithTextState = hadith.Arabic_Text
                hadithRefState = "صحيح مسلم (${hadith.Chapter_Title_Arabic})"
            }
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                KitabAlHudaTheme {
                    val reciters by homeViewModel.reciteurs.observeAsState(emptyList())
                    val historyList by historyDao.getRecentHistory(1).observeAsState(emptyList())
                    val playerUiState by playerViewModel.playerUiState.observeAsState(PlayerUiState.Idle)

                    val lastPlayedItem = historyList.firstOrNull()
                    val bottomPadding = if (playerUiState !is PlayerUiState.Idle) 64 else 0

                    DiscoverScreen(
                        greeting = TimeOfDayManager.getGreeting(),
                        lastPlayedItem = lastPlayedItem,
                        hadithText = hadithTextState,
                        hadithRef = hadithRefState,
                        reciters = reciters,
                        onResumeReadingClick = {
                            val item = lastPlayedItem
                            if (item != null) {
                                ReciterPreferences.setSelectedReciter(requireContext(), item.reciteur.id, item.reciteur.nom)
                                playerViewModel.playSurahWithReciter(
                                    item.sourate.numero,
                                    item.reciteur.id,
                                    item.reciteur.nom
                                )
                            } else {
                                ReciterPreferences.setSelectedReciter(requireContext(), 1, "مشاري بن راشد العفاسي")
                                playerViewModel.playSurah(1)
                            }
                            findNavController().navigate(R.id.navigation_quran)
                        },
                        onReciterClick = { reciter ->
                            ReciterPreferences.setSelectedReciter(requireContext(), reciter.id, reciter.nom)
                            playerViewModel.playSurahWithReciter(1, reciter.id, reciter.nom)
                            findNavController().navigate(R.id.navigation_quran)
                        },
                        bottomPadding = bottomPadding
                    )
                }
            }
        }
    }
}

