package com.mikuwxc.autoreply.callrecorder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CallDateUtils {


    /*
     * 将时间转换为时间戳
     */
    public static String dateToStamp(String s) throws Exception{
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = simpleDateFormat.parse(s);
        long ts = date.getTime();
        res = String.valueOf(ts);
        return res;
    }


    /*
     * 将时间戳转换为时间
     */
    public static String stampToDate(String s){
        String res;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long lt = new Long(s);
        Date date = new Date(lt);
        res = simpleDateFormat.format(date);
        return res;
    }


    public static int getAmrDuration(File file) throws IOException {
        long duration = -1;
        int[] packedSize = { 12, 13, 15, 17, 19, 20, 26, 31, 5, 0, 0, 0, 0, 0,
                0, 0 };
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            // 文件的长度
            long length = file.length();
            // 设置初始位置
            int pos = 6;
            // 初始帧数
            int frameCount = 0;
            int packedPos = -1;
            // 初始数据值
            byte[] datas = new byte[1];
            while (pos <= length) {
                randomAccessFile.seek(pos);
                if (randomAccessFile.read(datas, 0, 1) != 1) {
                    duration = length > 0 ? ((length - 6) / 650) : 0;
                    break;
                }
                packedPos = (datas[0] >> 3) & 0x0F;
                pos += packedSize[packedPos] + 1;
                frameCount++;
            }
            // 帧数*20
            duration += frameCount * 20;
        } finally {
            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
        return (int)((duration/1000)+1);
    }

}
