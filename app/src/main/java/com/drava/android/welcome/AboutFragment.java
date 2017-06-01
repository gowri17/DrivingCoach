package com.drava.android.welcome;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.drava.android.R;
import com.drava.android.base.BaseFragment;
import com.drava.android.utils.DravaLog;
import com.drava.android.utils.TextUtils;


public class AboutFragment extends BaseFragment {

    private TextView txtAbout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        init(view);
        return view;
    }

    private void init(View view) {
        txtAbout = (TextView) view.findViewById(R.id.txt_about_us);
        String aboutus = TextUtils.decodeBase64(getApp().getUserPreference().getAboutUs());
        DravaLog.print("aboutUs==>"+aboutus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) {
            txtAbout.setText(Html.fromHtml("<p align=justify> <b>"+aboutus+"</p>", Html.FROM_HTML_MODE_LEGACY));
        }else {
            txtAbout.setText(Html.fromHtml("<p align=justify> <b>"+aboutus+"</p>"));
        }
        txtAbout.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
