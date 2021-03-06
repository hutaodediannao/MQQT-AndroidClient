package com.app.smarthome;

import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;

import androidx.appcompat.widget.AppCompatSeekBar;

import org.greenrobot.eventbus.EventBus;

public class RobotActivity extends BaseActivity {

    private static final String TAG = "RobotActivity";
    private AppCompatSeekBar s1, s2, s3, s4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_robot);

        s1 = findViewById(R.id.s1);
        s2 = findViewById(R.id.s2);
        s3 = findViewById(R.id.s3);
        s4 = findViewById(R.id.s4);

        setListener();
    }

    private void setListener() {
        s1.setOnSeekBarChangeListener(onSeekBarChangeListener);
        s2.setOnSeekBarChangeListener(onSeekBarChangeListener);
        s3.setOnSeekBarChangeListener(onSeekBarChangeListener);
        s4.setOnSeekBarChangeListener(onSeekBarChangeListener);
    }

    private SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            switch (seekBar.getId()) {
                case R.id.s1:
                    Log.i(TAG, "A" + progress);
                    EventBus.getDefault().post(new EventMsg(100, "A" + (180-progress)));
                    break;
                case R.id.s2:
                    Log.i(TAG, "B" + progress);
                    EventBus.getDefault().post(new EventMsg(100, "B" + progress));
                    break;
                case R.id.s3:
                    Log.i(TAG, "C" + progress);
                    EventBus.getDefault().post(new EventMsg(100, "C" + (180-progress)));
                    break;
                case R.id.s4:
                    Log.i(TAG, "D" + progress);
                    EventBus.getDefault().post(new EventMsg(100, "D" + progress));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean needRegistEventBus() {
        return true;
    }
}
