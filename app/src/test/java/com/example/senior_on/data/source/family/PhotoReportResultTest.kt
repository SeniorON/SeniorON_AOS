package com.example.senior_on.data.source.family

import com.example.senior_on.data.remote.dto.ApiResponse
import com.example.senior_on.domain.model.auth.RemoteRequestException
import com.example.senior_on.domain.model.family.FamilyPhotoReportResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoReportResultTest {
    @Test fun distinguishesCreatedFromDuplicateAndHiddenResponses() = runTest {
        for ((code, expected) in mapOf(
            "COMMON_200" to FamilyPhotoReportResult.Created,
            "FAMILY_PHOTO200_1" to FamilyPhotoReportResult.Duplicate("안내", false),
            "FAMILY_PHOTO200_2" to FamilyPhotoReportResult.Duplicate("안내", true),
            "FAMILY_PHOTO200_3" to FamilyPhotoReportResult.Duplicate("안내", false),
            "FAMILY_PHOTO200_4" to FamilyPhotoReportResult.Duplicate("안내", true),
        )) {
            assertEquals(expected, reportResult { ApiResponse("200 OK", code, "안내", null) })
        }
    }

    @Test fun alreadyHiddenConflictsAlsoRemoveStaleContent() = runTest {
        for (code in listOf("FAMILY_PHOTO409_1", "FAMILY_PHOTO409_2")) {
            assertEquals(FamilyPhotoReportResult.Duplicate("이미 숨김", true), reportResult {
                throw RemoteRequestException(409, code, null, "이미 숨김", IllegalStateException())
            })
        }
    }
}
