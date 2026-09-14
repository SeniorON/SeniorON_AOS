package com.example.senior_on

import android.app.Application
import android.util.Log
import com.example.senior_on.data.local.AccessTokenStore
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
import com.example.senior_on.data.source.inquiry.RemoteInquiryDataSource
import com.example.senior_on.data.source.notification.RemoteNotificationDataSource
import com.example.senior_on.data.source.settings.RemoteUserSettingsDataSource
import com.example.senior_on.di.AppContainer
import com.example.senior_on.di.DefaultAppContainer
import com.example.senior_on.notification.SeniorOnNotificationManager
import com.example.senior_on.notification.FcmTokenSyncScheduler
import com.example.senior_on.map.KakaoMapAvailability
import com.kakao.vectormap.KakaoMapSdk
import com.kakao.sdk.common.KakaoSdk

class SeniorOnApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AccessTokenStore.initialize(this)
        FcmTokenSyncScheduler.enqueueIfLoggedIn(this)
        SeniorOnNotificationManager.createAlertChannel(this)
        if (BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank()) {
            KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }
        if (
            BuildConfig.KAKAO_NATIVE_APP_KEY.isNotBlank() &&
            KakaoMapAvailability.isSupportedDevice()
        ) {
            runCatching {
                KakaoMapSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
            }.onFailure { throwable ->
                Log.e("SeniorOnKakaoMap", "Kakao Maps SDK initialization failed", throwable)
            }
        }
    }

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
            remoteFamilySource = RemoteFamilyDataSource(
                api = SeniorOnNetwork.familyApi,
                storageApi = SeniorOnNetwork.familyPhotoStorageApi,
            ),
            hospitalDataSource = RemoteHospitalDataSource(SeniorOnNetwork.hospitalApi),
            medicationDataSource = RemoteMedicationDataSource(SeniorOnNetwork.medicationApi),
            notificationDataSource = RemoteNotificationDataSource(SeniorOnNetwork.notificationApi),
            eventDataSource = RemoteEventDataSource(SeniorOnNetwork.eventApi),
            userSettingsDataSource = RemoteUserSettingsDataSource(SeniorOnNetwork.userSettingsApi),
            deviceDataSource = RemoteDeviceDataSource(SeniorOnNetwork.deviceApi),
            inquiryDataSource = RemoteInquiryDataSource(SeniorOnNetwork.inquiryApi),
        )
    }
}
