package io.agora.app.karaoke

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.agora.asceneskit.karaoke.AUIKaraokeRoomServiceRespObserver
import io.agora.asceneskit.karaoke.KaraokeRoomView
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

    private val isCreate by lazy {
        intent.getBooleanExtra(EXTRA_IS_CREATE, false)
    }

    // 房间事件观察者
    private val roomManagerRespObserver = object: AUIKaraokeRoomServiceRespObserver {

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
        val karaokeRoomView = findViewById<KaraokeRoomView>(R.id.karaokeRoomView)
        karaokeRoomView.setFragmentActivity(this@RoomActivity)
        karaokeRoomView.setOnShutDownClick {
            // 主动退出房间
            destroyRoom()
            finish()
        }

        // 检查运行时权限
        PermissionHelp(this).checkMicPerm(
            granted = {
                // 获取到权限
                KaraokeUIKit.generateToken(roomInfo.roomId,
                    onSuccess = {roomConfig ->
                        // 并启动房间
                        if(isCreate){
                            // 创建并进入房间
                            KaraokeUIKit.createRoom(roomInfo, roomConfig, karaokeRoomView){ ex, roomInfo ->
                                if(ex != null){
                                    // 创建房间失败，直接退出
                                    finish()
                                }
                            }
                        }else{
                            // 进入房间
                            KaraokeUIKit.enterRoom(roomInfo, roomConfig, karaokeRoomView){ ex, roomInfo ->
                                if(ex != null){
                                    // 进入房间失败，直接退出
                                    finish()
                                }
                            }
                        }
                    },
                    onFailure = {
                        // 获取token失败，直接退出
                        finish()
                    })
            },
            unGranted = {
                // 没有获取到权限，直接退出
                finish()
            }
        )

        // 注册房间事件观察者
        KaraokeUIKit.registerRoomRespObserver(roomInfo.roomId, roomManagerRespObserver)
    }

    override fun onBackPressed() {
        // 系统键盘返回主动退出房间时
        destroyRoom()
        super.onBackPressed()
    }

    private fun destroyRoom() {
        // 退出/销毁房间
        KaraokeUIKit.leaveRoom(roomInfo.roomId)
        // 取消注册房间事件观察者
        KaraokeUIKit.unRegisterRoomRespObserver(roomInfo.roomId, roomManagerRespObserver)
    }
}