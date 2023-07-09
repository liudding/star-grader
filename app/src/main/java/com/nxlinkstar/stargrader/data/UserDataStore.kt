package com.nxlinkstar.stargrader.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.nxlinkstar.stargrader.StarGraderApplication
import com.nxlinkstar.stargrader.data.model.LoggedInUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

object UserDataStore {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user")

     val ACCESS_TOKEN_KEY = stringPreferencesKey("ACCESS_TOKEN")
     val USER_ID_KEY = stringPreferencesKey("USER_ID")
     val USER_NAME_KEY = stringPreferencesKey("USER_NAME")
     val SCHOOL_ID_KEY = stringPreferencesKey("SCHOOL_ID")
     val SCHOOL_NAME_KEY = stringPreferencesKey("SCHOOL_NAME")
     val SCHOOL_SHORT_NAME_KEY = stringPreferencesKey("SCHOOL_SHORT_NAME")
     val SCHOOL_CODE_KEY = stringPreferencesKey("SCHOOL_CODE")


     val userIdFlow: Flow<String?> = StarGraderApplication.context.dataStore.data.map {
        it[USER_NAME_KEY]
    }


    fun getUser() {
        GlobalScope.launch {
            val store = StarGraderApplication.context.dataStore.data.first()
            // You should also handle IOExceptions here.

        }
    }


     suspend fun storeUser(data: LoggedInUser) {
        StarGraderApplication.context.dataStore.edit { user ->
            user[ACCESS_TOKEN_KEY] = data.accessToken
            user[USER_ID_KEY] = data.userId
            user[USER_NAME_KEY] = data.name

            user[SCHOOL_ID_KEY] = data.schoolId
            user[SCHOOL_NAME_KEY] = data.schoolName
            user[SCHOOL_SHORT_NAME_KEY] = data.schoolShortName
            user[SCHOOL_CODE_KEY] = data.schoolCode
        }
    }


     suspend fun clearUser() {
        StarGraderApplication.context.dataStore.edit { user ->
            user.remove(ACCESS_TOKEN_KEY)
            user.remove(USER_ID_KEY)
            user.remove(USER_NAME_KEY)
        }
    }
}