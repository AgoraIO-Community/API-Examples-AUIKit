# VoiceChatUIKit

<!-- TOC START -->

- [VoiceChatUIKit](#voicechatuikit)
  - [开发环境](#开发环境)
  - [快速集成](#快速集成)
    - [1.集成SDK](#1集成sdk)
      - [a.新建项目](#a新建项目)
      - [b.添加依赖](#b添加依赖)
      - [c.设置权限](#c设置权限)
    - [2. 初始化VoiceChatUIKit](#2-初始化voicechatuikit)
    - [3. 房主创建房间](#3-房主创建房间)
      - [a.添加“创建房间”按钮](#a添加创建房间按钮)
      - [b.获取token](#b获取token)
      - [c.创建VoiceChat房间](#c创建voicechat房间)
        - [ViewController里声明一个房间容器的对象](#viewcontroller里声明一个房间容器的对象)
        - [创建房间并启动房间详情页](#创建房间并启动房间详情页)
    - [4.进入房间](#4进入房间)
      - [加入房间并启动房间详情页](#加入房间并启动房间详情页)
    - [5. 观众进入房间前准备（可选）](#5-观众进入房间前准备可选)
      - [a.添加“加入房间”按钮](#a添加加入房间按钮)
      - [b.获取VoiceChat房间信息](#b获取voicechat房间信息)
    - [6. 退出/销毁房间](#6-退出销毁房间)
      - [a.主动退出](#a主动退出)
      - [b.被动退出](#b被动退出)


## 开发环境
- Xcode 14.0及以上版本
- 最低支持系统：iOS 13.0
- 请确保您的项目已设置有效的开发者签名

## 快速集成

### 1.集成SDK
#### a.新建项目
**打开XCode如按下列步骤新建一个项目, 例如以```AUIKitDemo```为项目名称**
>
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/ios/create_new_ios_project1.jpg)
>
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/ios/create_new_ios_project2.jpg)
>
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/ios/create_new_ios_project3.jpg)
>
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/ios/create_new_ios_project4.jpg)


#### b.添加依赖

**将以下源码复制到自己的项目里，例如放在```AUIKitDemo.xcodeproj```同级目录下**

- [KeyCenter.swift](https://github.com/AgoraIO-Community/AUIVoiceRoom/blob/main/iOS/AUIVoiceRoom/AUIVoiceRoom/KeyCenter.swift)
- [VoiceChatUIKit.swift](https://github.com/AgoraIO-Community/AUIVoiceRoom/blob/main/iOS/AUIVoiceRoom/AUIVoiceRoom/VoiceChatUIKit.swift)
- [AScenesKit](https://github.com/AgoraIO-Community/AUIVoiceRoom/tree/main/iOS/AScenesKit/AScenesKit)
- [AScenesKit.podspec](https://github.com/AgoraIO-Community/AUIVoiceRoom/blob/main/iOS/AScenesKit/AScenesKit.podspec)
>


**把 `KeyCenter.swift` 和 `VoiceChatUIKit.swift` 拖进工程里**
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/voicechat/ios/example-addsource-voicechat.png)

**在```AUIKitDemo.xcodeproj```同级目录下创建一个```Podfile```文件，并添加如下内容**
```
source 'https://github.com/CocoaPods/Specs.git'
platform :ios, '13.0'

target 'AUIKitDemo' do
  use_frameworks!
  
  pod 'AScenesKit', :path => './AScenesKit.podspec'
  pod 'AgoraRtcEngine_Special_iOS', '4.1.1.29'
end

post_install do |installer|
  installer.pods_project.targets.each do |target|
    target.build_configurations.each do |config|
      config.build_settings['IPHONEOS_DEPLOYMENT_TARGET'] = '13.0'
    end
  end
end
```

**打开终端执行以下命令，即可把AUISceneKit的相关依赖集成进Demo里**
```
 cd {Podfile的目录}

 pod update --verbose
```

#### c.设置权限
**打开```AUIKitDemo.xcworkspace```(和AUIKitDemo.xcodeproj同级目录)，配置麦克风权限**

![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/ios/add_privacy_to_project.jpg)

**即可体验添加了AUIKit依赖的工程**

>⚠️Xcode15编译报错 ```Sandbox: rsync.samba(47334) deny(1) file-write-create...```

解决方法: Build Setting里搜索 ```ENABLE_USER_SCRIPT_SANDBOXING```把```User Script Sandboxing```改为```NO```
>
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/ios/fix_compiler_xcode15_sandbox_error.jpg)

### 2. 初始化VoiceChatUIKit
**在AppDelegate.swift里设置依赖**
```swift
import AUIKitCore
import AScenesKit
```
**在AppDelegate.swift里初始化VoiceChatUIKit**
```swift
func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        //随机设置用户uid
        let uid = Int(arc4random_uniform(99999999))

        // 设置基础信息到VoiceChatUIKit里
        let commonConfig = AUICommonConfig()
        commonConfig.appId = KeyCenter.AppId
        commonConfig.appCert = KeyCenter.AppCertificate
        commonConfig.basicAuth = KeyCenter.AppBasicAuth
        commonConfig.imAppKey = KeyCenter.IMAppKey
        commonConfig.imClientId = KeyCenter.IMClientId
        commonConfig.imClientSecret = KeyCenter.IMClientSecret
        commonConfig.host = KeyCenter.HostUrl
        let ownerInfo = AUIUserThumbnailInfo()
        ownerInfo.userId = "\(uid)"
        ownerInfo.userName = "user_\(uid)"
        ownerInfo.userAvatar = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_1.png"
        commonConfig.owner = ownerInfo
        VoiceChatUIKit.shared.setup(commonConfig: commonConfig,
                                    apiConfig: nil)
    
    // Override point for customization after application launch.
    return true
}
```

### 3. 房主创建房间
**在ViewController.swift里设置依赖**
```swift
import AUIKitCore
import AScenesKit
```

#### a.添加“创建房间”按钮
```swift
override func viewDidLoad() {
    super.viewDidLoad()
    
    //作为房主创建房间的按钮
    let createButton = UIButton(frame: CGRect(x: 10, y: 100, width: 100, height: 60))
    createButton.setTitle("创建房间", for: .normal)
    createButton.setTitleColor(.red, for: .normal)
    createButton.addTarget(self, action: #selector(onCreateAction), for: .touchUpInside)
    view.addSubview(createButton)
}
```
#### b.获取token
```swift
private func generateToken(channelName: String,
                           roomConfig: AUIRoomConfig,
                           completion: @escaping ((Error?) -> Void)) {
    let uid = VoiceChatUIKit.shared.commonConfig?.owner?.userId ?? ""
    let rtcChorusChannelName = "\(channelName)_rtc_ex"
    roomConfig.channelName = channelName
    roomConfig.rtcChorusChannelName = rtcChorusChannelName
    print("generateTokens: \(uid)")

    let group = DispatchGroup()

    var err: Error?
    group.enter()
    let tokenModel1 = AUITokenGenerateNetworkModel()
    tokenModel1.channelName = channelName
    tokenModel1.userId = uid
    tokenModel1.request { error, result in
        defer {
            if err == nil {
                err = error
            }
            group.leave()
        }
        guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}
        roomConfig.rtcToken = tokenMap["rtcToken"] ?? ""
        roomConfig.rtmToken = tokenMap["rtmToken"] ?? ""
    }

    group.enter()
    let tokenModel2 = AUITokenGenerateNetworkModel()
    tokenModel2.channelName = rtcChorusChannelName
    tokenModel2.userId = uid
    tokenModel2.request { error, result in
        defer {
            if err == nil {
                err = error
            }
            group.leave()
        }

        guard let tokenMap = result as? [String: String], tokenMap.count >= 2 else {return}

        roomConfig.rtcChorusRtcToken = tokenMap["rtcToken"] ?? ""
    }

    group.notify(queue: DispatchQueue.main) {
        completion(err)
    }
}
```
#### c.创建VoiceChat房间
##### ViewController里声明一个房间容器的对象
```swift
class ViewController: UIViewController {
    var voiceChatView: AUIVoiceChatRoomView?

    ....
}
```
##### 创建房间并启动房间详情页
```swift
@objc func onCreateAction(_ button: UIButton) {
    button.isEnabled = false
        
    let roomId = Int(arc4random_uniform(99999))
    
    let roomInfo = AUIRoomInfo()
    roomInfo.roomId = "\(roomId)"
    roomInfo.roomName = "\(roomId)"
    roomInfo.owner = AUIRoomContext.shared.currentUserInfo
            
    let roomConfig = AUIRoomConfig()
    //创建房间容器
    let voiceChatView = AUIVoiceChatRoomView(frame: self.view.bounds)
    voiceChatView.onClickOffButton = { [weak self] in
        //房间内点击退出
        //self?.destroyRoom(roomId: roomInfo.roomId)
        assert(false, "正常退出需要打开上面的注释并且删掉当前的assert")
    }
    generateToken(channelName: "\(roomId)",
                  roomConfig: roomConfig,
                  completion: {[weak self] error in
        guard let self = self else {return}
        if let error = error { 
            button.isEnabled = true
            return 
        }
        VoiceChatUIKit.shared.createRoom(roomInfo: roomInfo,
                                         roomConfig: roomConfig,
                                         chatView: voiceChatView) {[weak self] error in
            guard let self = self else {return}
            button.isEnabled = true
            if let error = error { return }
        }
        
        // 订阅房间被销毁回调
        //VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
    })
    
    self.view.addSubview(voiceChatView)
    self.voiceChatView = voiceChatView
}
```

### 4.进入房间

#### 加入房间并启动房间详情页
```swift
func enterRoom(roomInfo: AUIRoomInfo) {
  let voiceChatView = AUIVoiceChatRoomView(frame: self.view.bounds)
        
  voiceChatView.onClickOffButton = { [weak self] in
      //房间内点击退出
      //self?.destroyRoom(roomId: roomInfo.roomId)
      assert(false, "正常退出需要打开上面的注释并且删掉当前的assert")
  }
  let roomId = roomInfo.roomId
  let roomConfig = AUIRoomConfig()
  generateToken(channelName: roomId,
                roomConfig: roomConfig) {[weak self] err  in
      guard let self = self else { return }
      VoiceChatUIKit.shared.enterRoom(roomId: roomId,
                                      roomConfig: roomConfig,
                                      chatView: self.voiceChatView!) {[weak self] roomInfo, error in
          guard let self = self else {return}
          if let error = error { return }
      }
      
      // 订阅房间被销毁回调
      //VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
  }

  self.view.addSubview(voiceChatView)
  self.voiceChatView = voiceChatView
}
```

### 5. 观众进入房间前准备（可选）
#### a.添加“加入房间”按钮
```swift
override func viewDidLoad() {
    super.viewDidLoad()
    
    //作为观众加入房间的按钮
    let joinButton = UIButton(frame: CGRect(x: 10, y: 160, width: 100, height: 60))
    joinButton.setTitle("加入房间", for: .normal)
    joinButton.setTitleColor(.red, for: .normal)
    joinButton.addTarget(self, action: #selector(onJoinAction), for: .touchUpInside)
    view.addSubview(joinButton)
}
```
#### b.获取VoiceChat房间信息
```swift
@objc func onJoinAction() {
    let alertController = UIAlertController(title: "房间名", message: "", preferredStyle: .alert)
    alertController.addTextField { (textField) in
        textField.placeholder = "请输入"
    }
    let cancelAction = UIAlertAction(title: "取消", style: .cancel) { (_) in
    }
    let saveAction = UIAlertAction(title: "确认", style: .default) { (_) in
        if let inputText = alertController.textFields?.first?.text {
            // 处理用户输入的内容
            VoiceChatUIKit.shared.getRoomInfoList(lastCreateTime: 0, pageSize: 50) { error, roomList in
                guard let roomList = roomList else {return}
                for room in roomList {
                    if room.roomName == inputText {
                        self.enterRoom(roomInfo: room)
                        break
                    }
                }
            }
        }
    }
    alertController.addAction(cancelAction)
    alertController.addAction(saveAction)

    present(alertController, animated: true, completion: nil)
}
```



### 6. 退出/销毁房间
**设置退出方法**
```swift
func destroyRoom(roomId: String) {
    //点击退出
    self.voiceChatView?.onBackAction()
    self.voiceChatView?.removeFromSuperview()
    
    VoiceChatUIKit.shared.leaveRoom(roomId: roomId)
    //在退出房间时取消订阅
    VoiceChatUIKit.shared.unbindRespDelegate(delegate: self)
}
```
#### a.主动退出
**在 [创建房间并启动房间详情页](#创建房间并启动房间详情页) 与 [加入房间并启动房间详情页](#加入房间并启动房间详情页) 里打开注释设置回调，调用destroyRoom方法，即可主动退出房间**
```swift
//AUIVoiceChatRoomView提供了onClickOffButton点击返回的clousure
VoiceChatView.onClickOffButton = { [weak self] in
    //点击退出
    self?.destroyRoom(roomId: roomInfo.roomId)
}
```

#### b.被动退出
**首先在 [创建房间并启动房间详情页](#创建房间并启动房间详情页) 与 [加入房间并启动房间详情页](#加入房间并启动房间详情页) 里打开注释订阅 `AUIVoiceChatRoomServiceRespDelegate` 的回调**  
```swift
@objc func onCreateAction(_ button: UIButton) {
    //...
        
    // 订阅房间被销毁回调
    VoiceChatUIKit.shared.bindRespDelegate(delegate: self)

    //...
}
```

```swift
func enterRoom(roomInfo: AUIRoomInfo) {
    //...
        
    // 订阅房间被销毁回调
    VoiceChatUIKit.shared.bindRespDelegate(delegate: self)

    //...
}
```

**然后通过 `AUIVoiceChatRoomServiceRespDelegate` 回调方法中的 `onRoomDestroy` 和 `onRoomUserBeKicked` 来处理房间销毁**
```swift
extension ViewController: AUIKaraokeRoomServiceRespDelegate {
    //房间销毁
    func onRoomDestroy(roomId: String) {
        self.destroyRoom(roomId: roomId)
    }
    
    //被踢出房间
    func onRoomUserBeKicked(roomId: String, userId: String) {
        self.destroyRoom(roomId: roomId)
    }
}
```

