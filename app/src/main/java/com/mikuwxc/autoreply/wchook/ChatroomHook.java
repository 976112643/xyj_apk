package com.mikuwxc.autoreply.wchook;

import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.bean.ChatRoomBean;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.modle.FriendBean;
import com.mikuwxc.autoreply.receiver.Constance;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wcentity.WxEntity;
import com.mikuwxc.autoreply.wx.WechatDb;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import okhttp3.Call;
import okhttp3.Response;

public class ChatroomHook{

    public static void createChatroom(final  ClassLoader classLoader,
                                      final Context context,
                                      final WechatEntity wechat) throws Exception{

        String         name    = "快活呀";
        XposedBridge.log("快活呀");
        String clearList=MyFileUtil.readFromFile(AppConfig.APP_FILE+"/clearList");
        XposedBridge.log("快活呀"+clearList.toString());
        List<FriendBean> wxEntities =new Gson().fromJson(clearList,new TypeToken<List<FriendBean>>(){}.getType());
        XposedBridge.log("快活呀"+wxEntities.size());
        if (wxEntities.isEmpty()){
            return;
        }
        List<String> members=new ArrayList<>();

        Iterator<FriendBean> wxEntityIterator=wxEntities.iterator();
        while (wxEntityIterator.hasNext()&&members.size()<39){
            FriendBean wxEntity=wxEntityIterator.next();
            members.add(wxEntity.getWxid());
            wxEntityIterator.remove();
        }
        XposedBridge.log("membersmembersmembers:"+members);
        String friendsIdListJson = new Gson().toJson(wxEntities);
        MyFileUtil.writeToNewFile(AppConfig.APP_FILE+"/clearList",friendsIdListJson);

//        Map<String, String> chatroomJs = FileUtils.getProperties(context, PathUtils.getChatroomJs());
//        String       name        = chatroomJs.get("name");
//        String       membersJson = chatroomJs.get("members");
//        List<String> members     = GsonUtils.deserialize(membersJson, new TypeToken<List<String>>(){}.getType());

        Object object1 = XposedHelpers.callStaticMethod(classLoader.loadClass(wechat.create_chatroom_class1), wechat.create_chatroom_method1, new Object[0]);
        Object object2 = XposedHelpers.newInstance(classLoader.loadClass(wechat.create_chatroom_class2), new Object[]{name, members});

        XposedHelpers.callMethod(object1, wechat.create_chatroom_method2, new Object[]{object2, Integer.valueOf(0)});

        //删除建群信息
       // FileUtils.delete(context, PathUtils.getChatroomJs());
    }

    public static void hook(final XC_LoadPackage.LoadPackageParam loadPackageParam,
                            final Context context,
                            final WechatEntity wechat) throws Exception{

        final ClassLoader classLoader = loadPackageParam.classLoader;
        final Class       clazz       = XposedHelpers.findClass("com.tencent.mm.network.q", classLoader);

        XposedHelpers.findAndHookMethod(wechat.create_chatroom_class2, classLoader, wechat.create_chatroom_method2,
                                        new Object[]{Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class,
                                                     clazz, byte[].class, new XC_MethodHook(){
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable{

                Object     object     = XposedHelpers.getObjectField(param.thisObject, wechat.refreash_create_chatroom_field1);
                Object     object2    = XposedHelpers.getObjectField(object, wechat.refreash_create_chatroom_field2);
                Object     object3    = XposedHelpers.getObjectField(object2, wechat.refreash_create_chatroom_field3);
                Object     object4    = XposedHelpers.getObjectField(object3, wechat.refreash_create_chatroom_field4);
                String     chatroomId = (String) XposedHelpers.getObjectField(object4, wechat.refreash_create_chatroom_field5);
                LinkedList wechatIds  = (LinkedList) XposedHelpers.getObjectField(object3, wechat.refreash_create_chatroom_field6);

                List<String> members = new ArrayList<String>();

                Iterator iterator = wechatIds.iterator();
                while(iterator.hasNext()){
                    Object it = iterator.next();

                    Object  object5 = XposedHelpers.getObjectField(it, wechat.refreash_create_chatroom_field7);
                    Integer object6 = (Integer) XposedHelpers.getObjectField(it, wechat.refreash_create_chatroom_field9);
                    String  id      = (String) XposedHelpers.getObjectField(object5, wechat.refreash_create_chatroom_field8);
                    XposedBridge.log("object5:"+object5+"object6:"+object6+"id:"+id);
                    if (object6==4){
                        members.add(id);
                    }

                    //4:非好友,3:不能加入群
                   // WxBroadcastSender.msg(context,id + " : " + String.valueOf(object6));
                }
                XposedBridge.log("membersmembersmembers::"+members.toString());
                if (!members.isEmpty()){
                    UserEntity userEntity = WechatDb.getInstance().selectSelf();
                    String alias=userEntity.getAlias();
                    if (StringUtils.isBlank(alias)){
                        alias=userEntity.getUserName();
                    }


                    XposedBridge.log("僵尸粉：："+new Gson().toJson(members));

                   // handleMessageCreateChatroom()
                }
                Intent intent=new Intent();
                intent.putExtra("name","name");
                intent.putExtra("type","203");
                intent.setAction(Constance.action_getWechatFriends);
                intent.setClassName(Constance.packageName_wechat,Constance.receiver_wechat);
                context.sendBroadcast(intent);


             /*  Class clazz2 = XposedHelpers.findClass(wechat.refreash_create_chatroom_class1, classLoader);
                XposedHelpers.callStaticMethod(clazz2, wechat.refreash_create_chatroom_method1, new Object[]{chatroomId, members, "你邀请%s加入了群聊", Boolean.valueOf(false), ""});*/
            }
        }});
    }


    private void handleMessageCreateChatroom(ChatRoomBean chatRoomBean) {
        OkGo.post(AppConfig.OUT_NETWORK+ NetApi.clearFriends).upJson(new Gson().toJson(chatRoomBean)).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                XposedBridge.log("sssssss");
            }


            @Override
            public void onError(Call call, Response response, Exception e) {
                XposedBridge.log("sssssss"+e.toString());
            }
        });
    }
}
