package com.mikuwxc.autoreply.bean;

public class ChatRoomBean {
    private String roomid;
    private String memberString;
    private String owner;
    private String name;
    private String wechatId;  //微信号

    public String getWechatId() {
        return wechatId;
    }

    public void setWechatId(String wechatId) {
        this.wechatId = wechatId;
    }



    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }

    public String getMemberString() {
        return memberString;
    }

    public void setMemberString(String memberString) {
        this.memberString = memberString;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
