package com.example.signhook;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "SIGN_HOOK";
    private static final String TARGET_CLASS = "com.xqhy.common.encryption.EncryptionUntils";
    private static final String TARGET_METHOD = "sha256Str";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.xqhy.legendbox")) return;

        Log.e(TAG, "===== 目标 App 已加载，开始等待业务类 =====");

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 60; i++) {
                    try { Thread.sleep(2000); } catch (InterruptedException e) {}

                    Log.e(TAG, "第 " + (i + 1) + " 次尝试 Hook...");
                    if (tryHook(lpparam.classLoader)) {
                        Log.e(TAG, "===== Hook 安装成功，之后一直生效 =====");
                        return;
                    }
                }
                Log.e(TAG, "===== 120 秒内未找到类，放弃 =====");
            }
        }).start();
    }

    private boolean tryHook(ClassLoader loader) {
        try {
            Class<?> clazz = XposedHelpers.findClass(TARGET_CLASS, loader);

            // 打印类里所有方法（便于确认方法名和参数）
            for (java.lang.reflect.Method m : clazz.getDeclaredMethods()) {
                Log.e(TAG, "方法: " + m.getName() + " 参数: " + java.util.Arrays.toString(m.getParameterTypes()));
            }

            XposedHelpers.findAndHookMethod(
                TARGET_CLASS, loader, TARGET_METHOD,
                String.class, boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        Log.e(TAG, "===== sha256Str 被调用 =====");
                        Log.e(TAG, "输入原文: " + param.args[0]);
                        Log.e(TAG, "boolean: " + param.args[1]);
                    }
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Log.e(TAG, "输出签名: " + param.getResult());
                    }
                }
            );
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}