package com.example.senior_on.ui.parent

import com.example.senior_on.ui.parent.schedule.viewmodel.toParentDisplayTime

import com.example.senior_on.ui.parent.schedule.viewmodel.ParentScheduleViewModel

import com.example.senior_on.ui.parent.schedule.viewmodel.ParentScheduleUiState

import com.example.senior_on.ui.parent.photo.viewmodel.ParentFamilyPhotoViewModel

import com.example.senior_on.ui.parent.medication.viewmodel.ParentMedicationViewModel

import com.example.senior_on.ui.parent.link.viewmodel.ParentLinkDetectionViewModel

import com.example.senior_on.ui.parent.link.viewmodel.ParentLinkDetectionStatus

import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertViewModel

import com.example.senior_on.ui.parent.emergency.viewmodel.ParentEmergencyAlertStatus

import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyViewModel

import com.example.senior_on.ui.parent.chat.viewmodel.ChatBuddyUiState

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.senior_on.R
import com.example.senior_on.data.repository.mock.fixtures.MockAuthFixtures
import com.example.senior_on.domain.repository.parent.ChatBuddyRepository
import com.example.senior_on.data.repository.mock.parent.MockChatBuddyRepository
import com.example.senior_on.data.repository.mock.parent.MockParentEmergencyAlertRepository
import com.example.senior_on.data.repository.mock.parent.MockParentFamilyPhotoRepository
import com.example.senior_on.data.repository.mock.parent.MockParentMedicationRepository
import com.example.senior_on.data.repository.mock.parent.MockParentLinkSafetyRepository
import com.example.senior_on.data.repository.mock.parent.MockParentScheduleRepository
import com.example.senior_on.domain.repository.parent.ParentFamilyPhotoRepository
import com.example.senior_on.domain.repository.parent.ParentEmergencyAlertRepository
import com.example.senior_on.domain.repository.parent.ParentMedicationRepository
import com.example.senior_on.domain.repository.parent.ParentLinkSafetyRepository
import com.example.senior_on.domain.repository.parent.ParentScheduleRepository
import com.example.senior_on.ui.parent.chat.ChatBuddyScreen
import com.example.senior_on.ui.parent.emergency.ParentEmergencyAlertScreen
import com.example.senior_on.ui.parent.medication.ParentMedicationScreen
import com.example.senior_on.ui.parent.link.ParentLinkDetectionScreen
import com.example.senior_on.ui.parent.photo.ParentFamilyMembersPhotoScreen
import com.example.senior_on.ui.parent.photo.ParentMemberPhotoGridScreen
import com.example.senior_on.ui.parent.photo.ParentPhotoSourceBottomSheet
import com.example.senior_on.ui.parent.photo.ParentPhotoViewerScreen
import com.example.senior_on.ui.parent.schedule.ParentScheduleScreen
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class ParentLauncherDestination {
    Home,
    Schedule,
    ChatBuddy,
    Medication,
    Emergency,
    LinkDetection,
    FamilyPhotos,
    MemberPhotos,
    PhotoViewer
}

