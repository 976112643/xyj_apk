package com.mikuwxc.autoreply.bean;

import java.util.List;

/**
 *
 * create by : 喻敏航
 * create time : 8018-10-16
 * description : 写入配置文件的实体bean对象
 *
 * **/
public class SharePerSmsBean {

    /**
     * content : sms
     * data : [{"content":"短信内容","type":"true","phone":"13812121212","time":"145645554695"}]
     */

    private String content;
    private List<DataBean> data;

    public SharePerSmsBean() {
    }

    public SharePerSmsBean(String content, List<DataBean> data) {
        this.content = content;
        this.data = data;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * content : 短信内容
         * type : true
         * phone : 13798650574
         * time : 14564555
         */

        private String content;
        private String type;
        private String phone;
        private String time;

        public DataBean() {
        }

        public DataBean(String content, String type, String phone, String time) {
            this.content = content;
            this.type = type;
            this.phone = phone;
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

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }
    }
}
