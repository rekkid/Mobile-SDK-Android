package com.dji.sdk.sample.demo.mydemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.dji.sdk.sample.internal.controller.DJISampleApplication;
import com.dji.sdk.sample.internal.utils.JWebSocketClient;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MyService extends Service {
    private static int TIME_INTERVAL = 50; // 这是1s
    PendingIntent pendingIntent;
    AlarmManager alarmManager;
    private static SimpleDateFormat sdfTwo = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    @Override
    public void onCreate() {
        super.onCreate();
//        IntentFilter intentFilter = new IntentFilter(TEST_ACTION);
//        registerReceiver(receiver, intentFilter);
//        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
//        Intent intent = new Intent();
//        intent.setAction(TEST_ACTION);
//        pendingIntent = PendingIntent.getBroadcast(MyService.this, 0, intent, 0);
//
//        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 100, pendingIntent);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//6.0低电量模式需要使用该方法触发定时任务
//            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {//4.4以上 需要使用该方法精确执行时间
//            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
//        } else {//4。4一下 使用老方法
//            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 100, pendingIntent);
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(receiver);
        mBinder.handler.removeCallbacksAndMessages(null);
    }

    private WebsocketBinder mBinder = new WebsocketBinder();

    private JWebSocketClient client;

    public class TransferHandler extends Handler {

    }

    public static class WebsocketBinder extends Binder {

        public void startDownload() {
            Log.d("MyService", "startDownload executed");
        }

        public int getProgress() {
            Log.d("MyService", "getProgress executed");
            return 0;
        }

        private MessageHandler handler;

        public static final int SEND_WEBSOCKET_MSG = 1;
        public static final int STOP_WEBSOCKET_MSG = 2;

        public static int count;

        private static class MessageHandler extends Handler {
            private final WeakReference<WebsocketBinder> websocketBinderWeakReference;
            //            private WebsocketBinder websocketBinder;
            private JWebSocketClient client;
            private TextView tv;
            private ScrollView scroll;

            public MessageHandler(WebsocketBinder websocketBinder, JWebSocketClient client, TextView tv) {
                this.websocketBinderWeakReference = new WeakReference<WebsocketBinder>(websocketBinder);
                this.client = client;
                this.tv = tv;
            }


            void refreshTextView(String msg) {
//                Handler mHandler = new Handler();
                ScrollView scroll = DJISampleApplication.getScroll();
                scroll.post(new Runnable() {
                    public void run() {
                        if (scroll == null || tv == null) {
                            return;
                        }
                        tv.append(msg);
                        View contentView = scroll.getChildAt(0);
                        if (contentView.getMeasuredHeight() <= scroll.getScrollY() + scroll.getHeight() + 50) {
                            scroll.fullScroll(View.FOCUS_DOWN);
                        }


                    }

//                    public void run() {
//                        if (scroll == null || tv == null) {
//                            return;
//                        }
//                        tv.append(msg);
//                        int offset = tv.getMeasuredHeight() - scroll.getHeight();
//                        if (offset < 0) {
//                            offset = 0;
//                        }
//                        scroll.scrollTo(0, offset);
//                    }
                });
            }

            @Override
            public void handleMessage(Message msg) {
                WebsocketBinder websocketBinder = websocketBinderWeakReference.get();
                super.handleMessage(msg);
                if (websocketBinder != null) {
                    switch (msg.what) {
                        case SEND_WEBSOCKET_MSG:
                            if (client != null) {
                                String sendMsg = (websocketBinder.count) + ":模拟测试消息123213214325432543642252354325325325425325325432接收测试消息123213214325432543642252354325325325425325325432接收";
                                client.send(sendMsg);
                                Log.d("JWebSClientService", "已发送:" + sendMsg);
                                sendEmptyMessageDelayed(SEND_WEBSOCKET_MSG, TIME_INTERVAL);

                                String time = sdfTwo.format( System.currentTimeMillis());
                                refreshTextView(time + " 已发送:" + sendMsg + "\n");
                                websocketBinder.count++;
                            }
                            break;
                        case STOP_WEBSOCKET_MSG:
                            Log.d("MyService", "stop msg");
                            break;
                        default:
                            break;
                    }
                } else {
                    Log.d("afafad", "nulll");
                }
            }

            public void setBasicInfoTV(TextView basicInfoView) {
                this.tv = basicInfoView;
            }
        }

        public void startTransfer(JWebSocketClient client, TextView tv, int interval) {
            TIME_INTERVAL = interval;
            handler = new MessageHandler(this, client, tv);
            DJISampleApplication.setWebsocketBinder(this);
            Log.d("MyService", "start transfer");
            handler.sendEmptyMessageDelayed(SEND_WEBSOCKET_MSG, TIME_INTERVAL);
        }

        public void stopTransfer() {
            handler.removeCallbacksAndMessages(null);
        }

        public void setBasicInfoTV(TextView basicInfoView) {
            handler.setBasicInfoTV(basicInfoView);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static final String TEST_ACTION = "XXX.XXX.XXX" + "_TEST_ACTION";

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TEST_ACTION.equals(action)) {
                if (client != null) {
                    client.send("我爱你中国");
                }

                alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 100, pendingIntent);
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 100, pendingIntent);
//                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 100, pendingIntent);
//                }
            }
        }
    };
}
