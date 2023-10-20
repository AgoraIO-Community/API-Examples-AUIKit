# KaraokeUIKit

*[English](KaraokeUIKit.md) | 中文*


<!-- TOC START -->

- [KaraokeUIKit](#karaokeuikit)
  - [开发环境](#开发环境)
  - [快速集成](#快速集成)
    - [1.集成SDK](#1集成sdk)
      - [a.新建项目](#a新建项目)
      - [b.添加依赖库](#b添加依赖库)
      - [c.设置权限](#c设置权限)
    - [2. 初始化KaraokeUIKit](#2-初始化karaokeuikit)
    - [3. 房主创建房间](#3-房主创建房间)
      - [a.添加“创建房间”按钮](#a添加创建房间按钮)
      - [b.创建Karaoke房间](#b创建karaoke房间)
    - [4.进入房间](#4进入房间)
      - [创建房间详情页并启动Karaoke房间](#创建房间详情页并启动karaoke房间)
    - [5. 观众进入房间准备（可选）](#5-观众进入房间准备可选)
      - [a.添加“加入房间”按钮](#a添加加入房间按钮)
      - [b.获取Karaoke房间信息](#b获取karaoke房间信息)
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


#### b.添加依赖库
**将以下源码复制到自己项目里，例如放在```AUIKitDemo.xcodeproj```同级目录下**

- [AScenesKit](https://github.com/AgoraIO-Community/AUIKaraoke/tree/main/iOS/AScenesKit)
>
![](https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/readme/ios/copy_asceneskit.jpg)

**在```AUIKitDemo.xcodeproj```同级目录下创建一个```Podfile```文件，并添加如下内容**
```
source 'https://github.com/CocoaPods/Specs.git'
platform :ios, '13.0'

target 'AUIKitDemo' do
  use_frameworks!
  
  pod 'AScenesKit', :path => './AScenesKit'
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

### 2. 初始化KaraokeUIKit
**在AppDelegate.swift里设置依赖**
```swift
import AUIKitCore
import AScenesKit
```
**在AppDelegate.swift里初始化KaraokeUIKit**
```swift
func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
    let uid = Int(arc4random_uniform(99999999))
    let commonConfig = AUICommonConfig()
    commonConfig.host = "https://service.agora.io/uikit-karaoke"
    commonConfig.userId = "\(uid)"
    commonConfig.userName = "user_\(uid)"
    commonConfig.userAvatar = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_1.png"
    KaraokeUIKit.shared.setup(roomConfig: commonConfig,
                              ktvApi: nil,      //如果有外部初始化的ktv api
                              rtcEngine: nil,   //如果有外部初始化的rtc engine
                              rtmClient: nil)   //如果有外部初始化的rtm client
    
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
#### b.创建Karaoke房间
```swift
@objc func onCreateAction(_ button: UIButton) {
    let roomId = Int(arc4random_uniform(99999999))
    let room = AUICreateRoomInfo()
    room.roomName = "\(roomId)"
    button.isEnabled = false
    KaraokeUIKit.shared.createRoom(roomInfo: room) { roomInfo in
        self.enterRoom(roomInfo: roomInfo!)
        button.isEnabled = true
    } failure: { error in
        print("on create room fail: \(error.localizedDescription)")
        button.isEnabled = true
    }
}
```

### 4.进入房间
**ViewController里声明一个Karaoke房间容器的属性**
```swift
class ViewController: UIViewController {
    var karaokeView: AUIKaraokeRoomView?

    ....
}
```
#### 创建房间详情页并启动Karaoke房间
```swift
func enterRoom(roomInfo: AUIRoomInfo) {
    karaokeView = AUIKaraokeRoomView(frame: self.view.bounds)
    karaokeView!.onClickOffButton = { [weak self] in
        //房间内点击退出
        //self?.destroyRoom()
        assert(false, "正常退出需要打开上面的注释并且删掉当前的assert")
    }
    KaraokeUIKit.shared.launchRoom(roomInfo: roomInfo,
                                   karaokeView: karaokeView!) {[weak self] error in
        guard let self = self else {return}
        if let _ = error { return }
        //订阅房间被销毁回调
        //KaraokeUIKit.shared.bindRespDelegate(delegate: self)
        self.view.addSubview(self.karaokeView!)
    }
}
```

### 5. 观众进入房间准备（可选）
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
#### b.获取Karaoke房间信息
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
            KaraokeUIKit.shared.getRoomInfoList(lastCreateTime: nil, pageSize: 50) { error, roomList in
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
    self.karaokeView?.onBackAction()
    self.karaokeView?.removeFromSuperview()
    
    KaraokeUIKit.shared.destoryRoom(roomId: roomId)
    //在退出房间时取消订阅
    KaraokeUIKit.shared.unbindRespDelegate(delegate: self)
}
```
#### a.主动退出
**在[创建房间详情页并启动Karaoke房间](#创建房间详情页并启动karaoke房间)里打开注释设置回调，调用destroyRoom方法，即可主动退出房间**
```swift
//AUIKaraokeRoomView提供了onClickOffButton点击返回的clousure
karaokeView.onClickOffButton = { [weak self] in
    //点击退出
    self?.destroyRoom()
}
```

#### b.被动退出
**首先在[创建房间详情页并启动Karaoke房间](#创建房间详情页并启动karaoke房间)里打开注释订阅AUIRoomManagerRespDelegate的回调**  
**然后在销毁房间时设置取消订阅**  
**最后后通过AUIRoomManagerRespDelegate回调方法中的onRoomDestroy来处理房间销毁**
```swift
KaraokeUIKit.shared.launchRoom(roomInfo: roomInfo,
                                   karaokeView: karaokeView!) {[weak self] error in
    ...
    
    KaraokeUIKit.shared.bindRespDelegate(delegate: self)

    ...
}

func destroyRoom(roomId: String) {
    ...

    KaraokeUIKit.shared.unbindRespDelegate(delegate: self)

    ...
}

extension ViewController: AUIRoomManagerRespDelegate {
    //房间销毁
    @objc func onRoomDestroy(roomId: String) {
        self.destroyRoom()
    }
}
```

