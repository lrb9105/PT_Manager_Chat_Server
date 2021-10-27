package com.teamnova.chatserver;

import com.google.firebase.messaging.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ClientManagerThread extends Thread{
    private Socket m_socket;
    private ChatUser chatUser;
    private ChatRoom chatRoom;
    private Connection connection;
    private int allUserCount;

    public ClientManagerThread(ChatUser chatUser) {
        this.chatUser = chatUser;
    }

    @Override
    public void run(){
        super.run();
        try{
            // 텍스트만 전송되는 경우 이렇게 받으면 됨
            BufferedReader in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));

            String text = in.readLine();

            System.out.println(text);

            // 객체를 전송받는 경우
            /*ObjectInputStream ois = new ObjectInputStream(m_socket.getInputStream());
            ChatMsgInfo chatMsg = null;

            try {
                chatMsg = (ChatMsgInfo)ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            System.out.println("111");
            System.out.println(chatMsg);

            String text = chatMsg.getMsg();
            */

            // 최초 생성 시 클라쪽에서 채팅방 id를 보내줌
            // ID가 리스트에 이미 있다면 해당 방에 입장시켜 줌
            if(text.contains("!@#$chatRoomIdAndUserId:")){
                System.out.println("111");

                // 채팅방id
                String roomId = text.split(":")[1];
                // 사용자id
                String userId = text.split(":")[2];

                chatUser.setUserId(userId);

                /** 채팅방이 이미 생성되어 있다면 해당 유저를 그 채팅방에 입장시킴 */
                // roomId에 해당하는 채팅방 반환
                chatRoom = MyServer.chatRoomManager.getChatRoom(roomId);

                // 해당하는 채팅방이 없다면 방을 생성해 줌
                if (chatRoom == null) {
                    chatRoom = MyServer.chatRoomManager.createRoom(roomId);
                }

                // 채팅방의 모든 사용자 수
                allUserCount = chatRoom.getUserCount();

                System.out.println("전체참여자수: " + allUserCount);

                this.connection = chatRoom.getConnection();

                chatRoom.enterUser(chatUser);
            }

            // 여기서 어떤 쓰레드에는 인원이 1명이고 어떤 쓰레드에는 인원이 2명이어서 동기화가 안될 수 있음
            // 이런 경우가 생기면 outputList를 동기화 시켜줄 수 있는 방법을 찾자
            while(true){
                // 문자열만 받을 때 이렇게 받으면 됨
                text = in.readLine();

                //text = chatMsg.getMsg();

                System.out.println(text);

                if(text!=null) {
                    // 연결 종료
                    if(text.contains("!@#$connectionExpire:")){
                        // 사용자가 채팅방을 나가도록
                        chatRoom.exitUser(chatUser);

                        break;
                    }

                    /** 채팅방 입장 시 마지막 인덱스 보냄 => 안읽은 사용자 업데이트*/
                    if(text.contains("!@#$!@#lsatIdx:")){
                        String[] textArr = text.split(":");
                        String userId = textArr[1];
                        String roomId = textArr[2];
                        int lastIdx = Integer.parseInt(textArr[3]);
                        String firstOrOld = textArr[4];

                        // 4. 클라로부터 받은 마지막 메시지 인덱스
                        System.out.println("4. 클라로부터 받은 마지막 메시지 인덱스:" + text);

                        /** 채팅방의 다른 유저들에게 뿌려주기 */
                        for(int i=0; i < chatRoom.getUserSize();++i){
                            System.out.println("5. 클라로 메시지 보내기 사이즈: " + chatRoom.getOutputList().size());

                            // 내가 보낸 메시지가 아닌경우 텍스트로 받아왔을 때 클라이언트에게 마지막 인덱스 쏴주기
                            if(!chatRoom.getChattingMemberList().get(i).getUserId().equals(userId)){
                                // 5. 클라로 다시 메시지 보내기
                                System.out.println("5. 클라로 메시지 보내기: " + "!@#$!@#lsatIdx:" + lastIdx);

                                chatRoom.getOutputList().get(i).println("!@#$!@#lsatIdx:" + lastIdx);
                                chatRoom.getOutputList().get(i).flush();
                            }


                            // 객체로 받아왔을 때 클라이언트에세 쏴주기
                        /*chatRoom.getObjOutputList().get(i).writeObject(chatMsg);
                        chatRoom.getObjOutputList().get(i).flush();*/
                        }

                        // 첫번째로 채팅방에 들어온 경우 인덱스를 -1 해줘야 한다.
                        if(firstOrOld.equals("first")){
                            lastIdx = lastIdx - 1;
                        }

                        // 테스트 코드...
                        if(lastIdx == 1){
                            lastIdx -= 1;
                        }

                        System.out.println("firstOrOld: " + firstOrOld);

                        /** db업데이트 해당 인덱스 이후의 모든 메시지 안읽은 사용자수 -- */
                        String sql = "UPDATE CHATTING_MSG SET  NOT_READ_USER_COUNT = NOT_READ_USER_COUNT - 1\n" +
                                "WHERE MSG_IDX > " + lastIdx + "\n" +
                                "AND CHATTING_ROOM_ID = '" + roomId + "'";


                        System.out.println(sql);

                        // 연결을 한 부분의 인스턴스 (현재 클래스의 메소드로 호출하기)
                        Statement stmt = null; // 디비 관련 statement
                        System.out.println("sql = " + sql); // SQL문은 항상 확인하는 것이 좋음

                        int count = 0; // 데이터가 몇개 변경 되었는지 확인하는 변수

                        try {
                            stmt = connection.createStatement();
                            // 현재 연결부분에 대한 상태를 생성
                            count = stmt.executeUpdate(sql); // 몇개가 업데이트되었는지에 대한 변수

                            // 이 아래코드부터는 데이터 추가에 성공한 다음의 코드
                            System.out.println("성공적으로 변경되었습니다." + count);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                if (stmt != null) { // 값이 들어가 있음
                                    stmt.close(); // statement 닫음
                                }
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }

                        // 아래로 내려가지 않고 다시 while문을 돌도록
                        continue;
                    }


                    /** 텍스트 메시지 분리 */
                    String[] textArr = text.split(":");
                    String userName = textArr[0];
                    String userId = textArr[1];
                    String roomId = textArr[2];
                    String msg = textArr[3];
                    String now = getTodayDateWithTime();

                    System.out.println("userName: " + userName);

                    // 새로 들어오거나 나간 경우 서버 채팅방 객체가 관리하는 전체사용자 수도 업데이트
                    // 채팅방을 처음 생성할 때만 전체 사용자수를 가져오기 때문에 채팅방 생성 후 사용자 추가 및 이탈에 대해
                    // 업데이트 해줘야 한다.
                    // 토큰리스트도 업데이트 해줘야한다.
                    if(userName.equals("new")){
                        chatRoom.setUserCount(allUserCount + 1);
                        allUserCount += 1;

                        System.out.println("들어왔을 때 참여자수: " + allUserCount);

                        // 새로들어온 인원의 토큰 가져와서 맵에 저장하기
                        HashMap<String, String> tokenMap = chatRoom.getTokenMap();

                        // 새로 들어온 유저의 토큰 가져오기기
                        try {
                           // 이방의 총인원 가져오기
                            Statement statement = connection.createStatement();
                            ResultSet resultSet = statement.executeQuery("SELECT USER_ID, TOKEN\n" +
                                    "FROM TOKEN\n" +
                                    "WHERE USER_ID= '" + userId + "'");

                            if(resultSet.next()){
                                String token = resultSet.getString("TOKEN");

                                tokenMap.put(userId,token);
                                chatRoom.setTokenMap(tokenMap);
                            }

                            resultSet.close();

                            statement.close();
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    } else if(userName.equals("exit")){
                        // 사용자를 방에서 내보냄
                        chatRoom.exitUser(chatUser);

                        allUserCount -= 1;

                        System.out.println("나갔을 때 참여자수: " + allUserCount);

                        // 나간 인원의 토큰 값 지우기
                        HashMap<String, String> tokenMap = chatRoom.getTokenMap();
                        tokenMap.remove(userId);
                        chatRoom.setTokenMap(tokenMap);

                    }

                    // 안읽은 사람 수 = 채팅방의 전체 유저수 - 현재 채팅방에 있는 유저수
                    int notReadUserCount = allUserCount - chatRoom.getUserSize();

                    System.out.println("아웃풋리스트 사이즈: " + chatRoom.getOutputList().size());
                    System.out.println("allUserCount: " + allUserCount);
                    System.out.println("chatRoom.getUserSize(): " + chatRoom.getUserSize());


                    /** DB에서 가장 높은 인덱스 값 가져오기*/
                    Statement statement = null;
                    int maxIdx = 0;

                    try {
                        statement = connection.createStatement();
                        ResultSet resultSet = statement.executeQuery("SELECT MAX(MSG_IDX) MAX_IDX FROM CHATTING_MSG WHERE CHATTING_ROOM_ID = '" + roomId + "'");
                        resultSet.next();

                        maxIdx = resultSet.getInt("MAX_IDX") + 1;

                        resultSet.close();
                        statement.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }

                    // 메시지 저장 시간까지 합쳐서 보냄
                    text += ":"+now;
                    text += ":"+notReadUserCount;
                    text += ":"+maxIdx;

                    System.out.println("text: " + text);

                    /** 채팅방의 다른 유저들에게 뿌려주기 */
                    for(int i=0; i < chatRoom.getChattingMemberList().size();++i){
                        // 내가 보낸 메시지가 아닌경우 텍스트로 받아왔을 때 클라이언트에게 쏴주기
                        if(!chatRoom.getChattingMemberList().get(i).getUserId().equals(userId)){
                            chatRoom.getOutputList().get(i).println(text);
                            System.out.println("메시지가 아예 안가나?");
                        } else {
                            // 내가보낸 메시지인 경우 idx와 읽지않은 사용자수, 수신시간 보내주고 클라이언트에서 업데이트 함
                            chatRoom.getOutputList().get(i).println(notReadUserCount + ":" + maxIdx + ":" + now);

                            System.out.println(chatRoom.getOutputList().get(i).toString());

                            System.out.println("메시지가 아예 안가나?222");
                        }
                        chatRoom.getOutputList().get(i).flush();

                        // 객체로 받아왔을 때 클라이언트에세 쏴주기
                        /*chatRoom.getObjOutputList().get(i).writeObject(chatMsg);
                        chatRoom.getObjOutputList().get(i).flush();*/
                    }

                    /** db에 저장하기 */
                    // 2. createStatement로 DB 테이블에 데이터 추가하기
                    String sql = "INSERT INTO CHATTING_MSG (CHATTING_MEMBER_ID,CHATTING_ROOM_ID,MSG, MSG_IDX, NOT_READ_USER_COUNT, CRE_DATETIME)\n" +
                            "VALUES ('" + userId + "', '" + roomId + "', '" + msg + "',  '" + maxIdx + "',  " +  notReadUserCount + ", '" + now + "' )";

                    System.out.println(sql);

                    // 연결을 한 부분의 인스턴스 (현재 클래스의 메소드로 호출하기)
                    Statement stmt = null; // 디비 관련 statement
                    System.out.println("sql = " + sql); // SQL문은 항상 확인하는 것이 좋음

                    int count = 0; // 데이터가 몇개 변경 되었는지 확인하는 변수

                    try {
                        stmt = connection.createStatement();
                        // 현재 연결부분에 대한 상태를 생성
                        count = stmt.executeUpdate(sql); // 몇개가 업데이트되었는지에 대한 변수

                        // 이 아래코드부터는 데이터 추가에 성공한 다음의 코드
                        System.out.println("성공적으로 추가되었습니다." + count);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (stmt != null) { // 값이 들어가 있음
                                stmt.close(); // statement 닫음
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }

                    /** notification 전송*/
                    //sendNotification(userName, msg, chatRoom.makeTokenList(chatRoom.getTokenMap(), userId));
                } else { //연결 종료
                    chatRoom.exitUser(chatUser);
                    break;
                }
            }


        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void setSocket(Socket _socket){
        m_socket = _socket;
    }

    public static String getTodayDateWithTime(){
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        String date = simpleDateFormat.format(new Date());
        return date;
    }

    // FCM서버에 메시지 요청을 한다.
    public void sendNotification(String userName, String msg, ArrayList<String> tokenList) {
        Notification notification = new Notification(userName, msg);

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(tokenList)
                .build();

        BatchResponse response = null;

        try {
            response = FirebaseMessaging.getInstance()
                    .sendMulticast(message);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }

        if (response.getFailureCount() > 0) {
            List<SendResponse> responses = response.getResponses();
            List<String> failedTokens = new ArrayList<>();
            for (int i = 0; i < responses.size(); i++) {
                if (!responses.get(i).isSuccessful()) {
                    System.out.println("에러코드 " + responses.get(i).getException().getErrorCode());
                    System.out.println("에러메시지 " + responses.get(i).getException().getMessage());

                    failedTokens.add(tokenList.get(i) + "\n");
                } else {
                    System.out.println(responses.get(i).getMessageId());
                    System.out.println("성공!");
                }
            }

            System.out.println("실패 갯수:" + response.getFailureCount());
            System.out.println("List of tokens that caused failures: " + failedTokens);
        }
    }
}
