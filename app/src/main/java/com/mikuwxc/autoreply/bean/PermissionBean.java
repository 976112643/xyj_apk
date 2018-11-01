package com.mikuwxc.autoreply.bean;

import java.util.List;

public class PermissionBean {
    /**
     * msg : 查询成功
     * code : 200
     * success : true
     * result : {"companyId":"077492fef8da40ed8ba4ea16c9022ef5","phone":"null","imei":"2382c47c9805","manufacturer":"xiaomi","appVersion":"1.06","patchCode":1,"androidVersion":"7.1.2","model":"Redmi 5 Plus","createTime":1540981731000,"updateTime":1541044447000,"setting":false,"groups":[],"id":"45bc94c903194a839807e3462f871372"}
     */

    private String msg;
    private String code;
    private boolean success;
    private ResultBean result;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "PermissionBean{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                ", success=" + success +
                ", result=" + result +
                '}';
    }

    public static class ResultBean {
        private boolean setting;


        public boolean isSetting() {
            return setting;
        }

        public void setSetting(boolean setting) {
            this.setting = setting;
        }


        @Override
        public String toString() {
            return "ResultBean{" +
                    "setting=" + setting +
                    '}';
        }
    }
}
