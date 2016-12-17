package com.atlas.mycirclemenu.defaultAdapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.atlas.mycirclemenu.R;

import java.util.List;

/**
 * Created by Atlas on 2016/12/14.
 */

public class CircleMenuAdapter extends BaseAdapter {
    private static final String TAG = "CircleMenuAdapter";
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
        Log.d(TAG, "getView: " + position + " " + convertView);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder(parent);
            convertView = holder.root;
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.fillData(mMenuItems.get(position));
        return convertView;
    }

    private static class ViewHolder {
        View root;
        ImageView img;
        TextView text;

        ViewHolder(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            root = inflater.inflate(R.layout.circle_menu_item, parent, false);
            img = (ImageView) root.findViewById(R.id.id_circle_menu_item_image);
            text = (TextView) root.findViewById(R.id.id_circle_menu_item_text);
        }

        void fillData(MenuItem item) {
            img.setVisibility(View.VISIBLE);
            img.setImageResource(item.imgId);
            text.setVisibility(View.VISIBLE);
            text.setText(item.title);
        }
    }
}
