package com.example.senior_on.ui.consent

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnRadius
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
internal fun SeniorConsentSummary(label: String, title: String, description: String, @DrawableRes icon: Int) {
    Column(
        Modifier.fillMaxWidth().background(SeniorOnColors.Primary200, RoundedCornerShape(SeniorOnRadius.XLarge)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Image(painterResource(icon), null, Modifier.size(40.dp).background(SeniorOnColors.White, CircleShape).padding(7.dp))
            Text(label, style = SeniorOnTextStyles.BodyMSemiBold, color = SeniorOnColors.Primary700)
        }
        Text(title, style = SeniorOnTextStyles.HeadingL, color = SeniorOnColors.Gray800)
        Text(description, style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray700)
    }
}

@Composable
internal fun SeniorConsentTermsRow(onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(SeniorOnRadius.Medium)).background(SeniorOnColors.White)
            .clickable(role = Role.Button, onClick = onClick).heightIn(min = 60.dp).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("건강정보 수집·이용 약관 전문 보기", Modifier.weight(1f), style = SeniorOnTextStyles.HeadingXXS, color = SeniorOnColors.Gray800)
        Icon(painterResource(R.drawable.ic_sm_arrow_right), null, Modifier.size(24.dp), tint = SeniorOnColors.Gray600)
    }
}
