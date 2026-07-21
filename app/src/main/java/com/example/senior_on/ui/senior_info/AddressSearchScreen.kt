package com.example.senior_on.ui.senior_info

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.ContextCompat
import com.example.senior_on.R
import com.example.senior_on.domain.model.AddressSearchResult
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

@Composable
fun AddressSearchScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAddressSelected: (AddressSearchResult) -> Unit = {},
    viewModel: AddressSearchViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val fusedLocationClient = remember(context) {
        LocationServices.getFusedLocationProviderClient(context)
    }
    var locationCancellationTokenSource by remember {
        mutableStateOf<CancellationTokenSource?>(null)
    }

    val fetchCurrentLocation: () -> Unit = {
        locationCancellationTokenSource?.cancel()
        val cancellationTokenSource = CancellationTokenSource()
        locationCancellationTokenSource = cancellationTokenSource
        viewModel.onLocationRequestStarted()

        requestCurrentLocation(
            fusedLocationProviderClient = fusedLocationClient,
            cancellationTokenSource = cancellationTokenSource,
            onSuccess = { latitude, longitude ->
                locationCancellationTokenSource = null
                viewModel.onCurrentLocationFound(
                    latitude = latitude,
                    longitude = longitude
                )
            },
            onFailure = {
                locationCancellationTokenSource = null
                viewModel.onLocationRequestFailed()
            }
        )
    }

    val locationSettingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (hasLocationPermission(context) && isLocationServiceEnabled(context)) {
            fetchCurrentLocation()
        } else {
            viewModel.onLocationSettingsRequired()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        when {
            !isGranted -> viewModel.onLocationPermissionDenied()
            isLocationServiceEnabled(context) -> fetchCurrentLocation()
            else -> {
                viewModel.onLocationSettingsRequired()
                locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
    }

    val onUseCurrentLocationClick: () -> Unit = {
        when {
            !hasLocationPermission(context) -> {
                locationPermissionLauncher.launch(LocationPermissions)
            }

            !isLocationServiceEnabled(context) -> {
                viewModel.onLocationSettingsRequired()
                locationSettingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }

            else -> fetchCurrentLocation()
        }
    }

    LaunchedEffect(uiState.currentLocationResult) {
        uiState.currentLocationResult?.let { result ->
            viewModel.consumeCurrentLocationResult()
            onAddressSelected(result)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            locationCancellationTokenSource?.cancel()
        }
    }

    AddressSearchScreenContent(
        uiState = uiState,
        onSearchQueryChange = viewModel::onQueryChange,
        onBackClick = onBackClick,
        onUseCurrentLocationClick = onUseCurrentLocationClick,
        onAddressSelected = onAddressSelected,
        modifier = modifier
    )
}

@Composable
private fun AddressSearchScreenContent(
    uiState: AddressSearchUiState,
    onSearchQueryChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onUseCurrentLocationClick: () -> Unit,
    onAddressSelected: (AddressSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AddressSearchTopBar(onBackClick = onBackClick)
        Spacer(modifier = Modifier.height(8.dp))

        AddressSearchContent(
            uiState = uiState,
            onSearchQueryChange = onSearchQueryChange,
            onUseCurrentLocationClick = onUseCurrentLocationClick,
            onAddressSelected = onAddressSelected
        )
    }
}

@Composable
private fun AddressSearchContent(
    uiState: AddressSearchUiState,
    onSearchQueryChange: (String) -> Unit,
    onUseCurrentLocationClick: () -> Unit,
    onAddressSelected: (AddressSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            AddressSearchField(
                value = uiState.query,
                onValueChange = onSearchQueryChange,
                onClearClick = { onSearchQueryChange("") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(49.dp)
            )

            CurrentLocationAction(onClick = onUseCurrentLocationClick)
        }

        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(SeniorOnColors.Background1)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            AddressSearchBody(
                uiState = uiState,
                onSearchQueryChange = onSearchQueryChange,
                onAddressSelected = onAddressSelected
            )
        }
    }
}

@Composable
private fun AddressSearchTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_back),
            contentDescription = "뒤로가기",
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(26.dp)
                .clickable(onClick = onBackClick),
            tint = SeniorOnColors.Gray800
        )

        Text(
            text = "주소 검색",
            modifier = Modifier.align(Alignment.Center),
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray800
        )
    }
}

@Composable
private fun AddressSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .background(SeniorOnColors.Gray50)
            .border(
                width = 1.dp,
                color = SeniorOnColors.Gray100,
                shape = RoundedCornerShape(SeniorOnRadius.Small)
            ),
        singleLine = true,
        textStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Search
        ),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = SeniorOnColors.Gray400
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = "도로명, 지번, 건물",
                            style = SeniorOnTextStyles.BodyMMedium,
                            color = SeniorOnColors.Gray300
                        )
                    }
                    innerTextField()
                }

                if (value.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(onClick = onClearClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sm_close),
                            contentDescription = "검색어 지우기",
                            modifier = Modifier.size(24.dp),
                            tint = SeniorOnColors.Gray400
                        )
                    }
                }
            }
        }
    )
}

