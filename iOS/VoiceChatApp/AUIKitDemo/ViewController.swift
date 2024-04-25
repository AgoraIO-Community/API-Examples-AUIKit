//
//  ViewController.swift
//  AUIKitDemo
//
//  Created by 朱继超 on 2023/10/18.
//

import UIKit
import AUIKitCore
import AScenesKit

class ViewController: UIViewController {
    
    var voiceChatView : AUIVoiceChatRoomView?

    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view.
        //作为房主创建房间的按钮
        let createButton = UIButton(frame: CGRect(x: 10, y: 100, width: 100, height: 60))
        createButton.setTitle("创建房间", for: .normal)
        createButton.setTitleColor(.red, for: .normal)
        createButton.addTarget(self, action: #selector(onCreateAction), for: .touchUpInside)
        view.addSubview(createButton)
        
        let joinButton = UIButton(frame: CGRect(x: 10, y: 160, width: 100, height: 60))
        joinButton.setTitle("加入房间", for: .normal)
        joinButton.setTitleColor(.red, for: .normal)
        joinButton.addTarget(self, action: #selector(onJoinAction), for: .touchUpInside)
        view.addSubview(joinButton)
    }

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
            self?.destroyRoom(roomId: roomInfo.roomId)
        }
        generateToken(channelName: "\(roomId)",
                      roomConfig: roomConfig,
                      completion: {[weak self] error in
            guard let self = self else {return}
            defer {
                button.isEnabled = true
            }
            if let error = error {
                self.navigationController?.popViewController(animated: true)
                AUIToast.show(text: error.localizedDescription)
                return
            }
            VoiceChatUIKit.shared.createRoom(roomInfo: roomInfo,
                                             roomConfig: roomConfig,
                                             chatView: voiceChatView) {[weak self] error in
                guard let self = self else {return}
                if let error = error {
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
            }
            
            // 订阅房间被销毁回调
            VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
        })
        
        self.view.addSubview(voiceChatView)
        self.voiceChatView = voiceChatView
    }
    
    func enterRoom(roomInfo: AUIRoomInfo) {
        let voiceChatView = AUIVoiceChatRoomView(frame: self.view.bounds)
        
        voiceChatView.onClickOffButton = { [weak self] in
            //房间内点击退出
            self?.destroyRoom(roomId: roomInfo.roomId)
        }
        let roomId = roomInfo.roomId
        let roomConfig = AUIRoomConfig()
        generateToken(channelName: roomId,
                      roomConfig: roomConfig) {[weak self] err  in
            guard let self = self else {return}
            VoiceChatUIKit.shared.enterRoom(roomId: roomId,
                                            roomConfig: roomConfig,
                                            chatView: self.voiceChatView!) {[weak self] roomInfo, error in
                guard let self = self else {return}
                if let error = error {
                    self.navigationController?.popViewController(animated: true)
                    AUIToast.show(text: error.localizedDescription)
                    return
                }
            }
            
            // 订阅房间被销毁回调
            VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
        }
        
        self.view.addSubview(voiceChatView)
        self.voiceChatView = voiceChatView
    }
    
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
                            return
                        }
                    }
                    
                    AUIToast.show(text: "房间'\(inputText)'不存在")
                }
            }
        }
        alertController.addAction(cancelAction)
        alertController.addAction(saveAction)

        present(alertController, animated: true, completion: nil)
    }
    
    func destroyRoom(roomId: String) {
        //点击退出
        self.voiceChatView?.onBackAction()
        self.voiceChatView?.removeFromSuperview()
        
        VoiceChatUIKit.shared.leaveRoom(roomId: roomId)
        //在退出房间时取消订阅
        VoiceChatUIKit.shared.unbindRespDelegate(delegate: self)
    }
    
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
}

extension ViewController: AUIVoiceChatRoomServiceRespDelegate {
    //房间销毁
    func onRoomDestroy(roomId: String) {
        self.destroyRoom(roomId: roomId)
    }
    
    //被踢出房间
    func onRoomUserBeKicked(roomId: String, userId: String) {
        self.destroyRoom(roomId: roomId)
    }
}
