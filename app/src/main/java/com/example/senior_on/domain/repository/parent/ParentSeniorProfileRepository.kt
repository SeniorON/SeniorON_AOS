package com.example.senior_on.domain.repository.parent

/** Resolves the signed-in parent's own profile, never the guardian's selected senior. */
fun interface ParentSeniorProfileRepository {
    suspend fun getOwnSeniorId(): Long
}
