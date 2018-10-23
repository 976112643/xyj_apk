package com.mikuwxc.autoreply.wxmoment;

public class MomentBean {

    private String snsId;
    private String userName;
    private String createTime;
    private String content;
    private String attrBuf;
    private String sourceType;

    public MomentBean() {
    }

    public MomentBean(String snsId, String userName, String createTime, String content, String attrBuf, String sourceType) {
        this.snsId = snsId;
        this.userName = userName;
        this.createTime = createTime;
        this.content = content;
        this.attrBuf = attrBuf;
        this.sourceType = sourceType;
    }

    public String getSnsId() {
        return snsId;
    }

    public void setSnsId(String snsId) {
        this.snsId = snsId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAttrBuf() {
        return attrBuf;
    }

    public void setAttrBuf(String attrBuf) {
        this.attrBuf = attrBuf;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }
}
