package io.agora.app.voiceroom

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.agora.app.voice.kit.AUIVoiceRoomUIKit
import io.agora.app.voiceroom.ui.theme.VoiceRoomAppTheme
import io.agora.asceneskit.voice.AUIAPIConfig
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.ui.micseats.MicSeatType
import java.util.Random
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContent {
        //     VoiceRoomAppTheme {
        //         // A surface container using the 'background' color from the theme
        //         Surface(
        //             modifier = Modifier.fillMaxSize(),
        //             color = MaterialTheme.colorScheme.background
        //         ) {
        //             Greeting("Android")
        //         }
        //     }
        // }
        // 将jectpack compose布局改成xml布局
        setContentView(R.layout.main_activity)

        // 初始化AUIVoiceRoomUIKit
        AUIVoiceRoomUIKit.init(
            AUICommonConfig().apply {
                context = applicationContext

                host = "https://service.shengwang.cn/uikit-v2"
                appId = ""
                appCert = ""
                imAppKey = ""
                imClientId = ""
                imClientSecret = ""
                basicAuth = ""

                owner = AUIUserThumbnailInfo().apply {
                    userId = (Random().nextInt(1000) + 10000).toString()
                    userName = "User-$userId"
                    userAvatar =
                        "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_1.png"
                }
            },
            AUIAPIConfig()
        )

        findViewById<Button>(R.id.btnCreateRoom).setOnClickListener {
            // 点击创建房间按钮
            // 随机生成房间名
            val roomName = "${Random().nextInt(100) + 1000}"
            // 创建房间
            val roomInfo = AUIRoomInfo()
            roomInfo.owner = AUIRoomContext.shared().currentUserInfo
            roomInfo.roomId = UUID.randomUUID().toString()
            roomInfo.thumbnail = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_1.png"
            roomInfo.roomName = roomName
            roomInfo.micSeatCount = 8
            roomInfo.micSeatStyle = MicSeatType.EightTag.value.toString()
            // 创建房间成功，跳转到房间详情页
            RoomActivity.launch(this@MainActivity, roomInfo, isCreate = true)
        }

        findViewById<Button>(R.id.btnJoinRoom).setOnClickListener {
            // 弹出房间名输入弹窗
            val input = EditText(this@MainActivity)
            input.hint = "Room Name"
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Launch Room")
                .setView(input)
                .setPositiveButton("Confirm") { dialog: DialogInterface, i: Int ->
                    // 确认加入房间
                    val roomName = input.text.toString()
                    if (TextUtils.isEmpty(roomName)) {
                        Toast.makeText(this@MainActivity, "Room name is empty", Toast.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                        return@setPositiveButton
                    }
                    // 查询房间信息
                    AUIVoiceRoomUIKit.getRoomList(0, 50,
                        success = { list ->
                            val roomInfo = list.findLast { it.roomName == roomName }
                            if (roomInfo == null) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "The room is unavailable. RoomName=$roomName",
                                    Toast.LENGTH_SHORT
                                ).show()
                                dialog.dismiss()
                                return@getRoomList
                            }
                            // 进入房间
                            RoomActivity.launch(this@MainActivity, roomInfo, false)
                        },
                        failure = {
                            Toast.makeText(
                                this@MainActivity,
                                "Get room list failed. ${it.code} - ${it.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            dialog.dismiss()
                        })
                }
                .setNegativeButton("Cancel") { dialog: DialogInterface, i: Int ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    VoiceRoomAppTheme {
        Greeting("Android")
    }
}