package com.atlas.mycirclemenu.diyview;

import android.content.Context;
import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;

import com.atlas.mycirclemenu.R;

/**
 * 通过重构使之符合开闭原则
 * 使用setAdapter，适配器模式
 * 1、设置icons & texts(重构：通过setAdapter获取itemView，然后buildMenuItems)
 * 2、onMeasure（measureSelf & measureChildren）
 * 3、onLayout（）
 */
public class CircleMenuLayout extends ViewGroup {
    private static final String TAG = "CircleMenuLayout";
    //整体直径
    private int mDiameter;
    //周围菜单的比例
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 4f;
    //中间菜单的比例
    private float RADIO_DEFAULT_CENTER_ITEM_DIMENSION = 1 / 3f;
    //外围padding的比例
    private static final float RADIO_PADDING_LAYOUT = 1 / 12f;
    private float mPadding;
    //初始角度(X轴表示0度,顺时针为正方向)
    private double mStartAngle = 0;
    //单个周围菜单的角度
    private float angleDelay;
    //mLastAngle记录上次onTouch事件的角度
    private float mLastAngle;
    //mMoveAngel记录从ACTION_DOWN到ACTION_UP移动的角度
    private float mMoveAngel = 0;
    private Adapter mAdapter;
    private OnMenuItemClickListener mOnMenuItemClickListener;
    //自动滑动效果的实现
    private long mDownTime;
    private boolean isFling = false;
    private Runnable mRunnable;
    private float degreePerSecond;

