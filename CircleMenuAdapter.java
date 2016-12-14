package com.atlas.mycirclemenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Atlas on 2016/12/14.
 */

public class CircleMenuAdapter extends BaseAdapter {
    private List<MenuItem> mMenuItems;

    public CircleMenuAdapter(List<MenuItem> menuItems) {
        mMenuItems = menuItems;
    }

    @Override
    public int getCount() {
        return mMenuItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mMenuItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater mInflater = LayoutInflater.from(parent.getContext());
        View itemView = mInflater.inflate(R.layout.circle_menu_item, parent, false);
        ImageView iv = (ImageView) itemView.findViewById(R.id.id_circle_menu_item_image);
        TextView tv = (TextView) itemView.findViewById(R.id.id_circle_menu_item_text);

        MenuItem item = mMenuItems.get(position);
        iv.setVisibility(View.VISIBLE);
        iv.setImageResource(item.imgId);
        tv.setVisibility(View.VISIBLE);
        tv.setText(item.title);

        return itemView;
    }
}
