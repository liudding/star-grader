package com.nxlinkstar.stargrader.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser (
    val accessToken: String,

    val userId: String,
    val name: String,

    val schoolId: String,
    val schoolCode: String,
    val schoolName: String,
    val schoolShortName: String
)