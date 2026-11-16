package com.example.senior_on.domain.repository.auth

import kotlinx.coroutines.flow.Flow

interface ChildTutorialRepository {
    /** False without a signed-in child account with a known server usersId. */
    val pending: Flow<Boolean>

    /** Claims the current account's pending tutorial once; false if none was pending. */
    fun consume(): Boolean
}
