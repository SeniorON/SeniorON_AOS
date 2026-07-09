package com.example.senior_on.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.senior_on.R
import com.example.senior_on.ui.theme.SENIOR_ONTheme
import com.example.senior_on.ui.theme.SeniorOnColors
import com.example.senior_on.ui.theme.SeniorOnTextStyles

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SplashLogo()

            Text(
                text = buildAnnotatedString {
                    append("부모님과 자녀를 잇는 ")
                    withStyle(SpanStyle(color = SeniorOnColors.Primary600)) {
                        append("디지털 케어 서비스")
                    }
                },
                modifier = Modifier.padding(top = 12.dp),
                style = SeniorOnTextStyles.BodySRegular,
                color = SeniorOnColors.Gray800
            )

            Image(
                painter = painterResource(id = R.drawable.ic_splash_divider),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 6.dp)
                    .size(width = 78.dp, height = 40.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.img_splash_seniors),
                contentDescription = null,
                modifier = Modifier
                    .padding(top = 0.dp)
                    .offset(y = (-5).dp)
                    .size(width = 175.dp, height = 155.dp)
            )
        }
    }
}

@Composable
private fun SplashLogo() {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_splash_logo_text),
            contentDescription = null,
            modifier = Modifier.size(width = 110.dp, height = 34.dp)
        )
        Image(
            painter = painterResource(id = R.drawable.ic_splash_on),
            contentDescription = null,
            modifier = Modifier.size(width = 60.dp, height = 44.dp)
        )
    }
}

@Preview(
    name = "Splash",
    showBackground = true,
    widthDp = 360,
    heightDp = 800
)
@Composable
private fun SplashScreenPreview() {
    SENIOR_ONTheme {
        SplashScreen()
    }
}
