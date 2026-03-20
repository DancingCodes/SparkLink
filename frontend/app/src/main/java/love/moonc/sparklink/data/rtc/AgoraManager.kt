package love.moonc.sparklink.data.rtc

import android.content.Context
import android.util.Log
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

/**
 * 声网 RTC 管理器 (Singleton)
 * 适配依赖: cn.shengwang.rtc:voice-rtc-basic:4.6.3
 * 实际路径: io.agora.rtc2
 */
object AgoraManager {
    private const val TAG = "AgoraManager"
    private var rtcEngine: RtcEngine? = null
    private const val APP_ID = "cca0b8f4bd794b95b0479667be7c9cff"

    /**
     * 在 Application 中调用此方法初始化
     */
    fun init(context: Context) {
        if (rtcEngine != null) return

        try {
            val config = RtcEngineConfig().apply {
                mContext = context.applicationContext
                mAppId = APP_ID
                mEventHandler = object : IRtcEngineEventHandler() {
                    // 成功加入频道回调
                    override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
                        Log.d(TAG, "成功加入频道: $channel, 分配 UID: $uid")
                    }

                    // 远端用户加入回调
                    override fun onUserJoined(uid: Int, elapsed: Int) {
                        Log.d(TAG, "远端用户进入房间: $uid")
                    }

                    // 错误监控
                    override fun onError(err: Int) {
                        Log.e(TAG, "声网 SDK 报错, 错误码: $err")
                    }
                }
            }

            // 创建引擎实例
            rtcEngine = RtcEngine.create(config)

            // 语音房基础配置
            rtcEngine?.apply {
                enableAudio() // 启用音频模块
                // 设置频道场景为直播模式（语音房标准）
                setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
                // 默认设为观众 (Audience)，只有在需要说话时切换到 Broadcaster
                setClientRole(Constants.CLIENT_ROLE_AUDIENCE)
            }
            Log.d(TAG, "Agora RTC 引擎在 data/rtc 层初始化成功")

        } catch (e: Exception) {
            Log.e(TAG, "Agora 初始化异常: ${e.message}")
        }
    }

    /**
     * 加入语音频道
     * @param token 后端生成的临时 Token
     * @param channelId 房间名
     * @param uid 用户 ID (0 表示由 SDK 自动分配)
     */
    fun joinChannel(token: String?, channelId: String, uid: Int = 0) {
        rtcEngine?.joinChannel(token, channelId, uid, null)
    }

    /**
     * 切换麦克风状态 (上麦/下麦)
     * @param isBroadcaster true 代表主播(能说话)，false 代表观众(只能听)
     */
    fun setMicEnabled(isBroadcaster: Boolean) {
        val role = if (isBroadcaster) {
            Constants.CLIENT_ROLE_BROADCASTER
        } else {
            Constants.CLIENT_ROLE_AUDIENCE
        }
        rtcEngine?.setClientRole(role)
    }

    /**
     * 离开当前频道
     */
    fun leaveChannel() {
        rtcEngine?.leaveChannel()
    }

    /**
     * 彻底销毁引擎 (App 退出时调用)
     */
    fun destroy() {
        RtcEngine.destroy()
        rtcEngine = null
    }
}