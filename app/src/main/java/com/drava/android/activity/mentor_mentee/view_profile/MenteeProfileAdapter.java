package com.drava.android.activity.mentor_mentee.view_profile;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.drava.android.R;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.ArrayList;

public class MenteeProfileAdapter extends RecyclerView.Adapter<MenteeProfileAdapter.ProfileViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    protected ArrayList<ProfileBeanClass.MenteeProfileBean> profileList;
    protected Context context;

    public MenteeProfileAdapter(ArrayList<ProfileBeanClass.MenteeProfileBean> profileList, Context context) {
        this.profileList = profileList;
        this.context = context;
    }

    public class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView txtviewMaxSpeed;
        TextView txtviewViolation;
        TextView txtviewTimehr;
        TextView txtviewTimemin;
        TextView txtviewDistance;

        public ProfileViewHolder(View itemView) {
            super(itemView);
            txtviewMaxSpeed = (TextView) itemView.findViewById(R.id.txtviewMaxSpeed);
            txtviewViolation = (TextView) itemView.findViewById(R.id.txtviewViolation);
            txtviewTimehr = (TextView) itemView.findViewById(R.id.txtviewTimeHour);
            txtviewTimemin = (TextView) itemView.findViewById(R.id.txtviewTimeMinute);
            txtviewDistance = (TextView) itemView.findViewById(R.id.txtviewDistance);

        }
    }

    @Override
    public ProfileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_mentee_profile_list, parent, false);
        return new ProfileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ProfileViewHolder holder, int position) {
        ProfileBeanClass.MenteeProfileBean rowBean = profileList.get(position);
        holder.txtviewMaxSpeed.setText(rowBean.MaxSpeed);
        holder.txtviewViolation.setText(rowBean.ViolationCount);
        holder.txtviewTimehr.setText(rowBean.TotalTravelHours);
        holder.txtviewTimemin.setText(rowBean.TotalTravelMinutes);
        holder.txtviewDistance.setText(rowBean.TotalDistance);
    }

    @Override
    public long getHeaderId(int position) {

        if (profileList.get(position) != null)
            return Long.parseLong(String.valueOf(profileList.get(position).Month) + "" + profileList.get(position).Year);
        else
            return 0;
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_header, parent, false);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView.findViewById(R.id.txt_header);
        String month = profileList.get(position).getMonthOfYear();
        String headertext = month.concat(" " + profileList.get(position).Year);
        if (profileList.get(position) != null) {
            textView.setText(headertext);
//            textView.setTypeface(null, Typeface.ITALIC);
        }

    }
    public void updateList(ArrayList<ProfileBeanClass.MenteeProfileBean> menteeReportList) {
        this.profileList = menteeReportList;
        notifyDataSetChanged();
    }


    @Override
    public int getItemCount() {
        return profileList.size();
    }
}
