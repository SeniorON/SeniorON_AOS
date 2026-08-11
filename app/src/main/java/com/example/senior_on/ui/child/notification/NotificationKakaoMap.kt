package com.example.senior_on.ui.child.notification

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.senior_on.BuildConfig
import com.example.senior_on.R
import com.example.senior_on.map.KakaoMapAvailability
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import com.kakao.vectormap.GestureType
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView

@Composable
internal fun NotificationKakaoMap(
    latitude: Double?,
    longitude: Double?,
    modifier: Modifier = Modifier,
) {
    val hasValidCoordinate = latitude != null && longitude != null &&
        latitude in -90.0..90.0 && longitude in -180.0..180.0
    val isSupportedDevice = KakaoMapAvailability.isSupportedDevice()
    if (
        !hasValidCoordinate ||
        BuildConfig.KAKAO_NATIVE_APP_KEY.isBlank() ||
        !isSupportedDevice
    ) {
        NotificationMapUnavailable(
            message = when {
                !hasValidCoordinate -> "위치 좌표를 확인할 수 없습니다."
                !isSupportedDevice -> "이 에뮬레이터에서는 카카오 지도를 지원하지 않습니다."
                else -> "카카오 지도 앱 키가 필요합니다."
            },
            modifier = modifier,
        )
        return
    }

    val resolvedLatitude = requireNotNull(latitude)
    val resolvedLongitude = requireNotNull(longitude)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember(resolvedLatitude, resolvedLongitude) {
        MapView(context)
    }

    DisposableEffect(mapView, lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.resume()
                Lifecycle.Event.ON_PAUSE -> mapView.pause()
                else -> Unit
            }
        }
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() = Unit

                override fun onMapError(error: Exception) {
                    Log.w(MAP_LOG_TAG, "Kakao map failed", error)
                }
            },
            object : KakaoMapReadyCallback() {
                override fun onMapReady(kakaoMap: KakaoMap) {
                    DisabledMapGestures.forEach { gestureType ->
                        kakaoMap.setGestureEnable(gestureType, false)
                    }
                    Log.d(
                        MAP_LOG_TAG,
                        "Kakao map ready at latitude=$resolvedLatitude, longitude=$resolvedLongitude",
                    )
                }

                override fun getPosition(): LatLng =
                    LatLng.from(resolvedLatitude, resolvedLongitude)

                override fun getZoomLevel(): Int = MAP_ZOOM_LEVEL
            },
        )
        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            mapView.pause()
            mapView.finish()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MAP_HEIGHT)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium)),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier.fillMaxSize(),
        )
        Icon(
            painter = painterResource(R.drawable.ic_location),
            contentDescription = "긴급알림 발생 위치",
            tint = SeniorOnColors.Red300,
            modifier = Modifier.size(30.dp),
        )
    }
}

@Composable
private fun NotificationMapUnavailable(
    message: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MAP_HEIGHT)
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background5),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500,
        )
    }
}

private const val MAP_LOG_TAG = "SeniorOnKakaoMap"
private const val MAP_ZOOM_LEVEL = 16
private val MAP_HEIGHT = 102.dp
private val DisabledMapGestures = listOf(
    GestureType.Pan,
    GestureType.Zoom,
    GestureType.Rotate,
    GestureType.Tilt,
    GestureType.RotateZoom,
    GestureType.OneFingerDoubleTap,
    GestureType.TwoFingerSingleTap,
    GestureType.OneFingerZoom,
    GestureType.LongTapAndDrag,
)
