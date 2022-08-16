package com.dji.sdk.sample.demo.mydemo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dji.sdk.sample.internal.utils.JWebSocketClient;

public class MyService extends Service {
    int TIME_INTERVAL = 50; // 这是1s
    PendingIntent pendingIntent;
    AlarmManager alarmManager;

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
        unregisterReceiver(receiver);
    }

    private WebsocketBinder mBinder = new WebsocketBinder();

    private JWebSocketClient client;
    class WebsocketBinder extends Binder {

        public void startDownload() {
            Log.d("MyService", "startDownload executed");
        }

        public int getProgress() {
            Log.d("MyService", "getProgress executed");
            return 0;
        }

        private Handler handler;

        public static final int SEND_WEBSOCKET_MSG = 1;
        public static final int STOP_WEBSOCKET_MSG = 2;

        public void startTransfer(JWebSocketClient client) {
            MyService.this.client = client;
            Log.d("MyService", "start transfer");

            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case SEND_WEBSOCKET_MSG:
                            if (client != null) {
                                client.send("测试消息123213214325432543642252354325325325425325325432接收");
                            }
                            sendEmptyMessageDelayed(SEND_WEBSOCKET_MSG,TIME_INTERVAL);
                            break;
                        case STOP_WEBSOCKET_MSG:
                            Log.d("MyService", "stop msg");
                            break;
                        default:
                            break;
                    }
                }
            };

            handler.sendEmptyMessageDelayed(SEND_WEBSOCKET_MSG, TIME_INTERVAL);
        }

        public void stopTransfer() {
            handler.removeCallbacksAndMessages(null);
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
