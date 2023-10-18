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
        let roomId = Int(arc4random_uniform(99999999))
        let room = AUICreateRoomInfo()
        room.roomName = "\(roomId)"
        button.isEnabled = false
        VoiceChatUIKit.shared.createRoom(roomInfo: room) { roomInfo in
            self.enterRoom(roomInfo: roomInfo!)
            button.isEnabled = true
        } failure: { error in
            print("on create room fail: \(error.localizedDescription)")
            button.isEnabled = true
        }
    }
    
    func enterRoom(roomInfo: AUIRoomInfo) {
        if self.voiceChatView == nil {
            self.voiceChatView = AUIVoiceChatRoomView(frame: self.view.bounds,roomInfo: roomInfo)
        }
        
        voiceChatView?.onClickOffButton = { [weak self] in
            //房间内点击退出
            self?.destroyRoom(roomId: roomInfo.roomId)
        }
        if let roomView = self.voiceChatView {
            VoiceChatUIKit.shared.launchRoom(roomInfo: roomInfo,
                                             roomView: roomView) {[weak self] error in
                guard let self = self else {return}
                if let _ = error { return }
                //订阅房间被销毁回调
                VoiceChatUIKit.shared.bindRespDelegate(delegate: self)
                self.view.addSubview(roomView)
            }
        }
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
                VoiceChatUIKit.shared.getRoomInfoList(lastCreateTime: nil, pageSize: 50) { error, roomList in
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
    
    func destroyRoom(roomId: String) {
        //点击退出
        self.voiceChatView?.onBackAction()
        self.voiceChatView?.removeFromSuperview()
        
        VoiceChatUIKit.shared.destoryRoom(roomId: roomId)
        //在退出房间时取消订阅
        VoiceChatUIKit.shared.unbindRespDelegate(delegate: self)
    }
}

extension ViewController: AUIRoomManagerRespDelegate {
    //房间销毁
    func onRoomDestroy(roomId: String) {
        self.destroyRoom(roomId: roomId)
    }
    
    func onRoomInfoChange(roomId: String, roomInfo: AUIKitCore.AUIRoomInfo) {
    }
    
    func onRoomAnnouncementChange(roomId: String, announcement: String) {
    }
    
    //被房主踢出
    func onRoomUserBeKicked(roomId: String, userId: String) {
        self.destroyRoom(roomId: roomId)
    }
}
