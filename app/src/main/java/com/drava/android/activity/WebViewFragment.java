package com.drava.android.activity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.drava.android.R;
import com.drava.android.base.BaseFragment;
import com.drava.android.utils.AlertUtils;
import com.drava.android.utils.DeviceUtils;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;

import java.net.URL;

import retrofit2.http.Url;

/**
 * Created by admin on 12/27/2016.
 */

public class WebViewFragment extends BaseFragment {
    private static String LOAD_URL = "url";
    private WebView webView;
    private ProgressBar mProgressBar;
    private String str_url = "";

    public static WebViewFragment newInstance(String url) {
        Bundle args = new Bundle();
        args.putString(LOAD_URL,url);
        WebViewFragment fragment = new WebViewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_web_view,container,false);
        init(view);
        setUpDefaults();
        setUpEvents();
        return view;
    }

    private void init(View view) {
        webView = (WebView) view.findViewById(R.id.web_view);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
    }

    private void setUpDefaults() {
        str_url = getArguments().getString(LOAD_URL);
        DravaLog.print("Webview==>StrUrl====>"+str_url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDisplayZoomControls(false);
        if (DeviceUtils.isInternetConnected(getActivity())) {
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
                    DravaLog.print("setUpDefaults: error====>");
                    mProgressBar.setVisibility(View.GONE);
                    super.onReceivedError(view, request, error);
                }
            });
        } else {
            AlertUtils.showAlert(getActivity(), getString(R.string.check_your_internet_connection));
        }
    }

    private void setUpEvents() {

    }
}
