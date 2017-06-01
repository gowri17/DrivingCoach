package com.drava.android.activity.notifications;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.base.BaseFragment;
import com.drava.android.parser.NotificationListParser;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DividerItemDecoration;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;
import com.google.gson.Gson;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class NotificationFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener, NotificationDeleteListener{
    private RecyclerView notificationList;
    private LinearLayoutManager layoutManager;
    private NotficationAdapter notificationAdapter;
    private Drawable drawable;
    private ProgressDialog progressDialog;
    private RelativeLayout progressBarLayout;
    private boolean isLoading = false;
    private int visibleThreshold = 1;
    private int totalCount = 0, start = 0;
    private ArrayList<NotificationListParser.NotificationTrackingList> notificationTrackingLists;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView txtNoNotificationFound;
    private boolean deleteStatus=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        init(view);
        setupDefault();
        return view;
    }

    public void init(View view) {
        notificationList = (RecyclerView) view.findViewById(R.id.notification_list);
        progressBarLayout = (RelativeLayout) view.findViewById(R.id.progress_bar_layout);
        layoutManager = new LinearLayoutManager(getActivity());
        drawable = ContextCompat.getDrawable(getActivity(), R.drawable.notification_div_col);
        swipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipe_refresh);
        txtNoNotificationFound = (TextView) view.findViewById(R.id.txt_no_notification);

        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);

        notificationTrackingLists = new ArrayList<>();
        notificationAdapter = new NotficationAdapter(getActivity(), notificationTrackingLists);
        notificationAdapter.setNotificationDeleteListener(this);
    }

    public void setupDefault() {
        setupRecycleData();
        getNotificationList(true, false);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.theme_color));
        swipeRefreshLayout.setOnRefreshListener(this);

        notificationList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastVisibleItem, totalItemCount;
                LinearLayoutManager layoutManager = (LinearLayoutManager)notificationList.getLayoutManager();
                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                DravaLog.print("totalItemCount==>" + totalItemCount + "==>lastVisibleItem==>" + lastVisibleItem + "==>isLoading==>" + isLoading);

                if(!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)){
                    onLoadMore();
                    isLoading = true;
                }
            }
        });
    }

    private void setupRecycleData() {
        notificationList.setLayoutManager(layoutManager);
        notificationList.addItemDecoration(new DividerItemDecoration(drawable,true));
        notificationList.setAdapter(notificationAdapter);
    }

    private void onLoadMore(){
        if(DeviceUtils.isInternetConnected(getActivity())) {
            if (start < totalCount) {
                getNotificationList(false, true);
            }
        }else {
            AlertUtils.showAlert(getActivity(), getString(R.string.check_your_internet_connection, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }));
        }
    }

    private void getNotificationList(boolean isProgressDialog, final boolean loadMoreProgress){
        if(isProgressDialog){
            progressDialog.show();
        }
        if(loadMoreProgress){
            progressBarLayout.setVisibility(View.VISIBLE);
        }

        getApp().getRetrofitInterface().getNotificationList(String.valueOf(start)).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);

                if(progressDialog != null && progressDialog.isShowing() && isAdded()){
                    progressDialog.dismiss();
                }
                if(progressBarLayout != null && progressBarLayout.getVisibility() == View.VISIBLE){
                    progressBarLayout.setVisibility(View.INVISIBLE);
                }
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }

                if(!TextUtils.isNullOrEmpty(content)){
                    NotificationListParser notificationListParser = new Gson().fromJson(content, NotificationListParser.class);
                    if(notificationListParser != null && notificationListParser.meta.code == 200){
                        swipeRefreshLayout.setVisibility(View.VISIBLE);
                        txtNoNotificationFound.setVisibility(View.GONE);
                        isLoading = false;
                        if(!loadMoreProgress){
                            notificationTrackingLists.clear();
                        }
                        notificationTrackingLists.addAll(notificationListParser.notificationTrackingList);
                        totalCount = notificationListParser.meta.TotalCount;
                        start += notificationListParser.meta.ListedCount;
                    }
                }

                if(notificationTrackingLists.size() > 0) {
                    notificationAdapter.updateNotificaitonList(notificationTrackingLists);
                    Log.e("Response", "Notification List Response : " + content);
                }
                else{
                    swipeRefreshLayout.setVisibility(View.GONE);
                    txtNoNotificationFound.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);

                if(progressDialog != null && progressDialog.isShowing() && isAdded()){
                    progressDialog.dismiss();
                }
                if(progressBarLayout != null && progressBarLayout.getVisibility() == View.VISIBLE){
                    progressBarLayout.setVisibility(View.INVISIBLE);
                }
                if(swipeRefreshLayout.isRefreshing()){
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }


    @Override
    public void onRefresh() {
        if(DeviceUtils.isInternetConnected(getActivity())) {
            totalCount = 0;
            start = 0;
            isLoading = false;
            getNotificationList(false, false);
        }else {
            AlertUtils.showAlert(getActivity(), getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }, false);
        }
    }

    @Override
    public boolean deleteNotification(String notificationId) {
        if(DeviceUtils.isInternetConnected(getActivity())){
            getApp().getRetrofitInterface().deleteNotification(notificationId).enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    super.onSuccessCallback(call, content);
                    deleteStatus = true;
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    deleteStatus = false;
                }
            });

        }else {
            AlertUtils.showAlert(getActivity(), getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }, false);
        }
        return deleteStatus;
    }
}
