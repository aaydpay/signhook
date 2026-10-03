package com.example.signhook;

import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {
    private static final String TAG = "SIGN_HOOK";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("com.xqhy.legendbox")) return;

        Log.e(TAG, "===== 目标 App 已加载，开始 Hook =====");

        try {
            XposedHelpers.findAndHookMethod(
                "com.xqhy.common.encryption.EncryptionUntils",
                lpparam.classLoader,
                "sha256Str",
                String.class, boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        String input = (String) param.args[0];
                        boolean flag = (boolean) param.args[1];
                        Log.e(TAG, "===== sha256Str 调用 =====");
                        Log.e(TAG, "输入原文: " + input);
                        Log.e(TAG, "boolean 参数: " + flag);
                        Log.e(TAG, "原文长度: " + (input != null ? input.length() : 0));
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        Log.e(TAG, "输出签名: " + param.getResult());
                        Log.e(TAG, "===== 调用结束 =====");
                    }
                }
            );
            Log.e(TAG, "===== Hook 安装成功 =====");
        } catch (Throwable t) {
            Log.e(TAG, "===== Hook 失败: " + t.getMessage(), t);
        }
    }
}