@Composable
private fun AddressSearchBody(
    uiState: AddressSearchUiState,
    onSearchQueryChange: (String) -> Unit,
    onAddressSelected: (AddressSearchResult) -> Unit
) {
    when {
        uiState.isLocating -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = SeniorOnColors.Primary600,
                strokeWidth = 2.dp
            )
        }

        uiState.errorMessage != null -> AddressSearchStatusMessage(
            text = uiState.errorMessage
        )

        uiState.query.isBlank() -> AddressSearchGuide()

        uiState.query.trim().length < 2 -> AddressSearchStatusMessage(
            text = "두 글자 이상 입력해 주세요."
        )

        uiState.isLoading -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = SeniorOnColors.Primary600,
                strokeWidth = 2.dp
            )
        }

        uiState.hasSearched && uiState.results.isEmpty() -> AddressSearchStatusMessage(
            text = "검색 결과가 없어요.\n도로명과 건물번호를 함께 입력해 보세요."
        )

        uiState.results.isNotEmpty() -> AddressSearchResultList(
            results = uiState.results,
            onResultClick = { result ->
                if (result.needsMoreDetail) {
                    onSearchQueryChange(result.primaryAddress)
                } else {
                    onAddressSelected(result)
                }
            }
        )
    }
}

@Composable
private fun AddressSearchResultList(
    results: List<AddressSearchResult>,
    onResultClick: (AddressSearchResult) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        itemsIndexed(
            items = results,
            key = { index, result ->
                "${result.primaryAddress}_${result.longitude}_${result.latitude}_$index"
            }
        ) { _, result ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResultClick(result) }
                    .padding(horizontal = 16.dp, vertical = 18.dp)
            ) {
                Text(
                    text = result.primaryAddress,
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray800
                )

                result.secondaryAddress?.let { secondaryAddress ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = secondaryAddress,
                        style = SeniorOnTextStyles.BodySMedium,
                        color = SeniorOnColors.Gray400
                    )
                }
            }

            HorizontalDivider(
                thickness = 1.dp,
                color = SeniorOnColors.Gray100
            )
        }
    }
}

@Composable
private fun AddressSearchStatusMessage(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500
        )
    }
}

@Composable
private fun CurrentLocationAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_my_location),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = SeniorOnColors.Gray700
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "현위치로 설정하기",
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray700
        )
    }
}

@Composable
private fun AddressSearchGuide(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 24.dp, end = 24.dp)
    ) {
        Text(
            text = "이렇게 검색해보세요",
            style = SeniorOnTextStyles.BodySSemiBold,
            color = SeniorOnColors.Gray500
        )

        Spacer(modifier = Modifier.height(22.dp))
        AddressSearchGuideItem(
            title = "1. 도로명 + 건물번호",
            example = "예) 동소문로 315, 보문로 168"
        )

        Spacer(modifier = Modifier.height(24.dp))
        AddressSearchGuideItem(
            title = "2. 지역명(동/리) + 번지",
            example = "예) 정릉동 508-90, 길음동\n1285-3"
        )

        Spacer(modifier = Modifier.height(24.dp))
        AddressSearchGuideItem(
            title = "3. 지역명(동/리) + 건물명(아파트)",
            example = "예) 래미안길음센터피스"
        )
    }
}

@Composable
private fun AddressSearchGuideItem(
    title: String,
    example: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500
        )
        Text(
            text = example,
            modifier = Modifier.padding(start = 17.dp, top = 1.dp),
            style = SeniorOnTextStyles.BodySMedium,
            color = SeniorOnColors.Gray500
        )
    }
}

private val LocationPermissions = arrayOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
}

private fun isLocationServiceEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        locationManager.isLocationEnabled
    } else {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

@SuppressLint("MissingPermission")
private fun requestCurrentLocation(
    fusedLocationProviderClient: FusedLocationProviderClient,
    cancellationTokenSource: CancellationTokenSource,
    onSuccess: (latitude: Double, longitude: Double) -> Unit,
    onFailure: () -> Unit
) {
    fusedLocationProviderClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        cancellationTokenSource.token
    ).addOnSuccessListener { location ->
        if (location == null) {
            onFailure()
        } else {
            onSuccess(location.latitude, location.longitude)
        }
    }.addOnFailureListener {
        onFailure()
    }
}

@Preview(
    name = "Address Search",
    showBackground = true,
    widthDp = 360,
    heightDp = 707
)
@Composable
private fun AddressSearchScreenPreview() {
    SENIOR_ONTheme {
        AddressSearchScreenContent(
            uiState = AddressSearchUiState(),
            onSearchQueryChange = {},
            onBackClick = {},
            onUseCurrentLocationClick = {},
            onAddressSelected = {}
        )
    }
}
