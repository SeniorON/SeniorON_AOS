package com.example.senior_on.ui.child.display

import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.domain.model.display.SeniorHomeButtonType
import com.example.senior_on.domain.model.display.SeniorScreenConfiguration
import org.junit.Assert.assertEquals
import org.junit.Test

class DisplayButtonOrderTest {
    @Test
    fun defaultButtonsMatchBackendInitialOrder() {
        assertEquals(
            listOf(
                SeniorHomeButtonType.Call,
                SeniorHomeButtonType.Message,
                SeniorHomeButtonType.Camera,
                SeniorHomeButtonType.Photo,
                SeniorHomeButtonType.YouTube,
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Emergency,
                SeniorHomeButtonType.KakaoTalk,
                SeniorHomeButtonType.Naver,
            ),
            SeniorScreenConfiguration().buttons.filterNot { button ->
                button.isMusicButton() || button == SeniorHomeButtonType.Schedule
            },
        )
    }

    @Test
    fun musicAndScheduleArePlacedBeforeDynamicButtons() {
        val call = defaultButton(SeniorHomeButtonType.Call)
        val message = defaultButton(SeniorHomeButtonType.Message)
        val result = createInitialButtonOrder(
            currentButtons = listOf(
                call,
                message,
                defaultButton(SeniorHomeButtonType.ChatBuddy),
                defaultButton(SeniorHomeButtonType.Medication),
            ),
            musicButton = SeniorHomeButtonType.Melon,
            appButtons = listOf(call, message),
        )

        assertEquals(SeniorHomeButtonType.Melon, result[0].type)
        assertEquals("SCHEDULE", result[1].actionValue)
        assertEquals(listOf("PHONE", "MESSAGE"), result.drop(2).take(2).map { it.actionValue })
    }

    @Test
    fun importedPackageKeepsItsDynamicIdentityInOrder() {
        val imported = DisplayHomeButton(
            name = "유튜브",
            actionType = "APP",
            actionValue = "com.google.android.youtube",
            packageName = "com.google.android.youtube",
        )
        val result = createInitialButtonOrder(
            currentButtons = emptyList(),
            musicButton = null,
            appButtons = listOf(imported),
        )

        assertEquals("SCHEDULE", result.first().actionValue)
        assertEquals(imported, result.first { it.packageName == imported.packageName })
    }

    @Test
    fun requiredButtonsCannotBeRemovedAndEmergencyUsesEighthGridSlot() {
        val result = listOf(
            defaultButton(SeniorHomeButtonType.Call),
            defaultButton(SeniorHomeButtonType.Message),
            defaultButton(SeniorHomeButtonType.Camera),
            appButton("유튜브", "com.google.android.youtube"),
            appButton("카카오톡", "com.kakao.talk"),
            appButton("네이버", "com.nhn.android.search"),
        ).withRequiredSeniorHomeButtons()

        val grid = result.filterNot { it.isMusicButton() || it.isDefaultAction("SCHEDULE") }
        assertEquals("EMERGENCY", grid[7].actionValue)
        assertEquals(
            setOf(
                SeniorHomeButtonType.ChatBuddy,
                SeniorHomeButtonType.Medication,
                SeniorHomeButtonType.Photo,
            ),
            grid.mapNotNull(DisplayHomeButton::type)
                .filter {
                    it == SeniorHomeButtonType.ChatBuddy ||
                        it == SeniorHomeButtonType.Medication ||
                        it == SeniorHomeButtonType.Photo
                }.toSet(),
        )
    }

    @Test
    fun emergencyStaysInTheEighthGridSlotWhenOtherButtonsMove() {
        val emergency = defaultButton(SeniorHomeButtonType.Emergency)
        val regularButtons = listOf(
            defaultButton(SeniorHomeButtonType.Call),
            defaultButton(SeniorHomeButtonType.Message),
            defaultButton(SeniorHomeButtonType.Camera),
            defaultButton(SeniorHomeButtonType.ChatBuddy),
            defaultButton(SeniorHomeButtonType.Medication),
            appButton("유튜브", "com.google.android.youtube"),
            defaultButton(SeniorHomeButtonType.Photo),
            appButton("지도", "com.example.map"),
            appButton("네이버", "com.nhn.android.search"),
        )

        val result = regularButtons.withEmergencyAtFixedGridSlot(emergency)

        assertEquals("EMERGENCY", result[7].actionValue)
        assertEquals(regularButtons, result.filterNot { it.isDefaultAction("EMERGENCY") })
    }

