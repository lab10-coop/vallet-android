package io.lab10.vallet.admin

import android.app.Application
import io.lab10.vallet.models.MyObjectBox
import io.objectbox.BoxStore

open class ValletApp: Application() {
    companion object {
        var box: BoxStore? = null

        fun getBoxStore(): BoxStore {
          return box as BoxStore
        }
    }

    override fun onCreate() {
        super.onCreate()
        initBox()
    }

    fun initBox() {
        if (box == null) {
           box = MyObjectBox.builder().androidContext(this).build()
        }
    }
}