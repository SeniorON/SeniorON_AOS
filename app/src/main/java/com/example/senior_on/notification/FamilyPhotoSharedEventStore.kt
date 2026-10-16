package com.example.senior_on.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** A refresh signal only: photo data always comes from the authenticated API. */
object FamilyPhotoSharedEventStore {
    private val _revision = MutableStateFlow(0L)
    val revision = _revision.asStateFlow()
    private val _deletedPhotoIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletedPhotoIds = _deletedPhotoIds.asStateFlow()

    fun publish(data: Map<String, String>) {
        if (data["type"] != "FAMILY_PHOTO_SHARED" && !isFamilyPhotoDeletion(data)) return
        val id = data["familyPhotoId"]?.toLongOrNull()?.takeIf { it > 0 } ?: return
        if (isFamilyPhotoDeletion(data)) {
            _deletedPhotoIds.update { (it + id).toList().takeLast(256).toSet() }
        }
        _revision.update { it + 1 }
    }
}

internal fun isFamilyPhotoDeletion(data: Map<String, String>): Boolean =
    data["type"] == "FAMILY_PHOTO_DELETED"