    @Test
    fun musicAndScheduleStayAboveGridWhileEmergencyKeepsItsFixedSlot() {
        val music = defaultButton(SeniorHomeButtonType.Melon)
        val schedule = defaultButton(SeniorHomeButtonType.Schedule)
        val emergency = defaultButton(SeniorHomeButtonType.Emergency)
        val regularButtons = listOf(
            defaultButton(SeniorHomeButtonType.Call),
            defaultButton(SeniorHomeButtonType.Message),
            defaultButton(SeniorHomeButtonType.Camera),
            defaultButton(SeniorHomeButtonType.ChatBuddy),
            defaultButton(SeniorHomeButtonType.Medication),
            defaultButton(SeniorHomeButtonType.Photo),
            appButton("유튜브", "com.google.android.youtube"),
            appButton("지도", "com.example.map"),
        )

        val result = (
            regularButtons.take(3) +
                emergency +
                schedule +
                regularButtons.drop(3) +
                music
            ).withFixedButtonOrderSections()

        assertEquals(music, result[0])
        assertEquals(schedule, result[1])
        assertEquals(emergency, result.drop(2)[7])
        assertEquals(
            regularButtons,
            result.filterNot { button ->
                button.isMusicButton() ||
                    button.isDefaultAction("SCHEDULE") ||
                    button.isDefaultAction("EMERGENCY")
            },
        )
    }

    @Test
    fun scheduleStaysFeaturedWhenMusicIsNotSelected() {
        val schedule = defaultButton(SeniorHomeButtonType.Schedule)
        val call = defaultButton(SeniorHomeButtonType.Call)

        val result = listOf(call, schedule).withFixedButtonOrderSections()

        assertEquals(schedule, result.first())
        assertEquals(call, result[1])
    }

    @Test
    fun selectedButtonEditsDeleteOnlyEditableButtons() {
        val call = defaultButton(SeniorHomeButtonType.Call)
        val message = defaultButton(SeniorHomeButtonType.Message)
        val initial = listOf(
            defaultButton(SeniorHomeButtonType.Schedule),
            call,
            message,
            defaultButton(SeniorHomeButtonType.ChatBuddy),
            defaultButton(SeniorHomeButtonType.Medication),
            defaultButton(SeniorHomeButtonType.Emergency),
        )

        val result = mergeSelectedButtonEdits(
            initialButtons = initial,
            editableButtons = listOf(message),
        )

        assertEquals(
            listOf("SCHEDULE", "MESSAGE", "COMPANION", "MEDICATION", "EMERGENCY"),
            result.map(DisplayHomeButton::actionValue),
        )
    }
}

private fun defaultButton(type: SeniorHomeButtonType): DisplayHomeButton {
    val (name, action) = when (type) {
        SeniorHomeButtonType.Call -> "전화" to "PHONE"
        SeniorHomeButtonType.Message -> "메시지" to "MESSAGE"
        SeniorHomeButtonType.Camera -> "카메라" to "CAMERA"
        SeniorHomeButtonType.Schedule -> "일정" to "SCHEDULE"
        SeniorHomeButtonType.ChatBuddy -> "말벗" to "COMPANION"
        SeniorHomeButtonType.Medication -> "복약" to "MEDICATION"
        SeniorHomeButtonType.Photo -> "사진" to "PHOTO"
        SeniorHomeButtonType.Emergency -> "긴급알림" to "EMERGENCY"
        else -> type.name to type.name.uppercase()
    }
    return DisplayHomeButton(
        name = name,
        actionType = "DEFAULT",
        actionValue = action,
        type = type,
    )
}

private fun appButton(name: String, packageName: String) = DisplayHomeButton(
    name = name,
    actionType = "APP",
    actionValue = packageName,
    packageName = packageName,
)
