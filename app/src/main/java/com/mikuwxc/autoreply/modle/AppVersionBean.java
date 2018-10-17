package com.mikuwxc.autoreply.modle;

public class AppVersionBean {
    //版本名
    private String  version;
    //版本号
    private int  versionCode;
    //更新内容
    private String  content;
    //路径
    private String  url;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
