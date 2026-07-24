package com.example.senior_on.data.di

import android.content.Context
import com.example.senior_on.data.local.FamilyPhotoUploadPreparer
import com.example.senior_on.data.repository.DisplayRepository
import com.example.senior_on.data.repository.FamilyRepository
import com.example.senior_on.data.repository.MockDisplayRepository
import com.example.senior_on.data.repository.MockFamilyRepository
import com.example.senior_on.data.repository.MockParentInfoFixtures
import com.example.senior_on.data.repository.MockParentInfoRepository
import com.example.senior_on.data.repository.ParentInfoRepository

interface AppContainer {
    val familyRepository: FamilyRepository
    val familyPhotoUploadPreparer: FamilyPhotoUploadPreparer
    val displayRepository: DisplayRepository
    val parentInfoRepository: ParentInfoRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    override val familyRepository: FamilyRepository = MockFamilyRepository()
    override val familyPhotoUploadPreparer = FamilyPhotoUploadPreparer(context)
    override val displayRepository: DisplayRepository = MockDisplayRepository()
    override val parentInfoRepository: ParentInfoRepository = MockParentInfoRepository(
        initialParentInfo = MockParentInfoFixtures.mother,
    )
}
