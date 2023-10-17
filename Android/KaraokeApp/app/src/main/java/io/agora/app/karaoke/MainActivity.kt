package io.agora.app.karaoke

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
import io.agora.app.karaoke.ui.theme.KaraokeAppTheme
import io.agora.asceneskit.karaoke.KaraokeUiKit
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUICreateRoomInfo
import java.util.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContent {
        //    KaraokeAppTheme {
        //        // A surface container using the 'background' color from the theme
        //        Surface(
        //            modifier = Modifier.fillMaxSize(),
        //            color = MaterialTheme.colorScheme.background
        //        ) {
        //            RoomListView("Android")
        //        }
        //    }
        //}
        setContentView(R.layout.main_activity)

        // 初始化KaraokeUiKit
        KaraokeUiKit.setup(AUICommonConfig().apply {
            context = applicationContext
            host = "https://service.agora.io/uikit-karaoke"
            userId = (Random().nextInt(1000) + 10000).toString()
            userName = "User-$userId"
            userAvatar = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_1.png"
        })

        findViewById<Button>(R.id.btnCreateRoom).setOnClickListener {
            // 创建房间按钮点击
            // 随机生成房间名
            val roomName = "Room-${Random().nextInt(100) + 1000}"
            // 调用KaraokeUiKit创建房间
            val createRoomInfo = AUICreateRoomInfo()
            createRoomInfo.roomName = roomName
            KaraokeUiKit.createRoom(
                createRoomInfo,
                success = { roomInfo ->
                    // 创建房间成功，跳转到房间详情页
                    RoomActivity.launch(this@MainActivity, roomInfo)
                },
                failure = {
                    // 创建房间失败
                    Toast.makeText(this@MainActivity, "Create Room failed. reason: ${it.code} - ${it.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }

        findViewById<Button>(R.id.btnJoinRoom).setOnClickListener {
            // 弹出房间名输入弹窗
            val input = EditText(this@MainActivity)
            input.hint = "Room Name"
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Create Room")
                .setView(input)
                .setPositiveButton("Confirm"){ dialog: DialogInterface, i: Int ->
                    // 确认创建房间
                    // 判断房间名是否为空
                    val roomName = input.text.toString()
                    if(TextUtils.isEmpty(roomName)){
                        Toast.makeText(this@MainActivity, "Room name is empty", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        return@setPositiveButton
                    }
                    // 查询房间信息
                    KaraokeUiKit.getRoomList(0, 100,
                        success = { list ->
                            val roomInfo = list.findLast { it.roomName == roomName }
                            if(roomInfo == null){
                                Toast.makeText(this@MainActivity, "The room is unavailable. RoomName=$roomName", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                                return@getRoomList
                            }

                            // 拉起房间详情页
                            RoomActivity.launch(this@MainActivity, roomInfo)
                        },
                        failure = {
                            Toast.makeText(this@MainActivity, "Get room list failed. ${it.code} - ${it.message}", Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        })
                }
                .setNegativeButton("Cancel"){ dialog: DialogInterface, i: Int ->
                    dialog.dismiss()
                }
                .show()

        }
    }
}

@Composable
fun RoomListView(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun RoomListViewPreview() {
    KaraokeAppTheme {
        RoomListView("Android")
    }
}