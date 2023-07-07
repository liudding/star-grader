package com.nxlinkstar.stargrader

import android.app.Application
import android.content.Context

class StarGraderApplication: Application() {

    companion object {
        lateinit var context: Application

        fun getApplication(): Context {
            return context
        }

    }

    init {
        context = this
    }
}