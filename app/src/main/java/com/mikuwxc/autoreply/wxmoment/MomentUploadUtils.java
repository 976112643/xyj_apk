package com.mikuwxc.autoreply.wxmoment;

import com.alibaba.fastjson.JSON;
import com.mikuwxc.autoreply.wxmoment.model.SnsInfo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;


public class MomentUploadUtils {


    public static void getAllJson(){
        String json = getFileFromSD(Config.EXT_DIR+"/all_sns.json");//所有的数据
        List<SnsInfo> snsInfos = JSON.parseArray(json, SnsInfo.class);//解析所有的数据



    }





    /**从sd卡获取json**/
    private static String getFileFromSD(String path) {
        String result = "";

        try {
            FileInputStream f = new FileInputStream(path);
            BufferedReader bis = new BufferedReader(new InputStreamReader(f));
            String line = "";
            while ((line = bis.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;

    }


}
