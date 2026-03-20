package love.moonc.sparklink

import android.app.Application
import love.moonc.sparklink.data.local.UserPreferences
import love.moonc.sparklink.data.remote.NetworkModule
import love.moonc.sparklink.data.rtc.AgoraManager

class SparkLinkApp : Application() {
    override fun onCreate() {
        super.onCreate()

        UserPreferences.init(this)
        NetworkModule.init(this)
        AgoraManager.init(this)
    }
}