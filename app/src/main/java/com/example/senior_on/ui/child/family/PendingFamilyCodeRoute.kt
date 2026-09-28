package com.example.senior_on.ui.child.family

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.senior_on.domain.model.server.FamilyCodeInfo
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import com.example.senior_on.ui.onboarding.familycode.FamilyShareCodeCreatedScreen
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles
import kotlinx.coroutines.CancellationException

@Composable
internal fun PendingFamilyCodeRoute(
    repository: FamilyServerRepository,
    sessionKey: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var code by remember(sessionKey) { mutableStateOf<FamilyCodeInfo?>(null) }
    var loading by remember(sessionKey) { mutableStateOf(true) }
    var error by remember(sessionKey) { mutableStateOf<String?>(null) }
    var retry by remember(sessionKey) { mutableIntStateOf(0) }
    LaunchedEffect(repository, sessionKey, retry) {
        loading = true
        error = null
        code = null
        try {
            code = repository.getPendingCode()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            error = failure.message ?: "가족 공유코드를 불러오지 못했어요."
        } finally {
            loading = false
        }
    }
    if (loading || error != null || code != null) {
        FamilyShareCodeCreatedScreen(
            onBackClick = onBackClick,
            onNextClick = {},
            modifier = modifier,
            familyShareCode = code?.code.orEmpty(),
            isLoading = loading,
            errorMessage = error,
            onRetryClick = { retry++ },
            showNextButton = false,
        )
    } else {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("관리할 시니어를 먼저 선택해 주세요.",
                style = SeniorOnTextStyles.BodyMMedium, color = SeniorOnColors.Gray500)
        }
    }
}
