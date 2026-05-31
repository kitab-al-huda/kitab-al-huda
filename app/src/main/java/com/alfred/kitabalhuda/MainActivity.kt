package com.alfred.kitabalhuda

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.work.WorkManager
import com.alfred.kitabalhuda.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as androidx.navigation.fragment.NavHostFragment
        val navController = navHostFragment.navController
        // No AppBarConfiguration needed if we want full custom look, or update IDs if we keep it.
        // For now, let's just link it.
        navView.setupWithNavController(navController)

        // Handle back press at the Activity level to ensure FullPlayerFragment is dismissed immediately
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fullPlayer = supportFragmentManager.findFragmentByTag("FullPlayer")
                if (fullPlayer != null && fullPlayer.isAdded) {
                    supportFragmentManager.popBackStack()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
        
        // Initialize ViewModel to bind service
        val viewModel = androidx.lifecycle.ViewModelProvider(this)[com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java]
        
        viewModel.player.observe(this) { player ->
            // Show mini player if player is connected (or valid state)
            // For now, simpler check: if we have a controller, show it (or use a dedicated liveData for visibility)
             if (player != null) {
                 binding.miniPlayerContainer.visibility = android.view.View.VISIBLE
             }
        }
        
        // Setup Toolbar
        binding.toolbar.inflateMenu(R.menu.home_menu)
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.settings_button -> {
                    val intent = android.content.Intent(this, com.alfred.kitabalhuda.ui.settings.SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        
        // Hide toolbar on Quran and Library tabs
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_discover -> {
                    binding.appBarLayout.visibility = android.view.View.VISIBLE
                }
                else -> {
                    binding.appBarLayout.visibility = android.view.View.GONE
                }
            }
        }
        
        // Initialiser WorkManager
        WorkManager.getInstance(applicationContext)
    }
}