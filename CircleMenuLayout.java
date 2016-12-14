package com.atlas.mycirclemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;

/**
 * 1、设置icons & texts
 * 2、onMeasure（measureSelf & measureChildren）
 * 3、onLayout（）
 */
public class CircleMenuLayout extends ViewGroup {
    private static final String TAG = "CircleMenuLayout";
    private int mDiameter;
    private static final float RADIO_DEFAULT_CHILD_DIMENSION = 1 / 4f;
    private float RADIO_DEFAULT_CENTER_ITEM_DIMENSION = 1 / 3f;
    private static final float RADIO_PADDING_LAYOUT = 1 / 12f;
    private float mPadding;
    private double mStartAngle = 30;

    public CircleMenuLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 无视padding
        setPadding(0, 0, 0, 0);
    }

    private Adapter mAdapter;

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
    }

    @Override
    protected void onAttachedToWindow() {
        if (mAdapter != null) {
            buildMenuItems();
        }
        super.onAttachedToWindow();
    }

    private void buildMenuItems() {
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

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int layoutDiameter = mDiameter;
        final int childCount = getChildCount();
        int left, top;
        // menu item 的尺寸
        int childWidth = (int) (layoutDiameter * RADIO_DEFAULT_CHILD_DIMENSION);

        // 根据menu item的个数，计算单个item的角度
        // 如果childCount == mItemImgs.length说明menu中间没有centerMenu
        float angleDelay;
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
                Log.d(TAG, "layout child: id_circle_menu_item_center");
                // 设置center item位置
                int cl = layoutDiameter / 2 - child.getMeasuredWidth() / 2;
                int cr = cl + child.getMeasuredWidth();
                child.layout(cl, cl, cr, cr);
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
            Log.d(TAG, "layout child: " + left + "  " + top + "  " + (left + childWidth) + "  " + (top + childWidth));
            // 叠加角度
            mStartAngle += angleDelay;
        }

        // 找到中心的view，如果存在设置onclick事件
        View centerItem = findViewById(R.id.id_circle_menu_item_center);
        if (centerItem != null) {
            centerItem.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnMenuItemClickListener != null) {
                        mOnMenuItemClickListener.itemCenterClick(v);
                    }
                }
            });
        }
    }

    private OnMenuItemClickListener mOnMenuItemClickListener;

    public interface OnMenuItemClickListener {
        void itemClick(View view, int pos);

        void itemCenterClick(View view);
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListener listener) {
        mOnMenuItemClickListener = listener;
    }


}
