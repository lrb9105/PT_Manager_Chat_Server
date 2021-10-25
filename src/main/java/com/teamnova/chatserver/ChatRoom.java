package com.teamnova.chatserver;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.mysql.cj.protocol.a.MysqlBinaryValueDecoder;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.*;

/** 채팅룸*/
public class ChatRoom {
    // 텍스트를 전송하는 경우
    private ArrayList<PrintWriter> outputList;
    // 채팅방에 참여되어있는 모든 참여자의 아이디-토큰 맵
    private HashMap<String, String> tokenMap;
    // 객체를 전송하는 경우d
    private ArrayList<ObjectOutputStream> objOutputList;

    private String roomId; // 룸 ID
    private ArrayList<ChatUser> chattingMemberList;
    private String roomName; // 방 이름
    private Connection connection;

    // 채팅방에 참여되어있는 모든 참여자의 수
    private int userCount;


    public ChatRoom(String roomId) { // 아무도 없는 방을 생성할 때
        this.roomId = roomId;
        chattingMemberList = new ArrayList();
        outputList = new ArrayList<>();
        objOutputList = new ArrayList<>();
        tokenMap = new HashMap<>();
        connection = getConnection();

        try {
            // 이방의 총인원 가져오기
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) COUNT FROM CHATTING_MEMBER WHERE CHATTING_ROOM_ID = '" + roomId + "'");
            resultSet.next();

            userCount = resultSet.getInt("COUNT");
            statement.close();
            resultSet.close();

            // 이방의 모든인원의 토큰 가져오기
            statement = connection.createStatement();

            resultSet = statement.executeQuery("SELECT CM.USER_ID\n" +
                                                ", T.TOKEN\n" +
                                                "FROM CHATTING_MEMBER CM\n" +
                                                "INNER JOIN TOKEN T ON CM.USER_ID = T.USER_ID\n" +
                                                "WHERE CM.CHATTING_ROOM_ID = '" + roomId + "'");

            // 토큰 저장하기
            while(resultSet.next()){
                String userId = resultSet.getString("USER_ID");
                String token = resultSet.getString("TOKEN");

                tokenMap.put(userId, token);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public ChatRoom(ChatUser user) { // 유저가 방을 만들때
        chattingMemberList = new ArrayList();
        outputList = new ArrayList<>();
        user.enterRoom(this);
        chattingMemberList.add(user); // 유저를 추가시킨 후
    }

    public ChatRoom(ArrayList<ChatUser> chatMemberList) { // 유저 리스트가 방을 생성할
        this.chattingMemberList =chatMemberList; // 유저리스트 복사

        // 룸 입장
        for(ChatUser user :chatMemberList){
            user.enterRoom(this);
        }
    }

    public void enterUser(ChatUser user) {
        user.enterRoom(this);
        chattingMemberList.add(user);
        try {
            // 채팅방의 PrintWriter에 새로 들어온 인원의 outputStream 추가
            outputList.add(new PrintWriter(user.getSocket().getOutputStream()));

            // 채팅방의 ObjectOutputStreamList에 새로 들어온 인원의 outputStream 추가
            //objOutputList.add(new ObjectOutputStream(user.getSocket().getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enterUser(ArrayList<ChatUser> chatMemberList) {
        for(ChatUser user :chatMemberList){
            user.enterRoom(this);
            try {
                // 채팅방의 PrintWriter에 새로 들어온 인원의 outputStream 추가
                outputList.add(new PrintWriter(user.getSocket().getOutputStream()));

                // 채팅방의 ObjectOutputStreamList에 새로 들어온 인원의 outputStream 추가
                //objOutputList.add(new ObjectOutputStream(user.getSocket().getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        chattingMemberList.addAll(chatMemberList);
    }

    /**
     * 해당 룸의 유저를 다 퇴장시키고 삭제함
     */
    public void close() {
        for (ChatUser user : chattingMemberList) {
            user.exitRoom(this);
        }
        this.chattingMemberList.clear();
        this.chattingMemberList = null;
    }

    /** 특정 유저를 퇴장 시킴*/
    public void exitUser(ChatUser user) {
        user.exitRoom(this);

        // 특정인덱스에 있는 유저와 아웃풋리스트 둘 다 삭제 해 줌
        for(int i = 0; i < chattingMemberList.size(); i++){
            if(user.getUserId().equals(chattingMemberList.get(i).getUserId())){
                this.chattingMemberList.remove(i);
                this.outputList.remove(i);
            }
        }

        // 모든 사용자가 나갔다면 채팅방을 제거한다.
        if(chattingMemberList.size() == 0){
            MyServer.chatRoomManager.removeRoom(this);

            // db커넥션이 열려있다면 닫히도록
            try {
                if(connection != null && !connection.isClosed()){
                    connection.close();
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    /**
     * 해당 byte 배열을 방의 모든 유저에게 전송
     * @param data 보낼 data
     */
    public void broadcast(byte[] data) {
        for (ChatUser user : chattingMemberList) { // 방에 속한 유저의 수만큼 반복
            // 각 유저에게 데이터를 전송하는 메서드 호출~
            // ex) user.SendData(data);

//			try {
//				user.sock.getOutputStream().write(data); // 이런식으로 바이트배열을 보낸다.
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
        }
    }

    public void setRoomName(String name) { // 방 이름을 설정
        this.roomName = name;
    }

    public ChatUser getUserById(String userId) { // 닉네임을 통해서 방에 속한 유저를 리턴함
        for (ChatUser chatMember : chattingMemberList) {
            if (chatMember.getChattingMemberInfo().getUserId().equals(userId)) {
                return chatMember; // 유저를 찾았다면
            }
        }
        return null; // 찾는 유저가 없다면
    }

    // ChatUser 객체로 get
    public ChatUser getUser(ChatUser ChatUser) {

        int idx = chattingMemberList.indexOf(ChatUser);

        // 유저가 존재한다면(ChatUser의 equals로 비교)
        if(idx > 0){
            return chattingMemberList.get(idx);
        }
        else{
            // 유저가 없다면
            return null;
        }
    }

    // db커넥션 생성
    public Connection getConnection(){
        String url = "jdbc:mysql://15.165.144.216:3306/pt_manager";
        String userName = "lrb9105";
        String password = "!vkdnj91556";
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(url, userName, password);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return connection;
    }

    // 토큰리스트를 만든다.
    public ArrayList<String> makeTokenList(HashMap<String, String> tokenMap, String sendUserId){
        ArrayList<String> tokenList = new ArrayList<>();

        Iterator it = tokenMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)it.next();

            // 원래 나한테는 안보내려고 했는데 나도 채팅방 리스트를 업데이트 해야하니까 받아야 할 것 같다.
            //tokenList.add(entry.getValue());

            // 메시지 송신자가 아니라면 notification리스트에 넣기
            /*if(!entry.getKey().equals(sendUserId)){
                tokenList.add(entry.getValue());
            }*/
        }

        tokenList.add("eCMZ8MAZRcOj0sVR-JKzF3:APA91bH5iUpPgJJK47LnqB35r4FQXh8O99xDW1DNL30IDEz3UdS2lAbVKNVuECiGC85x_gNDywM1aa3aCaJL-aHeejJj_pNZfITqCraS7zQQRVWupb9SKTYBbl2j3dPfbEfMwdGXibIR");

        return tokenList;
    }

    public String getRoomName() { // 방 이름을 가져옴
        return roomName;
    }

    public int getUserSize() { // 유저의 수를 리턴
        return chattingMemberList.size();
    }

    public ArrayList<ChatUser> getChattingMemberList() {
        return chattingMemberList;
    }

    public void setChattingMemberList(ArrayList<ChatUser> chattingMemberList) {
        this.chattingMemberList = chattingMemberList;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public ArrayList<PrintWriter> getOutputList() {
        return outputList;
    }

    public void setOutputList(ArrayList<PrintWriter> outputList) {
        this.outputList = outputList;
    }

    public ArrayList<ObjectOutputStream> getObjOutputList() {
        return objOutputList;
    }

    public void setObjOutputList(ArrayList<ObjectOutputStream> objOutputList) {
        this.objOutputList = objOutputList;
    }

    public int getUserCount() {
        return userCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public HashMap<String, String> getTokenMap() {
        return tokenMap;
    }

    public void setTokenMap(HashMap<String, String> tokenMap) {
        this.tokenMap = tokenMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatRoom chatRoom = (ChatRoom) o;

        return roomId.equals(chatRoom.getRoomId());
    }

    @Override
    public int hashCode() {
        return roomId.hashCode();
    }
}