@Composable
fun ParentLauncherScreen(
    scheduleRepository: ParentScheduleRepository,
    chatBuddyRepository: ChatBuddyRepository,
    familyPhotoRepository: ParentFamilyPhotoRepository,
    medicationRepository: ParentMedicationRepository,
    emergencyAlertRepository: ParentEmergencyAlertRepository,
    linkSafetyRepository: ParentLinkSafetyRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var destination by rememberSaveable {
        androidx.compose.runtime.mutableStateOf(ParentLauncherDestination.Home)
    }
    var isPhotoSourceSheetVisible by rememberSaveable {
        androidx.compose.runtime.mutableStateOf(false)
    }
    var selectedMemberId by rememberSaveable {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }
    var selectedPhotoId by rememberSaveable {
        androidx.compose.runtime.mutableStateOf<String?>(null)
    }
    val scheduleViewModel: ParentScheduleViewModel = viewModel(
        factory = ParentScheduleViewModel.factory(scheduleRepository)
    )
    val chatBuddyViewModel: ChatBuddyViewModel = viewModel(
        factory = ChatBuddyViewModel.factory(chatBuddyRepository)
    )
    val familyPhotoViewModel: ParentFamilyPhotoViewModel = viewModel(
        factory = ParentFamilyPhotoViewModel.factory(
            repository = familyPhotoRepository,
            familyCode = MockAuthFixtures.DISPLAY_FAMILY_SHARE_CODE
        )
    )
    val medicationViewModel: ParentMedicationViewModel = viewModel(
        factory = ParentMedicationViewModel.factory(medicationRepository)
    )
    val emergencyAlertViewModel: ParentEmergencyAlertViewModel = viewModel(
        factory = ParentEmergencyAlertViewModel.factory(emergencyAlertRepository)
    )
    val linkDetectionViewModel: ParentLinkDetectionViewModel = viewModel(
        factory = ParentLinkDetectionViewModel.factory(linkSafetyRepository)
    )
    val scheduleUiState by scheduleViewModel.uiState.collectAsStateWithLifecycle()
    val chatBuddyUiState by chatBuddyViewModel.uiState.collectAsStateWithLifecycle()
    val familyPhotoUiState by familyPhotoViewModel.uiState.collectAsStateWithLifecycle()
    val medicationUiState by medicationViewModel.uiState.collectAsStateWithLifecycle()
    val emergencyAlertUiState by
        emergencyAlertViewModel.uiState.collectAsStateWithLifecycle()
    val linkDetectionUiState by
        linkDetectionViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(emergencyAlertUiState.status) {
        if (emergencyAlertUiState.status == ParentEmergencyAlertStatus.Sent) {
            destination = ParentLauncherDestination.Home
            emergencyAlertViewModel.reset()
        }
    }

    LaunchedEffect(linkDetectionUiState.status) {
        if (linkDetectionUiState.status == ParentLinkDetectionStatus.Safe) {
            linkDetectionUiState.url?.let { url ->
                openExternalBrowser(context, url)
            }
            linkDetectionViewModel.reset()
            destination = ParentLauncherDestination.Home
        }
    }

    BackHandler(enabled = destination != ParentLauncherDestination.Home) {
        if (destination == ParentLauncherDestination.Emergency) {
            emergencyAlertViewModel.cancel()
        }
        if (destination == ParentLauncherDestination.LinkDetection) {
            linkDetectionViewModel.reset()
        }
        destination = when (destination) {
            ParentLauncherDestination.PhotoViewer ->
                ParentLauncherDestination.MemberPhotos
            ParentLauncherDestination.MemberPhotos ->
                ParentLauncherDestination.FamilyPhotos
            ParentLauncherDestination.FamilyPhotos,
            ParentLauncherDestination.Schedule,
            ParentLauncherDestination.ChatBuddy,
            ParentLauncherDestination.Medication,
            ParentLauncherDestination.Emergency,
            ParentLauncherDestination.LinkDetection ->
                ParentLauncherDestination.Home
            ParentLauncherDestination.Home -> ParentLauncherDestination.Home
        }
    }

    when (destination) {
        ParentLauncherDestination.Home -> ParentLauncherHome(
            onPhotoClick = {
                isPhotoSourceSheetVisible = true
            },
            onLinkDetectionClick = {
                linkDetectionViewModel.inspectLink(TEMPORARY_SAFE_LINK)
                destination = ParentLauncherDestination.LinkDetection
            },
            onMissingAppTestClick = {
                openAppOrPlayStore(
                    context = context,
                    packageName = TEMPORARY_TEST_APP_PACKAGE
                )
            },
            modifier = modifier
        )

        ParentLauncherDestination.Schedule -> ParentScheduleScreen(
            uiState = scheduleUiState,
            onBackClick = { destination = ParentLauncherDestination.Home },
            modifier = modifier
        )

        ParentLauncherDestination.ChatBuddy -> ChatBuddyScreen(
            uiState = chatBuddyUiState,
            onBackClick = { destination = ParentLauncherDestination.Home },
            onVoiceButtonClick = chatBuddyViewModel::onVoiceButtonClick,
            modifier = modifier
        )

        ParentLauncherDestination.Medication -> ParentMedicationScreen(
            uiState = medicationUiState,
            onBackClick = { destination = ParentLauncherDestination.Home },
            onTakenClick = medicationViewModel::markAsTaken,
            modifier = modifier
        )

        ParentLauncherDestination.Emergency -> ParentEmergencyAlertScreen(
            uiState = emergencyAlertUiState,
            onBackClick = {
                emergencyAlertViewModel.cancel()
                destination = ParentLauncherDestination.Home
            },
            onSendClick = emergencyAlertViewModel::sendEmergencyAlert,
            onCancelClick = {
                emergencyAlertViewModel.cancel()
                destination = ParentLauncherDestination.Home
            },
            modifier = modifier
        )

        ParentLauncherDestination.LinkDetection -> ParentLinkDetectionScreen(
            uiState = linkDetectionUiState,
            modifier = modifier
        )

        ParentLauncherDestination.FamilyPhotos -> ParentFamilyMembersPhotoScreen(
            uiState = familyPhotoUiState,
            onBackClick = { destination = ParentLauncherDestination.Home },
            onMemberClick = { memberId ->
                selectedMemberId = memberId
                destination = ParentLauncherDestination.MemberPhotos
            },
            onRetryClick = familyPhotoViewModel::loadFamilyPhotos,
            modifier = modifier
        )

        ParentLauncherDestination.MemberPhotos -> {
            val member = familyPhotoUiState.members
                .firstOrNull { it.memberId == selectedMemberId }
            if (member == null) {
                ParentFamilyMembersPhotoScreen(
                    uiState = familyPhotoUiState,
                    onBackClick = { destination = ParentLauncherDestination.Home },
                    onMemberClick = { memberId ->
                        selectedMemberId = memberId
                    },
                    onRetryClick = familyPhotoViewModel::loadFamilyPhotos,
                    modifier = modifier
                )
            } else {
                ParentMemberPhotoGridScreen(
                    member = member,
                    onBackClick = {
                        destination = ParentLauncherDestination.FamilyPhotos
                    },
                    onPhotoClick = { photoId ->
                        selectedPhotoId = photoId
                        destination = ParentLauncherDestination.PhotoViewer
                    },
                    modifier = modifier
                )
            }
        }

        ParentLauncherDestination.PhotoViewer -> {
            val member = familyPhotoUiState.members
                .firstOrNull { it.memberId == selectedMemberId }
            val photoId = selectedPhotoId
            if (member != null && photoId != null) {
                ParentPhotoViewerScreen(
                    member = member,
                    initialPhotoId = photoId,
                    onBackClick = {
                        destination = ParentLauncherDestination.MemberPhotos
                    },
                    modifier = modifier
                )
            }
        }
    }

    if (isPhotoSourceSheetVisible) {
        ParentPhotoSourceBottomSheet(
            onDismiss = { isPhotoSourceSheetVisible = false },
            onFamilyPhotosClick = {
                isPhotoSourceSheetVisible = false
                destination = ParentLauncherDestination.FamilyPhotos
            },
            onGalleryClick = {
                isPhotoSourceSheetVisible = false
                openSystemGallery(context)
            }
        )
    }

}

