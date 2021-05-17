package com.app.smarthome;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements IMQMsgCallBack {

    private LinearLayout layLed;
    private CheckBox ledCb;
    private TextView tvLed;
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
        layLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog = ProgressDialog.show(MainActivity.this, "指令下发", "正在执行...");
                loadingDialog.show();
                String status = ledCb.isChecked() ? "0" : "1";
                serviceConnection.getMqttService().publish(status);
            }
        });
    }

    private void initView() {
        layLed = findViewById(R.id.layLed);
        ledCb = findViewById(R.id.tvCb);
        tvLed = findViewById(R.id.tvLed);
    }

    @Override
    public void connectIng() {
        loadingDialog = ProgressDialog.show(MainActivity.this, "状态更新", "正在连接服务器...");
        loadingDialog.show();
    }

    @Override
    public void connected() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void connectFailed() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    public void setMessage(String message) {
        loadingDialog.dismiss();
        switch (message) {
            case "0":
                tvLed.setText("客厅灯:开启");
                ledCb.setChecked(false);
                tvLed.setTextColor(getColor(R.color.dimgrey));
                break;
            case "1":
                tvLed.setText("客厅灯:关闭");
                ledCb.setChecked(true);
                tvLed.setTextColor(getColor(R.color.firebrick));
                break;
            default:
                break;
        }
    }

    @Override
    public void onError(Throwable e) {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
