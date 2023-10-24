package io.agora.app.karaoke

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.agora.asceneskit.karaoke.KaraokeRoomView
import io.agora.asceneskit.karaoke.KaraokeUiKit
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.service.IAUIRoomManager.AUIRoomManagerRespObserver
import io.agora.auikit.utils.PermissionHelp

class RoomActivity : FragmentActivity() {

    companion object {
        const val EXTRA_ROOM_INFO = "RoomInfo"

        fun launch(context: Context, roomInfo: AUIRoomInfo){
            val intent = Intent(context, RoomActivity::class.java)
            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
            context.startActivity(intent)
        }
    }

    // 房间信息
    private val roomInfo by lazy {
        intent.getSerializableExtra(EXTRA_ROOM_INFO) as AUIRoomInfo
    }

    // 房间事件观察者
    private val roomManagerRespObserver = object: AUIRoomManagerRespObserver{

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
                // 并启动房间
                KaraokeUiKit.launchRoom(roomInfo, karaokeRoomView)
            },
            unGranted = {
                // 没有获取到权限，直接退出
                finish()
            }
        )

        // 注册房间事件观察者
        KaraokeUiKit.registerRoomRespObserver(roomManagerRespObserver)
    }

    override fun onBackPressed() {
        // 系统键盘返回主动退出房间时
        destroyRoom()
        super.onBackPressed()
    }

    private fun destroyRoom() {
        // 退出/销毁房间
        KaraokeUiKit.destroyRoom(roomInfo.roomId)
        // 取消注册房间事件观察者
        KaraokeUiKit.unRegisterRoomRespObserver(roomManagerRespObserver)
    }
}