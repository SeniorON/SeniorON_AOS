package com.example.senior_on.data.local

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.senior_on.R
import com.example.senior_on.domain.model.FamilyImageSource
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FamilyPhotoSaverInstrumentedTest {
    @Test
    fun localFamilyPhotoIsSavedAndReadableFromMediaStore() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            instrumentation.uiAutomation.grantRuntimePermission(
                context.packageName,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val saver = FamilyPhotoSaver(context)
        var savedUri: Uri? = null

        try {
            savedUri = saver.save(
                imageSource = FamilyImageSource.Local(
                    drawableResId = R.drawable.img_mock_family_primary
                ),
                displayName = "SeniorON_instrumented_test_${System.currentTimeMillis()}"
            )

            assertNotNull(savedUri)
            val firstByte = context.contentResolver.openInputStream(savedUri)?.use { input ->
                input.read()
            }
            assertTrue(
                "저장한 사진을 다시 읽을 수 있어야 합니다.",
                firstByte != null && firstByte >= 0
            )
        } finally {
            savedUri?.let { uri ->
                context.contentResolver.delete(uri, null, null)
            }
        }
    }
}