@Composable
private fun ParentLauncherHome(
    onPhotoClick: () -> Unit,
    onLinkDetectionClick: () -> Unit,
    onMissingAppTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.Background2)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Text(
            text = LocalDate.now().format(
                DateTimeFormatter.ofPattern("M월 d일 (E)", Locale.KOREAN)
            ),
            style = SeniorOnTextStyles.BodyLBold,
            color = SeniorOnColors.Gray600
        )

        Text(
            text = "부모님 화면",
            modifier = Modifier.padding(top = 4.dp),
            style = SeniorOnTextStyles.HeadingXXL,
            color = SeniorOnColors.Gray800
        )

        Spacer(modifier = Modifier.height(24.dp))

        ParentPhotoButton(onClick = onPhotoClick)

        Spacer(modifier = Modifier.height(12.dp))

        ParentLinkDetectionButton(onClick = onLinkDetectionClick)

        Spacer(modifier = Modifier.height(12.dp))

        ParentMissingAppTestButton(onClick = onMissingAppTestClick)
    }
}

@Composable
private fun TodayScheduleButton(
    uiState: ParentScheduleUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val firstSchedule = uiState.schedules.firstOrNull()
    val enabled = !uiState.isLoading && firstSchedule != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(84.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.White)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = SeniorOnColors.Primary600,
                strokeWidth = 3.dp
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_calendar),
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = if (enabled) {
                    SeniorOnColors.Primary600
                } else {
                    SeniorOnColors.Gray300
                }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            when {
                uiState.isLoading -> Text(
                    text = "오늘 일정을 불러오고 있어요",
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray400
                )

                firstSchedule == null -> Text(
                    text = "오늘은 일정이 없어요",
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray300
                )

                else -> {
                    Text(
                        text = firstSchedule.time.toParentDisplayTime(),
                        style = SeniorOnTextStyles.BodyLBold,
                        color = SeniorOnColors.Gray800
                    )
                    Text(
                        text = firstSchedule.title,
                        style = SeniorOnTextStyles.BodyMRegular,
                        color = SeniorOnColors.Gray500
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentChatBuddyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_chat_buddy),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = SeniorOnColors.Primary600
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "말벗",
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = "이야기를 나눠보세요",
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.Gray500
            )
        }
    }
}

