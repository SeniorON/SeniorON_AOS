package com.example.senior_on.ui.child.health

import java.time.LocalDate
import java.time.LocalTime

enum class MedicationEditorMode { Add, View, Edit }

data class MedicationDraft(
    val category: String,
    val name: String,
    val times: List<LocalTime>,
    val weekdays: Set<Int>,
    val startDate: LocalDate? = null,
    val repeat: MedicationRepeatSelection = MedicationRepeatSelection(),
)

enum class HospitalEditorMode { Add, View, Edit }

enum class HospitalReminder(val label: String) {
    DayBefore("하루 전"),
    SameDay("당일"),
    None("받지 않음"),
}

data class HospitalAppointmentDraft(
    val hospitalName: String,
    val specialty: String,
    val date: LocalDate,
    val time: LocalTime,
    val reminder: HospitalReminder,
)
