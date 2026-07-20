@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.example.senior_on.ui.family

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

private val SelectedPhotoMaxWidth = 288.dp
private const val SelectedPhotoAspectRatio = 8f / 9f

@Composable
fun FamilyPhotoShareScreen(
    photoUri: String,
    message: String,
    onMessageChange: (String) -> Unit,
    isTooltipVisible: Boolean,
    onUserInteraction: () -> Unit,
    onBackClick: () -> Unit,
    onReselectClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier,
    isUploading: Boolean = false,
    uploadErrorMessage: String? = null,
    selectedPhoto: (@Composable BoxScope.() -> Unit)? = null
) {
    val scrollState = rememberScrollState()
    val messageBringIntoViewRequester = remember { BringIntoViewRequester() }
    val density = LocalDensity.current
    val isKeyboardVisible = WindowInsets.ime.getBottom(density) > 0
    var isMessageFocused by remember { mutableStateOf(false) }

    LaunchedEffect(isKeyboardVisible, isMessageFocused) {
        if (isKeyboardVisible && isMessageFocused) {
            delay(200L)
            messageBringIntoViewRequester.bringIntoView()
        } else if (scrollState.value > 0) {
            scrollState.animateScrollTo(0)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SeniorOnColors.White)
            .pointerInput(onUserInteraction) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        onUserInteraction()
                    }
                }
            }
            .statusBarsPadding()
    ) {
        FamilyBackTopAppBar(
            title = "사진 공유",
            onBackClick = onBackClick,
            centerTitle = false,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(scrollState)
                .imePadding()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            FamilySelectedPhotoPreview(
                photoUri = photoUri,
                selectedPhoto = selectedPhoto
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(2.dp)
                    .background(SeniorOnColors.Background4)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f)
            ) {
                FamilyPhotoMessageInput(
                    message = message,
                    onMessageChange = onMessageChange,
                    bringIntoViewRequester = messageBringIntoViewRequester,
                    onFocusChange = { isMessageFocused = it }
                )

                FamilyPhotoMessageTooltipVisibility(
                    visible = isTooltipVisible,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(y = (-74).dp)
                        .zIndex(2f),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        AnimatedVisibility(
            visible = !isKeyboardVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            FamilyPhotoShareActions(
                onReselectClick = onReselectClick,
                onShareClick = onShareClick,
                isUploading = isUploading,
                errorMessage = uploadErrorMessage,
            )
        }
    }
}

@Composable
private fun FamilySelectedPhotoPreview(
    photoUri: String,
    selectedPhoto: (@Composable BoxScope.() -> Unit)?
) {
    val outerShape = RoundedCornerShape(SeniorOnRadius.Large)
    val innerShape = RoundedCornerShape(10.dp)

    Box(
        modifier = Modifier
            .widthIn(max = SelectedPhotoMaxWidth)
            .fillMaxWidth()
            .aspectRatio(SelectedPhotoAspectRatio)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .shadow(4.dp, outerShape)
                .clip(outerShape)
                .background(SeniorOnColors.White)
                .padding(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(innerShape)
                    .background(SeniorOnColors.Background4)
            ) {
                if (selectedPhoto == null) {
                    FamilySelectedPhotoUriImage(photoUri = photoUri)
                } else {
                    selectedPhoto()
                }
            }
        }

    }
}

@Composable
private fun FamilyPhotoMessageTooltipVisibility(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        FamilyPhotoMessageTooltip()
    }
}

@Composable
private fun FamilyPhotoMessageTooltip() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .width(176.dp)
                .height(54.dp)
                .background(
                    color = SeniorOnColors.Primary600,
                    shape = RoundedCornerShape(SeniorOnRadius.Small)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "보여드리고 싶은 사진과 함께\n한마디를 전해보세요.",
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.White,
                textAlign = TextAlign.Center,
            )
        }

        Canvas(
            modifier = Modifier
                .width(18.dp)
                .height(8.dp),
        ) {
            drawPath(
                path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                    close()
                },
                color = SeniorOnColors.Primary600,
            )
        }
    }
}

