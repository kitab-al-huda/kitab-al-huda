package com.alfred.kitabalhuda.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.alfred.kitabalhuda.databinding.FragmentLibraryBinding
import com.google.android.material.tabs.TabLayoutMediator

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup ViewPager and Tabs
        val adapter = LibraryPagerAdapter(this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(com.alfred.kitabalhuda.R.string.tab_playlists)
                1 -> getString(com.alfred.kitabalhuda.R.string.tab_history)
                2 -> getString(com.alfred.kitabalhuda.R.string.tab_downloads)
                else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class LibraryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3  // 3 tabs now

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> PlaylistListFragment.newInstance()
                1 -> HistoryFragment.newInstance()
                2 -> Fragment()  // Placeholder for Downloads
                else -> Fragment()
            }
        }
    }
}