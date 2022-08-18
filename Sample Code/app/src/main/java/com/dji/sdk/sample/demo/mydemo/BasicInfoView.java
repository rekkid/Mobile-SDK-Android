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
import android.text.Layout;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.dji.sdk.sample.R;
import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.controller.MainActivity;
import com.dji.sdk.sample.internal.utils.JWebSocketClient;
import com.dji.sdk.sample.internal.view.PresentableView;

import org.java_websocket.handshake.ServerHandshake;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Locale;

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
    private EditText intervalET;
    private ScrollView scroll;
    private SimpleDateFormat sdfTwo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());

    private int interval;
    private Handler viewHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            return false;
        }
    });

    public static final int UPDATE_WEBSOCKET_MSG = 1;

    private String message;
    private int count;
//    private Handler handler;
    private MyService.WebsocketBinder websocketBinder = null;
    private MyServiceConnection connection = null;


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

        basicInfoTV.setMovementMethod(ScrollingMovementMethod.getInstance());
        inputIpPort = (EditText) findViewById(R.id.input_ip_port);
        connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(this);
        disconnect = (Button) findViewById(R.id.disconnect);
        disconnect.setOnClickListener(this);
        transfer = (Button) findViewById(R.id.transfer);
        transfer.setOnClickListener(this);
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);
        test = (Button) findViewById(R.id.test);
        test.setOnClickListener(this);
        scroll = (ScrollView) findViewById(R.id.scroll_basicinfo);
        DJISampleApplication.setScroll(scroll);
        intervalET = (EditText) findViewById(R.id.et_interval);
        checkBox = (CheckBox) findViewById(R.id.checkbox_save);
        SharedPreferences pref = context.getSharedPreferences("data", MODE_PRIVATE);
        boolean isChecked = pref.getBoolean("ischecked", true);
        if (isChecked) {
            String ip = pref.getString("ip", "");
            inputIpPort.setText(ip);
            String info = pref.getString("info", "0");
            checkBox.setChecked(isChecked);
            basicInfoTV.setText(info);

            basicInfoTV.post(new Runnable() {
                @Override
                public void run() {
                    int offset = basicInfoTV.getLineCount() * basicInfoTV.getLineHeight();
                    if (offset > basicInfoTV.getHeight()){
                        basicInfoTV.scrollTo(0,offset - basicInfoTV.getHeight());
                    }
                }
            });

            interval = pref.getInt("interval", 50);
            intervalET.setText(String.valueOf(interval));
        }

        initButton();
    }

    private void initButton() {
        boolean isConnect = false;
        boolean isDisconnect = false;
        boolean isTransfer = false;
        boolean isStop = false;
        if (DJISampleApplication.getWebSocketClient() == null) {
            isConnect = true;
        } else {
            isDisconnect = true;

            if (DJISampleApplication.getWebsocketBinder() == null) {
                isTransfer = true;
            } else {
                isStop = true;
            }
        }

        updateButton(isConnect, isDisconnect, isTransfer, isStop);

    }

    private static class MessageHandler extends Handler {
        private final WeakReference<BasicInfoView> basicInfoViewWeakReference;

        public MessageHandler(BasicInfoView basicInfoView) {
            basicInfoViewWeakReference = new WeakReference<BasicInfoView>(basicInfoView);
        }

        @Override
        public void handleMessage(Message msg) {
            BasicInfoView basicInfoView = basicInfoViewWeakReference.get();
            super.handleMessage(msg);
            if (basicInfoView != null) {
                switch (msg.what) {
                    case UPDATE_WEBSOCKET_MSG:
                        basicInfoView.basicInfoTV.setText(basicInfoView.count + "\n");
                        basicInfoView.count++;
                        break;
                    default:
                        break;
                }
            }
        }
    }


    public class MyServiceConnection implements ServiceConnection {
        private TextView tv;
        private JWebSocketClient client;

        public MyServiceConnection(TextView tv, JWebSocketClient client) {
            this.tv = tv;
            this.client = client;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            websocketBinder = (MyService.WebsocketBinder) service;
            websocketBinder.startTransfer(client, tv, interval);
        }

        public void setBasicInfoTV(TextView basicInfoTV) {
            tv = basicInfoTV;
        }

        public void setClient(JWebSocketClient client) {
            this.client = client;
        }
    }

    private void restoreTransfer() {
        if (DJISampleApplication.getWebSocketClient() != null) {
            client = DJISampleApplication.getWebSocketClient();
        }
        connection = DJISampleApplication.getConnection();
        if (connection == null) {
            connection = new MyServiceConnection(basicInfoTV, client);
        } else {
            connection.setBasicInfoTV(basicInfoTV);
            connection.setClient(client);
        }
        websocketBinder = DJISampleApplication.getWebsocketBinder();
        if (websocketBinder != null) {
            websocketBinder.setBasicInfoTV(basicInfoTV);
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.transfer:
                startTransfer();
                updateButton(false, true, false, true);
                break;
            case R.id.stop:
                stopTransfer();
                updateButton(false, true, true, false);
                break;
            case R.id.connect:
                boolean isOk = connectServer();
                updateButton(!isOk, isOk, isOk, false);
                break;
            case R.id.disconnect:
                disconnectServer();
                updateButton(true, false, false, false);
                break;
            case R.id.test:
                basicInfoTV.setText("aaaaaaaaaaaaaaaaa\n");
            default:
                break;
        }
    }

    private void startTransfer() {
        interval = Integer.valueOf(intervalET.getText().toString());
        connection.setBasicInfoTV(basicInfoTV);
        connection.setClient(client);
        Intent intent = new Intent(this.context, MyService.class);
        this.context.startService(intent);
        this.context.bindService(intent, connection, BIND_AUTO_CREATE); // 绑定服务
        stop.setEnabled(true);
        transfer.setEnabled(false);
    }

    private void stopTransfer() {
        this.context.unbindService(connection);
        websocketBinder = DJISampleApplication.getWebsocketBinder();
        if (websocketBinder != null) {
            websocketBinder.stopTransfer();
        }
        Intent intent = new Intent(this.context, MyService.class);
        this.context.stopService(intent);
        websocketBinder = null;
        DJISampleApplication.setWebsocketBinder(null);
        stop.setEnabled(false);
        transfer.setEnabled(true);
    }


    private void appendTextView(String s) {
        scroll.post(new Runnable() {
            public void run() {
                if (scroll == null || basicInfoTV == null) {
                    return;
                }
                basicInfoTV.append(s);

                View contentView = scroll.getChildAt(0);
                if (contentView.getMeasuredHeight() <= scroll.getScrollY() + scroll.getHeight() + 50) {
                    scroll.fullScroll(View.FOCUS_DOWN);
                }

            }



//            public void run() {
//                if (scroll == null || basicInfoTV == null) {
//                    return;
//                }
//                basicInfoTV.append(s);
//                int offset = basicInfoTV.getMeasuredHeight() - scroll.getHeight();
//                if (offset < 0) {
//                    offset = 0;
//                }
//                scroll.scrollTo(0, offset);
//            }
        });

//        basicInfoTV.post(new Runnable() {
//            @Override
//            public void run() {
//                basicInfoTV.append(s);
//                int offset = basicInfoTV.getLineCount() * basicInfoTV.getLineHeight();
//                if (offset > basicInfoTV.getHeight()){
//                    basicInfoTV.scrollTo(0,offset - basicInfoTV.getHeight());
//                }
//            }
//        });

//        basicInfoTV.append(s);
//        int offset = basicInfoTV.getLineCount() * basicInfoTV.getLineHeight();
//        if (offset > basicInfoTV.getHeight()){
//            basicInfoTV.scrollTo(0,offset - basicInfoTV.getHeight());
//        }
    }




    public boolean connectServer() {
        basicInfoTV.setText("connect to" + inputIpPort.getText() + "\n");
        URI uri;
        try {
            uri = URI.create("ws://" + inputIpPort.getText());
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        client = new JWebSocketClient(uri) {
            private int count;
            @Override
            public void onMessage(String message) {
                // 处理接收到的消息
                Log.d("JWebSClientService", "已收到:" + message);

                String time = sdfTwo.format( System.currentTimeMillis());
                appendTextView(time + " 已收到:" + message + "\n");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                super.onClose(code, reason, remote);
                Log.e("JWebSClientService", "websocket close");
                disconnectServer();
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

        return true;
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
        }
    }

    private void updateButton(boolean isConnect, boolean isDisconnect, boolean isTransfer, boolean isStop) {
        connect.setEnabled(isConnect);
        disconnect.setEnabled(isDisconnect);
        transfer.setEnabled(isTransfer);
        stop.setEnabled(isStop);
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
        SharedPreferences.Editor editor = context.getSharedPreferences("data", MODE_PRIVATE).edit();
        if (checkBox.isChecked()) {
            editor.putString("ip", String.valueOf(inputIpPort.getText()));
            editor.putString("info", String.valueOf(basicInfoTV.getText()));
            editor.putBoolean("ischecked", true);
        } else {
            editor.putString("ip", "");
            editor.putString("info", "");
            editor.putBoolean("ischecked", false);
        }
        editor.apply();

        DJISampleApplication.setWebSocketClient(client);
        DJISampleApplication.setBasicInfoView(basicInfoTV);
        DJISampleApplication.setConnection(connection);
    }
}