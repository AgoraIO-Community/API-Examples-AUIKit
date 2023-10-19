//
//  AppDelegate.swift
//  AUIKitDemo
//
//  Created by 朱继超 on 2023/10/18.
//

import UIKit
import AUIKitCore
import AScenesKit

@main
class AppDelegate: UIResponder, UIApplicationDelegate {



    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        //随机设置用户uid
        let uid = Int(arc4random_uniform(99999999))
        let commonConfig = AUICommonConfig()
        commonConfig.host = "https://service.agora.io/uikit-karaoke"
        commonConfig.userId = "\(uid)"
        commonConfig.userName = "user_\(uid)"
        commonConfig.userAvatar = "https://accktvpic.oss-cn-beijing.aliyuncs.com/pic/sample_avatar/sample_avatar_1.png"
        VoiceChatUIKit.shared.setup(roomConfig: commonConfig,
                                  rtcEngine: nil,   //如果有外部初始化的rtc engine
                                  rtmClient: nil)   //如果有外部初始化的rtm client
        
        // Override point for customization after application launch.
        return true
    }

    // MARK: UISceneSession Lifecycle

    func application(_ application: UIApplication, configurationForConnecting connectingSceneSession: UISceneSession, options: UIScene.ConnectionOptions) -> UISceneConfiguration {
        // Called when a new scene session is being created.
        // Use this method to select a configuration to create the new scene with.
        return UISceneConfiguration(name: "Default Configuration", sessionRole: connectingSceneSession.role)
    }

    func application(_ application: UIApplication, didDiscardSceneSessions sceneSessions: Set<UISceneSession>) {
        // Called when the user discards a scene session.
        // If any sessions were discarded while the application was not running, this will be called shortly after application:didFinishLaunchingWithOptions.
        // Use this method to release any resources that were specific to the discarded scenes, as they will not return.
    }


}

