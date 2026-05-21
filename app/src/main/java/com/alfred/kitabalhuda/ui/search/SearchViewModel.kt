package com.alfred.kitabalhuda.ui.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
// import com.alfred.kitabalhuda.database.entity.AnimeEntity
// import com.alfred.kitabalhuda.repository.AnimeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.flowOf
import androidx.lifecycle.AndroidViewModel

@OptIn(ExperimentalCoroutinesApi::class)
// class SearchViewModel(application: Application, private val repository: AnimeRepository) : AndroidViewModel(application) {
// ...
// }
class SearchViewModel(application: Application) : AndroidViewModel(application) {
    // Disabled
}
 