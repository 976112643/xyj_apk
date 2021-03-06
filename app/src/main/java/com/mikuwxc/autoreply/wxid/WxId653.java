package com.mikuwxc.autoreply.wxid;

/**
 * Created by Administrator on 2017/2/14.
 */
public class WxId653 extends WxId {
    public static void init() {
        /**
         * 添加好友操作
         */
        newfriendId = "com.tencent.mm:id/arn";//新的朋友Id
        searchListId = "com.tencent.mm:id/gn";//微信号/QQ号/手机号 Id
        searchEtId = "com.tencent.mm:id/gn";//搜索用户编辑框
        searchfriendId = "com.tencent.mm:id/auh";//搜索输入内容的按钮
        addfriendId = "com.tencent.mm:id/abg";//添加到通讯录
        permissionEtId = "com.tencent.mm:id/c5r";//验证消息编辑框
        sendPermissionId = "com.tencent.mm:id/g_";//发送加好友申请
        addFriendRightTop = "com.tencent.mm:id/g9";//右上角的添加好友按钮Id
        clearEditId = "com.tencent.mm:id/go";//输入微信号/QQ号/手机号框的清空按钮
        noUserId = "com.tencent.mm:id/au_";//该用户不存在
        phoneAddressBook = "com.tencent.mm:id/a97";//手机通讯录

        /**
         * 发朋友圈需要操作的控件
         */
        friendCircleEditCompleted = "com.tencent.mm:id/g_";//右上角发送按钮
        friendCircleComment = "com.tencent.mm:id/cew";//朋友圈右下角评论按钮
        friendCircleComment1 = "com.tencent.mm:id/cev";//点击评论后弹出的评论按钮
        friendCirclePraise = "com.tencent.mm:id/ceq";//点击评论后弹出的点赞按钮
        friendCircleCommentEditId = "com.tencent.mm:id/cfb";//评论编辑框Id
        friendCircleCommentSendId = "com.tencent.mm:id/cfd";//评论发送ID
        friendCircleListId = "com.tencent.mm:id/cik";//朋友圈listviewId
        friendCircleLocation = "com.tencent.mm:id/b9m";//所在位置
        friendCricleLocationSearch = "com.tencent.mm:id/aq";//位置搜索右上角搜索按钮
        locationItem = "com.tencent.mm:id/bqr";//首页地址item Id
        searchLocationEditId = "com.tencent.mm:id/gn";//输入位置编辑框
        searchId = "com.tencent.mm:id/gp";//搜索  按钮
        locationList = "com.tencent.mm:id/br6";//搜索到的地址的ListView Id
        createNewLocation = "com.tencent.mm:id/ae2";//没有找到你的位置？
        createLocationDetail = "com.tencent.mm:id/bqv";//详细地址
        createNewLocationComplete = "com.tencent.mm:id/g_";//创建新位置  完成  按钮
        progressFriendCircle = "com.tencent.mm:id/cjy";//彩色进度条加载

        /**
         * 获取通讯录列表的控件
         */
        addressBookListId = "com.tencent.mm:id/hn";//通讯录的listview的Id
        wechatNum = "com.tencent.mm:id/abn";//微信号
        wechatNick = "com.tencent.mm:id/abx";//微信昵称
        wechatRemark = "com.tencent.mm:id/l5";//备注
        wechatPhone = "com.tencent.mm:id/ac4";//手机号 FrameLayout


        /**
         * 自动回复的相关控件
         */
        chatListId = "com.tencent.mm:id/big";//聊天联系人列表 listview
        chatUserItemId = "com.tencent.mm:id/aeb";//每个联系人的item的id
        chatContentListId = "com.tencent.mm:id/a25";//聊天页面 聊天内容 listview
        chatContentTextId = "com.tencent.mm:id/ib";//聊天页面 文本信息的 item 的id
        qunId = "com.tencent.mm:id/g1";//当前左上角聊天对象名称 id
        editId = "com.tencent.mm:id/a27";//聊天页面 输入框id
        sendId = "com.tencent.mm:id/a2c";//发送按钮 id
        gongzhonghaoId = "com.tencent.mm:id/o";//公众号的消息 item
        tengxunxinwenId = "android:id/text1";//腾讯新闻的消息 item

        /**
         * 互聊功能的相关控件
         */
        huliao_searchId = "com.tencent.mm:id/gn";//搜索框Id
        huliao_searchItemId = "com.tencent.mm:id/auh";
        huliao_searchIv = "com.tencent.mm:id/av1";//查找手机/QQ号/微信号
        huliao_addFriend = "com.tencent.mm:id/abv";//添加到通讯录
        huliao_sendMessage = "com.tencent.mm:id/abh";//发消息Id
        huliao_noUser = "com.tencent.mm:id/blw";//用户不存在

        permissionEtId = "com.tencent.mm:id/c8w";//验证消息编辑框
        sendPermissionId = "com.tencent.mm:id/fx";//发送加好友申请
        /**
         * 通过好友验证
         */
        receiveFriend = "com.tencent.mm:id/aso";//接受
        permissionTv = "com.tencent.mm:id/c9v";
        completeAdd = "com.tencent.mm:id/gd";//完成
        communicateId = "com.tencent.mm:id/abw";//同意加好友后打招呼按钮id
    }
}
