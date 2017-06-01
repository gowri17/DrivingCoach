package com.drava.android.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.Settings;
import android.test.suitebuilder.TestMethod;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.drava.android.R;
import com.drava.android.base.BaseActivity;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class WebViewActivity extends BaseActivity {

    private static String LOAD_URL = "url";
    private WebView webView;
    private ProgressBar mProgressBar;
    private String str_url = "";
    private String strTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        init();
        setUpDefaults();
        setUpEvents();
    }

    private void init() {
        webView = (WebView) findViewById(R.id.web_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);

        if(getIntent() != null) {
            str_url = getIntent().getStringExtra("WEB_URL");
            strTitle = getIntent().getStringExtra("TITLE");
        }
        setToolbar(strTitle);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setStatusBarColor();
    }

    private void setUpDefaults() {

        if (DeviceUtils.isInternetConnected(this)) {
            if (!TextUtils.isNullOrEmpty(str_url)) {
                webView.loadUrl(str_url);
                mProgressBar.setVisibility(View.GONE);
            }
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    mProgressBar.setVisibility(View.GONE);
                    super.onPageFinished(view, url);
                }

                @Override
                public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                    mProgressBar.setVisibility(View.GONE);
                    super.onReceivedError(view, request, error);
                }
            });
        } else {
            AlertUtils.showAlert(this, getString(R.string.check_your_internet_connection));
        }
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(false);

    }

    private void setUpEvents() {

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

