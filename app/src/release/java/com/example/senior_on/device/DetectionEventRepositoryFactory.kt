package com.example.senior_on.device

import android.content.Context
import com.example.senior_on.domain.repository.server.EventRepository

fun detectionEventRepository(context: Context, real: EventRepository, config: DetectionConfig): EventRepository = real
