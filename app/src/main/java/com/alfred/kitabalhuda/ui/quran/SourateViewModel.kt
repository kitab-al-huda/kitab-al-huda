package com.alfred.kitabalhuda.ui.quran

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.alfred.kitabalhuda.database.entity.SourateEntity
import com.alfred.kitabalhuda.repository.SourateRepository

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap

class SourateViewModel(private val repository: SourateRepository) : ViewModel() {

    private val allSourates = repository.getAllSourates().asLiveData()
    
    val searchQuery = MutableLiveData("")
    val filterType = MutableLiveData("ALL") // ALL, MECCA, MEDINA

    val sourates = MediatorLiveData<List<SourateEntity>>().apply {
        addSource(allSourates) { value = filterList() }
        addSource(searchQuery) { value = filterList() }
        addSource(filterType) { value = filterList() }
    }

    private fun filterList(): List<SourateEntity> {
        val list = allSourates.value ?: return emptyList()
        val query = searchQuery.value ?: ""
        val trimmedQuery = query.trim()
        val type = filterType.value ?: "ALL"

        if (trimmedQuery.isEmpty()) {
            return list.filter { sourate ->
                val matchesType = when (type) {
                    "MECCA" -> sourate.lieuRevelation == "Meccan"
                    "MEDINA" -> sourate.lieuRevelation == "Medinan"
                    else -> true
                }
                matchesType
            }
        }

        val lowercaseQuery = trimmedQuery.lowercase()
        val normalizedQuery = normalizeArabic(trimmedQuery)

        return list.filter { sourate ->
            val matchesQuery = sourate.nomPhonetique.lowercase().contains(lowercaseQuery) ||
                               sourate.numero.toString() == lowercaseQuery ||
                               sourate.numero.toString() == normalizedQuery ||
                               normalizeArabic(sourate.nomArabe).contains(normalizedQuery)
            val matchesType = when (type) {
                "MECCA" -> sourate.lieuRevelation == "Meccan"
                "MEDINA" -> sourate.lieuRevelation == "Medinan"
                else -> true
            }
            matchesQuery && matchesType
        }
    }

    companion object {
        fun normalizeArabic(text: String): String {
            // 1. Remove diacritics and tatweel
            val diacritics = Regex("[\\u064B-\\u065F\\u0640]")
            var result = text.replace(diacritics, "")

            // 2. Normalize Alif variants to plain Alif 'ا' (\u0627)
            result = result.replace(Regex("[أإآٱ]"), "ا")

            // 3. Normalize Teh Marbuta 'ة' (\u0629) to Heh 'ه' (\u0647)
            result = result.replace(Regex("[ة]"), "ه")

            // 4. Normalize Alef Maksura 'ى' (\u0649) to Yeh 'ي' (\u064A)
            result = result.replace(Regex("[ى]"), "ي")

            // 5. Normalize Arabic-Indic digits to Latin digits
            val arabicIndicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
            for (i in 0..9) {
                result = result.replace(arabicIndicDigits[i], '0' + i)
            }

            return result.trim().lowercase()
        }
    }
}
