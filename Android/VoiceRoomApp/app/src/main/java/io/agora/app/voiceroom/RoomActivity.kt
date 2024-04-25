package io.agora.app.voiceroom

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.agora.app.voice.kit.AUIVoiceRoomUIKit
import io.agora.asceneskit.voice.AUIVoiceRoomObserver
import io.agora.asceneskit.voice.AUIVoiceRoomView
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.utils.PermissionHelp

class RoomActivity : FragmentActivity() {

    companion object {
        const val EXTRA_ROOM_INFO = "RoomInfo"
        const val EXTRA_IS_CREATE = "isCreate"

        fun launch(context: Context, roomInfo: AUIRoomInfo, isCreate: Boolean){
            val intent = Intent(context, RoomActivity::class.java)
            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
            intent.putExtra(EXTRA_IS_CREATE, isCreate)
            context.startActivity(intent)
        }
    }

    // 房间信息
    private val roomInfo by lazy {
        intent.getSerializableExtra(EXTRA_ROOM_INFO) as AUIRoomInfo
    }

    // 房间事件观察者
    private val roomManagerRespObserver = object: AUIVoiceRoomObserver {
        override fun onRoomDestroy(roomId: String) {
            super.onRoomDestroy(roomId)
            // 房间被销毁
            destroyRoom()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置布局
        setContentView(R.layout.room_activity)

        // 初始化布局
        val roomView = findViewById<AUIVoiceRoomView>(R.id.roomView)
        roomView.setFragmentActivity(this@RoomActivity)
        roomView.setOnShutDownClick {
            // 主动退出房间
            destroyRoom()
            finish()
        }

        // 是否创建房间信息
        val isCreate = intent.getBooleanExtra(EXTRA_IS_CREATE, false)

        // 检查并申请运行时权限
        PermissionHelp(this).checkMicPerm(
            granted = {
                // 获取到权限
                // 生成Token配置
                AUIVoiceRoomUIKit.generateToken(roomInfo.roomId,
                    onSuccess = { roomConfig ->
                        // 启动房间
                        if (isCreate) {
                            AUIVoiceRoomUIKit.createRoom(roomInfo, roomConfig, roomView) { error, _ ->
                                if (error != null) {
                                    // 创建进入房间失败
                                    finish()
                                }
                            }
                        } else {
                            AUIVoiceRoomUIKit.launchRoom(roomInfo, roomConfig, roomView) { error, _ ->
                                if (error != null) {
                                    // 进入房间失败
                                    finish()
                                }
                            }
                        }
                    },
                    onFailure = {
                        // 生成Token配置失败
                        finish()
                    }
                )
            },
            unGranted = {
                // 没有获取到权限
                finish()
            }
        )

        // 注册房间事件观察者
        AUIVoiceRoomUIKit.registerRespObserver(roomInfo.roomId, roomManagerRespObserver)
    }

    override fun onBackPressed() {
        // 系统键盘返回主动退出房间时
        destroyRoom()
        super.onBackPressed()
    }

    private fun destroyRoom() {
        // 退出/销毁房间
        AUIVoiceRoomUIKit.destroyRoom(roomInfo.roomId)
        // 取消注册房间事件观察者
        AUIVoiceRoomUIKit.unRegisterRespObserver(roomInfo.roomId, roomManagerRespObserver)
    }
}