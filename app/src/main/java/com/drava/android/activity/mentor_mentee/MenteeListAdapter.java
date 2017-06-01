package com.drava.android.activity.mentor_mentee;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.mentor_mentee.view_profile.ViewProfileActivity;
import com.drava.android.parser.MenteeListParser;
import com.drava.android.utils.TextUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.drava.android.base.AppConstants.CURRENT_LOCATION;
import static com.drava.android.base.AppConstants.FIRST_NAME;
import static com.drava.android.base.AppConstants.LAST_NAME;
import static com.drava.android.base.AppConstants.MENTEE_ID;
import static com.drava.android.base.AppConstants.PROFILE_PHOTO;

public class MenteeListAdapter extends RecyclerView.Adapter<MenteeListAdapter.MenteeListViewHolder>{

    private Context context;
    private List<MenteeListParser.MentorsMenteeList> menteeList;
    View itemView;

    public MenteeListAdapter(Context context, ArrayList<MenteeListParser.MentorsMenteeList> menteeList){
        this.context = context;
        this.menteeList = menteeList;
    }

    @Override
    public MenteeListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.mentee_listview_item, parent, false);
        return new MenteeListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MenteeListViewHolder holder, final int position) {
        if(!TextUtils.isEmpty(menteeList.get(position).Photo)){
            Picasso.with(context).load(menteeList.get(position).Photo).placeholder(ContextCompat.getDrawable(context, R.drawable.user)).into(holder.profileImage);
        }else{
            Picasso.with(context).load(R.drawable.user).into(holder.profileImage);
        }
        if(!TextUtils.isEmpty(menteeList.get(position).CurrentLocation)){
            holder.menteeAddress.setText(menteeList.get(position).CurrentLocation);
            holder.menteeAddress.setVisibility(View.VISIBLE);
        }else{
            holder.menteeAddress.setVisibility(View.GONE);
        }
        holder.menteeName.setText(menteeList.get(position).FirstName+" "+menteeList.get(position).LastName);
        if(TextUtils.isNullOrEmpty(menteeList.get(position).Distance)){     //R.L
            holder.distance.setText("0.00");
        }else{
            holder.distance.setText(menteeList.get(position).Distance);
        }

        holder.layoutMenteeItemBase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context,ViewProfileActivity.class);
                intent.putExtra(FIRST_NAME, menteeList.get(position).FirstName);
                intent.putExtra(LAST_NAME, TextUtils.isNullOrEmpty(menteeList.get(position).LastName)?"":menteeList.get(position).LastName);
                intent.putExtra(PROFILE_PHOTO, menteeList.get(position).Photo);
                intent.putExtra(CURRENT_LOCATION, menteeList.get(position).CurrentLocation);
                intent.putExtra(MENTEE_ID, menteeList.get(position).MenteeId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return menteeList.size();
    }

    public void updateMenteeList(List<MenteeListParser.MentorsMenteeList> menteeList){
        this.menteeList=menteeList;
        notifyDataSetChanged();
    }

    class MenteeListViewHolder extends RecyclerView.ViewHolder{
        private ImageView profileImage, vehicleImage;
        private TextView menteeName, menteeAddress, distance;
        private RelativeLayout layoutMenteeItemBase, layoutUserData,layoutTripData;

        public MenteeListViewHolder(View itemView){
            super(itemView);
            profileImage = (ImageView)itemView.findViewById(R.id.mentee_profile_image);
            menteeName = (TextView)itemView.findViewById(R.id.mentee_name);
            menteeAddress = (TextView)itemView.findViewById(R.id.txt_location);
            vehicleImage = (ImageView)itemView.findViewById(R.id.img_vehicle);
            distance = (TextView)itemView.findViewById(R.id.txt_distance);
            layoutUserData = (RelativeLayout)itemView.findViewById(R.id.rlv_user_data_info_layout);
            layoutTripData = (RelativeLayout)itemView.findViewById(R.id.rlv_user_trip_info_layout);
            layoutMenteeItemBase = (RelativeLayout)itemView.findViewById(R.id.mentee_item_base_layout);
        }
    }
}
