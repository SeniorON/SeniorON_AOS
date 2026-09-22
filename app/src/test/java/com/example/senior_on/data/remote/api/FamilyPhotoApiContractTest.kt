package com.example.senior_on.data.remote.api

import org.junit.Assert.*
import org.junit.Test
import retrofit2.http.Path
import retrofit2.http.Query

class FamilyPhotoApiContractTest {
    @Test fun parentPhotoRequestsIncludeSeniorQueryAndPathPrecedesQuery() {
        listOf("getAlbums", "getPhotos", "markViewed").forEach { name ->
            val method = FamilyApi::class.java.methods.single { it.name == name }
            assertTrue(name, method.parameterAnnotations.flatten()
                .filterIsInstance<Query>().any { it.value == "seniorId" })
            var seenQuery = false
            method.parameterAnnotations.forEach { annotations ->
                if (annotations.any { it is Query }) seenQuery = true
                if (annotations.any { it is Path }) assertFalse(name, seenQuery)
            }
        }
    }
}
