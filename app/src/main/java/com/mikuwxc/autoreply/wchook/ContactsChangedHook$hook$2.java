package com.mikuwxc.autoreply.wchook;

import android.content.ContentValues;

import com.google.gson.Gson;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.common.util.LogUtils;
import com.mikuwxc.autoreply.modle.FriendBean;
import com.mikuwxc.autoreply.modle.HttpBean;
import com.mikuwxc.autoreply.wcentity.UserEntity;
import com.mikuwxc.autoreply.wcentity.WxEntity;
import com.mikuwxc.autoreply.wcutil.Throttle;
import com.mikuwxc.autoreply.wx.WechatDb;
import com.orhanobut.logger.Logger;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Response;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* compiled from: ContactsChangedHook.kt */
public final class ContactsChangedHook$hook$2 extends XC_MethodHook {
    final /* synthetic */ Throttle $throttle;

    ContactsChangedHook$hook$2(Throttle $captured_local_variable$0) {
        this.$throttle = $captured_local_variable$0;
    }

    protected void afterHookedMethod(@Nullable MethodHookParam param) {
        if (param == null) {
            try {
                Intrinsics.throwNpe();
            } catch (Throwable ex) {
                Logger.e(ex, "updateWithOnConflict: 联系人信息修改钩子出现异常。", new Object[0]);
                return;
            }
        }
        String table = (String) param.args[0];
        XposedBridge.log("222222222222222222222table:"+table);
        if (table == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.String");
        }
        Object table2 = table;
        XposedBridge.log("2222222222222222222222table2:"+table2);
        ContentValues values = (ContentValues) param.args[1];
        XposedBridge.log("222222222222222222222values:"+values);
        XposedBridge.log("type::"+values.getAsString("type"));
        if (values == null) {
            throw new TypeCastException("null cannot be cast to non-null type android.content.ContentValues");
        }
        values = values;

        String where = (String) param.args[2];
        XposedBridge.log("2222222222222222222222where:"+where);
        if (where == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.String");
        }
        Object where2 = where;
        XposedBridge.log("22222222222222222222222where2:"+where2);
        String[] params = (String[]) param.args[3];

       // if ((table2.equals("rcontact")/*&&where2.equals("username=?"))&&!table2.equals("rconversation")*/)){

        if ((Intrinsics.areEqual((Object) "rcontact", table2) ^ true) == false || (Intrinsics.areEqual((Object) "username=?", where2) ^ true) == false) {
            String username = values.getAsString("username");
            XposedBridge.log("222222222222222222222username:"+username);
            if (!StringUtils.isBlank(username)) {
                Intrinsics.checkExpressionValueIsNotNull(username, "username");
                if (!StringUtils.startsWith(username, "fake_")&&!StringUtils.endsWith(username, "@stranger")&&!StringUtils.endsWith(username, "@chatroom")&&!StringUtils.endsWith(username, "@qqim")){
                    Set set=new HashSet();
                    set.add(username);



                    List<WxEntity> wxEntities = WechatDb.getInstance().selectContacts(set);
                    XposedBridge.log("wxEntities::::::::"+wxEntities.toString());
                    if (wxEntities.get(0).getOpType()==2){

                        if ("3".equals(values.getAsString("type"))){
                            $throttle.call(username);
                        }
                    }
                }
            }
        }


    }


}
