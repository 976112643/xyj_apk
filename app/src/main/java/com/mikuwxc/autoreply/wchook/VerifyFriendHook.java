package com.mikuwxc.autoreply.wchook;

import android.os.Handler;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wx.WechatDb;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import okhttp3.Call;
import okhttp3.Response;

public class VerifyFriendHook {
    public static Handler handler = new Handler();

    public static void hook(final WechatEntity wechatEntity, LoadPackageParam param) {
        final ClassLoader wxClassLoader = param.classLoader;
        Class class_auto_verify_user = XposedHelpers.findClass(wechatEntity.verify_callback_class1, wxClassLoader);
        Class class_q = XposedHelpers.findClass(wechatEntity.verify_callback_class2, wxClassLoader);
        XposedHelpers.findAndHookMethod(class_auto_verify_user, wechatEntity.verify_callback_method1, new Object[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, class_q, byte[].class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
               // WxEventBus.publish(new WxVerifyFriend(wechatEntity, wxClassLoader, VerifyFriendHook.handler, param.thisObject, ((Integer) param.args[1]).intValue(), ((Integer) param.args[2]).intValue()));
               XposedBridge.log("接受好友4："+new Gson().toJson(param.args[4]));
                XposedBridge.log("接受好友0："+new Gson().toJson(param.args[0]));
                XposedBridge.log("接受好友1："+new Gson().toJson(param.args[1]));
                XposedBridge.log("接受好友2："+new Gson().toJson(param.args[2]));
                XposedBridge.log("接受好友3："+new Gson().toJson(param.args[3]));
                XposedBridge.log("接受好友5："+new Gson().toJson(param.args[5]));
              /*  Object ecE=XposedHelpers.getObjectField(param.args[4],wechatEntity.add_friend_behavior_method1);
                Object ecN=XposedHelpers.getObjectField(ecE,wechatEntity.add_friend_behavior_method2);
                Object syV=XposedHelpers.getObjectField(ecN,wechatEntity.add_friend_behavior_method3);
                LinkedList tRZ= (LinkedList) XposedHelpers.getObjectField(ecN,wechatEntity.add_friend_behavior_method4);
                Object o=tRZ.get(0);
                Object nFs=XposedHelpers.getObjectField(o,wechatEntity.add_friend_behavior_method5);
                XposedBridge.log("-----------------------------------------------------------");
                XposedBridge.log("ecE:::"+ecE);
                XposedBridge.log("ecN:::"+ecN);
                XposedBridge.log("syV:::"+syV);
                XposedBridge.log(nFs.toString());*/


                if (param.args[3]!=null){
                    //加好友动作
                    UserEntity userEntity = WechatDb.getInstance().selectSelf();
                    String userTalker = userEntity.getUserTalker();
                    String alias = userEntity.getAlias();  //微信号
                    if (StringUtils.isBlank(alias)){
                        alias = userTalker;
                    }
                    XposedBridge.log("加好友动作成功");
                    handleAddFriend(alias,"".toString());

                }else{
                    XposedBridge.log("//加好友动作失败");
                }


               /* if(new Gson().toJson(param.args[3])==null&&syV.equals("3")){
                    //接受好友申请动作
                }*/


            }
        }});
    }


    //同步加好友的动作统计
    private static void handleAddFriend(String wxno,String friendWxId) {
        OkGo.post(AppConfig.OUT_NETWORK+ NetApi.handleAddFriend+"?wxno="+wxno).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                XposedBridge.log("同步加好友的动作成功:"+s);
            }


            @Override
            public void onError(Call call, Response response, Exception e) {
                XposedBridge.log("同步加好友的动作失败"+e.toString());
            }
        });
    }
}
