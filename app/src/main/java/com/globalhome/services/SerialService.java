package com.globalhome.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import com.globalhome.activities.MainActivity;
import com.globalhome.views.dialogs.Mac_Dialog;
import com.globalhome.views.dialogs.MusicDialog;
import com.globalhome.views.dialogs.Sound_Effect_Dialog;
import com.h3launcher.BuildConfig;
import com.h3launcher.R;

import android_serialport_api.SerialPort;
import utils.IMyAidlInterface;
import utils.SerialPortUtils;

public class SerialService extends Service {

    private SerialPortUtils serialPortUtils = new SerialPortUtils();
    private byte[] SerialPortReceiveBuffer;
    private String lo;
    private Handler SerialPortReceivehandler;
    private MusicDialog dialog;
    private Sound_Effect_Dialog sdialog;
    private Mac_Dialog mdialog;
    private SerialPort serialPort;
    private Callback callback;
    private byte[] bytes;
    private int h = 0;

    private static int MicVolume = 0;
    private static int MusicVolume = 0;
    private MyReceiver receiver = null;
    //private String mType = "";
    private byte tbb[] = {0, 0, 0, 0};
    private byte[] SetMusicVolume = {0x02, 0x01, 0x02, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x0b, 0x7E};//设置音乐音量  K70//
    private byte[] SetMusicVolumeK50 = {0x01, 0x01, 0x02, 0x00, 0x00, 0x02, 0x00, 0x04, 0x00, 0x0a, 0x7E};//设置音乐音量  K50

