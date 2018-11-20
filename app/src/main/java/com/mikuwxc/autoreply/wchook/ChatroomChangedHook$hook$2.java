package com.mikuwxc.autoreply.wchook;

import android.content.ContentValues;

import com.mikuwxc.autoreply.wcutil.Throttle;

import org.jetbrains.annotations.Nullable;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import kotlin.TypeCastException;
import kotlin.jvm.internal.Intrinsics;

/* compiled from: ChatroomChangedHook.kt */
public final class ChatroomChangedHook$hook$2 extends XC_MethodHook {
    final /* synthetic */ Throttle $throttle;

    ChatroomChangedHook$hook$2(Throttle $captured_local_variable$0) {
        this.$throttle = $captured_local_variable$0;
    }

    protected void beforeHookedMethod(@Nullable MethodHookParam param) throws Exception {
        if (param == null) {
            try {
                Intrinsics.throwNpe();
            } catch (Throwable ex) {
                XposedBridge.log(ex+"群组信息修改钩子出现异常。"+ new Object[0]);
                return;
            }
        }
        String table = (String) param.args[0];
        //XposedBridge.log("table22::"+table);
        if (table == null) {
            throw new TypeCastException("null cannot be cast to non-null type kotlin.String");
        }
        if ((Intrinsics.areEqual((Object) "chatroom", (Object) table)) == true) {
            ContentValues values = (ContentValues) param.args[1];
            if (values == null) {
                throw new TypeCastException("null cannot be cast to non-null type android.content.ContentValues");
            }
            //this.$throttle.call(values.getAsString("chatroomname"));
      //  XposedBridge.log("values.getAsString(\"chatroomname\")::"+values.getAsString("chatroomname"));
        }
    }
}
