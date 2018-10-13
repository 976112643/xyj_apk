package com.mikuwxc.autoreply.bean;
/**
 *
 * create by : 喻敏航
 * create time : 8018-10-13
 * description : 短信广播监听实体类
 *
 * **/

public class SmsObserverBean {

    private String content;
    private String type;//2发出  1收到
    private String phoneNum;
    private Long time;

    public SmsObserverBean() {
    }

    public SmsObserverBean(String content, String type, String phoneNum, Long time) {
        this.content = content;
        this.type = type;
        this.phoneNum = phoneNum;
        this.time = time;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return "SmsObserverBean{" +
                "content='" + content + '\'' +
                ", type='" + type + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", time=" + time +
                '}';
    }
}
