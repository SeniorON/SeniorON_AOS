package com.example.senior_on.data.remote.dto

import com.google.gson.TypeAdapter
import com.google.gson.annotations.JsonAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

@JsonAdapter(ButtonRequestJsonAdapter::class)
data class ButtonRequest(
    val buttonOrder: Int,
    val buttonName: String,
    val actionType: String,
    val actionValue: String,
    val packageName: String?,
)

class ButtonRequestJsonAdapter : TypeAdapter<ButtonRequest>() {
    override fun write(output: JsonWriter, source: ButtonRequest?) {
        if (source == null) {
            output.nullValue()
            return
        }

        output.beginObject()
        output.name("buttonOrder").value(source.buttonOrder)
        output.name("buttonName").value(source.buttonName)
        output.name("actionType").value(source.actionType)
        output.name("actionValue").value(source.actionValue)

        val previousSerializeNulls = output.serializeNulls
        output.serializeNulls = true
        output.name("packageName")
        source.packageName?.let(output::value) ?: output.nullValue()
        output.serializeNulls = previousSerializeNulls
        output.endObject()
    }

    override fun read(input: JsonReader): ButtonRequest =
        error("ButtonRequest is request-only and cannot be deserialized.")
}

data class HomeButtonSaveRequest(
    val musicApp: String?,
    val buttons: List<ButtonRequest>,
)
data class HomeButtonCreateRequest(val optionId: Long)
data class HomeButtonUpdateItemRequest(
    val button_id: Long,
    val button_order: Int,
    val button_name: String?,
    val icon: String?,
)
data class HomeButtonUpdateRequest(val buttons: List<HomeButtonUpdateItemRequest>)
data class HomeFontSizeUpdateRequest(val font_size: String)
data class SeniorProfileUpdateRequest(
    val name: String,
    val relation: String,
    val customRelation: String?,
    val birth: String,
    val phoneNumber: String,
    val address: String?,
    val detailAddress: String?
)
data class ConnectionResponse(val connected: Boolean?, val battery: Int?, val device_name: String?)
data class HomeButtonResponse(
    val icon: String?, val button_id: Long?, val button_order: Int?,
    val button_name: String?, val action_type: String?, val action_value: String?,
    val package_name: String? = null,
)
data class MusicCardResponse(
    val enabled: Boolean?, val icon: String?, val music_app: String?,
    val app_name: String?, val action_type: String?, val action_value: String?
)
data class TodayScheduleResponse(
    val title: String?, val description: String?, val schedule_count: Int?,
    val display_type: String?, val schedule_id: Long?, val scheduled_time: String?
)
data class SeniorProfileResponse(
    val senior_id: Long?, val name: String?, val relation: String?,
    val birth: String?, val age: Int?, val address: String?, val phone: String?
)
data class HomeResponse(
    val connection: ConnectionResponse?, val buttons: List<HomeButtonResponse>?,
    val user_name: String?, val senior_profile: SeniorProfileResponse?,
    val font_size: String?, val music_card: MusicCardResponse?,
    val today_schedule: TodayScheduleResponse?
)
data class SeniorHomeResponse(
    val buttons: List<HomeButtonResponse>?, val font_size: String?,
    val music_card: MusicCardResponse?, val today_schedule: TodayScheduleResponse?
)
data class WeatherResponse(
    val temperature: Int?, val weatherStatus: String?,
    val weatherText: String?, val observedAt: String?
)
data class HomeButtonCreateResponse(
    val buttonId: Long?, val buttonOrder: Int?, val buttonName: String?, val icon: String?
)
data class ButtonOptionResponse(
    val icon: String?, val option_id: Long?, val button_name: String?,
    val action_type: String?, val action_value: String?
)
data class SeniorProfileUpdateResponse(
    val seniorId: Long?, val name: String?, val relation: String?,
    val customRelation: String?, val birth: String?, val phoneNumber: String?,
    val address: String?, val detailAddress: String?
)
data class TodayHospitalListResponse(
    val hospitalId: Long?, val hospitalName: String?, val department: String?,
    val scheduleDate: String?, val scheduleTime: String?, val reminderType: String?,
    val registeredBy: String?
)
data class DeviceDetailResponse(
    val deviceName: String?, val connected: Boolean?, val connectionStatus: String?,
    val batteryLevel: Int?, val networkConnected: Boolean?,
    val lastConnectedAt: String?, val lastLocationUpdatedAt: String?
)
