package com.example.vetcare_grupo11.model

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val successMessage: String? = null
)