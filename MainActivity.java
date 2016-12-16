package com.atlas.mycirclemenu;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    private CircleMenuLayout mCircleMenuLayout;
    private ArrayList<MenuItem> mList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        mCircleMenuLayout = (CircleMenuLayout) findViewById(R.id.menu_layout);
        mCircleMenuLayout.setAdapter(new CircleMenuAdapter(mList));
        mCircleMenuLayout.setOnMenuItemClickListener(new CircleMenuLayout.OnMenuItemClickListener() {
            @Override
            public void itemClick(View view, int pos) {
                Toast.makeText(getBaseContext(), mList.get(pos).title, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void itemCenterClick(View view) {
                Toast.makeText(getBaseContext(), "you can do something just like ccb  ", Toast.LENGTH_SHORT).show();
            }
        });
        //添加布局动画效果
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(200);
        LayoutAnimationController controller = new LayoutAnimationController(animation, 1.1f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        mCircleMenuLayout.setLayoutAnimation(controller);
    }

    private void initData() {
        String[] texts = new String[]{"安全中心1 ", "特色服务2", "投资理财3",
                "转账汇款4", "我的账户5", "信用卡6"};
        int[] imgs = new int[]{R.drawable.home_mbank_1_normal,
                R.drawable.home_mbank_2_normal, R.drawable.home_mbank_3_normal,
                R.drawable.home_mbank_4_normal, R.drawable.home_mbank_5_normal,
                R.drawable.home_mbank_6_normal};
        for (int i = 0; i < imgs.length; i++) {
            MenuItem item = new MenuItem(imgs[i], texts[i]);
            mList.add(item);
        }
    }

    //添加模拟抽奖功能
    public void try_it(View view) {
        float startAngle = (float) mCircleMenuLayout.getStartAngle();
        ValueAnimator animator
                = ValueAnimator.ofFloat(startAngle, startAngle + 360 * 2 + (int) (Math.random() * 10) * 60);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(5000);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float result = (float) animation.getAnimatedValue();
                mCircleMenuLayout.setStartAngle(result);
                mCircleMenuLayout.requestLayout();
            }
        });
        animator.start();
    }
}
