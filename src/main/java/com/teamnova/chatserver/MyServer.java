package com.teamnova.chatserver;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

public class MyServer {
    //public static ArrayList<PrintWriter> m_OutputList;
    public static ChatRoomManager chatRoomManager;

    public static void main(String[] args){
        // Firebase Admin SDK 초기화 - jar파일로 묶을 땐 경로 초기화 해줘야 함
        FileInputStream serviceAccount = null;
        try {
            serviceAccount = new FileInputStream("C:/firebase/ptmanager-3d62a-firebase-adminsdk-i0u9u-95a5165afa.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 채팅방 매니저
        chatRoomManager = new ChatRoomManager();

        //m_OutputList = new ArrayList<PrintWriter>();

        try{
            System.out.println("시작!");

            ServerSocket s_socket = new ServerSocket(8888);

            // while문이 왜 필요한가 했더니 여러명의 요청을 받아야 해서 그랬던 거구나!
            while(true){
                // 클라이언트로부터 요청완료하면 통신소켓 생성
                Socket c_socket = s_socket.accept();

                // 채팅에 참여할 유저 객체 생성
                ChatUser chatUser = new ChatUser(c_socket);

                // 송수신 쓰레드 시작 => 채팅방 입장
                chatUser.startMsgThread();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
