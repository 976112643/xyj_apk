package com.mikuwxc.autoreply.wchook;


import com.mikuwxc.autoreply.wcutil.Throttle;

import java.util.Set;

import kotlin.jvm.internal.Intrinsics;

/* compiled from: ChatroomChangedHook.kt */
final class ChatroomChangedHook$hook$throttle$1<T> implements Throttle.Action<T> {
    public static final ChatroomChangedHook$hook$throttle$1 INSTANCE = new ChatroomChangedHook$hook$throttle$1();

    ChatroomChangedHook$hook$throttle$1() {
    }


    @Override
    public void call(Set<T> chatroomIds) {
        Intrinsics.checkExpressionValueIsNotNull(chatroomIds, "chatroomIds");
    }
}
