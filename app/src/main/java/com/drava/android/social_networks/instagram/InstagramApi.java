package com.drava.android.social_networks.instagram;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.drava.android.R;
import com.drava.android.base.AppConstants;
import com.drava.android.base.BaseActivity;
import com.drava.android.base.Log;
import com.drava.android.rest.RetrofitCallback;
import com.drava.android.social_networks.SocialNetworksUpdateDataInterface;
import com.drava.android.utils.TextUtils;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * Created by evuser on 27-10-2016.
 */

public class InstagramApi
{
    private Activity activity;
    private SocialNetworksUpdateDataInterface listener;
    private ProgressBar progressBar;
    private Dialog dialog;
    private String TAG =  InstagramApi.class.getName();

    public InstagramApi(Activity activity){
        this.activity = activity;
    }

    public void callInstagramSignIn(String url){
        dialog = new Dialog(activity);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        View instagramView = LayoutInflater.from(activity).inflate(R.layout.login_dialog_layout, null);
        dialog.setContentView(instagramView);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setGravity(Gravity.CENTER);

        WebView webView = (WebView)instagramView.findViewById(R.id.web_view);
        progressBar = (ProgressBar)instagramView.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        webView.setHorizontalFadingEdgeEnabled(false);
        webView.setVerticalFadingEdgeEnabled(false);
        webView.setWebViewClient(new AuthWebViewClient());
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
        dialog.show();
    }

    public void setListener(SocialNetworksUpdateDataInterface listener){
        this.listener = listener;
    }

    public class AuthWebViewClient extends WebViewClient{
        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            if(url.contains("access_token")){
                String parts[] = view.getUrl().split("=");
                Log.e("Instagram onPageFinished url ----------------------- " + view.getUrl());
                Log.e("Instagram onPageFinished access token ----------------------- " + parts[1]);
                progressBar.setVisibility(View.VISIBLE);

                ((BaseActivity)activity).getApp().getRetrofitInterface().getInstagramSelfUserInfo(parts[1]).enqueue(new RetrofitCallback<ResponseBody>() {
                    @Override
                    public void onSuccessCallback(Call<ResponseBody> call, String content) {
                        super.onSuccessCallback(call, content);
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Instagram Success Response : "+content);
                        dialog.dismiss();

                        if(!TextUtils.isEmpty(content)) {
                            try {
                                JSONObject dataJsonObject = new JSONObject(content);
                                JSONObject jsonObject = dataJsonObject.getJSONObject("data");
                                if(jsonObject != null) {
                                    String userName = jsonObject.optString("username");
                                    String profilePic = jsonObject.optString("profile_picture");
                                    String firstName = jsonObject.optString("full_name");
                                    String instagramId = jsonObject.optString("id");
                                    String lastName = "";
                                    if (firstName.contains(" ")) {
                                        lastName = firstName.substring(firstName.lastIndexOf(' '), firstName.length());
                                        firstName = firstName.substring(0, firstName.indexOf(' ', 0));
                                    }

                                    listener.socialNetworksUpdateUserDataInterface(AppConstants.INSTAGRAM, instagramId, "", userName, firstName, lastName, "", "", profilePic);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }


                    }

                    @Override
                    public void onFailureCallback(Call<ResponseBody> call, Throwable t, String message, int code) {
                        super.onFailureCallback(call, t, message, code);
                        progressBar.setVisibility(View.GONE);
                        Log.e(TAG, "Instagram Failure Message :"+ message);
                        dialog.dismiss();
                    }
                });
            }
            super.onPageFinished(view, url);
        }
    }
}
