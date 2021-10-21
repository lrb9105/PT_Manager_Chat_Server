package com.teamnova.chatserver;

import java.io.IOException;
import java.net.Socket;
import java.sql.Connection;

/** 채팅 참여자*/
public class ChatUser {
    private Socket socket;
    private ChatRoom room; 		// 유저가 속한 룸이다.
    private ChattingMemberInfo chattingMemberInfo;
    private String userId;

    public ChatUser(Socket socket) {
        this.socket = socket;
    }

    public ChatUser(Socket socket, ChattingMemberInfo chattingMemberInfo) {
        this.socket = socket;
        this.chattingMemberInfo = chattingMemberInfo;
        this.userId = chattingMemberInfo.getUserId();
    }

    /** 송수신 쓰레드 생성 후 송수신 작업을 시작해라 */
    public void startMsgThread(){
        ClientManagerThread c_thread = new ClientManagerThread(this);

        c_thread.setSocket(this.socket);
        c_thread.start();
    }

    /**
     * 방에 입장시킴
     * @param room  입장할 방
     */
    public void enterRoom(ChatRoom room) {
        this.room = room; // 유저가 속한 방을 룸으로 변경한다.(중요)
    }

    /**
     * 방에서 퇴장
     * @param room 퇴장할 방
     */
    public void exitRoom(ChatRoom room){
        this.room = null;

        // 소켓을 닫아준다
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Exit Room!");

        // 퇴장처리(화면에 메세지를 준다는 등)

    }

    public ChatRoom getRoom() {
        return room;
    }

    public void setRoom(ChatRoom room) {
        this.room = room;
    }

    public ChattingMemberInfo getChattingMemberInfo() {
        return chattingMemberInfo;
    }

    public void setChattingMemberInfo(ChattingMemberInfo chattingMemberInfo) {
        this.chattingMemberInfo = chattingMemberInfo;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    /*
              equals와 hashCode를 override 해줘야, 동일유저를 비교할 수 있다
              비교할 때 -> gameUser 간 equals 비교, list에서 find 등
             */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatUser chatUser = (ChatUser) o;

        return this.userId.equals(chatUser.getUserId());
    }

    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}
