package com.example.senior_on.domain.repository.parent

import kotlinx.coroutines.flow.Flow

enum class ParentHomeUpdateEvent {
    Connecting,
    Subscribed,
    HomeUpdated,
    ScheduleUpdated,
    MedicationUpdated,
    Disconnected,
}

interface ParentHomeUpdatesRepository {
    /** Collection owns the connection; cancellation releases it and stops retries. */
    fun observeUpdates(): Flow<ParentHomeUpdateEvent>
}
