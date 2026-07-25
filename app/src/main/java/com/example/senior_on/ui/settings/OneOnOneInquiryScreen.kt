package com.example.senior_on.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private enum class OneOnOneInquiryTab {
    Write,
    History
}

private enum class InquiryAnswerStatus {
    Waiting,
    Answered
}

private data class InquiryHistoryItem(
    val id: String,
    val status: InquiryAnswerStatus,
    val createdAtLabel: String,
    val question: String,
    val answer: String? = null
)

private const val MaxInquiryImages = 5

private val InquiryDateTimeFormatter = DateTimeFormatter.ofPattern("yy.MM.dd HH:mm")

private val SampleAnsweredInquiry = InquiryHistoryItem(
    id = "sample-answered",
    status = InquiryAnswerStatus.Answered,
    createdAtLabel = "26.04.28 14:12",
    question = "가족 코드를 잃어버렸어요",
    answer = "가족코드는 앱의 [설정] > [내 계정]에서 언제든 다시 확인할 수 있어요. " +
        "가족코드를 잃어버렸다면 해당 메뉴에서 코드를 확인한 후 가족에게 다시 공유해 주세요."
)

@Composable
fun OneOnOneInquiryScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable { mutableStateOf(OneOnOneInquiryTab.Write) }
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }
    var imageUris by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var showSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var historyItems by remember { mutableStateOf(emptyList<InquiryHistoryItem>()) }
    var pendingSubmitQuestion by rememberSaveable { mutableStateOf<String?>(null) }

    val remainingSlots = (MaxInquiryImages - imageUris.size).coerceAtLeast(0)
    val multiImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(MaxInquiryImages)
    ) { uris ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        imageUris = (imageUris + uris.map(Uri::toString))
            .distinct()
            .take(MaxInquiryImages)
    }
    val singleImagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        imageUris = (imageUris + uri.toString())
            .distinct()
            .take(MaxInquiryImages)
    }

    val canSubmit = title.isNotBlank() && content.isNotBlank()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.White)
                .statusBarsPadding()
        ) {
            SettingsBackTopAppBar(
                title = "1:1 문의하기",
                onBackClick = onBackClick
            )

            OneOnOneInquiryTabRow(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            when (selectedTab) {
                OneOnOneInquiryTab.Write -> OneOnOneInquiryWriteContent(
                    title = title,
                    onTitleChange = { title = it },
                    content = content,
                    onContentChange = { content = it },
                    imageUris = imageUris,
                    onAddImageClick = {
                        when {
                            remainingSlots <= 0 -> Unit
                            remainingSlots == 1 -> singleImagePicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                            else -> multiImagePicker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                    },
                    canSubmit = canSubmit,
                    onSubmitClick = {
                        pendingSubmitQuestion = title.trim()
                        showSuccessDialog = true
                    },
                    modifier = Modifier.weight(1f)
                )

                OneOnOneInquiryTab.History -> OneOnOneInquiryHistoryContent(
                    items = historyItems,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (showSuccessDialog) {
            InquirySubmitSuccessDialog(
                onConfirmClick = {
                    val question = pendingSubmitQuestion.orEmpty().ifBlank { title.trim() }
                    if (question.isNotBlank()) {
                        historyItems = listOf(
                            InquiryHistoryItem(
                                id = "inquiry-${System.currentTimeMillis()}",
                                status = InquiryAnswerStatus.Waiting,
                                createdAtLabel = LocalDateTime.now().format(InquiryDateTimeFormatter),
                                question = question
                            )
                        ) + historyItems
                    }
                    showSuccessDialog = false
                    pendingSubmitQuestion = null
                    title = ""
                    content = ""
                    imageUris = emptyList()
                    selectedTab = OneOnOneInquiryTab.History
                }
            )
        }
    }
}

@Composable
private fun OneOnOneInquiryTabRow(
    selectedTab: OneOnOneInquiryTab,
    onTabSelected: (OneOnOneInquiryTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth()) {
        OneOnOneInquiryTabItem(
            text = "문의하기",
            selected = selectedTab == OneOnOneInquiryTab.Write,
            onClick = { onTabSelected(OneOnOneInquiryTab.Write) },
            modifier = Modifier.weight(1f)
        )
        OneOnOneInquiryTabItem(
            text = "내 문의내역",
            selected = selectedTab == OneOnOneInquiryTab.History,
            onClick = { onTabSelected(OneOnOneInquiryTab.History) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun OneOnOneInquiryTabItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(top = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            style = if (selected) {
                SeniorOnTextStyles.BodyMBold
            } else {
                SeniorOnTextStyles.BodyMMedium
            },
            color = if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray300
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (selected) 2.dp else 1.dp)
                .background(
                    if (selected) SeniorOnColors.Primary600 else SeniorOnColors.Gray200
                )
        )
    }
}

@Composable
private fun OneOnOneInquiryWriteContent(
    title: String,
    onTitleChange: (String) -> Unit,
    content: String,
    onContentChange: (String) -> Unit,
    imageUris: List<String>,
    onAddImageClick: () -> Unit,
    canSubmit: Boolean,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_information2),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = SeniorOnColors.Gray300
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = buildAnnotatedString {
                        append("답변 등록시 ")
                        withStyle(
                            SpanStyle(
                                color = SeniorOnColors.Gray800,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("앱 알림")
                        }
                        append("과 ")
                        withStyle(
                            SpanStyle(
                                color = SeniorOnColors.Gray800,
                                fontWeight = FontWeight.SemiBold
                            )
                        ) {
                            append("이메일")
                        }
                        append("로 알려드려요!")
                    },
                    style = SeniorOnTextStyles.BodySRegular,
                    color = SeniorOnColors.Gray500
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            InquirySingleLineField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = "문의 제목 입력"
            )

            Spacer(modifier = Modifier.height(12.dp))

            InquiryMultiLineField(
                value = content,
                onValueChange = onContentChange,
                placeholder = "문의 내용 입력",
                modifier = Modifier.height(180.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "이미지 첨부 (최대 5장)",
                style = SeniorOnTextStyles.BodyMSemiBold,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (imageUris.size < MaxInquiryImages) {
                    InquiryAddImageButton(onClick = onAddImageClick)
                }
                imageUris.forEach { uri ->
                    InquiryImageThumbnail(uri = uri)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        SettingsPrimaryButton(
            text = "문의하기",
            enabled = canSubmit,
            onClick = onSubmitClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun InquirySingleLineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, SeniorOnColors.Gray200, shape)
            .clip(shape)
            .background(SeniorOnColors.White)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        singleLine = true,
        textStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800),
        cursorBrush = SolidColor(SeniorOnColors.Primary600),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = SeniorOnTextStyles.BodyMMedium,
                        color = SeniorOnColors.Gray300
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun InquiryMultiLineField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, SeniorOnColors.Gray200, shape)
            .clip(shape)
            .background(SeniorOnColors.White)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        textStyle = SeniorOnTextStyles.BodyMMedium.copy(color = SeniorOnColors.Gray800),
        cursorBrush = SolidColor(SeniorOnColors.Primary600),
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = SeniorOnTextStyles.BodyMMedium,
                        color = SeniorOnColors.Gray300
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun InquiryAddImageButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .border(1.dp, SeniorOnColors.Gray200, RoundedCornerShape(SeniorOnRadius.Small))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_plus),
            contentDescription = "이미지 추가",
            modifier = Modifier.size(24.dp),
            tint = SeniorOnColors.Gray300
        )
    }
}

@Composable
private fun InquiryImageThumbnail(
    uri: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier
            .size(72.dp)
            .clip(RoundedCornerShape(SeniorOnRadius.Small))
            .border(1.dp, SeniorOnColors.Gray200, RoundedCornerShape(SeniorOnRadius.Small)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun InquirySubmitSuccessDialog(
    onConfirmClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SeniorOnColors.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(SeniorOnRadius.Large))
                    .background(SeniorOnColors.White)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_modal_check),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = androidx.compose.ui.graphics.Color.Unspecified
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "접수가 완료됐어요",
                    style = SeniorOnTextStyles.BodyLBold,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "문의하신 내역은 설정 > 도움말·문의 > 내 문의내역에서 확인하실 수 있습니다 :)",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray500,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(SeniorOnRadius.Small))
                        .background(SeniorOnColors.Primary600)
                        .clickable(onClick = onConfirmClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "확인",
                        style = SeniorOnTextStyles.ButtonS,
                        color = SeniorOnColors.White
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OneOnOneInquiryScreenPreview() {
    SENIOR_ONTheme {
        OneOnOneInquiryScreen(onBackClick = {})
    }
}

@Composable
private fun OneOnOneInquiryHistoryContent(
    items: List<InquiryHistoryItem>,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(SeniorOnColors.White)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "아직 문의한 내용이 없어요",
                    style = SeniorOnTextStyles.BodyMBold,
                    color = SeniorOnColors.Gray800,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "시니어 ON에 궁금한 부분이 있다면 문의를 남겨주세요",
                    style = SeniorOnTextStyles.BodySMedium,
                    color = SeniorOnColors.Gray400,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    var expandedId by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items.forEach { item ->
            InquiryHistoryCard(
                item = item,
                expanded = expandedId == item.id,
                onToggle = {
                    if (item.status == InquiryAnswerStatus.Answered) {
                        expandedId = if (expandedId == item.id) null else item.id
                    }
                }
            )
        }
    }
}

@Composable
private fun InquiryHistoryCard(
    item: InquiryHistoryItem,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canExpand = item.status == InquiryAnswerStatus.Answered && !item.answer.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(SeniorOnRadius.Medium))
            .background(SeniorOnColors.Background1)
            .then(
                if (canExpand) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onToggle
                    )
                } else {
                    Modifier
                }
            )
            .padding(16.dp)
    ) {
        InquiryStatusBadge(status = item.status)

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.createdAtLabel,
            style = SeniorOnTextStyles.CaptionRegular,
            color = SeniorOnColors.Gray400
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Q",
                style = SeniorOnTextStyles.BodyMBold,
                color = SeniorOnColors.Primary600
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = item.question,
                modifier = Modifier.weight(1f),
                style = SeniorOnTextStyles.BodyMMedium,
                color = SeniorOnColors.Gray800
            )
            if (canExpand) {
                Icon(
                    painter = painterResource(
                        id = if (expanded) {
                            R.drawable.ic_sm_fold
                        } else {
                            R.drawable.ic_sm_chevron_down_2
                        }
                    ),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = SeniorOnColors.Gray300
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && canExpand,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(SeniorOnRadius.Small))
                    .background(SeniorOnColors.White)
                    .padding(12.dp)
            ) {
                Text(
                    text = item.answer.orEmpty(),
                    style = SeniorOnTextStyles.BodySRegular,
                    color = SeniorOnColors.Gray600
                )
            }
        }
    }
}

