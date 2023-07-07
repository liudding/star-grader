package com.nxlinkstar.stargrader.data

import com.nxlinkstar.stargrader.data.model.LoggedInUser
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException


/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    private val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    val client = OkHttpClient()

    @Throws(IOException::class)
    fun post(url: String, json: String): String {
        val request: Request = Request.Builder()
            .url(url)
            .post(json.toRequestBody())
            .build()
        client.newCall(request).execute().use { response -> return response.body!!.string() }
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication

            val url = "https://ps.edu.purvar.com/cjn-sso/api/v3/sso/security/getAccessToken"
            val params = mapOf("loginId" to username, "password" to password, "roleTypeUuid" to "ROLE_TYPE_001")

            val result = post(url, params.toString())

            val json = JSONTokener(result).nextValue() as JSONObject
            val data = json.getJSONObject("operativeDto")
            val school = json.getJSONObject("school")


            val user = LoggedInUser(
                json.getString("accessToken"),
                data.getString("userUuid"),
                data.getString("fullName"),
                data.getString("schUuid"),
                school.getString("schCode"),
                school.getString("schName"),
                school.getString("schShortName"))
            return Result.Success(user)
        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}