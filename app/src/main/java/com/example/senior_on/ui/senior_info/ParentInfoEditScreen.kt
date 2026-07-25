package com.example.senior_on.ui.senior_info

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.senior_on.data.repository.MockParentInfoFixtures
import com.example.senior_on.domain.model.ParentInfo
import com.example.senior_on.ui.theme.SENIOR_ONTheme

@Composable
fun ParentInfoEditScreen(
    parentInfo: ParentInfo?,
    modifier: Modifier = Modifier,
    selectedAddress: String = parentInfo?.address.orEmpty(),
    selectedAddressLatitude: Double? = parentInfo?.addressLatitude,
    selectedAddressLongitude: Double? = parentInfo?.addressLongitude,
    onBackClick: () -> Unit = {},
    onSearchAddressClick: () -> Unit = {},
    onSaveClick: (ParentInfoInputState) -> Unit = {},
) {
    ParentInfoInputScreen(
        modifier = modifier,
        initialState = parentInfo?.toInputState(),
        mode = ParentInfoScreenMode.Edit,
        selectedAddress = selectedAddress,
        selectedAddressLatitude = selectedAddressLatitude,
        selectedAddressLongitude = selectedAddressLongitude,
        onBackClick = onBackClick,
        onSearchAddressClick = onSearchAddressClick,
        onSaveClick = onSaveClick,
    )
}

@Preview(
    name = "Parent information edit",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
)
@Composable
private fun ParentInfoEditScreenPreview() {
    SENIOR_ONTheme {
        ParentInfoEditScreen(parentInfo = MockParentInfoFixtures.mother)
    }
}
