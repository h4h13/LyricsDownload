package code.name.monkey.companion

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDexApplication

/**
 * @author Hemanth S (h4h13).
 */
class App : MultiDexApplication() {
    companion object {
        var app: App? = null
        fun getContext(): Context {
            return app!!.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        app = this
    }
}
