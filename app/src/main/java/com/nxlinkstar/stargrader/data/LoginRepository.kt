package com.nxlinkstar.stargrader.data

import android.util.Log
import androidx.datastore.dataStore
import com.nxlinkstar.stargrader.StarGraderApplication
import com.nxlinkstar.stargrader.data.UserDataStore.ACCESS_TOKEN_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.SCHOOL_CODE_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.SCHOOL_ID_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.SCHOOL_NAME_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.SCHOOL_SHORT_NAME_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.USER_ID_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.USER_NAME_KEY
import com.nxlinkstar.stargrader.data.UserDataStore.clearUser
import com.nxlinkstar.stargrader.data.UserDataStore.dataStore
import com.nxlinkstar.stargrader.data.UserDataStore.storeUser
import com.nxlinkstar.stargrader.data.UserDataStore.userIdFlow
import com.nxlinkstar.stargrader.data.model.LoggedInUser
import com.nxlinkstar.stargrader.utils.Api
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */


@OptIn(DelicateCoroutinesApi::class)
class LoginRepository() {


    private val dataSource: LoginDataSource = LoginDataSource()

    companion object {


        lateinit var instance: LoginRepository

        fun getRepo(): LoginRepository {
            return instance
        }
    }


    // in-memory cache of the loggedInUser object
    var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        instance = this
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null

        GlobalScope.launch {
            val store = StarGraderApplication.context.dataStore.data.first()
            if (store[USER_ID_KEY].isNullOrEmpty()) {
                return@launch
            }

            Log.d("REPO", "user: " + store[USER_ID_KEY] + " " + store[ACCESS_TOKEN_KEY])
            user = LoggedInUser(
                store[ACCESS_TOKEN_KEY]!!,
                store[USER_ID_KEY]!!,
                store[USER_NAME_KEY]!!,
                store[SCHOOL_ID_KEY]!!,
                store[SCHOOL_CODE_KEY]!!,
                store[SCHOOL_NAME_KEY]!!,
                store[SCHOOL_SHORT_NAME_KEY]!!
            )
        }
    }

    suspend fun logout() {
        user = null
        dataSource.logout()

        clearUser()
    }

    suspend fun login(username: String, password: String): Result<LoggedInUser> {
        // handle login
//        val result = dataSource.login(username, password)


        val result = Api.login(username, password)

        Log.d("REPO", "" + result)

        if (result.isNullOrEmpty()) {
            return Result.Error(IOException("Error logging in"))
        }

        val json = JSONTokener(result).nextValue() as JSONObject

        val status = json.getInt("statusCode")
        if (status != 200) {
            return Result.Error(IOException("Error logging in"))
        }


        val data = json.getJSONObject("operativeDto")
        val school = data.getJSONObject("school")


        val user = LoggedInUser(
            json.getString("accessToken"),
            data.getString("userUuid"),
            data.getString("fullName"),
            data.getString("schUuid"),
            school.getString("schCode"),
            school.getString("schName"),
            school.getString("schShortName")
        )

        setLoggedInUser(user)

        return Result.Success(user)


//        if (result is Result.Success) {
//            setLoggedInUser(result.data)
//        }
//
//        return result
    }


    @OptIn(DelicateCoroutinesApi::class)
    private suspend fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore


        storeUser(loggedInUser)

    }


}
