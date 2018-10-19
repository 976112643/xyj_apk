package com.mikuwxc.autoreply.bean;

public class SystemBean {
    private String manufacturer; // 厂商
    private String appVersion;   //app版本
    private String androidVersion; //android版本;
    private String model;               // 型号
    private String phone; //电话号码;
    private Integer patchCode;  //热更新需要版本
    public Integer getPatchCode() {
        return patchCode;
    }

    public void setPatchCode(Integer patchCode) {
        this.patchCode = patchCode;
    }






    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "SystemBean{" +
                "manufacturer='" + manufacturer + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", androidVersion='" + androidVersion + '\'' +
                ", model='" + model + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
