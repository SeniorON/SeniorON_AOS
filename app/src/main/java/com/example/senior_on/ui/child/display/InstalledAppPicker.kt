package com.example.senior_on.ui.child.display

import android.content.Context
import android.content.Intent
import com.example.senior_on.domain.model.display.DisplayHomeButton
import com.example.senior_on.ui.common.homebutton.findMatchingDefaultButton

internal data class PickedInstalledApp(
    val packageName: String,
    val label: String?,
)

private val AndroidPackageNamePattern = Regex(
    pattern = "^[A-Za-z][A-Za-z0-9_]*(?:\\.[A-Za-z0-9_]+)+$",
)

internal fun createInstalledAppPickerIntent(): Intent {
    val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
    return Intent(Intent.ACTION_PICK_ACTIVITY).apply {
        putExtra(Intent.EXTRA_INTENT, launcherIntent)
        putExtra(Intent.EXTRA_TITLE, "추가할 앱 선택")
    }
}

internal fun readPickedInstalledApp(
    context: Context,
    resultIntent: Intent?,
): PickedInstalledApp? {
    val selectedIntent = resultIntent ?: return null
    val packageName = selectedIntent.component?.packageName
        ?: selectedIntent.`package`
        ?: selectedIntent.resolveActivity(context.packageManager)?.packageName
        ?: return null
    val packageManager = context.packageManager
    val activityLabel = runCatching {
        selectedIntent.component
            ?.let { packageManager.getActivityInfo(it, 0) }
            ?.loadLabel(packageManager)
            ?.toString()
    }.getOrNull()
    val applicationLabel = runCatching {
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        packageManager.getApplicationLabel(applicationInfo).toString()
    }.getOrNull()
    val label = selectInstalledAppDisplayName(
        packageName = packageName,
        activityLabel,
        applicationLabel,
    )

    return PickedInstalledApp(
        packageName = packageName,
        label = label,
    )
}

internal fun PickedInstalledApp.toDisplayHomeButton(
    context: Context,
    defaultButtons: List<DisplayHomeButton>,
    buttonName: String? = label,
): DisplayHomeButton? = findMatchingDefaultButton(
    context = context,
    packageName = packageName,
    defaultButtons = defaultButtons,
) ?: buttonName
    ?.trim()
    ?.takeIf(String::isNotEmpty)
    ?.let { name ->
        DisplayHomeButton(
            name = name,
            actionType = "APP",
            actionValue = packageName,
            packageName = packageName,
        )
    }

internal fun selectInstalledAppDisplayName(
    packageName: String,
    vararg candidates: String?,
): String? = candidates
    .asSequence()
    .mapNotNull { candidate -> candidate?.trim()?.takeIf(String::isNotEmpty) }
    .firstOrNull { candidate ->
        !candidate.equals(packageName, ignoreCase = true) &&
            !AndroidPackageNamePattern.matches(candidate)
    }