@Composable
private fun ParentPhotoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_photo),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = SeniorOnColors.Primary600
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "사진",
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = "가족이 보낸 사진을 보세요",
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.Gray500
            )
        }
    }
}

@Composable
private fun ParentMedicationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_health),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = SeniorOnColors.Primary600
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "복약",
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = "오늘 먹을 약을 확인하세요",
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.Gray500
            )
        }
    }
}

@Composable
private fun ParentEmergencyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.Red300)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_alert_filled),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = SeniorOnColors.White
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "긴급알림",
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.White
            )
            Text(
                text = "자녀에게 긴급알림을 보내요",
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.White
            )
        }
    }
}

@Composable
private fun ParentLinkDetectionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_link),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = SeniorOnColors.Red200
        )

        Column(modifier = Modifier.padding(start = 16.dp)) {
            Text(
                text = "링크 검사",
                style = SeniorOnTextStyles.HeadingM,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = "임시 링크 검사 화면을 확인해요",
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.Gray500
            )
        }
    }
}

@Composable
private fun ParentMissingAppTestButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(104.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Large))
            .background(SeniorOnColors.White)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_big_download),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = SeniorOnColors.Primary600
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        ) {
            Text(
                text = "없는 앱 실행 테스트",
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray800
            )
            Text(
                text = "듀오링고가 없으면 Play 스토어를 열어요",
                style = SeniorOnTextStyles.BodyMRegular,
                color = SeniorOnColors.Gray500
            )
        }
    }
}

private fun openAppOrPlayStore(
    context: Context,
    packageName: String
) {
    val launchIntent = context.packageManager
        .getLaunchIntentForPackage(packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    val appOpened = launchIntent?.let { intent ->
        runCatching {
            context.startActivity(intent)
        }.isSuccess
    } == true

    if (appOpened) return

    val playStoreIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("market://details?id=$packageName")
    ).apply {
        setPackage("com.android.vending")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    val playStoreOpened = runCatching {
        context.startActivity(playStoreIntent)
    }.isSuccess

    if (playStoreOpened) return

    val webStoreIntent = Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
    ).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    runCatching { context.startActivity(webStoreIntent) }
}

private fun openExternalBrowser(context: Context, url: String) {
    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addCategory(Intent.CATEGORY_BROWSABLE)
    }
    runCatching { context.startActivity(browserIntent) }
}

private fun openSystemGallery(context: Context) {
    val galleryIntent = Intent(
        Intent.ACTION_VIEW,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    ).apply {
        type = "image/*"
    }

    try {
        context.startActivity(galleryIntent)
    } catch (_: ActivityNotFoundException) {
        val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        runCatching { context.startActivity(fallbackIntent) }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ParentLauncherScreenPreview() {
    SENIOR_ONTheme {
        ParentLauncherScreen(
            scheduleRepository = MockParentScheduleRepository(),
            chatBuddyRepository = MockChatBuddyRepository(),
            familyPhotoRepository = MockParentFamilyPhotoRepository(),
            medicationRepository = MockParentMedicationRepository(),
            emergencyAlertRepository = MockParentEmergencyAlertRepository(),
            linkSafetyRepository = MockParentLinkSafetyRepository()
        )
    }
}

private const val TEMPORARY_SAFE_LINK = "https://www.naver.com"
private const val TEMPORARY_TEST_APP_PACKAGE = "com.duolingo"
