package com.mikuwxc.autoreply.bean;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ApphttpBean {

    /**
     * msg : 查询成功
     * code : 200
     * success : true
     * result : [{"name":"超级宇宙无敌App","packageName":"com.superman","type":true,"createTime":1535612398000,"updateTime":1535688761000,"addDevice":1,"id":"b142b6774a9f4cc28d83806438ee3c86"},{"name":"计算器","packageName":"com.miui.calculator","type":false,"createTime":1535612600000,"updateTime":1535612600000,"addDevice":1,"id":"16e6fd7187bd4c81a55bdba5127fa7b8"}]
     */

    private String msg;
    private String code;
    private boolean success;
    private List<ResultBean> result;

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

    public List<ResultBean> getResult() {
        return result;
    }

    public void setResult(List<ResultBean> result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * name : 超级宇宙无敌App
         * packageName : com.superman
         * type : true
         * createTime : 1535612398000
         * updateTime : 1535688761000
         * addDevice : 1
         * id : b142b6774a9f4cc28d83806438ee3c86
         */

        private String name;
        private String packageName;
        private boolean type;
        private long createTime;
        private long updateTime;
        private int addDevice;
        private String id;

        public ResultBean(String packageName){
            this.packageName = packageName;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public boolean isType() {
            return type;
        }

        public void setType(boolean type) {
            this.type = type;
        }

        public long getCreateTime() {
            return createTime;
        }

        public void setCreateTime(long createTime) {
            this.createTime = createTime;
        }

        public long getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(long updateTime) {
            this.updateTime = updateTime;
        }

        public int getAddDevice() {
            return addDevice;
        }

        public void setAddDevice(int addDevice) {
            this.addDevice = addDevice;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object object){
            if (this == object) {
                return true;
            }

            if(object != null &&  this.getClass() == object.getClass()){
                ResultBean entity = (ResultBean) object;

                return this.packageName.equals(entity.packageName);
            }

            return false;
        }
    }
}
