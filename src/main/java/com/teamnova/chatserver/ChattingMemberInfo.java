package com.teamnova.chatserver;

import java.io.Serializable;

public class ChattingMemberInfo implements Serializable {
    private String chattingMemberId;
    private String userId;
    private String profileId;
    private int userType;
    private String loginId;
    private String userName;
    private String phoneNum;
    private String branchOffice;
    private String birth;
    private int gender;

    public ChattingMemberInfo(String chattingMemberId, String userId, String profileId, int userType, String loginId, String userName, String phoneNum, String branchOffice, String birth, int gender) {
        this.chattingMemberId = chattingMemberId;
        this.userId = userId;
        this.profileId = profileId;
        this.userType = userType;
        this.loginId = loginId;
        this.userName = userName;
        this.phoneNum = phoneNum;
        this.branchOffice = branchOffice;
        this.birth = birth;
        this.gender = gender;
    }

    public String getChattingMemberId() {
        return chattingMemberId;
    }

    public void setChattingMemberId(String chattingMemberId) {
        this.chattingMemberId = chattingMemberId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getBranchOffice() {
        return branchOffice;
    }

    public void setBranchOffice(String branchOffice) {
        this.branchOffice = branchOffice;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }
}
