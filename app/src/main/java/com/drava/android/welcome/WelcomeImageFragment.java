package com.drava.android.welcome;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.drava.android.R;
import com.drava.android.base.BaseFragment;

/**
 * Created by admin on 10/18/2016.
 */

public class WelcomeImageFragment extends BaseFragment {
    public static String PAGE_NO = "pageno";

    public static WelcomeImageFragment newInstance(int pageNo) {
        Bundle args = new Bundle();
        args.putInt(PAGE_NO,pageNo);
        WelcomeImageFragment fragment = new WelcomeImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_welcome_image,container,false);
        init(view);
        return view;
    }

    private void init(View view) {
        ImageView imageView =(ImageView) view.findViewById(R.id.welcome_image);
        int pageNo = getArguments().getInt(PAGE_NO);
        switch (pageNo){
            case 0:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_leftmenu_invite_accept));
                break;

            case 1:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_invite));
                break;

            case 2:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_mymentees));
                break;

            case 3:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_leftmenu_recomend_app));
                break;

            case 4:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_recomend_app));
                break;

            case 5:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_recomend_app_referal));
                break;

            case 6:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_accept_invitation_1));
                break;

            case 7:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_pending));
                break;

            case 8:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_pending_list));
                break;

            case 9:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_leftmenu_mymentees));
                break;

            case 10:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_seleted_mentees));
                break;

            case 11:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_mentees_action));
                break;

            case 12:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_leftmenu_settings));
                break;

            case 13:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_settings_notification));
                break;

            case 14:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_leftmenu_myprofile));
                break;

            case 15:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_view_profile_referral_point));
                break;
        }
    }
}
