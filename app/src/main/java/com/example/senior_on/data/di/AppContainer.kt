package com.example.senior_on.data.di

import android.content.Context
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.data.repository.FamilyRepository
import com.example.senior_on.data.repository.MockFamilyRepository

interface AppContainer {
    val familyRepository: FamilyRepository
    val familyPhotoUploadPreparer: FamilyPhotoUploadPreparer
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val familyRepository: FamilyRepository = MockFamilyRepository()
    override val familyPhotoUploadPreparer = FamilyPhotoUploadPreparer(context)
}
