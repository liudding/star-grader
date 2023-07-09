package com.nxlinkstar.stargrader.utils

import com.nxlinkstar.stargrader.data.model.LoggedInUser
import com.nxlinkstar.stargrader.data.model.Textbook

object Api {

    private const val TAG = "API"

    suspend fun login(username: String, password: String): String? {
        val url = "https://ps.edu.purvar.com/cjn-sso/api/v3/sso/security/getAccessToken"
        return HttpUtils.post(url, Utils.toMap("loginId", username, "password", password, "roleTypeUuid", "ROLE_TYPE_001"))
    }

    suspend fun getTextbook(subject: String) {
        val accessToken = ""
        val schoolId = ""
        val url = "https://ps.edu.purvar.com/cjn-resource/api/v3/resource/book/getBookListByTeachKemuShort?teachKemuShort=${subject}&access_token=${accessToken}&schUuid=${schoolId}"

        HttpUtils.get(url, Textbook, true)
    }
}