    public CircleMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 无视padding
        setPadding(0, 0, 0, 0);
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
    }

    public interface OnMenuItemClickListener {
        void itemClick(View view, int pos);

        void itemCenterClick(View view);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mOnMenuItemClickListener = listener;
    }

    /**
     * 添加周围菜单
     */
    @Override
    protected void onAttachedToWindow() {
        if (mAdapter != null) {
            addMenuItems();
        }
        super.onAttachedToWindow();
    }

    private void addMenuItems() {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View itemView = mAdapter.getView(i, null, this);
            final int finalI = i;
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnMenuItemClickListener != null) {
                        mOnMenuItemClickListener.itemClick(v, finalI);
                    }
                }
            });
            // 添加view到容器中
            addView(itemView);
        }
    }

    /**
     * 测量
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        measureSelf(widthMeasureSpec, heightMeasureSpec);
        measureChildren();
        mPadding = RADIO_PADDING_LAYOUT * mDiameter;
    }

    private void measureSelf(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth;
        int resHeight;

        /**
         * 根据传入的参数，分别获取测量模式和测量值
         */
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        /**
         * 如果宽或者高的测量模式非精确值
         */
        if (widthMode != MeasureSpec.EXACTLY || heightMode != MeasureSpec.EXACTLY) {
            // 主要设置为背景图的高度
            resWidth = getSuggestedMinimumWidth();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

            resHeight = getSuggestedMinimumHeight();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else {
            // 如果都设置为精确值，则直接取小值；
            resWidth = resHeight = Math.min(width, height);
        }

        setMeasuredDimension(resWidth, resHeight);
    }

    private void measureChildren() {
        // 获得直径
        mDiameter = Math.max(getMeasuredWidth(), getMeasuredHeight());
        // menu item数量
        final int count = getChildCount();
        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int childMeasureSpec;
            if (child.getId() == R.id.id_circle_menu_item_center) {
                //中心菜单测量模式
                childMeasureSpec = MeasureSpec.makeMeasureSpec(
                        (int) (mDiameter * RADIO_DEFAULT_CENTER_ITEM_DIMENSION),
                        MeasureSpec.EXACTLY);
            } else {
                //围绕菜单测量模式
                childMeasureSpec = MeasureSpec.makeMeasureSpec(
                        (int) (mDiameter * RADIO_DEFAULT_CHILD_DIMENSION),
                        MeasureSpec.EXACTLY);
            }

            child.measure(childMeasureSpec, childMeasureSpec);
        }
    }

    private int getDefaultWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }

    /**
     * 布局
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutDiameter = mDiameter;
        final int childCount = getChildCount();
        int left, top;
        // menu item 的尺寸
        int childWidth = (int) (layoutDiameter * RADIO_DEFAULT_CHILD_DIMENSION);

        // 根据menu item的个数，计算单个item的角度
        // 如果childCount == mItemImgs.length说明menu中间没有centerMenu
        if (childCount == mAdapter.getCount()) {
            angleDelay = 360 / childCount;
        } else {
            angleDelay = 360 / (childCount - 1);
        }

        // 遍历去设置menu item的位置
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) {
                continue;
            }
            if (child.getId() == R.id.id_circle_menu_item_center) {
                // 设置center item位置
                int cl = layoutDiameter / 2 - child.getMeasuredWidth() / 2;
                int ct = layoutDiameter / 2 - child.getMeasuredWidth() / 2;
                child.layout(cl, ct, cl + child.getMeasuredWidth(), ct + child.getMeasuredHeight());
                continue;
            }

            mStartAngle %= 360;
            // 计算，中心点到menu item中心的距离
            float distanceFromCenter = layoutDiameter / 2f - childWidth / 2 - mPadding;
            // distanceFromCenter * cos 即menu item中心点的横坐标
            left = layoutDiameter / 2
                    + (int) Math.round(distanceFromCenter * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f * childWidth);
            // distanceFromCenter * sin 即menu item中心点的纵坐标
            top = layoutDiameter / 2
                    + (int) Math.round(distanceFromCenter * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f * childWidth);

            child.layout(left, top, left + childWidth, top + childWidth);
            // 叠加角度
            mStartAngle += angleDelay;
        }

        // 找到中心的view，设置onclick事件
        View centerItem = findViewById(R.id.id_circle_menu_item_center);
        if (centerItem != null && mOnMenuItemClickListener != null) {
            centerItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnMenuItemClickListener.itemCenterClick(v);
                }
            });
        }
    }

    /**
     * 处理touch事件实现旋转菜单的效果
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (isInCenter(x, y)) {
            return super.dispatchTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastAngle = getAngle(x, y);
                mMoveAngel = 0;
                mDownTime = SystemClock.currentThreadTimeMillis();
                if (isFling) {
                    removeCallbacks(mRunnable);
                    isFling = false;
                    return true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float newAngle = getAngle(x, y);
                //mStartAngle表示首次onLayout的角度
                mStartAngle += newAngle - mLastAngle;
                mMoveAngel += newAngle - mLastAngle;
                //请求重新布局
                requestLayout();
                mLastAngle = newAngle;
                break;
            case MotionEvent.ACTION_UP:
                //通过消息机制不断刷新界面实现滑动效果
                degreePerSecond = mMoveAngel * 1000
                        / (SystemClock.currentThreadTimeMillis() - mDownTime);
                if (Math.abs(degreePerSecond) > 300 && !isFling) {
                    post(mRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (Math.abs(degreePerSecond) < 20) {
                                isFling = false;
                                return;
                            }
                            isFling = true;
                            mStartAngle += degreePerSecond / 80;
                            degreePerSecond /= 1.066F;
                            postDelayed(this, 16);
                            requestLayout();
                        }
                    });
                    return true;
                }
                //如果移动角度超过3度，那么不向子view分发此次touch事件
                if (Math.abs(mMoveAngel) > 3) {
                    return true;
                }
                break;
        }

        return super.dispatchTouchEvent(event);
    }

    /**
     * according the location of a point,
     * return it's degree base on the center of parentView
     *
     * @param layoutX the X pixes of the point in parentView
     * @param layoutY the Y pixes of the point in parentView
     * @return degree base on the center of parentView, clockwise
     */
    private float getAngle(float layoutX, float layoutY) {
        float x = layoutX - mDiameter / 2;
        float y = layoutY - mDiameter / 2;
        float degree = (float) (Math.asin(y / Math.hypot(x, y)) * 180 / Math.PI);
        int quadrant = getQuadrant(x, y);
        if (quadrant == 3) {
            degree = 180 - degree;
        } else if (quadrant == 2) {
            degree = -degree - 180;
        }
        return degree;
    }

    private int getQuadrant(float x, float y) {
        if (y >= 0) {
            return x >= 0 ? 1 : 2;
        } else {
            return x < 0 ? 3 : 4;
        }
    }

    private boolean isInCenter(float eventX, float eventY) {
        float centerRadius = mDiameter * RADIO_DEFAULT_CENTER_ITEM_DIMENSION / 2;
        float x = eventX - mDiameter / 2;
        float y = eventY - mDiameter / 2;
        return Math.hypot(x, y) < centerRadius;
    }

    public double getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(double startAngle) {
        mStartAngle = startAngle;
    }

    public float getAngleDelay() {
        return angleDelay;
    }

    /**
     * 以相应的角度绘制childView，实现围绕中心"辐射"绘制的效果
     */
    @Override
    protected boolean drawChild(Canvas canvas, final View child, long drawingTime) {
        //System.out.println("this is a test msg: " + indexOfChild(child));
        if (child.getId() == R.id.id_circle_menu_item_center) {
            return super.drawChild(canvas, child, drawingTime);
        }
        int px = child.getLeft() + child.getWidth() / 2;
        int py = child.getTop() + child.getHeight() / 2;
        float rotateAngle = 90 + getAngle(px, py);
        canvas.save();
        canvas.rotate(rotateAngle, px, py);
        boolean tmp = super.drawChild(canvas, child, drawingTime);
        canvas.restore();
        return tmp;
    }
}
