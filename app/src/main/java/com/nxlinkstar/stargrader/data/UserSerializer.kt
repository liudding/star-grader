//package com.nxlinkstar.stargrader.data
//
//import androidx.datastore.core.CorruptionException
//import androidx.datastore.core.Serializer
//import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
//import com.nxlinkstar.stargrader.data.model.LoggedInUser
//import java.io.InputStream
//import java.io.OutputStream
//
//object UserSerializer : Serializer<User> {
//    override val defaultValue: User = User.getDefaultInstance()
//
//    override suspend fun readFrom(input: InputStream): User {
//        try {
//            return LoggedInUser.parseFrom(input)
//        } catch (exception: InvalidProtocolBufferException) {
//            throw CorruptionException("Cannot read user.proto.", exception)
//        }
//    }
//
//    override suspend fun writeTo(
//        t: User,
//        output: OutputStream
//    ) = t.writeTo(output)
//}