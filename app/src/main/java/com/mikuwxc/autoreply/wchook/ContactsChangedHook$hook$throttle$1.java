package com.mikuwxc.autoreply.wchook;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.modle.FriendBean;
import com.mikuwxc.autoreply.modle.HttpBean;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wcentity.WxEntity;
import com.mikuwxc.autoreply.wcutil.Throttle;
import com.mikuwxc.autoreply.wx.WechatDb;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Response;

/* compiled from: ContactsChangedHook.kt */
final class ContactsChangedHook$hook$throttle$1<T> implements Throttle.Action<T> {
    public static final ContactsChangedHook$hook$throttle$1 INSTANCE = new ContactsChangedHook$hook$throttle$1();

    ContactsChangedHook$hook$throttle$1() {
    }


    @Override
    public void call(Set<T> set) {
        Set<String> set1= (Set<String>) set;
        Intrinsics.checkExpressionValueIsNotNull(set, "wechatIds");
        // WxEventBus.publish(new WxContactsChanged(set));
        XposedBridge.log("setsetsetsetsetsetset::" + set.toString());

        List<WxEntity> wxEntities = WechatDb.getInstance().selectContacts(set1);
        XposedBridge.log("wxEntities::::::::" + wxEntities.toString());
        if (wxEntities.get(0).getOpType() == 2) {
            UserEntity userEntity = WechatDb.getInstance().selectSelf();
            String userTalker = userEntity.getUserTalker();
            String alias = userEntity.getAlias();  //微信号
            if (StringUtils.isBlank(alias)) {
                alias = userTalker;
            }


            FriendBean friendBean = new FriendBean();
            friendBean.setWxid(wxEntities.get(0).getUserName());
            friendBean.setNickname(wxEntities.get(0).getNickName());
            friendBean.setHeadImgUrl(wxEntities.get(0).getHeadPic());
            friendBean.setCity(wxEntities.get(0).getRegion());
            friendBean.setSex(wxEntities.get(0).getGender());
            friendBean.setPhone(wxEntities.get(0).getPhone());
            friendBean.setAddFrom(wxEntities.get(0).getAddFrom());
            XposedBridge.log(friendBean.toString());
            addNewFriend(alias, friendBean); //新的好友,通知后台
        }

    }

        /**
         * 有新的好友,通知后台
         *
         * @param wxToken
         * @param friend
         */
        private void addNewFriend(String wxToken, FriendBean friend) {
            Gson gson = new Gson();
            String friendStr = gson.toJson(friend);
       /* if (AppConfig.getSelectHost() == null) {
            AppConfig.setHost(AppConfig.OUT_NETWORK);
        }*/
            XposedBridge.log("wxTokenwxTokenwxToken:"+wxToken);
            XposedBridge.log("friendStrfriendStrfriendStr:"+friendStr);
            OkGo.post(AppConfig.OUT_NETWORK + NetApi.addFriend + "/" + wxToken).headers("Content-Type", "application/json").upJson(friendStr).execute(new StringCallback() {
                @Override
                public void onSuccess(String s, Call call, Response response) {
                    XposedBridge.log("添加好友1------------------------------");
                    try {
                        HttpBean bean = new Gson().fromJson(s, HttpBean.class);
                        if (bean.isSuccess()) {
                        } else {
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Call call, Response response, Exception e) {
                    super.onError(call, response, e);
                    XposedBridge.log("添加好友失败");
                }
            });
        }
}
