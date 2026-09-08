package com.example.auth

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class AuthPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("arcade_auth_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val adapter = moshi.adapter(GitHubUser::class.java)

    companion object {
        private const val KEY_ACCESS_TOKEN = "github_access_token"
        private const val KEY_USER_JSON = "github_user_json"
    }

    var accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)
        set(value) {
            prefs.edit().putString(KEY_ACCESS_TOKEN, value).apply()
        }

    var currentUser: GitHubUser?
        get() {
            val json = prefs.getString(KEY_USER_JSON, null) ?: return null
            return try {
                adapter.fromJson(json)
            } catch (e: Exception) {
                null
            }
        }
        set(value) {
            if (value != null) {
                val json = adapter.toJson(value)
                prefs.edit().putString(KEY_USER_JSON, json).apply()
            } else {
                prefs.edit().remove(KEY_USER_JSON).apply()
            }
        }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
