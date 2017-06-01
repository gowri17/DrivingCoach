package com.drava.android.activity.trips;

import android.content.Context;
import android.content.Intent;
import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.utils.DateConversion;
import com.drava.android.utils.TextUtils;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersAdapter;

import java.util.List;

import static com.drava.android.base.AppConstants.PROFILE_PHOTO;

/**
 * Created by admin on 11/21/2016.
 */

public class TripListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
    private int TYPE_VIEW = 0, TYPE_LOAD_MORE = 1;
    private Context mContext;
    private List<TripListParser.MenteeTripList> menteeTripList;
    private String profilePhoto="";

    public TripListAdapter(Context context, List<TripListParser.MenteeTripList> menteeTripList) {
        mContext = context;
        this.menteeTripList = menteeTripList;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == TYPE_VIEW) {
            view = LayoutInflater.from(mContext).inflate(R.layout.trip_list_item, parent, false);
            return new TripViewHolder(view);
        }else if (viewType == TYPE_LOAD_MORE){
            view = LayoutInflater.from(mContext).inflate(R.layout.view_load_more, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override

    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof  TripViewHolder) {
            TripViewHolder tripViewHolder = (TripViewHolder) holder;
            if (menteeTripList.get(position).Scores.equals("0")){
                tripViewHolder.txtDistance.setTextColor(ContextCompat.getColor(mContext,R.color.color_red));
                tripViewHolder.txtScores.setTextColor(ContextCompat.getColor(mContext,R.color.color_red));
            }else {
                tripViewHolder.txtDistance.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                tripViewHolder.txtScores.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
            }
            tripViewHolder.txtDistance.setText(menteeTripList.get(position).Distance + " Km");
            tripViewHolder.txtTripEndAddress.setText(TextUtils.isNullOrEmpty(menteeTripList.get(position).EndLocation)?mContext.getString(R.string.no_location):menteeTripList.get(position).EndLocation);
            tripViewHolder.txtTripStartAddress.setText(TextUtils.isNullOrEmpty(menteeTripList.get(position).StartLocation)?mContext.getString(R.string.no_location):menteeTripList.get(position).StartLocation);
            tripViewHolder.txtTripTime.setText(DateConversion.getTimeFromDate(DateConversion.stringToDate(menteeTripList.get(position).StartTime, "yyyy-MM-dd HH:mm:ss"), "HH:mm a") + " - "
                    + DateConversion.getTimeFromDate(DateConversion.stringToDate(menteeTripList.get(position).EndTime, "yyyy-MM-dd HH:mm:ss"), "HH:mm a"));
            tripViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(mContext,TripDetailsActivity.class);
                    intent.putExtra(PROFILE_PHOTO, profilePhoto);
                    intent.putExtra("TripId",menteeTripList.get(position).TripId);
                    mContext.startActivity(intent);
                }
            });

            tripViewHolder.imgTripLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(mContext, MapViewActivity.class);
                    intent.putExtra(PROFILE_PHOTO, profilePhoto);
                    intent.putExtra("menteeTripList",menteeTripList.get(position));
                    mContext.startActivity(intent);
                }
            });
            if (menteeTripList.get(position).IsPassenger.equals("1")){
                tripViewHolder.txtDistance.setTextColor(ContextCompat.getColor(mContext,R.color.colorPrimary));
                tripViewHolder.txtScores.setVisibility(View.GONE);
                tripViewHolder.imgPassenger.setVisibility(View.VISIBLE);
            }else {
                tripViewHolder.txtScores.setVisibility(View.VISIBLE);
                tripViewHolder.imgPassenger.setVisibility(View.GONE);
                tripViewHolder.txtScores.setText(menteeTripList.get(position).Scores + " Pts");
            }
        }else if (holder instanceof LoadingViewHolder){
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
//            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemViewType(int i) {
        return menteeTripList.get(i) == null ? TYPE_LOAD_MORE : TYPE_VIEW;
    }

    @Override
    public long getHeaderId(int position) {
        if (menteeTripList.get(position) != null) {
            return DateConversion.getMilliSecondFromStringForDate(menteeTripList.get(position).TripDate, "yyyy-MM-dd");
        }
        return DateConversion.getMilliSecondFromStringForDate(menteeTripList.get(position-1).TripDate, "yyyy-MM-dd");
    }

    @Override
    public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View convertView = LayoutInflater.from(mContext).inflate(R.layout.header_list_item, parent, false);
        return new TripHeaderHolder(convertView);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
        TripHeaderHolder tripHeaderHolder = (TripHeaderHolder) holder;
        tripHeaderHolder.txtHeaderDate.setText(DateConversion.formatDate(menteeTripList.get(position).TripDate, "yyyy-MM-dd", "dd MMMM yyyy"));
    }

    @Override
    public int getItemCount() {
        return menteeTripList.size();
    }

    public void updateList(List<TripListParser.MenteeTripList> menteeTripList) {
        this.menteeTripList = menteeTripList;
        notifyDataSetChanged();
    }

    public void setProfilePhoto(String profilePhoto){
        this.profilePhoto = profilePhoto;
    }

    private class TripViewHolder extends RecyclerView.ViewHolder {
        TextView txtTripTime, txtTripStartAddress, txtTripEndAddress, txtScores, txtDistance;
        ImageView imgTripLocation,imgPassenger;
        private TripViewHolder(View view) {
            super(view);
            txtDistance = (TextView) view.findViewById(R.id.txt_distance);
            txtTripTime = (TextView) view.findViewById(R.id.txt_trip_time);
            txtTripStartAddress = (TextView) view.findViewById(R.id.txt_start_address);
            txtTripEndAddress = (TextView) view.findViewById(R.id.txt_end_address);
            txtScores = (TextView) view.findViewById(R.id.txt_score);
            imgTripLocation = (ImageView) view.findViewById(R.id.img_trip_location);
            imgPassenger = (ImageView) view.findViewById(R.id.img_passenger);
        }
    }

    private class TripHeaderHolder extends RecyclerView.ViewHolder {
        TextView txtHeaderDate;

        private TripHeaderHolder(View view) {
            super(view);
            txtHeaderDate = (TextView) view.findViewById(R.id.txt_header_date);
        }
    }

    private class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progress_bar);
        }
    }
}
