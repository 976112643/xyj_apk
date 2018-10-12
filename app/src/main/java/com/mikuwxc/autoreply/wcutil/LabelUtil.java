package com.mikuwxc.autoreply.wcutil;


import com.mikuwxc.autoreply.wcentity.WechatEntity;
import com.mikuwxc.autoreply.wx.WechatDb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedHelpers;

public class LabelUtil {
    public static void addLabel(ClassLoader classLoader, WechatEntity wechatEntity, String str) throws Exception {
        Class loadClass = classLoader.loadClass(wechatEntity.manager_add_label_class1);
        Object callStaticMethod = XposedHelpers.callStaticMethod(loadClass, wechatEntity.manager_add_label_method1, new Object[0]);
        Object newInstance = XposedHelpers.newInstance(classLoader.loadClass(wechatEntity.manager_add_label_class2), new Object[]{str});
        XposedHelpers.callMethod(callStaticMethod, wechatEntity.manager_add_label_method2, new Object[]{newInstance, Integer.valueOf(0)});
        refreashLabel(classLoader, wechatEntity, loadClass, callStaticMethod);
    }

    public static void delLabel(ClassLoader classLoader, WechatEntity wechatEntity, String str) throws Exception {
        Class loadClass = classLoader.loadClass(wechatEntity.manager_del_label_class1);
        Object callStaticMethod = XposedHelpers.callStaticMethod(loadClass, wechatEntity.manager_del_label_method1, new Object[0]);
        Object newInstance = XposedHelpers.newInstance(classLoader.loadClass(wechatEntity.manager_del_label_class2), new Object[]{str});
        XposedHelpers.callMethod(callStaticMethod, wechatEntity.manager_del_label_method2, new Object[]{newInstance, Integer.valueOf(0)});
        refreashLabel(classLoader, wechatEntity, loadClass, callStaticMethod);
    }

    private static void refreashLabel(ClassLoader classLoader, final WechatEntity wechatEntity, Class<?> cls, final Object obj) throws Exception {
        Class loadClass = classLoader.loadClass(wechatEntity.manager_refreash_label_class1);
        Object callStaticMethod = XposedHelpers.callStaticMethod(cls, wechatEntity.manager_refreash_label_method1, new Object[0]);
        final Object newInstance = XposedHelpers.newInstance(loadClass, new Object[0]);
        Runnable runnable = new Runnable() {
            public void run() {
                XposedHelpers.callMethod(obj, wechatEntity.manager_refreash_label_method2, new Object[]{newInstance, Integer.valueOf(0)});
            }
        };
        XposedHelpers.callMethod(callStaticMethod, wechatEntity.manager_refreash_label_method3, new Object[]{runnable});
    }

