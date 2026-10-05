package com.example.senior_on.ui.parent.photo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ParentPhotoCacheKeyTest {
    @Test fun refreshedSignaturesReuseSameObject() {
        assertEquals(
            parentPhotoCacheKey("https://bucket.s3.amazonaws.com/photos/a.jpg?X-Amz-Date=old&X-Amz-Signature=one"),
            parentPhotoCacheKey("https://bucket.s3.amazonaws.com/photos/a.jpg?X-Amz-Date=new&X-Amz-Signature=two"),
        )
    }

    @Test fun differentObjectsAndImageVariantsNeverShareKeys() {
        val url = "https://bucket.s3.amazonaws.com/photos/a.jpg"
        assertNotEquals(parentPhotoCacheKey("$url?X-Amz-Signature=a"),
            parentPhotoCacheKey("${url.replace("a.jpg", "b.jpg")}?X-Amz-Signature=a"))
        assertNotEquals(parentPhotoCacheKey("$url?width=100&X-Amz-Signature=a"),
            parentPhotoCacheKey("$url?width=500&X-Amz-Signature=b"))
    }

    @Test fun unsignedUrlsKeepTheirQueryIdentity() {
        assertNotEquals(parentPhotoCacheKey("https://example.com/photo?v=1"),
            parentPhotoCacheKey("https://example.com/photo?v=2"))
    }
}
