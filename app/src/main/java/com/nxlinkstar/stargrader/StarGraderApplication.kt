package com.nxlinkstar.stargrader

import android.app.Application
import android.content.Context

class StarGraderApplication: Application() {

    companion object {
        var context: Application? = null

        fun getApplication(): Context {
            return context!!
        }

    }

    init {
        context = this
    }
}