package com.nxlinkstar.stargrader.utils

import com.nxlinkstar.stargrader.data.model.LoggedInUser

object Api {

    private const val TAG = "API"

    suspend fun login(username: String, password: String): String? {
        val url = "https://ps.edu.purvar.com/cjn-sso/api/v3/sso/security/getAccessToken"
        return HttpUtils.post(url, Utils.toMap("loginId", username, "password", password, "roleTypeUuid", "ROLE_TYPE_001"))
    }
}