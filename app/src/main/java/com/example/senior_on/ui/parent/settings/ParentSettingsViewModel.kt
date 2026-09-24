package com.example.senior_on.ui.parent.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senior_on.data.remote.api.SeniorPermissionSettings
import com.example.senior_on.data.repository.impl.ParentSettingsRepository
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.server.UserSettingsRepository
import com.example.senior_on.domain.repository.server.FamilyServerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ParentSettingsState(
    val profile: ParentSettingsProfile? = null,
    val permissions: SeniorPermissionSettings? = null,
    val busy: Boolean = false,
    val error: String? = null,
    val completed: String? = null,
    val shareCode: String? = null,
)

class ParentSettingsViewModel(
    private val accounts: UserSettingsRepository,
    private val auth: AuthRepository,
    private val settings: ParentSettingsRepository,
    private val family: FamilyServerRepository,
    private val photoPreparer: com.example.senior_on.data.local.FamilyPhotoUploadPreparer? = null,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ParentSettingsState())
    val state = mutableState.asStateFlow()

    init { loadAccount() }

    fun loadAccount() = perform {
        val account = accounts.getSettings()
        mutableState.update { it.copy(profile = ParentSettingsProfile(account.name, account.email,
            account.profileImageUrl.takeUnless { account.isDefaultProfileImage })) }
    }

    fun updatePhoto(uri: String) = perform {
        val prepared = checkNotNull(photoPreparer).prepareProfileImage(uri)
        val url = try {
            accounts.updateProfileImage(prepared)?.takeIf(String::isNotBlank)
                ?: accounts.getProfileImageUrl()?.takeIf(String::isNotBlank)
                ?: error("프로필 사진 주소를 확인하지 못했어요.")
        } finally { prepared.file.delete() }
        mutableState.update { it.copy(profile = it.profile?.copy(imageUrl = url, imageRevision = System.currentTimeMillis())) }
    }

    fun resetPhoto() = perform {
        val image = accounts.resetProfileImage()
        check(image.isDefaultProfileImage) { "기본 사진 적용을 확인하지 못했어요." }
        mutableState.update { it.copy(profile = it.profile?.copy(imageUrl = null, imageRevision = System.currentTimeMillis())) }
    }

    fun loadPermissions() = perform {
        val seniorId = auth.getOnboardingStatus().seniorId
            ?: error("연결된 시니어 정보를 확인하지 못했어요. 다시 시도해 주세요.")
        val permissions = settings.getPermissions(seniorId)
        mutableState.update { it.copy(permissions = permissions) }
    }

    fun loadShareCode() = perform {
        val seniorId = auth.getOnboardingStatus().seniorId
            ?: error("연결된 시니어 정보를 확인하지 못했어요.")
        val code = family.getCode(seniorId).code
        check(code.isNotBlank()) { "공유 코드를 확인하지 못했어요." }
        mutableState.update { it.copy(shareCode = code) }
    }

    fun saveName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || mutableState.value.profile == null) return
        perform {
            val saved = accounts.updateName(trimmed).ifBlank { trimmed }
            mutableState.update { it.copy(profile = it.profile?.copy(name = saved), completed = "account") }
        }
    }

    fun savePassword(current: String, new: String) = perform {
        check(accounts.changePassword(current, new, new)) { "비밀번호 변경을 완료하지 못했어요." }
        mutableState.update { it.copy(completed = "account") }
    }

    fun savePermissions(location: Boolean? = null, inactivity: Boolean? = null) {
        if (mutableState.value.permissions == null) return
        perform {
            val saved = settings.updatePermissions(location, inactivity)
            mutableState.update { it.copy(permissions = saved) }
        }
    }

    fun disconnect() = perform {
        settings.disconnect()
        mutableState.update { it.copy(completed = "disconnect") }
    }

    fun consumeError() { mutableState.update { it.copy(error = null) } }
    fun consumeCompleted() { mutableState.update { it.copy(completed = null) } }

    private fun perform(action: suspend () -> Unit) {
        if (mutableState.value.busy) return
        mutableState.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Exception) {
                mutableState.update { it.copy(error = failure.message ?: "요청에 실패했어요. 다시 시도해 주세요.") }
            } finally {
                mutableState.update { it.copy(busy = false) }
            }
        }
    }
}
