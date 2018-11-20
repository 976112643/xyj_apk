package com.mikuwxc.autoreply.wchook;


import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.mikuwxc.autoreply.common.net.NetApi;
import com.mikuwxc.autoreply.common.util.AppConfig;
import com.mikuwxc.autoreply.wcutil.Throttle;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;
import okhttp3.Call;
import okhttp3.Response;

/* compiled from: ChatroomChangedHook.kt */
public final class ChatroomChangedHook$hook$3 extends XC_MethodHook {
    final /* synthetic */ Throttle $throttle;

    ChatroomChangedHook$hook$3(Throttle $captured_local_variable$0) {
        this.$throttle = $captured_local_variable$0;
    }

    protected void beforeHookedMethod(@Nullable MethodHookParam param) throws Throwable {
        if (param == null) {
            try {
                Intrinsics.throwNpe();
            } catch (Throwable ex) {
                //XposedBridge.log(ex+ "群组信息修改钩子出现异常3。"+ new Object[0]);
                return;
            }
        }
        String table = (String) param.args[0];
        //XposedBridge.log("table3::"+table);
        if (table == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.String");
        }
        if ((Intrinsics.areEqual((Object) "chatroom", (Object) table) ) == true) {
            String[] params = (String[]) param.args[2];
            if (params == null) {
                throw new TypeCastException("null cannot be cast to non-null type kotlin.Array<kotlin.String>");
            }
            //this.$throttle.call(params[0]);
            for (int i = 0; i < params.length; i++) {
              //  XposedBridge.log("params[0]33::"+params[i]);
            }

        }
    }



}
