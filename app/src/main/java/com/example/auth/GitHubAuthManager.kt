package com.example.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class GitHubAuthManager(private val context: Context) {
    val authPreferences = AuthPreferences(context)

    companion object {
        const val CLIENT_ID = "Ov23liA6uaA4eaaRbBR0"
        const val CLIENT_SECRET = "d6c3347554baeec98c2369a4b8a1b7155d52a813"
        const val REDIRECT_URI = "pocketarcade://auth"
        private const val AUTH_URL = "https://github.com/login/oauth/authorize"
        private const val TOKEN_URL = "https://github.com/login/oauth/access_token"
        private const val USER_URL = "https://api.github.com/user"
    }

    private val httpClient = OkHttpClient.Builder().build()

    fun buildAuthIntent(): Intent {
        val uri = Uri.parse(AUTH_URL)
            .buildUpon()
            .appendQueryParameter("client_id", CLIENT_ID)
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", "read:user user:email")
            .build()
        return Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    suspend fun exchangeCodeForToken(code: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val formBody = FormBody.Builder()
                .add("client_id", CLIENT_ID)
                .add("client_secret", CLIENT_SECRET)
                .add("code", code)
                .add("redirect_uri", REDIRECT_URI)
                .build()

            val request = Request.Builder()
                .url(TOKEN_URL)
                .header("Accept", "application/json")
                .post(formBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
            }

            val json = JSONObject(body)
            if (json.has("error")) {
                val errorMsg = json.optString("error_description", json.optString("error"))
                return@withContext Result.failure(Exception(errorMsg))
            }

            val accessToken = json.optString("access_token")
            if (accessToken.isNullOrEmpty()) {
                return@withContext Result.failure(Exception("Access token not found in response"))
            }

            authPreferences.accessToken = accessToken
            Result.success(accessToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchUserProfile(token: String): Result<GitHubUser> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(USER_URL)
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "PocketArcade-AndroidApp")
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string() ?: ""
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: $body"))
            }

            val json = JSONObject(body)
            val user = GitHubUser(
                id = json.getLong("id"),
                login = json.getString("login"),
                name = if (json.has("name") && !json.isNull("name")) json.getString("name") else null,
                avatarUrl = if (json.has("avatar_url") && !json.isNull("avatar_url")) json.getString("avatar_url") else null,
                bio = if (json.has("bio") && !json.isNull("bio")) json.getString("bio") else null,
                htmlUrl = if (json.has("html_url") && !json.isNull("html_url")) json.getString("html_url") else null
            )

            authPreferences.currentUser = user
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        authPreferences.clear()
    }
}