@Composable
private fun BoxScope.FamilySelectedPhotoUriImage(photoUri: String) {
    val context = LocalContext.current
    val imageBitmap by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = photoUri
    ) {
        value = withContext(Dispatchers.IO) {
            decodeSelectedPhoto(context, Uri.parse(photoUri))?.asImageBitmap()
        }
    }

    val bitmap = imageBitmap
    if (bitmap == null) {
        Icon(
            painter = painterResource(id = R.drawable.ic_big_photo),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .size(34.dp),
            tint = SeniorOnColors.Gray400
        )
    } else {
        Image(
            bitmap = bitmap,
            contentDescription = "공유할 사진",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
private fun FamilyPhotoMessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    bringIntoViewRequester: BringIntoViewRequester,
    onFocusChange: (Boolean) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "💌",
                style = SeniorOnTextStyles.BodyMRegular,
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "한 마디 남기기",
                style = SeniorOnTextStyles.HeadingS,
                color = SeniorOnColors.Gray800
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "선택",
                modifier = Modifier
                    .clip(RoundedCornerShape(SeniorOnRadius.XLarge))
                    .background(SeniorOnColors.Gray100)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                style = SeniorOnTextStyles.CaptionMedium,
                color = SeniorOnColors.Gray500
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        BasicTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .bringIntoViewRequester(bringIntoViewRequester)
                .onFocusChanged { onFocusChange(it.isFocused) },
            textStyle = SeniorOnTextStyles.BodyMMedium.copy(
                color = SeniorOnColors.Gray800
            ),
            cursorBrush = SolidColor(SeniorOnColors.Primary600),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(SeniorOnRadius.Small))
                        .background(SeniorOnColors.Background3)
                        .padding(horizontal = 12.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (message.isEmpty()) {
                        Text(
                            text = "30자 이내 입력",
                            style = SeniorOnTextStyles.BodyMMedium,
                            color = SeniorOnColors.Gray300
                        )
                    }
                    innerTextField()
                }
            }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "${message.length}/30",
            modifier = Modifier.align(Alignment.End),
            style = SeniorOnTextStyles.CaptionMedium,
            color = SeniorOnColors.Gray500
        )
    }
}

@Composable
private fun FamilyPhotoShareActions(
    onReselectClick: () -> Unit,
    onShareClick: () -> Unit,
    isUploading: Boolean,
    errorMessage: String?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SeniorOnColors.White)
            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp),
    ) {
        errorMessage?.let {
            Text(
                text = it,
                style = SeniorOnTextStyles.CaptionRegular,
                color = SeniorOnColors.Red300,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FamilyTextActionButton(
                text = "다시 선택",
                backgroundColor = SeniorOnColors.White,
                contentColor = SeniorOnColors.Gray700,
                borderColor = SeniorOnColors.Gray200,
                enabled = !isUploading,
                onClick = onReselectClick,
                modifier = Modifier.weight(1f)
            )
            FamilyTextActionButton(
                text = if (isUploading) "올리는 중..." else "공유하기",
                backgroundColor = SeniorOnColors.Primary600,
                contentColor = SeniorOnColors.White,
                borderColor = null,
                enabled = !isUploading,
                onClick = onShareClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun decodeSelectedPhoto(
    context: android.content.Context,
    uri: Uri
): Bitmap? = runCatching {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(context.contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
            val sourceWidth = info.size.width
            val sourceHeight = info.size.height
            val scale = minOf(
                1f,
                1_080f / sourceWidth,
                1_200f / sourceHeight
            )
            decoder.setTargetSize(
                max(1, (sourceWidth * scale).roundToInt()),
                max(1, (sourceHeight * scale).roundToInt())
            )
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        }
    } else {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, bounds)
        }
        var sampleSize = 1
        while (
            bounds.outWidth / sampleSize > 1_080 ||
            bounds.outHeight / sampleSize > 1_200
        ) {
            sampleSize *= 2
        }
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }
}.getOrNull()

@Preview(
    name = "Family Photo Share",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun FamilyPhotoShareScreenPreview() {
    SENIOR_ONTheme {
        FamilyPhotoShareScreen(
            photoUri = "preview",
            message = "나 지금 보드게임 카페야",
            onMessageChange = {},
            isTooltipVisible = true,
            onUserInteraction = {},
            onBackClick = {},
            onReselectClick = {},
            onShareClick = {},
            selectedPhoto = {
                Image(
                    painter = painterResource(id = R.drawable.img_mock_family_primary),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            }
        )
    }
}
