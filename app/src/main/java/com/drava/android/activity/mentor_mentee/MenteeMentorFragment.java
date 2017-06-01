package com.drava.android.activity.mentor_mentee;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drava.android.DravaApplication;
import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseFragment;
import com.drava.android.parser.UserInformationParser;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.ui.DravaTextView;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.AppLog;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;


public class MenteeMentorFragment extends BaseFragment implements AppConstants, MenteeMentorInterface, PurchaseCompleteListener {
    private static final String TAG = MenteeMentorFragment.class.getSimpleName();
    private ExpandableListView expandableListView;
    private MenteeMentorAdapter menteeMentorAdapter;
    private HashMap<String, String> expandableList = new HashMap();
    private int lastExpandedGroup = -1;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<MentorListParser.MentorList> mentorMenteeLists = new ArrayList<>();
    private TextView txtEmptyView;
    private boolean isLoading = false;
    private int totalCount = 0, start = 0;
    private int visibleThreshold = 1;
    private boolean userScrolled = false;
    private ProgressBar progressBar;
    private int selectedMenteePosition;
    private RelativeLayout tutorialLayout;
    private int referralPoints;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mentee_mentor, container, false);
        init(view);
        setUpDefaults();
        setUpEvents();
        return view;
    }

    private void init(View view) {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        expandableListView = (ExpandableListView) view.findViewById(R.id.expandable_list);
        txtEmptyView = (DravaTextView) view.findViewById(R.id.empty_view);
        progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
        tutorialLayout = (RelativeLayout) view.findViewById(R.id.tutorial_mentee_mentor);
        setHasOptionsMenu(true);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void setUpDefaults() {
        if(!getApp().getUserPreference().getIsTutMenteeMentorViewed()){
            if(getApp().getUserPreference().getMentorOrMentee().equals(MENTEE))
                tutorialLayout.setBackground(getContext().getDrawable(R.drawable.tut_mentor_action_without_header));
            else
                tutorialLayout.setBackground(getContext().getDrawable(R.drawable.tut_mentees_action_without_header));
            getApp().getUserPreference().setIsTutMenteeMentorViewed(true);
        }else{
            tutorialLayout.setVisibility(View.GONE);
        }
        menteeMentorAdapter = new MenteeMentorAdapter(getActivity(), mentorMenteeLists, this);
        setIndicatorRight();
        expandableListView.setAdapter(menteeMentorAdapter);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setMessage(getString(R.string.loading));
        if (DeviceUtils.isInternetConnected(getActivity())) {
            progressDialog.show();
            if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
                getMentorList();
            } else {
                getMenteeList();
            }
        } else {
            AlertUtils.showAlert(getActivity(), getString(R.string.check_your_internet_connection));
        }
        swipeRefreshLayout.setEnabled(false);
        swipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(getActivity(), R.color.color_theme));
        ((HomeActivity)getActivity()).setOnPurchaseCompleteListener(this);
    }

    private void setUpEvents() {
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                if (lastExpandedGroup != -1
                        && i != lastExpandedGroup) {
                    expandableListView.collapseGroup(lastExpandedGroup);
                    expandableListView.setItemChecked(lastExpandedGroup, false);
                }

                expandableListView.setItemChecked(i, true);
                lastExpandedGroup = i;

            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int i) {
                expandableListView.setItemChecked(i, false);
            }
        });

        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
                if (mentorMenteeLists.size() > 0 && mentorMenteeLists.get(i) == null) {
                    return true;
                }
                return false;
            }
        });


        expandableListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    swipeRefreshLayout.setEnabled(true);
                } else {
                    swipeRefreshLayout.setEnabled(false);
                }
                int lastVisibleItem = absListView.getLastVisiblePosition();

                if (userScrolled && !isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold) && start < totalCount) {
                    isLoading = true;
                    onLoadMore();

                }
                Log.d(TAG, "onScroll: firstVisibleItem=>" + firstVisibleItem + "==>visibleItemCount=>" + visibleItemCount + "==>totalItemCount==>" + totalItemCount + "==>lastVisibleItem==>" + lastVisibleItem);
            }
        });

        expandableListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (view == expandableListView && motionEvent.getAction() == MotionEvent.ACTION_MOVE) {
                    userScrolled = true;
                }
                return false;
            }
        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                txtEmptyView.setVisibility(View.GONE);
                if (DeviceUtils.isInternetConnected(getActivity())) {
                    totalCount = 0;
                    start = 0;
                    isLoading = false;
                    mentorMenteeLists = new ArrayList<>();
                    if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
                        getMentorList();
                    } else {
                        getMenteeList();
                    }
                } else {
                    AlertUtils.showAlert(getActivity(), getString(R.string.check_your_internet_connection));
                }
            }

        });

        tutorialLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tutorialLayout.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.mentor_mentee_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.item_map_location) {
            FragmentChangeListener fragmentChangeListener = (FragmentChangeListener) getActivity();
            fragmentChangeListener.replaceFragment();
        }
        return super.onOptionsItemSelected(item);
    }

    private void getMentorList() {

        getApp().getRetrofitInterface().getMentorList(String.valueOf(start)).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }
                MentorListParser mentorListParser = new Gson().fromJson(content, MentorListParser.class);

                if (!(mentorListParser.meta.code == 200)) {
                    expandableListView.setVisibility(View.GONE);
                    txtEmptyView.setVisibility(View.VISIBLE);
                    txtEmptyView.setText(mentorListParser.meta.errorMessage);
                } else {
                    if (isLoading && mentorMenteeLists.size() > 0) {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                        mentorMenteeLists.addAll(mentorListParser.MentorList);
                        menteeMentorAdapter.updateList(mentorMenteeLists);
                    } else {
                        mentorMenteeLists = mentorListParser.MentorList;
                        menteeMentorAdapter.updateList(mentorMenteeLists);
                    }
                    start += mentorListParser.meta.ListedCount;
                    totalCount = mentorListParser.meta.TotalCount;
                    expandableListView.setVisibility(View.VISIBLE);
                    txtEmptyView.setVisibility(View.GONE);
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
            }
        });
    }

    public void getMenteeList() {

        getApp().getRetrofitInterface().getMenteeList(String.valueOf(start)).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }
                Log.d(TAG, "onSucessCallback: content=>" + content);
                MentorListParser mentorListParser = new Gson().fromJson(content, MentorListParser.class);

                if (!(mentorListParser.meta.code == 200)) {
                    expandableListView.setVisibility(View.GONE);
                    txtEmptyView.setVisibility(View.VISIBLE);
                    txtEmptyView.setText(mentorListParser.meta.errorMessage);
                } else {
                    if (isLoading && mentorMenteeLists.size() > 0) {
                        isLoading = false;
                        progressBar.setVisibility(View.GONE);
                        mentorMenteeLists.addAll(mentorListParser.MenteeList);
                        menteeMentorAdapter.updateList(mentorMenteeLists);
                    } else {
                        mentorMenteeLists = mentorListParser.MenteeList;
                        menteeMentorAdapter.updateList(mentorMenteeLists);
                    }
                    start += mentorListParser.meta.ListedCount;
                    totalCount = mentorListParser.meta.TotalCount;
                    expandableListView.setVisibility(View.VISIBLE);
                    txtEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void setIndicatorRight() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            expandableListView.setIndicatorBounds(width - GetPixelFromDips(60), width - GetPixelFromDips(5));
        } else {
            expandableListView.setIndicatorBoundsRelative(width - GetPixelFromDips(60), width - GetPixelFromDips(5));
        }
    }

    public int GetPixelFromDips(float pixels) {
        // Get the screen's density scale
        final float scale = getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (pixels * scale + 0.5f);
    }

    @Override
    public void onRemoveClick(final MentorListParser.MentorList mentorList) {

        if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
            AlertUtils.showAlert(getActivity(), "Alert", String.format(getResources().getString(R.string.remove_alert), "mentor"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    removeMentor(mentorList);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }, false);
        } else {
            AlertUtils.showAlert(getActivity(), "Alert", String.format(getResources().getString(R.string.remove_alert), "mentee"), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    removeMentee(mentorList);
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }

            }, false);
        }
    }

    public void getMentorReferralPoints(final int position){        //R.L v1.1
        AppLog.print(getActivity(), "In app purchase : ---------------Getting Mentor Referral point information---------------");
        DravaApplication.getApp().getRetrofitInterface().getUserInformation().enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                UserInformationParser userInformationParser = new Gson().fromJson(content, UserInformationParser.class);
                if(userInformationParser.getMeta().code == 200){
                    referralPoints = Integer.parseInt(userInformationParser.getUserDetails().ReferralPoints);
                    AppLog.print(getActivity(), "In app purchase : ---------------Mentor Referral Point : "+referralPoints);

                    if(referralPoints > 0) {
                        getApp().getRetrofitInterface().authorizeToLocateMentee(mentorMenteeLists.get(position).UserId).enqueue(new RetrofitCallback<ResponseBody>() {
                            @Override
                            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                                super.onSuccessCallback(call, content);

                                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                                    progressDialog.dismiss();
                                }
                                AppLog.print(getActivity(), "Is Authorized to Locate Mentee Success -----------> : " + content);
                                DravaLog.print("Is Authorized to Locate Mentee Success -----------> : " + content);
                                selectedMenteePosition = position;
                                ViewMenteeParser viewMenteeParser = new Gson().fromJson(content, ViewMenteeParser.class);
                                if (viewMenteeParser.meta.code == 201) {
                                    AppLog.print(getActivity(), "In app purchase : Authorized to view mentee using token....");
                                    ((HomeActivity) getActivity()).setAvailableToken(viewMenteeParser.ReferralPoints.RemainingPoints);
                                    Intent intent = new Intent(getActivity(), ViewOnMapActivity.class);
                                    intent.putExtra(MENTOR_LIST, mentorMenteeLists.get(selectedMenteePosition));
                                    intent.putExtra(MENTEE_LOCATION, viewMenteeParser.ReferralPoints.MenteeLocation);
                                    intent.putExtra(MENTEE_LATTITUDE, viewMenteeParser.ReferralPoints.MenteeLatitude);
                                    intent.putExtra(MENTEE_LONGITUDE, viewMenteeParser.ReferralPoints.MenteeLongitude);
                                    startActivity(intent);
                                } else {
                                    ((HomeActivity) getActivity()).purchaseViewMenteeToken(position);
                                    AppLog.print(getActivity(), "In app purchase : 2.UnAuthorized to view mentee so Start to purchase the token....");
                                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Oh, no! All your view Mentee token has Consumed! Try buying some!", Snackbar.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                                super.onFailureCallback(call, t, message, code);

                                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                                    progressDialog.dismiss();
                                }
                                AppLog.print(getActivity(), "Is Authorized to Locate Mentee Failure -----------> : " + message + "  Code--> : " + code);
                                DravaLog.print("Is Authorized to Locate Mentee Failure -----------> : " + message + " Code--> : " + code);
                            }
                        });
                    }else{
                        if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                            progressDialog.dismiss();
                        }
                        ((HomeActivity) getActivity()).purchaseViewMenteeToken(position);
