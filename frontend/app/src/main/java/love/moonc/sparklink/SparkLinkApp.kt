package love.moonc.sparklink

import android.app.Application
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule

class SparkLinkApp : Application() {
    override fun onCreate() {
        super.onCreate()

        UserPreferences.init(this)
        NetworkModule.init(this)
    }
}