package com.example.senior_on

import android.app.Application
import com.example.senior_on.di.AppContainer
import com.example.senior_on.di.DefaultAppContainer

class SeniorOnApplication : Application() {
    val appContainer: AppContainer by lazy {
        DefaultAppContainer(this)
    }
}
