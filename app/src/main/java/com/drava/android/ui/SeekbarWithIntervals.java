package com.drava.android.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


import com.drava.android.R;
import com.drava.android.activity.settings.SeekbarInterface;
import com.drava.android.utils.DravaLog;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class SeekbarWithIntervals extends LinearLayout {
    private RelativeLayout RelativeLayout = null;
    private SeekBar seekbar = null;
    private TextView progressValue, seekbarValue;
    private int WidthMeasureSpec = 0;
    private int HeightMeasureSpec = 0;
    private SeekbarInterface seekbarInterface;
    private Handler handler;

    public SeekbarWithIntervals(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        getActivity().getLayoutInflater()
                .inflate(R.layout.view_seekbar_with_intervals, this);
    }

    private Activity getActivity() {
        return (Activity) getContext();

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed) {
            alignIntervals();
            // We've changed the intervals layout, we need to refresh.
            RelativeLayout.measure(WidthMeasureSpec, HeightMeasureSpec);
            RelativeLayout.layout(RelativeLayout.getLeft(), RelativeLayout.getTop(), RelativeLayout.getRight(), RelativeLayout.getBottom());
        }
    }

    private void alignIntervals() {
        int widthOfSeekbarThumb = getSeekbarThumbWidth();
        int thumbOffset = widthOfSeekbarThumb / 2;
        int widthOfSeekbar = getSeekbar().getWidth();
        int firstIntervalWidth = getRelativeLayout().getChildAt(0).getWidth();
        int remainingPaddableWidth = widthOfSeekbar - firstIntervalWidth - widthOfSeekbarThumb;
        int numberOfIntervals = getSeekbar().getMax();
        int maximumWidthOfEachInterval = remainingPaddableWidth / numberOfIntervals;
        alignFirstInterval(thumbOffset);
        alignIntervalsInBetween(maximumWidthOfEachInterval);
//		getProgressValue().setText("ProgressValue: "+getSeekbar().getProgress());
//		getSeekbarValue().setText("SeekbarValue: "+((TextView) getRelativeLayout().getChildAt(0)).getText());
        seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
//				progressValue.setText("ProgressValue: "+seekBar.getProgress());
//				seekbarValue.setText("SeekbarValue: "+((TextView) getRelativeLayout().getChildAt((seekBar.getProgress()))).getText());
                handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {

                        if (msg.what == 1) {
                            DravaLog.print("SeekbarValue: "+((TextView) getRelativeLayout().getChildAt((seekBar.getProgress()))).getText());
                            seekbarInterface.onSeekBarChange(""+((TextView) getRelativeLayout().getChildAt((seekBar.getProgress()))).getText().toString().split(" ")[0]);
                        }

                    }
                };
                handler.removeMessages(1);
                handler.sendEmptyMessageDelayed(1, 5000);
            }
        });
    }

    private int getSeekbarThumbWidth() {
        return getResources().getDimensionPixelOffset(R.dimen.seekbar_thumb_width);
    }

    private void alignFirstInterval(int offset) {
        TextView firstInterval = (TextView) getRelativeLayout().getChildAt(0);
        firstInterval.setPadding(0, 0, 0, 0);
    }

    private void alignIntervalsInBetween(int maximumWidthOfEachInterval) {
        int widthOfPreviousIntervalsText = 0;

        // Don't align the first or last interval.
        for (int index = 1; index < (getRelativeLayout().getChildCount()); index++) {
            TextView textViewInterval = (TextView) getRelativeLayout().getChildAt(index);
            int widthOfText = textViewInterval.getWidth();
            int leftPadding;
            if (index == 1) {
                leftPadding = Math.round((maximumWidthOfEachInterval - (widthOfText / 2) - (widthOfPreviousIntervalsText / 2)) - 15);
                Log.d("tag", "=====leftPadding========" + leftPadding);
            } else if (index == 2) {
                leftPadding = Math.round((maximumWidthOfEachInterval - (widthOfText / 2) - (widthOfPreviousIntervalsText / 2)) - 3);
                Log.d("tag", "=====leftPadding========" + leftPadding);
            } else if (index == 3) {
                leftPadding = Math.round(maximumWidthOfEachInterval - (widthOfText / 2) - (widthOfPreviousIntervalsText / 2));
                Log.d("tag", "=====leftPadding========" + leftPadding);
            } else {
                leftPadding = Math.round((maximumWidthOfEachInterval - (widthOfText / 2) - (widthOfPreviousIntervalsText / 2)) + 7);
                Log.d("tag", "=====leftPadding========" + leftPadding);
            }
            textViewInterval.setPadding(leftPadding, 0, 0, 0);

            widthOfPreviousIntervalsText = widthOfText;
        }
    }

    private void alignLastInterval(int offset, int maximumWidthOfEachInterval) {
        int lastIndex = getRelativeLayout().getChildCount() - 1;

        TextView lastInterval = (TextView) getRelativeLayout().getChildAt(lastIndex);
        int widthOfText = lastInterval.getWidth();

        int leftPadding = Math.round(maximumWidthOfEachInterval - widthOfText - offset);
        lastInterval.setPadding(leftPadding, 0, 0, 0);
    }

    protected synchronized void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        WidthMeasureSpec = widthMeasureSpec;
        HeightMeasureSpec = heightMeasureSpec;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public int getProgress() {
        return getSeekbar().getProgress();
    }

    public void setProgress(int progress) {
        getSeekbar().setProgress(progress);
    }

    public void setIntervals(List<String> intervals,SeekbarInterface seekbarInterface) {
        displayIntervals(intervals);
        Log.d("tag", "======intervals=====" + intervals);
        getSeekbar().setMax(intervals.size() - 1);
        this.seekbarInterface = seekbarInterface;
    }

    private void displayIntervals(List<String> intervals) {
        int idOfPreviousInterval = 0;

        if (getRelativeLayout().getChildCount() == 0) {
            for (String interval : intervals) {
                TextView textViewInterval = createInterval(interval);
                alignTextViewToRightOfPreviousInterval(textViewInterval, idOfPreviousInterval);
                idOfPreviousInterval = textViewInterval.getId();
                getRelativeLayout().addView(textViewInterval);
            }
        }
    }

    private TextView createInterval(String interval) {
        View textBoxView = (View) LayoutInflater.from(getContext())
                .inflate(R.layout.view_seekbar_with_intervals_labels, null);

        TextView textView = (TextView) textBoxView
                .findViewById(R.id.textViewInterval);

        textView.setTextSize(10);
//generateViewId() call requires api level 17
        if (Build.VERSION.SDK_INT >= 17) {
            textView.setId(View.generateViewId());
        } else {
            final AtomicInteger sNextGeneratedId = new AtomicInteger(1);
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF)
                    newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    textView.setId(result);
                }
            }
        }
        textView.setText(interval);

        return textView;
    }

    private void alignTextViewToRightOfPreviousInterval(TextView textView, int idOfPreviousInterval) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        if (idOfPreviousInterval > 0) {
            params.addRule(RelativeLayout.RIGHT_OF, idOfPreviousInterval);
        }

        textView.setLayoutParams(params);
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener onSeekBarChangeListener) {
        getSeekbar().setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    private RelativeLayout getRelativeLayout() {
        if (RelativeLayout == null) {
            RelativeLayout = (RelativeLayout) findViewById(R.id.intervals);
        }
        return RelativeLayout;
    }

    private SeekBar getSeekbar() {
        if (seekbar == null) {
            seekbar = (SeekBar) findViewById(R.id.seekbar);
        }

        return seekbar;
    }

    private TextView getProgressValue() {
        if (progressValue == null) {
            progressValue = (TextView) findViewById(R.id.progress_value);
        }
        return progressValue;
    }

    private TextView getSeekbarValue() {
        if (seekbarValue == null) {
            seekbarValue = (TextView) findViewById(R.id.seekbar_value);
        }
        return seekbarValue;
    }
}