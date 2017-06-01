package com.drava.android.activity.mentor_mentee.view_profile;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.base.BaseFragment;
import com.drava.android.base.Log;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.google.gson.Gson;
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class OverallFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {
    protected LayoutManager linearLayoutManager;
    protected RecyclerView recyclerView;
    protected MenteeProfileAdapter profileAdapter;
    protected StickyRecyclerHeadersDecoration headersDecor;
    protected View rootView;
    protected ArrayList<ProfileBeanClass.MenteeProfileBean> profileList;
    protected ProfileBeanClass.MenteeProfileBean rowContent;
    protected ProfileBeanClass rowProfile;
    protected TextView txtviewNoResult;
    protected LinearLayout layoutFragment;
    protected ProgressDialog progressDialog;
    protected TextView txtviewoverallScore;
    protected SwipeRefreshLayout swipeRefreshLayout;
    protected NestedScrollView nestedScrollView;
    protected boolean isLoading = false;
    protected int totalCount = 0, start = 0;
    protected int visibleThreshold = 1;
    private RelativeLayout  loadmoreLayout;
    protected boolean showProgress;
    protected boolean loadmore;
    protected ArrayList<ProfileBeanClass.MenteeProfileBean> menteeTripRepostList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_overall, container, false);
        init();
        setupDefaults();
        setupEvents();
        return rootView;

    }

    public void init() {
        linearLayoutManager = new LayoutManager(getContext());
        linearLayoutManager.setOrientation(LayoutManager.VERTICAL);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        txtviewNoResult = (TextView) rootView.findViewById(R.id.txt_no_record_found);
        layoutFragment = (LinearLayout) rootView.findViewById(R.id.layoutOverallReport);
        txtviewoverallScore = (TextView) rootView.findViewById(R.id.txtviewOverallScore);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_refresh);
        nestedScrollView = (NestedScrollView) rootView.findViewById(R.id.nestedScrollView);
        loadmoreLayout = (RelativeLayout) rootView.findViewById(R.id.rl_root_progress_bar);
        menteeTripRepostList = new ArrayList<>();
    }

    public void setupDefaults() {
        settingProfileAdapter();
        setupGettingTripReport();

    }

    public void setupEvents() {
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
                if (isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold) && totalItemCount != rowProfile.meta.TotalCount) {
                    onLoadMore();
                    isLoading = false;
                }
            }
        });


    }

    public void setupGettingTripReport() {
        if (DeviceUtils.isInternetConnected(getActivity())) {
            getTripReport(true, false);
        } else {
            AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }, false);
        }
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.color_theme));
    }

    public void getTripReport(boolean swipe, boolean loading) {
        showProgress = swipe;
        loadmore = loading;
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.setCancelable(false);
        if (showProgress) {
            progressDialog.show();
        }

        if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
            progressDialog.dismiss();
        }
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }

        String strUserID = getArguments().getString("menteeID");
        getApp().getRetrofitInterface().getTripReport(strUserID, String.valueOf(start), "0").enqueue(new RetrofitCallback<ResponseBody>() {


            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);


                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }

                try {
                    rowProfile = new Gson().fromJson(content, ProfileBeanClass.class);

                    if (rowProfile != null && rowProfile.meta.code == 200) {

                        profileList = new ArrayList<>();
                        JSONObject mainJSONObj = null;
                        mainJSONObj = new JSONObject(content);
                        JSONObject menteeReportObject = mainJSONObj.getJSONObject("MenteeReportList");
                        String overallScore = menteeReportObject.getString("overallScores");
                        txtviewoverallScore.setText(overallScore);
                        JSONObject categoryJSONObj = menteeReportObject.getJSONObject("report");

                        Iterator<String> iterator = categoryJSONObj.keys();

                        while (iterator.hasNext()) {
                            String key = iterator.next();
                            Log.i("TAG Prethivi", "key:" + key + "--Value::" + categoryJSONObj.optString(key));
                            categoryJSONObj.optString(key);
                            JSONArray dynamicArray = categoryJSONObj.getJSONArray(key);
                            for (int i = 0; i < dynamicArray.length(); i++) {
                                rowContent = new Gson().fromJson(dynamicArray.get(i).toString(), ProfileBeanClass.MenteeProfileBean.class);
                                rowContent.setMonthOfYear(setupMonthofYear(key, rowContent.Month));
                                Log.i("Element adding...", "Dynamic array elements:" + ProfileBeanClass.MenteeProfileBean.class);
                                profileList.add(rowContent);
                            }

//                            if (isLoading && profileList.size() > 0) {
//                                isLoading = false;
//                                profileList.remove(profileList.size() - 1);
//                                profileAdapter.notifyItemRemoved(profileList.size());
//                                profileList.addAll(rowProfile.MenteeTripReport);
//                                profileAdapter.updateList(rowProfile.MenteeTripReport);
//                            } else {
//                                profileList = rowProfile.MenteeTripReport;
//                                profileAdapter.updateList(profileList);
//                               isLoading=true;
//                            }
//                            start += rowProfile.meta.ListedCount + 1;
//                            totalCount = rowProfile.meta.TotalCount;
                        }

                        layoutFragment.setVisibility(View.VISIBLE);
                        loadmoreLayout.setVisibility(View.GONE);
//                        if (profileList.size() < 3) {
//                            isLoading = false;
//                            start = 0;
//                        } else {
                            start = start + rowProfile.meta.ListedCount;
                            isLoading = true;
//                        }

                        if (showProgress) {
                            menteeTripRepostList.clear();
                            menteeTripRepostList.addAll(profileList);
                            android.util.Log.d("Swipe is called", "" + menteeTripRepostList.size());
                            profileAdapter.notifyDataSetChanged();
                        } else if (loadmore) {
                            menteeTripRepostList.addAll(profileList);
                            profileAdapter.notifyDataSetChanged();
                            loadmore = false;
                        }

                    } else {
                        nestedScrollView.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        layoutFragment.setVisibility(View.GONE);
                        txtviewNoResult.setVisibility(View.VISIBLE);
                        txtviewNoResult.setText(rowProfile.meta.errorMessage);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                DravaLog.print("Response Successfull Prethivi=>" + content);
                AppLog.print(getActivity(), "overallFragment===============>getTripReport=======>Response==========>Successfull");

            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
            }
        });

    }

    public void settingProfileAdapter() {
        profileAdapter = new MenteeProfileAdapter(menteeTripRepostList, getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        headersDecor = new StickyRecyclerHeadersDecoration(profileAdapter);
        recyclerView.addItemDecoration(headersDecor);
        recyclerView.setAdapter(profileAdapter);
    }

    @SuppressLint("SimpleDateFormat")
    public String setupMonthofYear(String year, String month) {

        String result;
        Calendar ca1 = Calendar.getInstance();

        ca1.set(Integer.parseInt(year), Integer.parseInt(month) - 1, 10);

        java.util.Date d = new java.util.Date(ca1.getTimeInMillis());

        result = new SimpleDateFormat("MMM").format(d);

        return result;
    }

    @Override
    public void onRefresh() {

        txtviewNoResult.setVisibility(View.GONE);
        if (DeviceUtils.isInternetConnected(getActivity())) {
            totalCount = 0;
            start = 0;
            getTripReport(true, false);
            swipeRefreshLayout.setRefreshing(false);
        } else {
            AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            }, false);
        }
    }

    private void onLoadMore() {
        DravaLog.print("start==>" + start + "==>totalCount==>" + totalCount);

        if (DeviceUtils.isInternetConnected(getActivity())) {
            if (isLoading) {
                loadmoreLayout.setVisibility(View.VISIBLE);
                 new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getTripReport(false, true);
                    }
                },2000);

            }
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
