package com.app.smarthome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends BaseActivity implements IMQMsgCallBack {

    private LinearLayout layLed;
    private CheckBox ledCb;
    private ImageView ivLz;
    private TextView tvLed, tvDigital;
    private SeekBar appCompatSeekBar;
    private LinearLayout layLedLeft;
    private LinearLayout layLedRight;
    private LinearLayout layLedLR;
    private LinearLayout layLedMusic;
    private LinearLayout layLedSz;
    private LinearLayout layLaZhu;
    private MyServiceConnection serviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        setListener();
        updateUI(false, "客厅灯:打开");
        //启动服务
        startMqttServer();
    }

    private void updateUI(boolean ledStatus, String ledContent) {
        ledCb.setChecked(ledStatus);
        tvLed.setText(ledContent);
    }

    private void startMqttServer() {
        serviceConnection = new MyServiceConnection(this);
        Intent intent = new Intent(this, MQTTService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private ProgressDialog loadingDialog;

    private void setListener() {
        layLed.setEnabled(false);
        layLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = ProgressDialog.show(MainActivity.this, "指令下发", "正在执行...");
                loadingDialog.show();
                String status = ledCb.isChecked() ? "0" : "1";
                serviceConnection.getMqttService().publish(status);
            }
        });
        appCompatSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //发送进度
                int pro = seekBar.getProgress() * 180 / 100;
                serviceConnection.getMqttService().publish(Integer.toString(pro));
            }
        });
        layLedLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConnection.getMqttService().publish("4");
            }
        });
        layLedRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConnection.getMqttService().publish("5");
            }
        });
        layLedLR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConnection.getMqttService().publish("6");
            }
        });
        layLedMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConnection.getMqttService().publish("7");
            }
        });
        layLedSz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                serviceConnection.getMqttService().publish("8");
            }
        });
        layLaZhu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //给蜡烛主题发送msg指令,可以远程控制蜡烛开关
                if (mLzStatus.equals("5")) {
                    mLzStatus = "175";
                    ivLz.setImageResource(R.mipmap.lz);
                } else {
                    mLzStatus = "5";
                    ivLz.setImageResource(R.mipmap.lz_off);
                }
                serviceConnection.getMqttService().publish("client-wifi-topic", mLzStatus);
            }
        });
        findViewById(R.id.layRobot).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RobotActivity.class));
            }
        });
    }

    String mLzStatus = "5";//标识给舵机传递的角度

    private void initView() {
        layLed = findViewById(R.id.layLed);
        ledCb = findViewById(R.id.tvCb);
        ivLz = findViewById(R.id.ivLz);
        tvLed = findViewById(R.id.tvLed);
        tvDigital = findViewById(R.id.tvDigital);
        appCompatSeekBar = findViewById(R.id.appCompatSeekBar);
        layLedLeft = findViewById(R.id.layLedLeft);
        layLedRight = findViewById(R.id.layLedRight);
        layLedLR = findViewById(R.id.layLedLR);
        layLedMusic = findViewById(R.id.layLedMusic);
        layLedSz = findViewById(R.id.layLedSz);
        layLaZhu = findViewById(R.id.layLaZhu);
    }

    @Override
    public void connectIng() {
        loadingDialog = ProgressDialog.show(MainActivity.this, "状态更新", "正在连接服务器...");
        loadingDialog.show();
        layLed.setEnabled(false);
    }

    @Override
    public void connected() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        layLed.setEnabled(true);
    }

    @Override
    public void connectFailed() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        layLed.setEnabled(false);
    }

    @Override
    public void setMessage(String message) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        switch (message) {
            case "0":
                tvDigital.setText("空调温度：0℃");
                tvLed.setText("客厅灯:开启");
                ledCb.setChecked(false);
                tvLed.setTextColor(getColor(R.color.dimgrey));
                break;
            case "1":
                tvDigital.setText("空调温度：1℃");
                tvLed.setText("客厅灯:关闭");
                ledCb.setChecked(true);
                tvLed.setTextColor(getColor(R.color.firebrick));
                break;
            default:
                tvDigital.setText("空调温度：" + Integer.parseInt(message) * 50 / 180 + "℃");
                break;
        }
    }

    @Override
    public void onError(Throwable e) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        layLed.setEnabled(false);
        serviceConnection.getMqttService().connect();
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public boolean needRegistEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGetMessage(EventMsg msg) {
        if (msg.code == 100) {
            serviceConnection.getMqttService().publish("client-wifi-topic", msg.data);
        }
    }


}
