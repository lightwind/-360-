package com.ly.a360floatwindowdemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.lang.reflect.Field;

/**
 * Function：小悬浮窗
 * Author：LightWind
 * Time：2017/11/23
 */

public class FloatWindowSmallView extends LinearLayout {

    /**
     * 记录小悬浮窗的宽度
     */
    public static int viewWidth;

    /**
     * 记录小悬浮窗的高度
     */
    public static int viewHeight;

    /**
     * 记录系统状态栏的高度
     */
    private static int statusBarHeight;

    /**
     * 用于更新小悬浮窗的位置
     */
    private WindowManager mWindowManager;

    /**
     * 小悬浮窗的参数
     */
    private WindowManager.LayoutParams mParams;

    /**
     * 记录当前手指在屏幕上的x轴坐标
     */
    private float xInScreen;

    /**
     * 记录当前手指在屏幕上的y轴坐标
     */
    private float yInScreen;

    /**
     * 记录按下时屏幕上的x轴坐标
     */
    private float xDownInScreen;

    /**
     * 记录按下时屏幕上的y轴坐标
     */
    private float yDownInScreen;

    /**
     * 记录按下时在小悬浮窗的View上的x轴坐标
     */
    private float xInView;

    /**
     * 记录按下时在小悬浮窗的View上的y轴坐标
     */
    private float yInView;


    public FloatWindowSmallView(Context context) {
        super(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.float_window_small, this);
        View view = findViewById(R.id.small_window_layout);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
        TextView percentView = findViewById(R.id.percent);
        percentView.setText(MyWindowManager.getUsedPercentValue(context));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录当前的坐标轴数据,y轴上的值要减去状态栏的高度
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候，更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                // 当手指离开的时候，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if (xInScreen == xDownInScreen && yInScreen == yDownInScreen) {
                    openBigWindow();
                }
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 更新小悬浮窗的位置
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        mWindowManager.updateViewLayout(this, mParams);
    }

    /**
     * 打开大悬浮窗，关闭小悬浮窗
     */
    private void openBigWindow() {
        MyWindowManager.createBigWindow(getContext());
        MyWindowManager.removeSmallWindow(getContext());
    }

    /**
     * 获取状态栏的高度
     *
     * @return 返回状态栏高度的像素值
     */
    private float getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                @SuppressLint("PrivateApi")
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

}
