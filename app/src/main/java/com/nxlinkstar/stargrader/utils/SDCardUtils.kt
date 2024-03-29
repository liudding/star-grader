@file:Suppress("DEPRECATION")

package com.open.camera.utils

import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.util.Log
import java.io.File
import java.util.*

/**
 * SD 卡相关信息
 */
object SDCardUtils {

    /**
     * 优先外存储外SD卡
     *
     * @param
     */
    @JvmStatic
    fun getSDMouthPath(context: Context, path: String?): File {
        val pictures =
            Objects.requireNonNull(context)
                .getExternalFilesDirs(path)
        return when {
            pictures.size > 1 -> {
                pictures[1]
            }
            pictures.size > 2 -> {
                pictures[2]
            }
            else -> {
                pictures[0]
            }
        }
    }


    /**
     * 优先内存储外SD卡
     *
     * @param
     */
    @JvmStatic
    fun getSDLocalPath(context: Context, path: String?): File? {
        val pictures =
            Objects.requireNonNull(context)
                .getExternalFilesDirs(path)
        if (pictures.isNotEmpty()) {
            return pictures[0]
        }
        return null
    }

    /**
     * 日志存储位置
     */
    fun getMyLog(): String {
        return "MyLog"
    }

    /**
     * 单位:M
     */
    fun readSDCardSize(): Int {
        var reSize = 0
        val blockSize: Long
        val availCount: Long
        val state = Environment.getExternalStorageState()
        if (Environment.MEDIA_MOUNTED == state) {
            val sdcardDir = Environment.getExternalStorageDirectory()
            val sf = StatFs(sdcardDir.path)
            blockSize = sf.blockSize.toLong()
            val blockCount = sf.blockCount;
            availCount = sf.availableBlocks.toLong()
            Log.d(
                "readSDCardSize",
                "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount / 1024 / 1024 + "M"
            )
            Log.d(
                "readSDCardSize",
                "可用的block数目：:" + availCount + ",剩余空间:" + availCount * blockSize / 1024 / 1024 + "M"
            )
            reSize = (availCount * blockSize / (1024 * 1024)).toInt()
        }
        return reSize
    }

}