package com.example.senior_on.ui.child.notification.route

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.senior_on.domain.repository.location.LocationRepository
import com.example.senior_on.ui.child.notification.NotificationCategory
import com.example.senior_on.ui.child.notification.NotificationDetailScreen
import com.example.senior_on.ui.child.notification.NotificationMessageUiState
import kotlinx.coroutines.launch

@Composable
internal fun NotificationDetailRoute(
    category: NotificationCategory,
    message: NotificationMessageUiState,
    parentPhoneNumber: String?,
    locationRepository: LocationRepository?,
    onBackClick: () -> Unit,
    onRefreshClick: () -> Unit = {},
    isRefreshing: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isResolvingDirections by remember { mutableStateOf(false) }

    fun openDirectionsFromCurrentLocation() {
        if (isResolvingDirections) return
        val destinationLatitude = message.latitude
        val destinationLongitude = message.longitude
        if (destinationLatitude == null || destinationLongitude == null) {
            Toast.makeText(
                context,
                "확인할 수 있는 위치 정보가 없습니다.",
                Toast.LENGTH_SHORT,
            ).show()
            return
        }

        coroutineScope.launch {
            isResolvingDirections = true
            try {
                val currentLocation = runCatching {
                    locationRepository?.getCurrentLocation()
                }.getOrNull()
                if (currentLocation == null) {
                    Toast.makeText(
                        context,
                        "현재 위치를 확인하지 못해 목적지만 전달합니다.",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
                openKakaoMapDirections(
                    context = context,
                    latitude = destinationLatitude,
                    longitude = destinationLongitude,
                    destinationName = message.senderName ?: "시니어 위치",
                    startLatitude = currentLocation?.latitude,
                    startLongitude = currentLocation?.longitude,
                )
            } finally {
                isResolvingDirections = false
            }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.values.any { granted -> granted }) {
            openDirectionsFromCurrentLocation()
        } else {
            Toast.makeText(
                context,
                "현재 위치를 출발지로 사용하려면 위치 권한이 필요합니다.",
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    NotificationDetailScreen(
        category = category,
        message = message,
        modifier = modifier,
        onBackClick = onBackClick,
        onRefreshClick = onRefreshClick,
        isRefreshing = isRefreshing,
        isDirectionsLoading = isResolvingDirections,
        onCallClick = {
            if (!openPhoneDialer(context, parentPhoneNumber)) {
                Toast.makeText(
                    context,
                    "등록된 시니어 전화번호가 없습니다.",
                    Toast.LENGTH_SHORT,
                ).show()
            }
        },
        onDirectionsClick = {
            if (context.hasLocationPermission()) {
                openDirectionsFromCurrentLocation()
            } else {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }
        },
    )
}

private fun Context.hasLocationPermission(): Boolean =
    ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
