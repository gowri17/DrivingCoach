package com.drava.android.activity.leftmenu;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.base.AppConstants;
import com.drava.android.preference.DravaPreference;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.TextUtils;

import java.util.ArrayList;

public class LeftMenuAdapter extends BaseAdapter implements AppConstants{

    private LayoutInflater layoutInflater;
    private Context context;
    private ArrayList<ViewHelper> mRowItems;
    private LeftMenuDrawerItem.Type selectedMenuType = LeftMenuDrawerItem.Type.HOME;
    private OnMenuClickListener mOnMenuClickListener;

    public LeftMenuAdapter(Context context, OnMenuClickListener menuClickListener){
        this.context = context;
        this.mOnMenuClickListener = menuClickListener;
        mRowItems = new ArrayList<>();
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return mRowItems.size();
    }

    @Override
    public ViewHelper getItem(int i) {
        return mRowItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getItemViewType(int position) {
        return mRowItems.get(position).type;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return mRowItems.get(i).getView(view, viewGroup);
    }

    public void setMenuType(LeftMenuDrawerItem.Type  menuType){
        selectedMenuType = menuType;
        notifyDataSetChanged();
    }

    abstract class ViewHelper{
        int layout, type;

        public ViewHelper(int layout, int type){
            this.layout = layout;
            this.type = type;
        }

        public View getView(View convertView, ViewGroup parent){
            if(convertView == null){
                convertView = layoutInflater.inflate(layout, parent, false);
            }
            return convertView;
        }
    }

    class MenuDivider extends ViewHelper{
        public MenuDivider(){
            super(R.layout.menu_divider, 0);
        }
    }

    class MenuItem extends ViewHelper{
        LeftMenuDrawerItem mItem;

        public MenuItem(LeftMenuDrawerItem item){
            super(R.layout.left_menu_item, 1);
            mItem = item;
        }

        @Override
        public View getView(View convertView, ViewGroup parent) {
            View view = super.getView(convertView, parent);
            LinearLayout llRoot = (LinearLayout) view.findViewById(R.id.ll_root);
            ImageView icon = (ImageView)view.findViewById(R.id.leftmenu_icon);
            TextView title = (TextView)view.findViewById(R.id.leftmenu_title);

            if(!TextUtils.isEmpty(mItem.title)){
                icon.setImageResource(mItem.icon);
                title.setText(mItem.title);
                view.setTag(mItem);
            }
            if (mItem.type == selectedMenuType) {
                llRoot.setSelected(true);
                title.setTextColor(ContextCompat.getColor(context,R.color.white));
                view.setBackgroundColor(ContextCompat.getColor(context,R.color.drawer_item_bg_selected));
            }else {
                llRoot.setSelected(false);
                title.setTextColor(ContextCompat.getColor(context,R.color.color_light_text));
                view.setBackgroundColor(ContextCompat.getColor(context,R.color.transparent));
            }
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LeftMenuDrawerItem item = mItem;
                    LeftMenuDrawerItem.Type type = item.type;
                    String version = context.getResources().getString(R.string.version);
                    try {
                        version += DeviceUtils.getAppVersionName(context);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (mOnMenuClickListener != null && type != LeftMenuDrawerItem.Type.VERSION&& !mItem.title.equalsIgnoreCase(version)) {
                        mOnMenuClickListener.onMenuClick(item, type);
                    }
                    selectedMenuType = type;
                    notifyDataSetChanged();
                }
            });
            return view;
        }
    }

    public void getDynamicMenuItems(){
        ArrayList<ViewHelper> listItems = new ArrayList<>();

        ArrayList<LeftMenuDrawerItem> leftMenuDrawerItems = new ArrayList<>();
        DravaPreference dravaPreference = new DravaPreference(context);
        if(dravaPreference.getMentorOrMentee().equals(MENTEE))
            leftMenuDrawerItems = MenuCollections.getMenteeMenuItems(context);
        else if(dravaPreference.getMentorOrMentee().equals(MENTOR)) {
            leftMenuDrawerItems = MenuCollections.getMentorMenuItems(context);
        }

        for(LeftMenuDrawerItem item : leftMenuDrawerItems){
            ViewHelper row;
            if(item.type == LeftMenuDrawerItem.Type.DIVIDER){
                row = new MenuDivider();
            }else{
                row = new MenuItem(item);
            }
            listItems.add(row);
        }
        mRowItems.addAll(listItems);
    }

    public interface OnMenuClickListener{
        void onMenuClick(LeftMenuDrawerItem view, LeftMenuDrawerItem.Type type);
    }
}
