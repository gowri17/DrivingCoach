package com.drava.android.activity.trips;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.base.BaseFragment;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DividerItemDecoration;
import com.drava.android.utils.DravaLog;
import com.google.gson.Gson;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by admin on 11/21/2016.
 */

public class TripListFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TripListAdapter tripListAdapter;
    private TextView txtEmptyView;
    private List<TripListParser.MenteeTripList> menteeTripList;
    private ProgressDialog progressDialog;
    private boolean isLoading = false;
    private int totalCount = 0, start = 0;
    private int visibleThreshold = 1;
    private String isPassenger = "0";
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        try {
            getApp().getDBSQLite().backupDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        View view = inflater.inflate(R.layout.fragment_trip_list, container, false);
        init(view);
        setUpDefaults();
        setUpEvents();
        return view;
    }

    private void init(View view) {
        view.findViewById(R.id.default_toolbar).setVisibility(View.GONE);
        recyclerView = (RecyclerView) view.findViewById(R.id.header_list_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        txtEmptyView = (TextView) view.findViewById(R.id.empty_view);
        menteeTripList = new ArrayList<>();
        tripListAdapter = new TripListAdapter(getActivity(), menteeTripList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new StickyRecyclerHeadersDecoration(tripListAdapter));
        Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.cl_recycler_view_divider);
        recyclerView.addItemDecoration(new DividerItemDecoration(drawable));
        recyclerView.setAdapter(tripListAdapter);

        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.color_theme));
        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            if (DeviceUtils.isInternetConnected(getActivity())) {
                getTripList(true);
            } else {
                AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }, false);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            txtEmptyView.setVisibility(View.VISIBLE);
            txtEmptyView.setText("No trip List Found");
        }

    }

    private void setUpDefaults() {
    }

    private void setUpEvents() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (DeviceUtils.isInternetConnected(getActivity())) {
                    totalCount = 0;
                    start = 0;
                    isLoading = false;
                    getTripList(false);
                } else {
                    AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
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
                DravaLog.print("start==>" + start + "==>totalCount==>" + totalCount);
                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold) && start < totalCount) {
                    isLoading = true;
                    onLoadMore();
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
        if (getHome().imgPassenger !=null){
            getHome().imgPassenger.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Set the button's appearance
                    getHome().imgPassenger.setSelected(!getHome().imgPassenger.isSelected());

                    if (getHome().imgPassenger.isSelected()) {
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

    private HomeActivity getHome() {
        return (HomeActivity) getActivity();
    }

    public void getTripList(boolean showProgress) {

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        if (showProgress) {
            progressDialog.show();
        }
        getApp().getRetrofitInterface().getTripList(getApp().getUserPreference().getUserId(), String.valueOf(start),isPassenger).enqueue(new RetrofitCallback<ResponseBody>() {

            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                DravaLog.print("Response========>getTripList=======>onSuccessCallback===>content==>" + content);
                AppLog.print(getActivity(),"Response========>getTripList=======>onSuccessCallback");
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
                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                DravaLog.print("Response========>getTripList=======>onFailureCallback" + message);
                AppLog.print(getActivity(),"Response========>getTripList=======>onFailureCallback");
            }
        });
    }

    private void onLoadMore() {
        if (DeviceUtils.isInternetConnected(getActivity())) {
                isLoading = true;
                menteeTripList.add(null);
                tripListAdapter.notifyItemInserted(menteeTripList.size() - 1);
                getTripList(false);
        } else {
            AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }, false);
        }
    }

}
