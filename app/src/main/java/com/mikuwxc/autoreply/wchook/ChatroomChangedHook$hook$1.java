package com.mikuwxc.autoreply.wchook;

import android.content.ContentValues;


import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.bean.ChatRoomBean;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.MyFileUtil;
import com.mikuwxc.autoreply.wcentity.ChatroomEntity;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wcutil.Throttle;
import com.mikuwxc.autoreply.wx.WechatDb;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Response;

/* compiled from: ChatroomChangedHook.kt */
public final class ChatroomChangedHook$hook$1 extends XC_MethodHook {
    final /* synthetic */ Throttle $throttle;

    ChatroomChangedHook$hook$1(Throttle $captured_local_variable$0) {
        this.$throttle = $captured_local_variable$0;
    }

    protected void afterHookedMethod(@Nullable MethodHookParam param) {
        if (param == null) {
            try {
                Intrinsics.throwNpe();
            } catch (Throwable ex) {
                XposedBridge.log(ex+"群组信息修改钩子出现异常。"+ new Object[0]);
                return;
            }
        }
        String table = (String) param.args[0];
        if ("DeletedConversationInfo".equals(table)){

        }


        if (table == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.String");
        }

        if ((Intrinsics.areEqual((Object) "chatroom", (Object) table))) {
            ContentValues values = (ContentValues) param.args[2];
            String rowid=values.getAsString("rowid");
            if (StringUtils.isBlank(rowid)){
                XposedBridge.log("values111:::"+values+"chatroomname::"+values.getAsString("chatroomname"));
                ChatRoomBean chatRoomBean=new ChatRoomBean();
                chatRoomBean.setRoomid(values.getAsString("chatroomname"));
                chatRoomBean.setName(values.getAsString("displayname"));
                chatRoomBean.setMemberString(values.getAsString("memberlist"));
                chatRoomBean.setOwner(values.getAsString("roomowner"));
                chatRoomBean.setWechatId(getToken());


                Set set=new HashSet();
                set.add(values.getAsString("chatroomname"));
                List<ChatroomEntity> list = WechatDb.getInstance().selectChatroomss(set);
                XposedBridge.log("创建群查找群资料和信息：："+list.toString());

                UserEntity userEntity = WechatDb.getInstance().selectSelf();
                String alias=userEntity.getAlias();
                if (StringUtils.isBlank(alias)){
                    alias=userEntity.getUserTalker();
                }

                handleMessageCreateChatroom(alias,new Gson().toJson(list.get(0)));


            }else{
                XposedBridge.log("rowidrowidrowidrowid::"+rowid);
            }
            if (values == null) {
                throw new TypeCastException("null cannot be cast to non-null type android.content.ContentValues");
            }
           // this.$throttle.call(values.getAsString("chatroomname"));

        }else {
            ContentValues values = (ContentValues) param.args[2];
          //  XposedBridge.log("values不认识的好友近来:::"+values+"chatroomname::"+values.getAsString("chatroomname"));
        }
    }



    private void handleMessageCreateChatroom(String wxno,String chatroomEntitiesJson) {
        OkGo.post(AppConfig.OUT_NETWORK+ NetApi.createchatroom+"/"+wxno).upJson(chatroomEntitiesJson).execute(new StringCallback() {
            @Override
            public void onSuccess(String s, Call call, Response response) {
                XposedBridge.log("同步创建群的消息成功："+s);
            }


            @Override
            public void onError(Call call, Response response, Exception e) {
                XposedBridge.log("同步创建群的消息失败"+e.toString());
            }
        });
    }



    private String getToken() {
          String  token = MyFileUtil.readFromFile(AppConfig.APP_FOLDER + "/token");
            token = token.substring(1, token.length() - 1);
            return token;
    }
}
