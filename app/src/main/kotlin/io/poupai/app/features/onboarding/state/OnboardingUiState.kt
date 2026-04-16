package io.poupai.app.features.onboarding.state

data class OnboardingUiState(
    val currentPage: Int = 0,
    val totalPages: Int = 3,
) {
    val isLastPage: Boolean get() = currentPage == totalPages - 1
}
