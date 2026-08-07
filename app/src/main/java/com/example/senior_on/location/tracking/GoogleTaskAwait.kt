package com.example.senior_on.location.tracking

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun <T> Task<T>.awaitResult(): T =
    suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { result ->
            if (continuation.isActive) continuation.resume(result)
        }
        addOnFailureListener { throwable ->
            if (continuation.isActive) continuation.resumeWithException(throwable)
        }
        addOnCanceledListener {
            continuation.cancel()
        }
    }
