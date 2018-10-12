package com.mikuwxc.autoreply.bean;

public class ImLoginBean {

    /**
     * msg : 查询成功
     * code : 200
     * success : true
     * result : {"relationId":"0","name":"hsl_test","nickname":"NA","sig":"eJxFj11vgjAUhv8Lty7bKW1xLPFiGlCcZHMjSnbTEFq0FispZR8u***yZsTL8z55c573x8tWb7dF00jOCsuw4d6DB96Ni8VXI41gRWWF6WNEKfUBBvohTCtPugc*IIp8DHCFkgttZSVdcQhbueuvNFrPknnXqXGk8mgbxF1bBRzuFjXwtp5jLvIlWapRmq7Ocbx5fZTTWpX43RyJMOfPp4We*tlhrzRkow3Nq*Q5fMGoJgeE8mQ9mQzPuGJu15856c2CkI7Df2jlUbhFGN0jQshgWJTlqdOW2e9GOPHfC4ERVPE_","sdkAppId":"1400069579","type":"1","createTime":1531814441000,"id":"afcbd96566004b2a844e03515125fc06"}
     */

    private String msg;
    private String code;
    private boolean success;
    private ResultBean result;






    public boolean isLuckPackage() {
        return luckPackage;
    }

    public void setLuckPackage(boolean luckPackage) {
        this.luckPackage = luckPackage;
    }

    private boolean luckPackage;

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
        return "ImLoginBean{" +
                "msg='" + msg + '\'' +
                ", code='" + code + '\'' +
                ", success=" + success +
                ", result=" + result +
                ", luckPackage=" + luckPackage +
                '}';
    }

    public static class ResultBean {
        /**
         * relationId : 0
         * name : hsl_test
         * nickname : NA
         * sig : eJxFj11vgjAUhv8Lty7bKW1xLPFiGlCcZHMjSnbTEFq0FispZR8u***yZsTL8z55c573x8tWb7dF00jOCsuw4d6DB96Ni8VXI41gRWWF6WNEKfUBBvohTCtPugc*IIp8DHCFkgttZSVdcQhbueuvNFrPknnXqXGk8mgbxF1bBRzuFjXwtp5jLvIlWapRmq7Ocbx5fZTTWpX43RyJMOfPp4We*tlhrzRkow3Nq*Q5fMGoJgeE8mQ9mQzPuGJu15856c2CkI7Df2jlUbhFGN0jQshgWJTlqdOW2e9GOPHfC4ERVPE_
         * sdkAppId : 1400069579
         * type : 1
         * createTime : 1531814441000
         * id : afcbd96566004b2a844e03515125fc06
         */

        private String relationId;
        private String name;
        private String nickname;
        private String sig;
        private String sdkAppId;
        private String type;
        private long createTime;
        private String id;
        private String wordsNotice; //通知敏感词
        private String wordsIntercept; //拦截
        private boolean luckyPackage;//是否自动抢红包
        private boolean passNewFriend;  //是否自动通过好友添加
        private boolean showWxno;       //是否能看好友微信号
        private boolean  scan;               //是否打开扫一扫
        private boolean  setting;       // 设置
        public boolean isSetting() {
            return setting;
        }

        public void setSetting(boolean setting) {
            this.setting = setting;
        }




        public boolean isScan() {
            return scan;
        }

        public void setScan(boolean scan) {
            this.scan = scan;
        }



        public boolean isShowWxno() {
            return showWxno;
        }





        public void setShowWxno(boolean showWxno) {
            this.showWxno = showWxno;
        }




        public boolean isLuckyPackage() {
            return luckyPackage;
        }

        public void setLuckyPackage(boolean luckyPackage) {
            this.luckyPackage = luckyPackage;
        }

        public boolean isPassNewFriend() {
            return passNewFriend;
        }

        public void setPassNewFriend(boolean passNewFriend) {
            this.passNewFriend = passNewFriend;
        }



        public String getWordsNotice() {
            return wordsNotice;
        }

        public void setWordsNotice(String wordsNotice) {
            this.wordsNotice = wordsNotice;
        }

        public String getWordsIntercept() {
            return wordsIntercept;
        }

        public void setWordsIntercept(String wordsIntercept) {
            this.wordsIntercept = wordsIntercept;
        }



        public String getRelationId() {
            return relationId;
        }

        public void setRelationId(String relationId) {
            this.relationId = relationId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getSig() {
            return sig;
        }

        public void setSig(String sig) {
            this.sig = sig;
        }

        public String getSdkAppId() {
            return sdkAppId;
        }

        public void setSdkAppId(String sdkAppId) {
            this.sdkAppId = sdkAppId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
