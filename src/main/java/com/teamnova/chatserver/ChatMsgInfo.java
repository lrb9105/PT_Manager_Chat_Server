package com.teamnova.chatserver;

import java.io.Serializable;

public class ChatMsgInfo implements Serializable {
    private String chattingMsgId;
    // userId임!
    private String chattingMemberId;
    private String chattingRoomId;
    private String msg;
    // 채팅을 읽지 않은 유저 수
    private int notReadUserCount;
    private int msgIdx;


    public ChatMsgInfo(String chattingMsgId, String chattingMemberId, String chattingRoomId, String msg, int notReadUserCount, int msgIdx) {
        this.chattingMsgId = chattingMsgId;
        this.chattingMemberId = chattingMemberId;
        this.chattingRoomId = chattingRoomId;
        this.msg = msg;
        this.notReadUserCount = notReadUserCount;
        this.msgIdx = msgIdx;
    }

    public String getChattingMsgId() {
        return chattingMsgId;
    }

    public void setChattingMsgId(String chattingMsgId) {
        this.chattingMsgId = chattingMsgId;
    }

    public String getChattingMemberId() {
        return chattingMemberId;
    }

    public void setChattingMemberId(String chattingMemberId) {
        this.chattingMemberId = chattingMemberId;
    }

    public String getChattingRoomId() {
        return chattingRoomId;
    }

    public void setChattingRoomId(String chattingRoomId) {
        this.chattingRoomId = chattingRoomId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getNotReadUserCount() {
        return notReadUserCount;
    }

    public void setNotReadUserCount(int notReadUserCount) {
        this.notReadUserCount = notReadUserCount;
    }

    public int getMsgIdx() {
        return msgIdx;
    }

    public void setMsgIdx(int msgIdx) {
        this.msgIdx = msgIdx;
    }
}