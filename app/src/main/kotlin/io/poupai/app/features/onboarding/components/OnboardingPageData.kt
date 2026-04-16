package io.poupai.app.features.onboarding.components

import androidx.annotation.DrawableRes

data class OnboardingPageData(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
)