    public static void updateContactLabel(ClassLoader classLoader, WechatEntity wechatEntity, String str, List<String> list) throws Exception {
        new ArrayList().addAll(list);
        Class loadClass = classLoader.loadClass(wechatEntity.manager_update_contact_label_class1);
        ArrayList arrayList = (ArrayList) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(classLoader.loadClass(wechatEntity.manager_update_contact_label_class2), wechatEntity.manager_update_contact_label_method1, new Object[0]), wechatEntity.manager_update_contact_label_method2, new Object[]{loadClass});
        String obj = XposedHelpers.callStaticMethod(loadClass, wechatEntity.manager_update_contact_label_method3, new Object[]{arrayList}).toString();
        Object newInstance = classLoader.loadClass(wechatEntity.manager_update_contact_label_class3).newInstance();
        XposedHelpers.setObjectField(newInstance, wechatEntity.manager_update_contact_label_field1, obj);
        XposedHelpers.setObjectField(newInstance, wechatEntity.manager_update_contact_label_field2, str);
        Class loadClass2 = classLoader.loadClass(wechatEntity.manager_update_contact_label_class4);
        new LinkedList().add(newInstance);
        Object newInstance2 = XposedHelpers.newInstance(loadClass2, new Object[]{loadClass2});
        XposedHelpers.callMethod(XposedHelpers.callStaticMethod(classLoader.loadClass(wechatEntity.manager_update_contact_label_class5), wechatEntity.manager_update_contact_label_method6, new Object[0]), wechatEntity.manager_update_contact_label_method7, new Object[]{newInstance2, Integer.valueOf(0)});
    }

    public static void updateContactLabel(ClassLoader pluginLoader, WechatEntity wechatEntity, String wechatId, List<String> labelList, boolean isCustom) throws Exception {
        for (int i = 0; i < labelList.size(); i++) {
            boolean isAddNewLable = false;
            HashMap<String, String> wxLabelTree = WechatDb.getInstance().selectLabel();
            LinkedList<String> wxLabelNameList = new LinkedList();
            for (Map.Entry<String, String> entry : wxLabelTree.entrySet()) {
                wxLabelNameList.add((String) entry.getValue());
            }
            for (String labelName : labelList) {
                boolean isInWx = false;
                Iterator it = wxLabelNameList.iterator();
                while (it.hasNext()) {
                    if (((String) it.next()).equals(labelName)) {
                        isInWx = true;
                        break;
                    }
                }
                if (!isInWx) {
                    addLabel(pluginLoader, wechatEntity, labelName);
                    isAddNewLable = true;
                }
            }
            Thread.sleep(1000);
            if (!isAddNewLable) {
                break;
            }
        }
        ArrayList labelNameList=new ArrayList();
        labelNameList.addAll(labelList);
        Class<?> class1 = pluginLoader.loadClass(wechatEntity.manager_update_contact_label_class1);
        ArrayList<String> res = (ArrayList) XposedHelpers.callMethod(XposedHelpers.callStaticMethod(pluginLoader.loadClass(wechatEntity.manager_update_contact_label_class2), wechatEntity.manager_update_contact_label_method1, new Object[0]), wechatEntity.manager_update_contact_label_method2, new Object[]{labelNameList});
        String str = XposedHelpers.callStaticMethod(class1, wechatEntity.manager_update_contact_label_method3, new Object[]{res}).toString();
        Object object3 = pluginLoader.loadClass(wechatEntity.manager_update_contact_label_class3).newInstance();
        XposedHelpers.setObjectField(object3, wechatEntity.manager_update_contact_label_field1, str);
        XposedHelpers.setObjectField(object3, wechatEntity.manager_update_contact_label_field2, wechatId);
        Class<?> class4 = pluginLoader.loadClass(wechatEntity.manager_update_contact_label_class4);
        LinkedList linkedList=new LinkedList();
        labelList.add((String) object3);
        Object object4 = XposedHelpers.newInstance(class4, new Object[]{linkedList});
        XposedHelpers.callMethod(XposedHelpers.callStaticMethod(pluginLoader.loadClass(wechatEntity.manager_update_contact_label_class5), wechatEntity.manager_update_contact_label_method6, new Object[0]), wechatEntity.manager_update_contact_label_method7, new Object[]{object4, Integer.valueOf(0)});
    }

    public static void updateLabel(ClassLoader classLoader, WechatEntity wechatEntity, int i, String str) throws Exception {
        Class loadClass = classLoader.loadClass(wechatEntity.manager_update_label_class1);
        Object callStaticMethod = XposedHelpers.callStaticMethod(loadClass, wechatEntity.manager_update_label_method1, new Object[0]);
        Object newInstance = XposedHelpers.newInstance(classLoader.loadClass(wechatEntity.manager_update_label_class2), new Object[]{Integer.valueOf(i), str});
        XposedHelpers.callMethod(callStaticMethod, wechatEntity.manager_update_label_method2, new Object[]{newInstance, Integer.valueOf(0)});
        refreashLabel(classLoader, wechatEntity, loadClass, callStaticMethod);
    }


}