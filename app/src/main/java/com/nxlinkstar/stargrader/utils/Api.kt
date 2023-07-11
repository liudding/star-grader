package com.nxlinkstar.stargrader.utils

import com.nxlinkstar.stargrader.StarGraderApplication
import com.nxlinkstar.stargrader.data.UserDataStore.ACCESS_TOKEN_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.PASSWORD_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.SCHOOL_ID_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.USERNAME_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.dataStore
import com.nxlinkstar.stargrader.data.model.LoggedInUser
import com.nxlinkstar.stargrader.data.model.ScanResult
import com.nxlinkstar.stargrader.data.model.ScanTemplate
import com.nxlinkstar.stargrader.data.model.Textbook
import com.nxlinkstar.stargrader.data.model.Workbook
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import java.io.File

object Api {

    private const val TAG = "API"

    suspend fun login(username: String, password: String): String? {
        val url = "https://ps.edu.purvar.com/cjn-sso/api/v3/sso/security/getAccessToken"
        return HttpUtils.post(url, Utils.toMap("loginId", username, "password", password, "roleTypeUuid", "ROLE_TYPE_001"))
    }


    data class TextbookData(val statusCode: Int, val message: String, val bookList: List<Textbook>) {

    }

    suspend fun getTextbooks(subject: String): List<Textbook> {
        val ds = StarGraderApplication.context.dataStore.data.first()
        val accessToken = ds[ACCESS_TOKEN_KEY]
        val schoolId = ds[SCHOOL_ID_KEY]
        val url = "https://ps.edu.purvar.com/cjn-resource/api/v3/resource/book/getBookListByTeachKemuShort?teachKemuShort=${subject}&access_token=${accessToken}&schUuid=${schoolId}"


        HttpUtils.get(url, TextbookData::class.java, true).let {
            return it?.bookList ?: emptyList()
        }
    }

    data class WorkbookData(val statusCode: Int, val message: String, val listHwTrainingCaseDto: List<Workbook>) {

    }
    suspend fun getWorkbooks(subject: String, textbook: String): List<Workbook> {
        val ds = StarGraderApplication.context.dataStore.data.first()
        val accessToken = ds[ACCESS_TOKEN_KEY]
        val schoolId = ds[SCHOOL_ID_KEY]
        val url = "https://ps.edu.purvar.com/cjn-ws/api/v3/ws/hw/arrangeWork/getBathTngCaseList?disStartIndex=0&access_token=${accessToken}&schUuid=${schoolId}&ansCardStyle=3&teachKemuShort=${subject}&bookUuid=${textbook}&disDataLength=100"


        HttpUtils.get(url, WorkbookData::class.java, true).let {
            return it?.listHwTrainingCaseDto ?: emptyList()
        }
    }


    suspend fun getTemplate(workbook: String): ScanTemplate? {
        val ds = StarGraderApplication.context.dataStore.data.first()
        val accessToken = ds[ACCESS_TOKEN_KEY]
        val url = "https://ps.edu.purvar.com/cjn-ws/api/v3/ws/answercard/getAnswercardInfoByTngCaseUuid?access_token=${accessToken}&tngCaseUuid=${workbook}"

        return HttpUtils.get(url, ScanTemplate::class.java, true)
    }


    suspend fun analyze(workbook: String, file: File): ScanResult? {
        val ds = StarGraderApplication.context.dataStore.data.first()
        val username = ds[USERNAME_KEY]
        val password = ds[PASSWORD_KEY]
        val url = "https://edu-rec.internal.purvar.com:7443/api/analysispage"


         return HttpUtils.postWithFile(url, Utils.toMap(
            "isDebug", "1",
            "UserName", username!!,
            "PassWord", password!!,
            "RoleType", "ROLE_TYPE_001",
            "ip", "",
            "LoginUrl", "https://ps.edu.purvar.com/cjn-sso/api/v3/sso/security/getAccessToken",
            "GetTemplateInfoUrl", "http://172.29.231.77/cjn-ws/api/v3/ws/answercard/getAnswerPaperInfo",
            "GetTemplateByTngCaseUuidInfoUrl", "https://ps.edu.purvar.com/cjn-ws/api/v3/ws/answercard/getAnswercardInfoByTngCaseUuid",
            "TngcaseUuid", workbook  // 练案ID
        ), file, ScanResult::class.java)?.result


    }
}