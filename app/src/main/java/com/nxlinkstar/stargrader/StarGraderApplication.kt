package com.nxlinkstar.stargrader

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class StarGraderApplication: Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        fun getApplication(): Context {
            return context
        }

    }

    init {
        context = this
    }
}