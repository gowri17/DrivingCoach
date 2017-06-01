package com.drava.android.activity.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.drava.android.R;
import com.drava.android.base.BaseFragment;
import com.drava.android.ui.SeekbarWithIntervals;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 12/9/2016.
 */

public class SettingsFragment extends BaseFragment implements SeekbarInterface {
    private SeekbarWithIntervals mSeekbarWithIntervals = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        init(view);
        return view;
    }
    private void init(View view) {
        List<String> seekbarIntervals = getIntervals();
        getSeekbarWithIntervals(view).setIntervals(seekbarIntervals,this);

    /*    seekBar = (SeekBar)view.findViewById(R.id.seekbar);
        seekBarValue = (TextView)view.findViewById(R.id.seek_bar_value);*/

    }

    private List<String> getIntervals() {
        return new ArrayList<String>() {{
            add("15mins");
            add("30mins");
            add("1hr");
            add("2hrs");
            add("3hrs");
            add("4hrs");
            add("5hrs");
            add("6hrs");
            add("7hrs");
            add("8hrs");
        }};
    }

    private SeekbarWithIntervals getSeekbarWithIntervals(View view) {
        if (mSeekbarWithIntervals == null) {
            mSeekbarWithIntervals = (SeekbarWithIntervals)view.findViewById(R.id.seekbarWithIntervals);
        }

        return mSeekbarWithIntervals;
    }

    @Override
    public void onSeekBarChange(String value) {

    }

}
