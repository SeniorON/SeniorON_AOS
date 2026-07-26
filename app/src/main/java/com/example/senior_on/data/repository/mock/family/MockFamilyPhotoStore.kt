package com.example.senior_on.data.repository.mock.family

import com.example.senior_on.domain.model.family.FamilyImageSource
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class MockFamilyPhotoRecord(
    val id: String,
    val authorMemberId: String,
    val createdAt: Instant,
    val imageSource: FamilyImageSource,
    val message: String = "",
)

class MockFamilyPhotoStore(
    initialPhotos: List<MockFamilyPhotoRecord>,
) {
    private val _photos = MutableStateFlow(
        initialPhotos.sortedByDescending(MockFamilyPhotoRecord::createdAt)
    )
    val photos: StateFlow<List<MockFamilyPhotoRecord>> = _photos.asStateFlow()

    fun addPhoto(photo: MockFamilyPhotoRecord) {
        _photos.update { current ->
            listOf(photo) + current.filterNot { item -> item.id == photo.id }
        }
    }

    fun removePhoto(photoId: String): MockFamilyPhotoRecord? {
        val photo = _photos.value.firstOrNull { item -> item.id == photoId }
            ?: return null
        _photos.update { current ->
            current.filterNot { item -> item.id == photoId }
        }
        return photo
    }
}
