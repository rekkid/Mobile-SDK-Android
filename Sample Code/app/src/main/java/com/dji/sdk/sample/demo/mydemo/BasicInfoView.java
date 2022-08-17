package com.dji.sdk.sample.demo.mydemo;

import static android.content.Context.ALARM_SERVICE;
import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.provider.SyncStateContract;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.utils.JWebSocketClient;
import com.dji.sdk.sample.internal.view.PresentableView;

import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class BasicInfoView extends LinearLayout implements View.OnClickListener, PresentableView {
    protected TextView basicInfoTV;
    private EditText inputIpPort;
    private Button connect;
    private Button disconnect;
    private Button transfer;
    private Button stop;
    private JWebSocketClient client;
    private AlarmManager alarmManager;
    private Context context;
    private CheckBox checkBox;
    protected Button test;
    private Handler viewHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            return false;
        }
    });

    public static final int UPDATE_WEBSOCKET_MSG = 1;

    private String message;
    private int count;
    private Handler handler;
    public BasicInfoView(Context context) {
        super(context);
        initUI(context);
        this.context = context;
        this.count = 0;
        restoreTransfer();
    }

    private void initUI(Context context) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Service.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.activity_basic_info_view, this, true);
        basicInfoTV = (TextView) findViewById(R.id.tv_basic_info);


//        basicInfoTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        inputIpPort = (EditText) findViewById(R.id.input_ip_port);
        connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(this);
        disconnect = (Button) findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);
        transfer = (Button) findViewById(R.id.transfer);
        transfer.setOnClickListener(this);
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);

        inputIpPort.setSaveEnabled(true);

        resetButton();

        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        String ip = pref.getString("ip", "");
        inputIpPort.setText(ip);
        String info = pref.getString("info", "0");

        basicInfoTV.setText(info);

        if (DJISampleApplication.getWebSocketClient() != null) {
            client = DJISampleApplication.getWebSocketClient();
        }
        test = (Button) findViewById(R.id.test);
        test.setOnClickListener(this);

        handler = new Handler() {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case UPDATE_WEBSOCKET_MSG:
                        BasicInfoView.this.basicInfoTV.setText(count + "\n");
                        count++;
                        break;
                    default:
                        break;
                }
            }
        };
    }

    private void restoreTransfer() {
        websocketBinder = DJISampleApplication.getWebsocketBinder();
        if (websocketBinder != null) {
            websocketBinder.startTransfer(client, basicInfoTV);
        }
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putString("input", String.valueOf(inputIpPort.getText()));
        bundle.putParcelable("parent", super.onSaveInstanceState());

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;

            String input = bundle.getString("input");
            Parcelable parent = bundle.getParcelable("parent");
            super.onRestoreInstanceState(parent);

            inputIpPort.setText(input);
            return;
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
//        initAccessLocker();
//        initAccessLockerListener();
    }

    private MyService.WebsocketBinder websocketBinder;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            websocketBinder = (MyService.WebsocketBinder) service;
            websocketBinder.startTransfer(client, basicInfoTV);
        }

    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transfer:
                startTransfer();
                break;
            case R.id.stop:
                stopTransfer();
                break;
            case R.id.connect:
                connectServer();
                break;
            case R.id.disconnect:
                disconnectServer();
                break;
            case R.id.test:
                basicInfoTV.setText("aaaaaaaaaaaaaaaaa\n");
            default:
                break;
        }
    }

    private void startTransfer() {
        Intent intent = new Intent(this.context, MyService.class);
        this.context.startService(intent);
        this.context.bindService(intent, connection, BIND_AUTO_CREATE); // 绑定服务
        stop.setEnabled(true);
        transfer.setEnabled(false);
    }

    private void stopTransfer() {

        this.context.unbindService(connection);
        if (websocketBinder != null) {
            websocketBinder.stopTransfer();
        }
        Intent intent = new Intent(this.context, MyService.class);
        this.context.stopService(intent);
        stop.setEnabled(false);
        transfer.setEnabled(true);
    }

//    private void aappendTextView(String s) {
////        basicInfoTV.append(s);
////        int offset = basicInfoTV.getLineCount() * basicInfoTV.getLineHeight();
////        if (offset > basicInfoTV.getHeight()) {
////            basicInfoTV.scrollTo(0, offset - basicInfoTV.getHeight());
////        }
//
//        handler.post(new Runnable(){
//            @Override
//            public void run() {
//                basicInfoTV.setText(s);
//            }
//        });
//    }



    public void connectServer() {
        basicInfoTV.setText("connect to" + inputIpPort.getText() + "\n");

        URI uri = URI.create("ws://" + inputIpPort.getText());
        client = new JWebSocketClient(uri) {
            private int count;
            @Override
            public void onMessage(String message) {
                //message就是接收到的消息
                Log.e("JWebSClientService", message);
//                Message msg = new Message();
//                msg.what = UPDATE_WEBSOCKET_MSG;
//                BasicInfoView.this.message = message;
//                handler.sendMessage(msg);
            }
        };

        try {
            client.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (client != null && client.isOpen()) {
            client.send("已连接服务器");
        }

        connect.setEnabled(false);
        disconnect.setEnabled(true);
        transfer.setEnabled(true);
        stop.setEnabled(false);
    }

    public void disconnectServer() {
        basicInfoTV.setText("已断开服务器" + inputIpPort.getText() + "\n");
        try {
            stopTransfer();

            if (null != client) {
                client.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client = null;
            resetButton();
        }
    }

    private void resetButton() {
        connect.setEnabled(true);
        disconnect.setEnabled(false);
        transfer.setEnabled(true);
        stop.setEnabled(false);
    }

    @Override
    public int getDescription() {
        return 0;
    }

    @NonNull
    @Override
    public String getHint() {
        return null;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        checkBox = (CheckBox) findViewById(R.id.checkbox_save);
        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
        if (checkBox.isChecked()) {
            editor.putString("ip", String.valueOf(inputIpPort.getText()));
            editor.putString("info", String.valueOf(basicInfoTV.getText()));
        } else {
            editor.putString("ip", "");
            editor.putString("info", "");
        }
        editor.apply();

        DJISampleApplication.setWebSocketClient(client);
        DJISampleApplication.setBasicInfoView(basicInfoTV);
        DJISampleApplication.setWebsocketBinder(websocketBinder);
    }
}