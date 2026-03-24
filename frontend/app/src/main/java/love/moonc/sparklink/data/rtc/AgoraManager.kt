package love.moonc.sparklink.data.rtc

import android.content.Context
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

object AgoraManager {
    private var rtcEngine: RtcEngine? = null

    // 请确保此 APP_ID 与后端环境变量中的 AGORA_APP_ID 严格一致
    private const val APP_ID = "cca0b8f4bd794b95b0479667be7c9cff"

    fun init(context: Context) {
        if (rtcEngine != null) return
        val config = RtcEngineConfig().apply {
            mContext = context.applicationContext
            mAppId = APP_ID
        }

        rtcEngine = RtcEngine.create(config)
        rtcEngine?.apply {
            enableAudio()
            // 语音房场景设置为直播模式
            setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
            // 默认初始化为观众
            setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
        }
    }

    fun joinChannel(token: String?, channelId: String, uid: Int) {
        val engine = rtcEngine ?: run {
            return
        }

        // 1. 加入前切换为 Broadcaster 角色，确保能说话
        engine.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
        engine.joinChannelWithUserAccount(token, channelId, uid.toString())
    }

    /**
     * 设置麦克风开关
     */
    fun setMicEnabled(isBroadcaster: Boolean) {
        val role = if (isBroadcaster) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        rtcEngine?.setClientRole(role)
    }

    /**
     * 离开频道
     */
    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }
}