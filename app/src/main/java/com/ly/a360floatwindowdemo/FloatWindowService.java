package com.ly.a360floatwindowdemo;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FloatWindowService extends Service {

    /**
     * 用于在线程中创建或移除悬浮窗
     */
    private Handler mHandler = new Handler();

    /**
     * 定时器，定时进行检查当前应该创建还是移除悬浮窗
     */
    private Timer mTimer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 开启服务
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 开启定时器，每隔0.5秒刷新一次
        if (mTimer == null) {
            mTimer = new Timer();
            mTimer.scheduleAtFixedRate(new RefreshTask(), 0, 500);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Service被终止的同时也停止定时器继续运行
        mTimer.cancel();
        mTimer = null;
    }

    class RefreshTask extends TimerTask {
        @Override
        public void run() {
            // 当前界面是桌面，且没有悬浮窗显示，则创建悬浮窗
            if (isHome() && !MyWindowManager.isWindowShowing()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.createSmallWindow(getApplicationContext());
                    }
                });
            }
            // 当前界面不是桌面，且有悬浮窗显示，则移除悬浮窗
            else if (!isHome() && MyWindowManager.isWindowShowing()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.removeSmallWindow(getApplicationContext());
                        MyWindowManager.removeBigWindow(getApplicationContext());
                    }
                });
            }
            // 当前界面是桌面，且有悬浮窗显示，则更新内存数据
            else if (isHome() && MyWindowManager.isWindowShowing()) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        MyWindowManager.updateUsedPercent(getApplicationContext());
                    }
                });
            }
        }
    }

    /**
     * 判断当前界面是否是桌面
     */
    private boolean isHome() {
        List<ActivityManager.RunningTaskInfo> runningTasks = null;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            runningTasks = manager.getRunningTasks(1);
        }
        return runningTasks != null && getHomes().contains(runningTasks.get(0).topActivity.getPackageName());
    }

    /**
     * 获取属于桌面的应用的应用程序包
     *
     * @return 返回包含所有包名的字符串列表
     */
    private List<String> getHomes() {
        List<String> names = new ArrayList<>();
        PackageManager manager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfoList = manager.queryIntentActivities(intent, PackageManager
                .MATCH_DEFAULT_ONLY);
        for (ResolveInfo resolveInfo : resolveInfoList) {
            names.add(resolveInfo.activityInfo.packageName);
        }
        return names;
    }
}
