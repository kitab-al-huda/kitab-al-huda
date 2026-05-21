package com.alfred.kitabalhuda.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.alfred.kitabalhuda.repository.ReciteurRepository

class HomeViewModel(private val repository: ReciteurRepository) : ViewModel() {
    
    // Convert Flow to LiveData for the UI
    val reciteurs = repository.getAllReciteurs().asLiveData()

}