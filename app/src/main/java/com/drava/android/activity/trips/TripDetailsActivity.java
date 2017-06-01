package com.drava.android.activity.trips;

import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.share.Sharer;
import com.facebook.share.internal.ShareFeedContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.google.gson.Gson;

import com.drava.android.R;
import com.drava.android.base.BaseActivity;
import com.drava.android.parser.TripViolationDetailParser;
import com.drava.android.parser.TripDetails;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.utils.DateConversion;
import com.drava.android.utils.TextUtils;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;

public class TripDetailsActivity extends BaseActivity {

    private TextView txtDate, txtTime, txtFromAddress, txtToAddress, txtPoints, txtDistance, txtOverallTime, txtSafeDistance, txtSafeTime,
            txtEmptyMsg, txtViolationTitle, txtShareOnFacebook;
    private String tripId;
    private TripDetails tripDetailParser;
    private ScrollView mScrollView;
    private ProgressBar mProgressBar;
    private TripViolationDetailParser mViolationDetailParser;
    private ViewGroup mLinearLayout;
    private RelativeLayout rlRootView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);
        init();
        setDefaults();
        setupEvents();
    }

    private void init() {
        txtDate = (TextView) findViewById(R.id.txt_date);
        txtTime = (TextView) findViewById(R.id.txt_time);
        txtFromAddress = (TextView) findViewById(R.id.txt_from);
        txtToAddress = (TextView) findViewById(R.id.txt_to);
        txtPoints = (TextView) findViewById(R.id.txt_points);
        txtDistance = (TextView) findViewById(R.id.txt_distance);
        txtOverallTime = (TextView) findViewById(R.id.txt_overall);
        txtSafeDistance = (TextView) findViewById(R.id.txt_safe_distance);
        txtSafeTime = (TextView) findViewById(R.id.txt_safe_time);
        mScrollView = (ScrollView) findViewById(R.id.root_scroll_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        txtEmptyMsg = (TextView) findViewById(R.id.txt_empty_msg);
        mLinearLayout = (ViewGroup) findViewById(R.id.violation_container);
        txtViolationTitle = (TextView) findViewById(R.id.txt_violation_title);
        txtShareOnFacebook = (TextView) findViewById(R.id.txt_share_on_facebook);
//        rlRootView = (RelativeLayout) findViewById(R.id.rl_root_view);
        txtEmptyMsg.setVisibility(View.GONE);
        mScrollView.setVisibility(View.GONE);
        txtViolationTitle.setVisibility(View.GONE);
        setToolbar("Trip Details");
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();
    }

    private void setDefaults() {

        if (getIntent() != null) {
            if (getIntent().hasExtra("TripId")) {
                tripId = getIntent().getStringExtra("TripId");
            }
            if (getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)) {
                if (getIntent().hasExtra(PROFILE_PHOTO)) {
                    String profilePhoto = getIntent().getStringExtra(PROFILE_PHOTO);
                    if (!TextUtils.isEmpty(profilePhoto)) {
                        setUserImage(profilePhoto);
                    }
                }
            }
        }
        getTripDetails();
        getViolationDetails();

    }

    private void setupEvents() {
        txtShareOnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareOnFacebook();
            }
        });
    }

    private void shareOnFacebook() {
        if (tripDetailParser != null) {
            if (DeviceUtils.isFacebookInstalled(this)) {
            FacebookSdk.sdkInitialize(getApplicationContext());
            ShareDialog shareDialog = new ShareDialog(this);
//            Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(),
//                    R.drawable.ic_drava);
            Bitmap bitmap = getScreenShot();
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
//                    .setCaption("The 'Hello Facebook' sample  showcases simple Facebook integration")//not allowed in fb
                    .build();
            SharePhotoContent linkContent = new SharePhotoContent.Builder()
                    .addPhoto(photo)
                    .build();
            shareDialog.show(linkContent);
            }else {
                AlertUtils.showAlert(this,getString(R.string.facebook_not_installed));
        }
    }
    }

    public Bitmap getScreenShot() {

        txtShareOnFacebook.setVisibility(View.GONE);
        View toolbarRootView = toolbar;
        toolbarRootView.setDrawingCacheEnabled(true);
        Bitmap toolbarBitmap = Bitmap.createBitmap(toolbarRootView.getDrawingCache());
        toolbarRootView.setDrawingCacheEnabled(false);
        Bitmap content=  getBitmapByView(mScrollView);
        txtShareOnFacebook.setVisibility(View.VISIBLE);
        return combineImages(content,toolbarBitmap);
    }

    public static Bitmap getBitmapByView(ScrollView scrollView) {
        int h = 0;
        Bitmap bitmap = null;
        //get the actual height of scrollview
        for (int i = 0; i < scrollView.getChildCount(); i++) {
            h += scrollView.getChildAt(i).getHeight();
//            scrollView.getChildAt(i).setBackgroundResource(R.color.white);
        }
        // create bitmap with target size
        bitmap = Bitmap.createBitmap(scrollView.getWidth(), h,
                Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        scrollView.draw(canvas);
     /*   FileOutputStream out = null;
        try {
            out = new FileOutputStream("/sdcard/screen_test.png");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            if (null != out) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                out.flush();
                out.close();
            }
        } catch (IOException e) {
            // TODO: handle exception
        }*/
        return bitmap;
    }

    private Bitmap combineImages(Bitmap c, Bitmap s) { // can add a 3rd parameter 'String loc' if you want to save the new image - left some code to do that at the bottom
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("sharing...");
        progressDialog.show();
        Bitmap cs = null;
        int width, height = 0;
            width = c.getWidth() /*+ s.getWidth()*/;
            height = c.getHeight() + s.getHeight();

        cs = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas comboImage = new Canvas(cs);

//        comboImage.drawBitmap(c, 0f, 0f, null);
        comboImage.drawBitmap(s,/* c.getWidth()*/0f, 0f, null);

//        int top = txtTripCreatedDate.getHeight() * 3 / 2 + toolbar.getHeight();
//        int mergeHeight = txtTripCreatedDate.getHeight() * 3 / 2 + toolbar.getHeight() + mapFragment.getView().getHeight();
//        DravaLog.print("Merge==>top===>" + top);
//        DravaLog.print("Merge==>Height===>" + mergeHeight);
        comboImage.drawBitmap(c, null, new Rect(0, s.getHeight(), c.getWidth(), s.getHeight()+c.getHeight()), null);
        // save all clip
        comboImage.save(Canvas.ALL_SAVE_FLAG);

        // store
        comboImage.restore();
        // this is an extra bit I added, just incase you want to save the new image somewhere and then return the location
//        String tmpImg = String.valueOf(System.currentTimeMillis()) + ".png";
//
//        OutputStream os = null;
//        try {
//            os = new FileOutputStream(this.getExternalCacheDir() + tmpImg);
//            cs.compress(Bitmap.CompressFormat.PNG, 100, os);
//        } catch (IOException e) {
//            Log.e("combineImages", "problem combining images", e);
//        }
        progressDialog.dismiss();
        return cs;
    }

    private void getTripDetails() {
        if (!TextUtils.isEmpty(tripId)) {
            getApp().getRetrofitInterface().getTripDetails(tripId).enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    super.onSuccessCallback(call, content);
                    tripDetailParser = new Gson().fromJson(content, TripDetails.class);
                    if (tripDetailParser != null && tripDetailParser.meta.code == 200 && tripDetailParser.TripDetail.size() > 0) {
                        Date startDate = DateConversion.stringToDate(tripDetailParser.TripDetail.get(0).StartTime, "yyyy-MM-dd HH:mm:ss");
                        Date endDate = DateConversion.stringToDate(tripDetailParser.TripDetail.get(0).EndTime, "yyyy-MM-dd HH:mm:ss");
                        if(startDate.getMonth()==endDate.getMonth() && startDate.getDate() == endDate.getDate()){
                            txtDate.setText(DateConversion.formatDate(tripDetailParser.TripDetail.get(0).StartTime, "yyyy-MM-dd HH:mm:ss", "dd, MMMM yyyy "));
                        }else {
                            txtDate.setText(DateConversion
                                    .formatDate(tripDetailParser.TripDetail.get(0).StartTime, "yyyy-MM-dd HH:mm:ss", "dd, MMMM yyyy ") + " - " + DateConversion
                                    .formatDate(tripDetailParser.TripDetail.get(0).EndTime, "yyyy-MM-dd HH:mm:ss", "dd, MMMM yyyy "));
                        }
                        String startTime = DateConversion
                                .formatDate(tripDetailParser.TripDetail.get(0).StartTime, "yyyy-MM-dd HH:mm:ss", "HH:mm ");
                        String endTime = DateConversion
                                .formatDate(tripDetailParser.TripDetail.get(0).EndTime, "yyyy-MM-dd HH:mm:ss", "HH:mm ");
                        txtTime.setText(startTime + " - " + endTime);
                        txtFromAddress.setText(tripDetailParser.TripDetail.get(0).StartLocation);
                        txtToAddress.setText(tripDetailParser.TripDetail.get(0).EndLocation);
                        txtPoints.setText(tripDetailParser.TripDetail.get(0).Scores);
                        txtDistance.setText(tripDetailParser.TripDetail.get(0).Distance + " km");

                        String travelledTime = tripDetailParser.TripDetail.get(0).Hours + " h " +
                                tripDetailParser.TripDetail.get(0).Minutes + " m " +
                                tripDetailParser.TripDetail.get(0).Seconds + " s ";

                        txtOverallTime.setText(travelledTime);
                        mProgressBar.setVisibility(View.GONE);
                        mScrollView.setVisibility(View.VISIBLE);
                    } else {
                        txtEmptyMsg.setVisibility(View.VISIBLE);
                        mProgressBar.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    txtEmptyMsg.setVisibility(View.VISIBLE);
                    mProgressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void getViolationDetails() {

        if (!TextUtils.isEmpty(tripId)) {
            getApp().getRetrofitInterface().getTripViolationDetails(tripId).enqueue(new RetrofitCallback<ResponseBody>() {
                @Override
                public void onSuccessCallback(Call<ResponseBody> call, String content) {
                    super.onSuccessCallback(call, content);
                    mViolationDetailParser = new Gson().fromJson(content, TripViolationDetailParser.class);
                    if (mViolationDetailParser != null && mViolationDetailParser.meta.getCode() == 200 && mViolationDetailParser.tripViolationDetail.size() > 0) {
                        txtViolationTitle.setVisibility(View.VISIBLE);
                        for (int i = 0; i < mViolationDetailParser.tripViolationDetail.size(); i++) {
                            String localisedTime = DateConversion.getLocalTimeFromGMT((mViolationDetailParser.tripViolationDetail.get(i).getDateCreated()));
                            String time = DateConversion
                                    .formatDate(localisedTime, "yyyy-MM-dd HH:mm:ss", "HH:mm");

                            addLayout((i + 1) + ". At " + time + " on " + mViolationDetailParser.tripViolationDetail.get(i).getLocation());

                        }

                    } else {
                        txtViolationTitle.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                    super.onFailureCallback(call, t, message, code);
                    txtViolationTitle.setVisibility(View.GONE);
                }
            });
        }

    }

    private void addLayout(String violationDetail) {
        View layout2 = LayoutInflater.from(this).inflate(R.layout.trip_violation_details_item, mLinearLayout, false);

        TextView txtViolation = (TextView) layout2.findViewById(R.id.txt_trip_violation);

        txtViolation.setText(violationDetail);

        mLinearLayout.addView(layout2);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