    @Override
    public void onCreate() {
        super.onCreate();

        SerialPortReceivehandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
            }
        };

        serialPort = serialPortUtils.openSerialPort();

        if (serialPort == null) {
            Log.e("Service", "onCreate：串口打开失败！！！！！");
        } else {
            Log.e("Service", "onCreate：串口打开成功！！！！！");
            CheckSerialPortEvent();
        }

    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        try {
            if (intent == null) return;

            bytes = intent.getByteArrayExtra("serial");
            if (bytes != null) {
                serialPortUtils.sendBuffer(bytes, bytes);
                Log.e("serialPortUtils", utils.ChangeTool.ByteArrToHexStr(bytes, 0, bytes.length));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }

    public class Binder extends IMyAidlInterface.Stub {  //android.os.Binder
        public SerialService getService() {

            receiver = new MyReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.iflytek.xiri2.hal.volume");
            registerReceiver(receiver, filter);

            return SerialService.this;
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public static interface Callback {
        void onDataChange(String data);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static int getMicVolume() {
        return MicVolume;
    }

    public static void setMicVolume(int micVolume) {
        MicVolume = micVolume;
    }

    public static int getMusicVolume() {
        return MusicVolume;
    }

    public static void setMusicVolume(int musicVolume) {
        MusicVolume = musicVolume;
    }

    private void CheckSerialPortEvent() {
        //串口数据监听事件
        serialPortUtils.setOnDataReceiveListener(new SerialPortUtils.OnDataReceiveListener() {
            @Override
            public void onDataReceive(byte[] buffer, int size) {
                SerialPortReceiveBuffer = buffer;

                if (buffer[2] == 0x06) {
                    MusicVolume = buffer[7];
                    MicVolume = buffer[8];
                    return;
                }
                lo = utils.ChangeTool.ByteArrToHexStr(SerialPortReceiveBuffer, 0, size);

                if (null != SerialPortReceivehandler) {
                    SerialPortReceivehandler.post(runnable);
                } else {
                    Log.e("Service", "SerialPortReceivehandler=null");
                }
            }


            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    if (lo.length() >= 16) {
                        VolumeChange(lo);
                    }
                }
            };

        });
    }

    private void VolumeChange(String volume) {

        String type = volume.substring(4, 8);
        String value = volume.substring(12, 16);
        int v = Integer.valueOf(value, 16);
        Log.e("Service", "type=" + type + "   lo=" + lo + "    h=" + h++);
        //mType = type;
        if (type.equals("0200")) {
            //音乐音量
            if ((v >= 0) && (v <= 60)) {
                showVolumeDialog(v, type);
                MusicVolume = v;
            }
        } else if (type.equals("0300")) {
            //mic音量
            if ((v >= 0) && (v <= 60)) {
                showVolumeDialog(v, type);
                MicVolume = v;
            }
        } else if (type.equals("0400")) {
            //音效
            showEffectDialog(v, type);
        } else if (type.equals("0500")) {
            if (callback != null) {
                callback.onDataChange(lo);
            }
        }else if (type.equals("0700")) {
            //低音效
            switch (v) {
                case 1:
                    showEffectDialog3(R.drawable.d1, v, "1", type);
                    break;
                case 2:
                    showEffectDialog3(R.drawable.d2, v, "2", type);
                    break;
                case 3:
                    showEffectDialog3(R.drawable.d3, v, "3", type);
                    break;
                case 4:
                    showEffectDialog3(R.drawable.d4, v, "4", type);
                    break;
                case 5:
                    showEffectDialog3(R.drawable.d5, v, "5", type);
                    break;
                case 6:
                    showEffectDialog3(R.drawable.d6, v, "6", type);
                    break;
                case 7:
                    showEffectDialog3(R.drawable.d7, v, "7", type);
                    break;
            }

        }
    }
    private void showEffectDialog3(int resID, int v, String Str, String type) {
        if (sdialog == null || sdialog.isShowing() != true) {
            sdialog = new Sound_Effect_Dialog(this);
            sdialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            sdialog.show();
        }
        sdialog.adjustVolume(v, true, type);
        sdialog.setContentValue(resID, Str);

    }
    private void showVolumeDialog(int direction, String type) {
        if (dialog == null || dialog.isShowing() != true) {
            dialog = new MusicDialog(this);
            //dialog.setVolumeAdjustListener();
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            //dialog.show();
        }
        dialog.adjustVolume(direction, true, type);
    }


    private void showEffectDialog(int direction, String type) {
        if (sdialog == null || sdialog.isShowing() != true) {
            sdialog = new Sound_Effect_Dialog(this);
            //sdialog.setVolumeAdjustListener();
            sdialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            sdialog.show();
        }
        sdialog.adjustVolume(direction, true, type);
    }

    public class MyReceiver extends BroadcastReceiver {
        //adb shell am broadcast -a com.iflytek.xiri2.hal.volume
        //adb shell am broadcast -a com.iflytek.xiri2.hal.volume --es volume 30
        //adb shell am broadcast -a com.globalhome.services
        @Override
        public void onReceive(Context context, Intent intent) {
            int vol = -1;
            String mType="0200";
            Bundle bundle = intent.getExtras();

            if(bundle != null) {
                vol = bundle.getInt("volume", -1);
                mType = bundle.getString("type", "0200");
            }

            //vol = vol * 100 / 60;
            if(vol >60) vol = 60;

            Log.d("MyReceiver--->", intent.getAction() + ":volume=" + vol);

            if (intent.getAction() == "com.iflytek.xiri2.hal.volume") {
                if ((vol >= 0) && (vol <= 60))
                {
                    if (mType.equals("0200")) {
                        //音乐音量
                        showVolumeDialog(vol, mType);
                        MusicVolume = vol;
                        setVolume(MusicVolume);
                    } else if (mType.equals("0300")) {
                        //mic音量
                        showVolumeDialog(vol, mType);
                        MicVolume = vol;
                    } else {
                        showVolumeDialog(vol, mType);
                        MusicVolume = vol;
                    }
                }
                else {
                    showVolumeDialog(MusicVolume, "0200");
                }
            }
        }
    }


    private void setVolume(int i)
    {
        tbb = utils.ChangeTool.intToBytes(i);

        SetMusicVolume[7] = tbb[3];
        SetMusicVolume[8] = tbb[2];
        SetMusicVolume[9] = utils.ChangeTool.BytesAdd(SetMusicVolume, 9);

        SetMusicVolumeK50[7] = tbb[3];
        SetMusicVolumeK50[8] = tbb[2];
        SetMusicVolumeK50[9] = utils.ChangeTool.BytesAdd(SetMusicVolumeK50, 9);


        serialPortUtils.sendBuffer(SetMusicVolume, SetMusicVolume);
        serialPortUtils.sendBuffer(SetMusicVolumeK50, SetMusicVolumeK50);
        Log.e("serialPortUtils", utils.ChangeTool.ByteArrToHexStr(bytes, 0, bytes.length));
    }

}
