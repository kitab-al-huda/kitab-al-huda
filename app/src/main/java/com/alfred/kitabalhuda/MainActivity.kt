package com.alfred.kitabalhuda

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import kotlinx.coroutines.launch
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
        
        // Initialize ViewModel to bind service
        val viewModel = androidx.lifecycle.ViewModelProvider(this)[com.alfred.kitabalhuda.ui.player.PlayerViewModel::class.java]
        
        viewModel.playerUiState.observe(this) { state ->
            binding.miniPlayerContainer.visibility = if (state !is com.alfred.kitabalhuda.ui.player.PlayerUiState.Idle) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
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

        // Afficher les dialogs distants si nécessaire
        lifecycleScope.launch {
            com.alfred.kitabalhuda.utils.DialogManager.showIfNeeded(this@MainActivity)
        }
    }
}