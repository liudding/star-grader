package com.nxlinkstar.stargrader.data

import com.nxlinkstar.stargrader.data.UserDataStore.clearUser
import com.nxlinkstar.stargrader.data.UserDataStore.storeUser
import com.nxlinkstar.stargrader.data.UserDataStore.userIdFlow
import com.nxlinkstar.stargrader.data.model.LoggedInUser
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
    private var user: LoggedInUser? = null
        private set

    val isLoggedIn: Boolean
        get() = user != null

    init {
        instance = this
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
        user = null



        GlobalScope.launch {

            userIdFlow.collect { it ->
                if (it != null) {
//                    user = LoggedInUser(it, "")
                }
            }
        }
    }

    fun logout() {
        user = null
        dataSource.logout()

        GlobalScope.launch {
            clearUser()
        }
    }

    fun login(username: String, password: String): Result<LoggedInUser> {
        // handle login
        val result = dataSource.login(username, password)

        if (result is Result.Success) {
            setLoggedInUser(result.data)
        }

        return result
    }


    @OptIn(DelicateCoroutinesApi::class)
    private fun setLoggedInUser(loggedInUser: LoggedInUser) {
        this.user = loggedInUser
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore


        GlobalScope.launch {
            storeUser(loggedInUser)
        }

    }


}
