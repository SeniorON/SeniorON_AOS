package com.example.senior_on.ui.onboarding.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.senior_on.data.local.ParentConnectionGate
import com.example.senior_on.data.repository.impl.ParentConnectionDestination
import com.example.senior_on.data.repository.impl.ParentReconnectionRepository
import com.example.senior_on.domain.repository.auth.AuthRepository
import com.example.senior_on.domain.repository.auth.SessionRepository
import com.example.senior_on.domain.repository.device.DeviceRegistrationRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ParentReconnectionState(val busy: Boolean = false, val error: String? = null)

class ParentReconnectionViewModel(
    private val repository: ParentReconnectionRepository,
    private val auth: AuthRepository,
    private val sessions: SessionRepository,
    private val devices: DeviceRegistrationRepository,
) : ViewModel() {
    private val mutableState = MutableStateFlow(ParentReconnectionState())
    val state = mutableState.asStateFlow()

    fun check(onResult: (ParentConnectionDestination) -> Unit, onFailure: (String) -> Unit) {
        run(onFailure) {
            val version = ParentConnectionGate.hold()
            val destination = repository.destination()
            if (destination == ParentConnectionDestination.Home && !ParentConnectionGate.approve(version)) return@run
            onResult(destination)
        }
    }

    fun confirm(onConnected: () -> Unit) {
        run {
            val version = ParentConnectionGate.hold()
            repository.reconnect()
            check(repository.destination() == ParentConnectionDestination.Home) { "재연결 상태를 확인하지 못했어요. 다시 시도해 주세요." }
            if (ParentConnectionGate.approve(version)) onConnected()
        }
    }

    fun cancel(onLoggedOut: () -> Unit) {
        run {
            ParentConnectionGate.hold()
            try {
                auth.logout(devices.getDeviceRegistration().deviceIdentifier)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // The rejected login must not survive a failed server logout.
            } finally {
                sessions.clearSession()
            }
            onLoggedOut()
        }
    }

    private fun run(onFailure: (String) -> Unit = {}, action: suspend () -> Unit) {
        if (mutableState.value.busy) return
        mutableState.value = ParentReconnectionState(busy = true)
        viewModelScope.launch {
            try {
                action()
                mutableState.value = ParentReconnectionState()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                val message = error.message ?: "연결 상태를 확인하지 못했어요. 다시 시도해 주세요."
                mutableState.value = ParentReconnectionState(error = message)
                onFailure(message)
            }
        }
    }
}