//                    ((HomeActivity)getActivity()).manualConsume();
                        AppLog.print(getActivity(), "In app purchase : 1.UnAuthorized to view mentee so Start to purchase the token....");
                        Snackbar.make(getActivity().findViewById(android.R.id.content), "Oh, no! All your view Mentee token has Consumed! Try buying some!", Snackbar.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);

                if (progressDialog != null && progressDialog.isShowing() && isAdded()) {
                    progressDialog.dismiss();
                }
                AppLog.print(getActivity(), "---------------failure Getting Mentor current location information---------------");
            }
        });
    }

    @Override
    public void checkUserTokenWithGooglePlayToViewMentee(final int position) {
//      For In-app purchase       //R.L v1.1

        AlertUtils.showAlert(getActivity(), getString(R.string.str_credit_usage), getString(R.string.str_consume_credit_alert),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                        if(DeviceUtils.isInternetConnected(getActivity())){
                            progressDialog.show();
                            getMentorReferralPoints(position);
                        } else {
                            AlertUtils.showAlert(getActivity(), getResources().getString(R.string.check_your_internet_connection), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            }, false);
                        }
                    }
                },

                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                }, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + "," + resultCode + "," + data);

    }

    private void removeMentor(MentorListParser.MentorList mentorList) {
        getApp().getRetrofitInterface().deleteMentor(mentorList.UserId).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                super.onFailure(call, t);
            }

            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                MetaParser metaParser = new Gson().fromJson(content, MetaParser.class);
                Toast.makeText(getActivity(), metaParser.notifications[0], Toast.LENGTH_SHORT).show();
                if (metaParser.meta.code.equals("200")) {
                    totalCount = 0;
                    start = 0;
                    getMentorList();
                }

            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
            }
        });
    }


    private void removeMentee(MentorListParser.MentorList mentorList) {
        getApp().getRetrofitInterface().deleteMentee(mentorList.UserId).enqueue(new RetrofitCallback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                super.onResponse(call, response);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                super.onFailure(call, t);
            }

            @Override
            public void onSuccessCallback(Call<ResponseBody> call, String content) {
                super.onSuccessCallback(call, content);
                MetaParser metaParser = new Gson().fromJson(content, MetaParser.class);
                Toast.makeText(getActivity(), metaParser.notifications[0], Toast.LENGTH_SHORT).show();
                if (metaParser.meta.code.equals("200")) {
                    totalCount = 0;
                    start = 0;
                    getMenteeList();
                }
            }

            @Override
            public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                super.onFailureCallback(call, t, message, code);
            }
        });
    }

    private void onLoadMore() {
        DravaLog.print("start==>" + start + "==>totalCount==>" + totalCount);
        if (DeviceUtils.isInternetConnected(getActivity())) {


            progressBar.setVisibility(View.VISIBLE);
            if (getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)) {
                getMentorList();
            } else {
                getMenteeList();
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

    @Override
    public void onPurchaseCompleteListener() {
        checkUserTokenWithGooglePlayToViewMentee(selectedMenteePosition);
    }
}
