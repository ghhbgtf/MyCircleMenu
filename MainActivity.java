package com.atlas.mycirclemenu;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import com.atlas.mycirclemenu.defaultAdapter.CircleMenuAdapter;
import com.atlas.mycirclemenu.defaultAdapter.MenuItem;
import com.atlas.mycirclemenu.diyview.CircleMenuLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private CircleMenuLayout mCircleMenuLayout;
    private ArrayList<MenuItem> mList = new ArrayList<>();
    private boolean haveRotate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initViews();
    }

    private void initData() {
        String[] texts = new String[]{
                "安全中心1",
                "特色服务2",
                "投资理财3",
                "转账汇款4",
                "我的账户5",
                "信用卡6"};
        int[] imgs = new int[]{
                R.drawable.home_mbank_1,
                R.drawable.home_mbank_2,
                R.drawable.home_mbank_3,
                R.drawable.home_mbank_4,
                R.drawable.home_mbank_5,
                R.drawable.home_mbank_6
        };
        for (int i = 0; i < imgs.length; i++) {
            MenuItem item = new MenuItem(imgs[i], texts[i]);
            mList.add(item);
        }
    }

    private void initViews() {
        mCircleMenuLayout = (CircleMenuLayout) findViewById(R.id.menu_layout);
        mCircleMenuLayout.setAdapter(new CircleMenuAdapter(mList));
        mCircleMenuLayout.setOnMenuItemClickListener(new CircleMenuLayout.OnMenuItemClickListener() {
            @Override
            public void itemClick(View view, int pos) {
                Toast.makeText(getBaseContext(), mList.get(pos).title, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void itemCenterClick(View view) {
                if (!haveRotate) {
                    //顺时针旋转 360 * 2 + angleDelay * N 度（N为随机数）
                    view.setVisibility(View.INVISIBLE);
                    view.setClickable(false);
                    float startAngle = (float) mCircleMenuLayout.getStartAngle();
                    // TODO: 2016/12/20 ensure whether mCircleMenuLayout.getAngleDelay() will return null
                    float endAngle = startAngle + 360 * 2 + (int) (Math.random() * 10) * mCircleMenuLayout.getAngleDelay();
                    rotate(view, startAngle, endAngle);
                } else {
                    //选择最近的角度归位（小于180度的方向）
                    Toast.makeText(getBaseContext(), "归位", Toast.LENGTH_SHORT).show();
                    float startAngle = (float) mCircleMenuLayout.getStartAngle();
                    float endAngle = startAngle + 360 - startAngle % 360;
                    if (endAngle - startAngle > 180) {
                        endAngle -= 360;
                    }
                    rotate(view, startAngle, endAngle);
                }
                haveRotate = !haveRotate;
            }
        });
        //添加布局动画效果
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(100);
        LayoutAnimationController controller = new LayoutAnimationController(animation, 1.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mCircleMenuLayout.setLayoutAnimation(controller);
    }

    //添加模拟抽奖功能
    public void rotate(final View view, float start, float end) {
        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setInterpolator(new DecelerateInterpolator());
        //速度固定为5秒3圈，根据旋转的角度计算持续时间
        animator.setDuration((long) (5000 * Math.abs(end - start) / (360 * 3)));
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float result = (float) animation.getAnimatedValue();
                mCircleMenuLayout.setStartAngle(result);
                mCircleMenuLayout.requestLayout();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.VISIBLE);
                super.onAnimationEnd(animation);
            }
        });
        animator.start();
    }
}
