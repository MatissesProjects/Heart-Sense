package com.heart.sense.ui.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heart.sense.data.MedicationRepository
import com.heart.sense.data.db.Medication
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(
    private val repository: MedicationRepository
) : ViewModel() {

    val medications: StateFlow<List<Medication>> = repository.activeMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addMedication(name: String, dose: String, frequency: String, reminderTime: String) {
        viewModelScope.launch {
            repository.addMedication(
                Medication(
                    name = name,
                    dose = dose,
                    frequency = frequency,
                    reminderTime = reminderTime
                )
            )
        }
    }

    fun deleteMedication(medId: Int) {
        viewModelScope.launch {
            repository.deleteMedication(medId)
        }
    }
}
