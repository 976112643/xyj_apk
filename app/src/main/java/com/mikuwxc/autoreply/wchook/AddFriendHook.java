package com.mikuwxc.autoreply.wchook;

import android.content.Context;

import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.utils.IntentUtil;
import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wcutil.FriendUtil;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Response;

public class AddFriendHook {

    private static Object localObject1;

    public static void hook(final WechatEntity wechatEntity, LoadPackageParam param, final Context context) {
            final ClassLoader wxClassLoader = param.classLoader;

            XposedHelpers.findAndHookMethod(wechatEntity.add_search_friend_class2, wxClassLoader, wechatEntity.add_search_friend_method3, new Object[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, XposedHelpers.findClass(wechatEntity.add_search_friend_class3, wxClassLoader), byte[].class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("===== searchFriend 222"+ new Object[0]);
                    Object localObject3;
                    Object localObject2;
                    String addContent = MyFileUtil.readFromFile(AppConfig.APP_ADD);
                    String addNo=MyFileUtil.readNewProperties("addNo",AppConfig.AddFriend);
                    String addType=MyFileUtil.readNewProperties("addType",AppConfig.AddFriend);
                    String addRemark=MyFileUtil.readNewProperties("addRemark",AppConfig.AddFriend);
                    String addId=MyFileUtil.readNewProperties("addId",AppConfig.AddFriend);
                    if (!"0".equals(addType)) {
                        if (Intrinsics.areEqual((Object) "Everything is OK", param.args[3].toString())) {
                            localObject3 = XposedHelpers.callMethod(param.thisObject, wechatEntity.add_search_friend_method4, new Object[0]);
                            localObject1 = XposedHelpers.getObjectField(localObject3, wechatEntity.add_search_friend_field1);
                            localObject2 = XposedHelpers.getObjectField(localObject3, wechatEntity.add_search_friend_field2);
                            Object paramAnonymousMethodHookParam1 = XposedHelpers.getObjectField(localObject3, wechatEntity.add_search_friend_field3);
                            Object localObject4 = XposedHelpers.getObjectField(XposedHelpers.getObjectField(localObject3, wechatEntity.add_search_friend_field4), wechatEntity.add_search_friend_field5);
                            if (String.valueOf(localObject1).endsWith("@stranger")) {
                                if (addContent != null) {
                                    FriendUtil.addFriendWithUpdateRemark(wxClassLoader, wechatEntity, localObject1.toString(), addRemark, addNo, "", 15);
                                    FriendUtil.addFriend11(wxClassLoader, wechatEntity, localObject1.toString(), (String) localObject2, Integer.parseInt(addType));   //15 是通过微信号加好友*/
                                    XposedBridge.log("addRemark::" + addRemark);
                                } else {
                                    XposedBridge.log("需要添加的好友已经存在");
                                }
                            }else{
                                IntentUtil.startAddFriendBroadcastReceiver(context);
                                writeNewProperties();
                                XposedBridge.log("需要添加的好友已经存在:[" + param.args[3].toString() + "]");
                                handleAddFriendRecode(addId,param.args[3].toString());
                                XposedBridge.log("error1:arg0[" + param.args[0].toString() + "]" + "arg1[" + param.args[1].toString() + "]" + "arg2[" + param.args[2].toString() + "]" + "arg3[" + param.args[3].toString() + "]");
                            }
                        }else{
                            IntentUtil.startAddFriendBroadcastReceiver(context);
                            writeNewProperties();
                            XposedBridge.log("微信返回1args:[" + param.args[3].toString() + "]");
                            handleAddFriendRecode(addId,param.args[3].toString());
                            XposedBridge.log("error2:arg0[" + param.args[0].toString() + "]" + "arg1[" + param.args[1].toString() + "]" + "arg2[" + param.args[2].toString() + "]" + "arg3[" + param.args[3].toString() + "]");

                        }

                    }
                }
            }});
            XposedHelpers.findAndHookMethod(XposedHelpers.findClass(wechatEntity.add_search_friend_class4, wxClassLoader), wechatEntity.add_search_friend_method5, new Object[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, XposedHelpers.findClass(wechatEntity.add_search_friend_class3, wxClassLoader), byte[].class, new XC_MethodHook() {
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("===== searchFriend 444"+Integer.parseInt(param.args[1].toString()));
                    String addContent = MyFileUtil.readFromFile(AppConfig.APP_ADD);
                    String addNo=MyFileUtil.readNewProperties("addNo",AppConfig.AddFriend);
                    String addType=MyFileUtil.readNewProperties("addType",AppConfig.AddFriend);
                    String addMsg=MyFileUtil.readNewProperties("addMsg",AppConfig.AddFriend);
                    String addId=MyFileUtil.readNewProperties("addId",AppConfig.AddFriend);
                    if (!"0".equals(addType)) {
                        if (Integer.parseInt(param.args[1].toString()) == 4 && Intrinsics.areEqual((Object) "user need verify", param.args[3].toString())) {
                            if (addContent != null) {
                                FriendUtil.addFriend12(wxClassLoader, wechatEntity, localObject1.toString(), addMsg, Integer.parseInt(addType));   //15 是通过微信号加好友*/
                                XposedBridge.log("addNo" + addNo + "addType::" + addType);
                                IntentUtil.startAddFriendBroadcastReceiver(context);
                                writeNewProperties();
                            } else {
                            }
                        } else if (Integer.parseInt(param.args[1].toString()) == 0 && Integer.parseInt(param.args[2].toString()) == 0) {
                            IntentUtil.startAddFriendBroadcastReceiver(context);
                        } else {
                            XposedBridge.log("加好友"+param.args[0].toString());
                            IntentUtil.startAddFriendBroadcastReceiver(context);
                        }
                        handleAddFriendRecode(addId,param.args[3].toString());
                        XposedBridge.log("error3:arg0[" + param.args[0].toString() + "]" + "arg1[" + param.args[1].toString() + "]" + "arg2[" + param.args[2].toString() + "]" + "arg3[" + param.args[3].toString() + "]");
                    }
                }

            }});
        }



        private static void writeNewProperties(){
            MyFileUtil.writeNewProperties("addType", "0", AppConfig.AddFriend);
            MyFileUtil.writeNewProperties("addMsg", "", AppConfig.AddFriend);
            MyFileUtil.writeNewProperties("addNo", "", AppConfig.AddFriend);
            MyFileUtil.writeNewProperties("addRemark", "", AppConfig.AddFriend);
            MyFileUtil.writeNewProperties("addId", "", AppConfig.AddFriend);
        }


         private static void handleAddFriendRecode(String id,String message) {
            OkGo.put(AppConfig.OUT_NETWORK+ NetApi.addFriendMessage+id+"?"+"content="+message).execute(new StringCallback() {
             @Override
                public void onSuccess(String s, Call call, Response response) {
                    XposedBridge.log("sssssss"+s);
             }


             @Override
             public void onError(Call call, Response response, Exception e) {
                 XposedBridge.log("sssssss"+e.toString());
             }
             });
         }


}