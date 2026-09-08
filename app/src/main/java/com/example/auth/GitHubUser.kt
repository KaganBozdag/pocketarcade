package com.example.auth

data class GitHubUser(
    val id: Long,
    val login: String,
    val name: String?,
    val avatarUrl: String?,
    val bio: String?,
    val htmlUrl: String?
)

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class Authenticated(val user: GitHubUser) : AuthState()
    data class Error(val message: String) : AuthState()
}
