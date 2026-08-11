package com.example.senior_on.data.remote.api

import com.example.senior_on.data.local.AccessTokenStore
import com.example.senior_on.data.remote.interceptor.AccessTokenAuthenticator
import com.example.senior_on.data.remote.interceptor.HttpLoggingInterceptorFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SeniorOnNetwork {
    private const val BASE_URL = "https://senioron.site/"
    private const val FAMILY_PHOTO_UPLOAD_WRITE_TIMEOUT_SECONDS = 120L
    private const val FAMILY_PHOTO_UPLOAD_CALL_TIMEOUT_SECONDS = 150L

    private val refreshOkHttpClient by lazy {
        configuredClientBuilder()
            .followRedirects(false)
            .followSslRedirects(false)
            .addInterceptor(
                HttpLoggingInterceptorFactory.create(tag = "SeniorOnHttp")
            )
            .build()
    }

    private val refreshUserApi: UserApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(refreshOkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    private val okHttpClient by lazy {
        configuredClientBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                val token = AccessTokenStore.getBearerToken()
                val authenticatedRequest =
                    if (token != null && request.header("Authorization") == null) {
                        request.newBuilder()
                            .header("Authorization", token)
                            .build()
                    } else {
                        request
                    }
                chain.proceed(authenticatedRequest)
            }
            .authenticator(AccessTokenAuthenticator(refreshUserApi))
            .addInterceptor(
                HttpLoggingInterceptorFactory.create(tag = "SeniorOnHttp")
            )
            .build()
    }

    private fun configuredClientBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val uploadRetrofit by lazy {
        val uploadClient = okHttpClient.newBuilder()
            .writeTimeout(FAMILY_PHOTO_UPLOAD_WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(FAMILY_PHOTO_UPLOAD_CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(uploadClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }

    val accountRecoveryApi: AccountRecoveryApi by lazy {
        retrofit.create(AccountRecoveryApi::class.java)
    }

    val socialAccountApi: SocialAccountApi by lazy {
        retrofit.create(SocialAccountApi::class.java)
    }

    val seniorApi: SeniorApi by lazy {
        retrofit.create(SeniorApi::class.java)
    }

    val homeApi: HomeApi by lazy { retrofit.create(HomeApi::class.java) }
    val familyApi: FamilyApi by lazy { retrofit.create(FamilyApi::class.java) }
    val familyPhotoUploadApi: FamilyPhotoUploadApi by lazy {
        uploadRetrofit.create(FamilyPhotoUploadApi::class.java)
    }
    val hospitalApi: HospitalApi by lazy { retrofit.create(HospitalApi::class.java) }
    val medicationApi: MedicationApi by lazy { retrofit.create(MedicationApi::class.java) }
    val notificationApi: NotificationApi by lazy { retrofit.create(NotificationApi::class.java) }
    val eventApi: EventApi by lazy { retrofit.create(EventApi::class.java) }
    val userSettingsApi: UserSettingsApi by lazy {
        uploadRetrofit.create(UserSettingsApi::class.java)
    }
    val deviceApi: DeviceApi by lazy { retrofit.create(DeviceApi::class.java) }
    val inquiryApi: InquiryApi by lazy {
        uploadRetrofit.create(InquiryApi::class.java)
    }
}
