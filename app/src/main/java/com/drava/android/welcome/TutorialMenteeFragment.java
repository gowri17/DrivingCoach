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

public class TutorialMenteeFragment extends BaseFragment {
    public static String PAGE_NO = "pageno";

    public static TutorialMenteeFragment newInstance(int pageNo) {
        Bundle args = new Bundle();
        args.putInt(PAGE_NO,pageNo);
        TutorialMenteeFragment fragment = new TutorialMenteeFragment();
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
            case 0:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_invite_mentor_1));
                break;

            case 1:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_invite_mentor_2));
                break;

            case 2:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_invite_mentor_3));
                break;

            case 3:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_accept_invitation_1));
                break;

            case 4:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_accept_invitation_2));
                break;

            case 5:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_accept_invitation_3));
                break;

            case 6:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_view_mentor_1));
                break;

            case 7:imageView.setImageDrawable(ContextCompat.getDrawable(getActivity(),R.drawable.tut_view_mentor_2));
                break;
        }
    }
}
