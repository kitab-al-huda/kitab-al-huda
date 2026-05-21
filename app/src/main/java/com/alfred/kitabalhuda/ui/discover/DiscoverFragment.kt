package com.alfred.kitabalhuda.ui.discover

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.util.ReciterPreferences
import com.alfred.kitabalhuda.database.dao.ListeningHistoryDao.HistoryItem
import com.alfred.kitabalhuda.databinding.FragmentDiscoverBinding
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }


    private lateinit var homeViewModel: com.alfred.kitabalhuda.ui.home.HomeViewModel
    private lateinit var reciteurAdapter: com.alfred.kitabalhuda.ui.home.ReciteurAdapter
    private var lastPlayedItem: HistoryItem? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup Greeting
        val greeting = com.alfred.kitabalhuda.utils.TimeOfDayManager.getGreeting()
        binding.textGreeting.text = greeting
        
        // Setup Logic for Reciters
        setupRecitersList()
        
        // Setup Hadith Card
        setupHadithCard()
        
        // Setup Resume Reading Card
        setupResumeReading()
    }

    private fun setupResumeReading() {
        val application = requireActivity().application as com.alfred.kitabalhuda.KitabAlHudaApplication
        val database = application.database
        val historyDao = database.listeningHistoryDao()
        
        historyDao.getRecentHistory(1).observe(viewLifecycleOwner) { historyList ->
            if (!historyList.isNullOrEmpty()) {
                val item = historyList[0]
                lastPlayedItem = item
                binding.textLastPlayedTitle.text = item.sourate.nomArabe
                binding.textLastPlayedSubtitle.text = item.reciteur.nom
            } else {
                // Fallback / Initial State (Default Quran browsing)
                binding.textLastPlayedTitle.text = getString(R.string.surah_al_mulk) // سورة الملك
                binding.textLastPlayedSubtitle.text = "مشاري بن راشد العفاسي"
                lastPlayedItem = null
            }
        }
        
        binding.cardReading.setOnClickListener {
            val item = lastPlayedItem
            val playerViewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java]
            
            if (item != null) {
                // Set as active reciter in preferences
                ReciterPreferences.setSelectedReciter(requireContext(), item.reciteur.id, item.reciteur.nom)
                // Play last played audio
                playerViewModel.playSurahWithReciter(
                    item.sourate.numero,
                    item.sourate.nomArabe,
                    item.reciteur.id,
                    item.reciteur.nom
                )
            } else {
                // Default: Play Surah 1 (Al-Fatiha) with Mishary Al-Afasy
                ReciterPreferences.setSelectedReciter(requireContext(), 1, "مشاري بن راشد العفاسي")
                playerViewModel.playSurah(1, "الفاتحة")
            }
            
            // Navigate to Quran Tab
            findNavController().navigate(R.id.navigation_quran)
        }
    }

    private fun setupHadithCard() {
        lifecycleScope.launch {
            val hadith = com.alfred.kitabalhuda.utils.HadithManager.getRandomHadith(requireContext())
            if (hadith != null) {
                binding.textHadithContent.text = hadith.Arabic_Text
                binding.textHadithRef.text = "صحيح مسلم (${hadith.Chapter_Title_Arabic})"
            } else {
                binding.cardHadith.visibility = View.GONE
            }
        }
    }

    private fun setupRecitersList() {
        val application = requireActivity().application as com.alfred.kitabalhuda.KitabAlHudaApplication
        val repository = com.alfred.kitabalhuda.repository.ReciteurRepository(application.database.reciteurDao())
        val factory = com.alfred.kitabalhuda.di.ViewModelFactory(this, repository)
        homeViewModel = androidx.lifecycle.ViewModelProvider(this, factory)[com.alfred.kitabalhuda.ui.home.HomeViewModel::class.java]

        reciteurAdapter = com.alfred.kitabalhuda.ui.home.ReciteurAdapter { reciteur ->
            // On Reciter Click -> Select this Qari, play Fatiha, and navigate
            ReciterPreferences.setSelectedReciter(requireContext(), reciteur.id, reciteur.nom)
            
            val playerViewModel = androidx.lifecycle.ViewModelProvider(requireActivity())[com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java]
            playerViewModel.playSurahWithReciter(1, "الفاتحة", reciteur.id, reciteur.nom)
            
            findNavController().navigate(R.id.navigation_quran)
        }

        binding.recyclerFeaturedReciters.apply {
            adapter = reciteurAdapter
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context, androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL, false)
        }

        homeViewModel.reciteurs.observe(viewLifecycleOwner) { list ->
            reciteurAdapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
