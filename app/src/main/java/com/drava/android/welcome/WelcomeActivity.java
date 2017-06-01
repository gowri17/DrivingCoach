package com.drava.android.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.activity.AuthorizationActivity;
import com.drava.android.activity.WebViewActivity;
import com.drava.android.base.BaseActivity;

import java.util.Timer;
import java.util.TimerTask;

public class WelcomeActivity extends BaseActivity implements View.OnClickListener{
    private ViewPager viewPager;
    private WelcomePageAdapter welcomePageAdapter;
    private LinearLayout mLinearLayout;
    private TextView txtDescription,txtGetStarted,txtAlreadyLogin, txtLogin, txtTermsofUse, txtPrivacyPolicy;
    private String[] welcomeString;
    private static int currentPage=0;
    private WelcomePageIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        init();
        setUpDefaults();
        setUpEvents();
    }

    public void init(){
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        mLinearLayout = (LinearLayout) findViewById(R.id.pagesContainer);
        txtAlreadyLogin = (TextView) findViewById(R.id.txt_already_login);
        txtLogin = (TextView) findViewById(R.id.txt_login);
        txtDescription = (TextView) findViewById(R.id.txt_description);
        txtGetStarted = (TextView) findViewById(R.id.txt_get_started);
        txtTermsofUse = (TextView) findViewById(R.id.txt_terms_of_use);
        txtPrivacyPolicy = (TextView) findViewById(R.id.txt_private_policy);
    }

    public void setUpDefaults(){
        txtAlreadyLogin.setText(getResources().getString(R.string.str_by_using)+" ");
        welcomeString = getResources().getStringArray(R.array.welcome_description);
        txtDescription.setText(welcomeString[0]);
        welcomePageAdapter = new WelcomePageAdapter(getSupportFragmentManager());
        viewPager.setAdapter(welcomePageAdapter);
        mIndicator = new WelcomePageIndicator(this, mLinearLayout, viewPager, R.drawable.indicator_circle);
        mIndicator.setPageCount(13);
        mIndicator.show();
        txtTermsofUse.setOnClickListener(this);
        txtPrivacyPolicy.setOnClickListener(this);

        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            public void run() {
                if (currentPage == 13) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
            }
        };
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 3000, 3000);
    }

    public void setUpEvents(){
        txtGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, AuthorizationActivity.class));
            }
        });

        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, AuthorizationActivity.class));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.txt_terms_of_use:
                Intent termsIntent = new Intent(WelcomeActivity.this, WebViewActivity.class);
                termsIntent.putExtra("WEB_URL", getApp().getUserPreference().getTermsConditions());
                termsIntent.putExtra("TITLE", "Term of Use");
                startActivity(termsIntent);
                break;

            case R.id.txt_private_policy:
                Intent policyIntent = new Intent(WelcomeActivity.this, WebViewActivity.class);
                policyIntent.putExtra("WEB_URL", getApp().getUserPreference().getPrivacyPolicy());
                policyIntent.putExtra("TITLE", "Privacy Policy");
                startActivity(policyIntent);
        }
    }
}

