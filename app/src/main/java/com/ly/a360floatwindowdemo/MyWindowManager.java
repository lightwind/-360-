package com.ly.a360floatwindowdemo;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Function：屏幕界面管理类，用于悬浮窗的添加和移除，已经内存使用情况的获取和更新
 * Author：LightWind
 * Time：2017/11/23
 */

public class MyWindowManager {

    /**
     * 小悬浮窗实例
     */
    private static FloatWindowSmallView smallWindow;

    /**
     * 大悬浮窗实例
     */
    private static FloatWindowBigView bigWindow;

    /**
     * 小悬浮窗参数
     */
    private static WindowManager.LayoutParams smallWindowParams;

    /**
     * 大悬浮窗参数
     */
    private static WindowManager.LayoutParams bigWindowParams;

    /**
     * 用于在屏幕上添加和移除悬浮窗
     */
    private static WindowManager mWindowManager;

    /**
     * 用于获取手机可用内存
     */
    private static ActivityManager mActivityManager;

    /**
     * 单例模式创建WindowManager
     *
     * @param context 必须是应用程序的Context
     * @return WindowManager的实例，用于控制屏幕上悬浮窗的添加和移除
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }

    /**
     * 单例模式创建ActivityManager
     *
     * @param context 应用程序的上下文
     * @return ActivityManager的实例，用于获取手机内存信息
     */
    private static ActivityManager getActivityManager(Context context) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
        return mActivityManager;
    }

    /**
     * 创建小悬浮窗
     */
    @SuppressLint("RtlHardcoded")
    public static void createSmallWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (smallWindow == null) {
            smallWindow = new FloatWindowSmallView(context);
            if (smallWindowParams == null) {
                smallWindowParams = new WindowManager.LayoutParams();
                smallWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                smallWindowParams.format = PixelFormat.RGBA_8888;
                smallWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                smallWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                smallWindowParams.width = FloatWindowSmallView.viewWidth;
                smallWindowParams.height = FloatWindowSmallView.viewHeight;
                smallWindowParams.x = screenWidth;
                smallWindowParams.y = screenHeight / 2;
            }
            smallWindow.setParams(smallWindowParams);
            windowManager.addView(smallWindow, smallWindowParams);
        }
    }

    /**
     * 移除小悬浮窗
     */
    public static void removeSmallWindow(Context context) {
        if (smallWindow != null) {
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(smallWindow);
            smallWindow = null;
        }
    }

    /**
     * 创建大悬浮窗。位置放在屏幕正中间
     */
    @SuppressLint("RtlHardcoded")
    public static void createBigWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        if (bigWindow == null) {
            bigWindow = new FloatWindowBigView(context);
            if (bigWindowParams == null) {
                bigWindowParams = new WindowManager.LayoutParams();
                bigWindowParams.x = screenWidth / 2 - FloatWindowBigView.viewWidth / 2;
                bigWindowParams.y = screenHeight / 2 - FloatWindowBigView.viewHeight / 2;
                bigWindowParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                bigWindowParams.format = PixelFormat.RGBA_8888;
                bigWindowParams.gravity = Gravity.LEFT | Gravity.TOP;
                bigWindowParams.width = FloatWindowBigView.viewWidth;
                bigWindowParams.height = FloatWindowBigView.viewHeight;
            }
            windowManager.addView(bigWindow, bigWindowParams);
        }
    }

    /**
     * 移除大悬浮窗
     */
    public static void removeBigWindow(Context context) {
        if (bigWindow != null) {
            getWindowManager(context).removeView(bigWindow);
            bigWindow = null;
        }
    }

    /**
     * 计算已使用内存的百分比，并返回。
     *
     * @return 已使用内存的百分比，以字符串的形式返回
     */
    public static String getUsedPercentValue(Context context) {
        String dir = "/proc/memifo";
        try {
            FileReader fileReader = new FileReader(dir);
            BufferedReader bufferedReader = new BufferedReader(fileReader, 2048);
            String memoryLine = bufferedReader.readLine();
            String subMemoryLine = memoryLine.substring(memoryLine.indexOf("MemTotal:"));
            bufferedReader.close();
            long totalMemorySize = Integer.parseInt(subMemoryLine.replaceAll("\\D+", ""));
            long availableSize = getAvailableMemory(context);
            int percent = (int) ((totalMemorySize - availableSize) / (float) totalMemorySize * 100);
            return percent + "%";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "悬浮窗";
    }

    /**
     * 获取当前可用内存，单位为字节
     */
    private static long getAvailableMemory(Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        getActivityManager(context).getMemoryInfo(memoryInfo);
        return memoryInfo.availMem;
    }

    /**
     * 更新小悬浮窗的TextView上的数据，显示内存使用的百分比
     */
    public static void updateUsedPercent(Context context) {
        if (smallWindow != null) {
            TextView percentView = smallWindow.findViewById(R.id.percent);
            percentView.setText(getUsedPercentValue(context));
        }
    }

    /**
     * 是否有悬浮窗显示在屏幕上
     */
    public static boolean isWindowShowing() {
        return smallWindow != null || bigWindow != null;
    }

}
