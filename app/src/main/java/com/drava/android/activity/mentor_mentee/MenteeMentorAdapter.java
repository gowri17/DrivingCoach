package com.drava.android.activity.mentor_mentee;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.mentor_mentee.view_profile.ViewMentorProfileActivity;
import com.drava.android.activity.mentor_mentee.view_profile.ViewProfileActivity;
import com.drava.android.base.AppConstants;
import com.drava.android.preference.DravaPreference;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.TextUtils;
import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by admin on 10/27/2016.
 */

public class MenteeMentorAdapter extends BaseExpandableListAdapter implements AppConstants {
    private int LOAD_MORE = 1, EXPANDED_VIEW = 0;
    private Context mContext;
    private DravaPreference dravaPreference;
    private List<MentorListParser.MentorList> mentorMenteeLists;
    private MenteeMentorInterface menteeMentorInterface;

    public MenteeMentorAdapter(Context context, List<MentorListParser.MentorList> mentorMenteeLists, MenteeMentorInterface menteeMentorInterface) {
        this.mContext = context;
        this.dravaPreference = new DravaPreference(context);
        this.mentorMenteeLists = mentorMenteeLists;
        this.menteeMentorInterface = menteeMentorInterface;
    }

    @Override
    public int getGroupCount() {
        return mentorMenteeLists.size();
    }

    @Override
    public int getChildrenCount(int i) {
        return 1;
    }

    @Override
    public Object getGroup(int i) {
        return mentorMenteeLists.get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return null;
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int i, boolean isExpanded, View view, ViewGroup viewGroup) {
        GroupViewHolder groupViewHolder = null;
        if (view == null) {
            groupViewHolder = new GroupViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.mentor_mentee_header_item, null);
            groupViewHolder.imgProfile = (ImageView) view.findViewById(R.id.img_profile);
            groupViewHolder.txtUserName = (TextView) view.findViewById(R.id.txt_user_name);
            view.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) view.getTag();
        }
        if (isExpanded) {
            groupViewHolder.txtUserName.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        } else {
            groupViewHolder.txtUserName.setTextColor(ContextCompat.getColor(mContext, R.color.text_dark_color));
        }
        groupViewHolder.txtUserName.setText(mentorMenteeLists.get(i).FirstName + " " + mentorMenteeLists.get(i).LastName);
        if (!TextUtils.isNullOrEmpty(mentorMenteeLists.get(i).Photo)) {
            Picasso.with(mContext).load(mentorMenteeLists.get(i).Photo).into(groupViewHolder.imgProfile);
        }else {
            if (DravaApplication.getApp().getUserPreference().getMentorOrMentee().equalsIgnoreCase(MENTOR)) {
                groupViewHolder.imgProfile.setImageResource(R.drawable.mentee);
            } else {
                groupViewHolder.imgProfile.setImageResource(R.drawable.mentor);
            }
        }
        return view;
    }

    @Override
    public View getChildView(final int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        ChildViewHolder childViewHolder = null;
        if (view == null) {
            childViewHolder = new ChildViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.mentor_mentee_child_item, null);
            childViewHolder.imgViewProfile = (ImageView) view.findViewById(R.id.img_profile);
            childViewHolder.imgViewTrips = (ImageView) view.findViewById(R.id.img_view_trips);
            childViewHolder.imgViewOnMap = (ImageView) view.findViewById(R.id.img_view_on_map);
            childViewHolder.imgRemove = (ImageView) view.findViewById(R.id.img_remove);
            childViewHolder.llViewOnMap = (LinearLayout) view.findViewById(R.id.ll_view_on_map);
            childViewHolder.llViewTrips = (LinearLayout) view.findViewById(R.id.ll_view_trips);
            childViewHolder.llViewProfile = (LinearLayout)view.findViewById(R.id.ll_profile);
            childViewHolder.llRemove = (LinearLayout) view.findViewById(R.id.ll_remove);
            childViewHolder.txtRemove = (TextView) view.findViewById(R.id.txt_remove);
            view.setTag(childViewHolder);
        } else {
            childViewHolder = (ChildViewHolder) view.getTag();
        }
        if (dravaPreference.getMentorOrMentee().equals(MENTOR)) {
            childViewHolder.imgViewProfile.setVisibility(View.VISIBLE);
            childViewHolder.llViewTrips.setVisibility(View.VISIBLE);
            childViewHolder.llViewOnMap.setVisibility(View.VISIBLE);
            childViewHolder.imgRemove.setVisibility(View.VISIBLE);
            childViewHolder.txtRemove.setText(mContext.getString(R.string.remove_mentee));
        } else {
            childViewHolder.imgViewProfile.setVisibility(View.VISIBLE);
            childViewHolder.llViewTrips.setVisibility(View.GONE);
            childViewHolder.llViewOnMap.setVisibility(View.GONE);
            childViewHolder.imgRemove.setVisibility(View.VISIBLE);
            childViewHolder.txtRemove.setText(mContext.getString(R.string.remove_mentor));
        }
        childViewHolder.llRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                menteeMentorInterface.onRemoveClick(mentorMenteeLists.get(i));
            }
        });
        childViewHolder.llViewTrips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mentorMenteeLists.size()>0) {
                    Intent intent = new Intent(mContext, ViewTripsActivity.class);
                    intent.putExtra(MENTOR_LIST, mentorMenteeLists.get(i));
                    mContext.startActivity(intent);
                }
            }
        });
        childViewHolder.llViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mentorMenteeLists.size()>0) {
                    if(dravaPreference.getMentorOrMentee().equals(MENTOR)) {
                        Intent intent = new Intent(mContext, ViewProfileActivity.class);
                        intent.putExtra(MENTOR_LIST, mentorMenteeLists.get(i));
                        mContext.startActivity(intent);
                    }else if(dravaPreference.getMentorOrMentee().equals(MENTEE)){
                        Intent intent = new Intent(mContext, ViewMentorProfileActivity.class);
                        intent.putExtra(MENTOR_LIST, mentorMenteeLists.get(i));
                        mContext.startActivity(intent);
                    }
                }
            }
        });

        childViewHolder.llViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                For In-app purchase       //R.L v1.1
                menteeMentorInterface.checkUserTokenWithGooglePlayToViewMentee(i);
//                if(mentorMenteeLists.size()>0) {
//                    Intent intent = new Intent(mContext, ViewOnMapActivity.class);
//                    intent.putExtra(MENTOR_LIST, mentorMenteeLists.get(i));
//                    mContext.startActivity(intent);
//                }
            }
        });

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }


    public void updateList(List<MentorListParser.MentorList> mentorMenteeLists) {
        this.mentorMenteeLists = mentorMenteeLists;
        notifyDataSetChanged();
    }

    private class ChildViewHolder {
        ImageView imgViewProfile, imgViewTrips, imgViewOnMap, imgRemove;
        LinearLayout llViewTrips, llViewOnMap, llRemove,llViewProfile;
        TextView txtViewProfile, txtViewTrips, txtViewOnMap, txtRemove;
    }

    private class GroupViewHolder {
        TextView txtUserName;
        ImageView imgProfile;
    }

}
