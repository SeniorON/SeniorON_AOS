package com.example.senior_on

import android.app.Application
import com.example.senior_on.data.remote.api.SeniorOnNetwork
import com.example.senior_on.data.source.auth.RemoteAccountRecoveryDataSource
import com.example.senior_on.data.source.auth.RemoteAuthDataSource
import com.example.senior_on.data.source.auth.RemoteSocialAuthDataSource
import com.example.senior_on.data.source.senior.RemoteSeniorDataSource
import com.example.senior_on.data.source.device.RemoteDeviceDataSource
import com.example.senior_on.data.source.event.RemoteEventDataSource
import com.example.senior_on.data.source.family.RemoteFamilyDataSource
import com.example.senior_on.data.source.health.RemoteHospitalDataSource
import com.example.senior_on.data.source.home.RemoteHomeDataSource
import com.example.senior_on.data.source.medication.RemoteMedicationDataSource
import com.example.senior_on.data.source.notification.RemoteNotificationDataSource
import com.example.senior_on.data.source.settings.RemoteUserSettingsDataSource
import com.example.senior_on.di.AppContainer
import com.example.senior_on.di.DefaultAppContainer

class SeniorOnApplication : Application() {
    val appContainer: AppContainer by lazy {
        DefaultAppContainer(
            context = this,
            authDataSource = RemoteAuthDataSource(SeniorOnNetwork.userApi),
            accountRecoveryDataSource = RemoteAccountRecoveryDataSource(
                SeniorOnNetwork.accountRecoveryApi
            ),
            socialAuthDataSource = RemoteSocialAuthDataSource(
                SeniorOnNetwork.socialAccountApi
            ),
            seniorDataSource = RemoteSeniorDataSource(SeniorOnNetwork.seniorApi),
            homeDataSource = RemoteHomeDataSource(SeniorOnNetwork.homeApi),
            remoteFamilySource = RemoteFamilyDataSource(SeniorOnNetwork.familyApi),
            hospitalDataSource = RemoteHospitalDataSource(SeniorOnNetwork.hospitalApi),
            medicationDataSource = RemoteMedicationDataSource(SeniorOnNetwork.medicationApi),
            notificationDataSource = RemoteNotificationDataSource(SeniorOnNetwork.notificationApi),
            eventDataSource = RemoteEventDataSource(SeniorOnNetwork.eventApi),
            userSettingsDataSource = RemoteUserSettingsDataSource(SeniorOnNetwork.userSettingsApi),
            deviceDataSource = RemoteDeviceDataSource(SeniorOnNetwork.deviceApi)
        )
    }
}
