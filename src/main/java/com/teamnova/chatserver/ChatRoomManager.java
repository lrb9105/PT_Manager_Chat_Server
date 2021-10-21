package com.teamnova.chatserver;

import java.util.ArrayList;

/** 채팅방을 관리하는 채팅룸 매니저*/
public class ChatRoomManager {
    private static ArrayList<ChatRoom> chatRoomList;

    public ChatRoomManager() {
        chatRoomList = new ArrayList<>();
    }

    /**
     * 빈 룸을 생성
     * @return ChatRoom
     */
    public ChatRoom createRoom(String cahtRoomId) { // 룸을 새로 생성(빈 방)
        ChatRoom room = new ChatRoom(cahtRoomId);
        chatRoomList.add(room);
        System.out.println("Room Created!");
        return room;
    }

    /**
     * 룸으로 입장시킴
     * @return ChatRoom
     */
    public ChatRoom enterRoom(ChatUser chatUser, String cahtRoomId) { // 룸을 새로 생성(빈 방)
        ChatRoom room = new ChatRoom(cahtRoomId);
        chatRoomList.add(room);
        System.out.println("Room Created!");
        return room;
    }

    /**
     * 유저 리스트로 방을 생성
     * @param users 입장시킬 유저 리스트
     * @return ChatRoom
     */
    public ChatRoom createRoom(ArrayList<ChatUser> users, String roomId) {
        ChatRoom room = new ChatRoom(roomId);
        room.enterUser(users);

        chatRoomList.add(room);
        System.out.println("Room Created!");
        return room;
    }

    public ChatRoom getRoom(ChatRoom ChatRoom){

        int idx = chatRoomList.indexOf(ChatRoom);

        if(idx > 0){
            return chatRoomList.get(idx);
        }
        else{
            return null;
        }
    }

    /**
     * 전달받은 룸을 제거
     * @param room 제거할 룸
     */
    public void removeRoom(ChatRoom room) {
        room.close();
        chatRoomList.remove(room); // 전달받은 룸을 제거한다.
        System.out.println("Room Deleted!");
    }

    /**
     * 방의 현재 크기를 리턴
     * @return 현재 size
     */
    public static int roomCount() {
        return chatRoomList.size();
    }

    /** 특정id의 채팅방 반환*/
    public ChatRoom getChatRoom(String roomId){
        ChatRoom chatRoom = null;
        for(ChatRoom cRoom : ChatRoomManager.chatRoomList){
            if(cRoom.getRoomId().equals(roomId)){
                chatRoom = cRoom;
                break;
            }
        }
        return chatRoom;
    }
}
