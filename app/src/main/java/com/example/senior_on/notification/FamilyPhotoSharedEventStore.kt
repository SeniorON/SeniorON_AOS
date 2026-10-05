package com.example.senior_on.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** A refresh signal only: photo data always comes from the authenticated API. */
object FamilyPhotoSharedEventStore {
    private val _revision = MutableStateFlow(0L)
    val revision = _revision.asStateFlow()

    fun publish(data: Map<String, String>) {
        if (data["type"] != "FAMILY_PHOTO_SHARED") return
        if (data["familyPhotoId"]?.toLongOrNull()?.let { it > 0 } != true) return
        _revision.update { it + 1 }
    }
}
