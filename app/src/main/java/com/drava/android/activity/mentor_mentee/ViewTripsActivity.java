package com.drava.android.activity.mentor_mentee;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.trips.TripListAdapter;
import com.drava.android.activity.trips.TripListParser;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseActivity;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DividerItemDecoration;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by admin on 12/9/2016.
 */

public class ViewTripsActivity extends BaseActivity implements AppConstants{

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TripListAdapter tripListAdapter;
    private TextView txtEmptyView;
    private List<TripListParser.MenteeTripList> menteeTripList;
    private ProgressDialog progressDialog;
    private boolean isLoading = false;
    private int totalCount = 0, start = 0;
    private int visibleThreshold = 1;
    private MentorListParser.MentorList mentorMenteeList;
    private String profilePhoto;
    private String userId;
    private String isPassenger = "0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_trip_list);
        init();
        setUpDefaults();
        setUpEvents();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void init() {
        setToolbar("Trip List");
        setPassengerImage(View.VISIBLE);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();

        //--------From Marker Cluster info window HomeFragment-----------
        if(getIntent().hasExtra(MENTEE_ID)){
            userId = getIntent().getStringExtra(MENTEE_ID);
        }
        if(getIntent().hasExtra(PROFILE_PHOTO)){
            String profile_Photo = getIntent().getStringExtra(PROFILE_PHOTO);
            if (!TextUtils.isNullOrEmpty(profile_Photo)) {
                if(getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)) {
                    setUserImage(profile_Photo);
                }
                profilePhoto = profile_Photo;
            }
        }
        //----------------------------------------------------

        //--------From MenteeMentorAdapter -----------
        if (getIntent().hasExtra(MENTOR_LIST)){
            mentorMenteeList =(MentorListParser.MentorList)getIntent().getSerializableExtra(MENTOR_LIST);

            if (!TextUtils.isNullOrEmpty(mentorMenteeList.Photo)) {
                if(getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)) {
                    setUserImage(mentorMenteeList.Photo);
                }
                profilePhoto = mentorMenteeList.Photo;
            }
            if (!TextUtils.isNullOrEmpty(mentorMenteeList.UserId)) {
                userId = mentorMenteeList.UserId;
            }
        }

        recyclerView = (RecyclerView) findViewById(R.id.header_list_view);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        txtEmptyView = (TextView) findViewById(R.id.empty_view);
        menteeTripList = new ArrayList<>();
        tripListAdapter = new TripListAdapter(this, menteeTripList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(tripListAdapter));
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.cl_recycler_view_divider);
        recyclerView.addItemDecoration(new DividerItemDecoration(drawable));
        recyclerView.setAdapter(tripListAdapter);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(this, R.color.color_theme));

//        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
        if (DeviceUtils.isInternetConnected(this)) {
            getTripList(true);
        } else {
            AlertUtils.showAlert(this, getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }, false);
        }
//        } else {
//            recyclerView.setVisibility(View.GONE);
//            txtEmptyView.setVisibility(View.VISIBLE);
//            txtEmptyView.setText("No trip List Found");
//        }

        if(!TextUtils.isEmpty(profilePhoto)){
            tripListAdapter.setProfilePhoto(profilePhoto);
        }
    }

    private void setUpDefaults() {
    }

    private void setUpEvents() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (DeviceUtils.isInternetConnected(ViewTripsActivity.this)) {
                    totalCount = 0;
                    start = 0;
                    isLoading = false;
                    getTripList(false);
                } else {
                    AlertUtils.showAlert(ViewTripsActivity.this, getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    }, false);
                }
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem, totalItemCount;

                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                DravaLog.print("totalItemCount==>" + totalItemCount + "==>lastVisibleItem==>" + lastVisibleItem + "==>isLoading==>" + isLoading);
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    onLoadMore();
                    isLoading = true;
                }
//                if (linearLayoutManager.findFirstVisibleItemPosition() == 0){
//                    swipeRefreshLayout.setRefreshing(true);
//                }
            }
        });
//        recyclerView.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView absListView, int i) {
//
//            }
//
//            @Override
//            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                int position = firstVisibleItem + visibleItemCount;
//                int limit = totalItemCount;
//                int totalItems = menteeTripList.size();
//
//                DravaLog.print("TOTAL ITEMS=>"+String.valueOf(totalItems));
//                DravaLog.print("LIMIT :::::::::::: "+ String.valueOf(limit));
//                DravaLog.print("POSITION ::::::::::::"+ String.valueOf(position));
//                DravaLog.print("REFRESHING :::::::::::::::"+ String.valueOf(swipeRefreshLayout.isRefreshing()));
//
//                if(position>=limit && totalItemCount>0 && !swipeRefreshLayout.isRefreshing() && position <totalItems){
//                    swipeRefreshLayout.setRefreshing(true);
//                    //In the below method I made my call to my ws...
////                    onRefresh();
//
//                }
//            }
//        });
        if (imgPassenger !=null){
            imgPassenger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Set the button's appearance
                    imgPassenger.setSelected(!imgPassenger.isSelected());

                    if (imgPassenger.isSelected()) {
                        //Handle selected state change
                        isPassenger = "1";
                    } else {
                        //Handle de-select state change
                        isPassenger = "0";
                    }
                    start = 0;
                    getTripList(true);
                }
            });
        }
    }

    public void getTripList(boolean showProgress) {

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        if (showProgress) {
            progressDialog.show();
        }
        getApp().getRetrofitInterface().getTripList(userId, String.valueOf(start),isPassenger).enqueue(new RetrofitCallback<ResponseBody>() {

            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                DravaLog.print("Response=>" + content);
                AppLog.print(ViewTripsActivity.this,"Response=========>getTripList======>Success");
                TripListParser tripListParser = new Gson().fromJson(content, TripListParser.class);
                if (tripListParser != null && tripListParser.meta.code == 200) {
                    if (isLoading && menteeTripList.size() > 0) {
                        isLoading = false;
                        menteeTripList.remove(menteeTripList.size() - 1);
                        tripListAdapter.notifyItemRemoved(menteeTripList.size());
                        menteeTripList.addAll(tripListParser.MenteeTripList);
                        tripListAdapter.updateList(menteeTripList);
                    } else {
                        menteeTripList = tripListParser.MenteeTripList;
                        tripListAdapter.updateList(menteeTripList);
                    }
                    start += tripListParser.meta.ListedCount;
                    totalCount = tripListParser.meta.TotalCount;
                } else {
                    recyclerView.setVisibility(View.GONE);
                    txtEmptyView.setVisibility(View.VISIBLE);
                    txtEmptyView.setText(tripListParser.meta.errorMessage);
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                DravaLog.print("Response=>message=>" + message);
                if (t !=null && t.getCause()!=null) {
                    AppLog.print(ViewTripsActivity.this, "Response=========>getTripList======>Failed=>message=====>Cause====>"+t.getCause());
                }
            }
        });
    }

    private void onLoadMore() {
        DravaLog.print("start==>" + start + "==>totalCount==>" + totalCount);
        if (DeviceUtils.isInternetConnected(this)) {
            if (start < totalCount) {
                menteeTripList.add(null);
                tripListAdapter.notifyItemInserted(menteeTripList.size() - 1);
                getTripList(false);
            }
        } else {
            AlertUtils.showAlert(this, getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }, false);
        }
    }
}