@Composable
private fun InquiryStatusBadge(
    status: InquiryAnswerStatus,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(SeniorOnRadius.Small)
    when (status) {
        InquiryAnswerStatus.Waiting -> {
            Box(
                modifier = modifier
                    .border(1.dp, SeniorOnColors.Gray200, shape)
                    .clip(shape)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "답변 대기 중",
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.Gray500
                )
            }
        }

        InquiryAnswerStatus.Answered -> {
            Box(
                modifier = modifier
                    .clip(shape)
                    .background(SeniorOnColors.Primary600)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "답변 완료",
                    style = SeniorOnTextStyles.CaptionMedium,
                    color = SeniorOnColors.White
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OneOnOneInquiryHistoryEmptyPreview() {
    SENIOR_ONTheme {
        OneOnOneInquiryHistoryContent(items = emptyList())
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun OneOnOneInquiryHistoryListPreview() {
    SENIOR_ONTheme {
        OneOnOneInquiryHistoryContent(
            items = listOf(
                InquiryHistoryItem(
                    id = "waiting",
                    status = InquiryAnswerStatus.Waiting,
                    createdAtLabel = "26.04.28 14:12",
                    question = "가족 코드를 잃어버렸어요"
                ),
                SampleAnsweredInquiry
            )
        )
    }
}
