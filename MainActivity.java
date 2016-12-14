package com.atlas.mycirclemenu;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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
        mCircleMenuLayout = (CircleMenuLayout) findViewById(R.id.id_menulayout);
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

}
