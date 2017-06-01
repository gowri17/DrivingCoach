package com.drava.android.activity.help;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.drava.android.R;
import com.drava.android.activity.HomeActivity;
import com.drava.android.activity.leftmenu.LeftMenuDrawerItem;
import com.drava.android.base.BaseFragment;
import com.drava.android.welcome.TutorialMenteePageAdapter;
import com.drava.android.welcome.WelcomePageAdapter;
import com.drava.android.welcome.WelcomePageIndicator;

import java.util.Timer;
import java.util.TimerTask;

public class HelpFragment extends BaseFragment {

    private ViewPager viewPager;
    private LinearLayout viewPagerIndicator;
    private WelcomePageAdapter welcomePageAdapter;
    private TutorialMenteePageAdapter tutorialMenteePageAdapter;
    private int currentPage;
    private ImageView menuImage;
    private RelativeLayout rlvMenuImage;
    private WelcomePageIndicator mIndicator;
    private int MAX_PAGES;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);
        init(view);
        setupDefaults();
        setupEvents();
        return view;
    }

    private void init(View view) {
        ((HomeActivity)getActivity()).hideToolbar();
        viewPager  = (ViewPager)view.findViewById(R.id.view_pager);
        menuImage = (ImageView)view.findViewById(R.id.img_menu);
        rlvMenuImage = (RelativeLayout)view.findViewById(R.id.rlv_image_menu);
        viewPagerIndicator = (LinearLayout)view.findViewById(R.id.pagesContainer);
        welcomePageAdapter = new WelcomePageAdapter(getActivity().getSupportFragmentManager());
        tutorialMenteePageAdapter = new TutorialMenteePageAdapter(getActivity().getSupportFragmentManager());
    }

    private void setupDefaults() {
        currentPage = 0;
        if(getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)) {
            viewPager.setAdapter(welcomePageAdapter);
            MAX_PAGES = 16;
        }else if(getApp().getUserPreference().getMentorOrMentee().equals(MENTEE)){
            viewPager.setAdapter(tutorialMenteePageAdapter);
            MAX_PAGES = 8;
        }
        mIndicator = new WelcomePageIndicator(getActivity(), viewPagerIndicator, viewPager, R.drawable.indicator_circle);
        mIndicator.setPageCount(MAX_PAGES);
        mIndicator.show();

        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            public void run() {
                if(isAdded() && getActivity() != null) {
                    if (getApp().getUserPreference().getMentorOrMentee().equals(MENTOR)) {
                        MAX_PAGES = 16;
                    } else {
                        MAX_PAGES = 8;
                    }
                    if (currentPage == MAX_PAGES) {
                        currentPage = 0;
                    }
                    viewPager.setCurrentItem(currentPage++, true);
                }
            }
        };
        new Timer().schedule(new TimerTask()
        {
            @Override
            public void run() {
                if(isAdded() && getActivity() != null) {
                    handler.post(update);
                }
            }
        }, 3000, 3000);
    }

    private void setupEvents(){
        rlvMenuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((HomeActivity)getActivity()).externalOnOptionsItemSelected();
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                int index = position % MAX_PAGES;
                mIndicator.setIndicatorAsSelected(index);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
