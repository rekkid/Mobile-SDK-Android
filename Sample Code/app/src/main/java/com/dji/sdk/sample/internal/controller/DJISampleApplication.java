package com.dji.sdk.sample.internal.controller;

import android.app.Application;
import android.content.Context;
import android.content.ServiceConnection;
import android.widget.TextView;

import com.dji.sdk.sample.demo.mydemo.BasicInfoView;
import com.dji.sdk.sample.demo.mydemo.MyService;
import com.dji.sdk.sample.internal.utils.JWebSocketClient;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import androidx.multidex.MultiDex;
import dji.sdk.base.BaseProduct;
import dji.sdk.products.Aircraft;
import dji.sdk.products.HandHeld;
import dji.sdk.sdkmanager.BluetoothProductConnector;
import dji.sdk.sdkmanager.DJISDKManager;

/**
 * Main application
 */
public class DJISampleApplication extends Application {

    public static final String TAG = DJISampleApplication.class.getName();

    private static BaseProduct product;
    private static BluetoothProductConnector bluetoothConnector = null;
    private static Bus bus = new Bus(ThreadEnforcer.ANY);
    private static Application app = null;

    public static BasicInfoView.MyServiceConnection getConnection() {
        return connection;
    }

    public static void setConnection(BasicInfoView.MyServiceConnection connection) {
        DJISampleApplication.connection = connection;
    }

    private static BasicInfoView.MyServiceConnection connection = null;

    public static TextView getBasicInfoView() {
        return basicInfoView;
    }

    public static void setBasicInfoView(TextView basicInfoView) {
        DJISampleApplication.basicInfoView = basicInfoView;
    }

    private static TextView basicInfoView = null;
    private static JWebSocketClient webSocketClient = null;
    private static MyService.WebsocketBinder websocketBinder = null;

    public static MyService.WebsocketBinder getWebsocketBinder() {
        return websocketBinder;
    }

    public static void setWebsocketBinder(MyService.WebsocketBinder websocketBinder) {
        DJISampleApplication.websocketBinder = websocketBinder;
    }



    /**
     * Gets instance of the specific product connected after the
     * API KEY is successfully validated. Please make sure the
     * API_KEY has been added in the Manifest
     */
    public static synchronized BaseProduct getProductInstance() {
        product = DJISDKManager.getInstance().getProduct();
        return product;
    }

    public static synchronized BluetoothProductConnector getBluetoothProductConnector() {
        bluetoothConnector = DJISDKManager.getInstance().getBluetoothProductConnector();
        return bluetoothConnector;
    }

    public static boolean isAircraftConnected() {
        return getProductInstance() != null && getProductInstance() instanceof Aircraft;
    }

    public static boolean isHandHeldConnected() {
        return getProductInstance() != null && getProductInstance() instanceof HandHeld;
    }

    public static synchronized Aircraft getAircraftInstance() {
        if (!isAircraftConnected()) {
            return null;
        }
        return (Aircraft) getProductInstance();
    }

    public static synchronized HandHeld getHandHeldInstance() {
        if (!isHandHeldConnected()) {
            return null;
        }
        return (HandHeld) getProductInstance();
    }

    public static Application getInstance() {
        return DJISampleApplication.app;
    }

    public static Bus getEventBus() {
        return bus;
    }

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        MultiDex.install(this);
        com.secneo.sdk.Helper.install(this);
        app = this;
    }

    public static synchronized void setWebSocketClient(JWebSocketClient client) {
        webSocketClient = client;
    }

    public static synchronized JWebSocketClient getWebSocketClient() {
        return webSocketClient;
    }
}