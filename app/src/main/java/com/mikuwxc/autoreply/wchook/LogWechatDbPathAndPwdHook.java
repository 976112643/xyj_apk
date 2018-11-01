package com.mikuwxc.autoreply.wchook;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.modle.FriendBean;
import com.mikuwxc.autoreply.receiver.Constance;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wcutil.FileIoUtil;
import com.mikuwxc.autoreply.wcutil.GlobalUtil;
import com.mikuwxc.autoreply.wx.AbstractWeChatDb;
import com.mikuwxc.autoreply.wx.WechatDb;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class LogWechatDbPathAndPwdHook {
    private static boolean EnMicroMsgNull=true;
    public static void hook(WechatEntity wechatEntity, LoadPackageParam loadPackageParam, ClassLoader r0, final Context context) throws ClassNotFoundException {
        XposedHelpers.findAndHookMethod(wechatEntity.sqlitedatabase_class_name, loadPackageParam.classLoader, "openDatabase", new Object[]{String.class, byte[].class, r0.loadClass(wechatEntity.sqlitecipherspec_class_name), r0.loadClass(wechatEntity.sqlitedatabase$cursorfactory_class_name), Integer.TYPE, loadPackageParam.classLoader.loadClass(wechatEntity.databaseerrorhandler_class_name), Integer.TYPE, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                String str = (String) methodHookParam.args[0];
                byte[] bArr = (byte[]) methodHookParam.args[1];
                if (str.contains("EnMicroMsg.db")) {
                    AbstractWeChatDb.initWechatDb(methodHookParam.getResult());
                    FileIoUtil.setValueToPath(AbstractWeChatDb.getUniqueNameFromPath(str), false, GlobalUtil.WX_UNIQUENAME_SAVE_PATH);
                    FileIoUtil.setValueToPath("===" + System.currentTimeMillis() + "\npath:[" + str + "]\npwd:[" + new String(bArr) + "]\n", false, "sdcard/wxPwd.txt");
                    XposedBridge.log("===" + System.currentTimeMillis() + "\npath:[" + str + "]\npwd:[" + new String(bArr) + "]\n");


                  /*  if (EnMicroMsgNull){
                        Intent in=new Intent();
                        in.setAction(Constance.action_getWechatDb);
                        in.setClassName(Constance.packageName_wechat,Constance.receiver_wechat);
                        in.putExtra(Constance.dabase_Route,str);
                        in.putExtra(Constance.dabase_Password,new String(bArr));
                        context.sendBroadcast(in);
                        EnMicroMsgNull=false;
                    }*/


                    if (EnMicroMsgNull){  //防止点击重连微信的时候监听到两次
                        Intent in=new Intent();
                        in.setClassName(Constance.packageName_me,Constance.receiver_my);
                        in.setAction(Constance.action_getcpWechatDb);

                        UserEntity userEntity = WechatDb.getInstance().selectSelf();
                        String userName = userEntity.getUserName();
                        String userTalker = userEntity.getUserTalker();
                        String headPic = userEntity.getHeadPic();
                        String alias = userEntity.getAlias();  //微信号
                        XposedBridge.log(alias+userName+userTalker+headPic);     //获取数据库里面的历史数据
                        in.putExtra("wxno",alias);
                        in.putExtra("wxid",userTalker);
                        in.putExtra("headImgUrl",headPic);
                        in.putExtra("userName",userName);
                        List<FriendBean> friendBeans = WechatDb.getInstance().selectContactTree();
                        String friendBeansJson = JSON.toJSONString(friendBeans);
                        //in.putExtra("friendBeans",friendBeansJson);
                        MyFileUtil.writeToNewFile(AppConfig.APP_FILE+"/friendBeans",friendBeansJson);

                        context.sendBroadcast(in);
                        EnMicroMsgNull=false;
                    }


                }
            }
        }});
    }
}