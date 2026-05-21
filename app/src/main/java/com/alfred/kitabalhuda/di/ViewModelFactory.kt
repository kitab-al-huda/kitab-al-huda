package com.alfred.kitabalhuda.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.alfred.kitabalhuda.repository.ReciteurRepository
import com.alfred.kitabalhuda.repository.SourateRepository
import com.alfred.kitabalhuda.ui.home.HomeViewModel
import com.alfred.kitabalhuda.ui.quran.SourateViewModel

class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    private val repository: Any
) : androidx.lifecycle.AbstractSavedStateViewModelFactory(owner, null) {

    override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: androidx.lifecycle.SavedStateHandle): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository as ReciteurRepository) as T
        }
        if (modelClass.isAssignableFrom(SourateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SourateViewModel(repository as SourateRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}