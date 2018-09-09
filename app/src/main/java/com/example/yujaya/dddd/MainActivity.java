package com.example.yujaya.dddd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.widget.CircularProgressDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;

import static android.view.View.INVISIBLE;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static final boolean D = true;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;

    public static final int MODE_REQUEST = 1;

    private static final int STATE_SENDING = 1;
    private static final int STATE_NO_SENDING = 2;
    private int mSendingState;

    private static final int CNT_4 = 4;

    private Button bluetooth_btn;
    private Button cam_btn;
    private BluetoothService bluetoothService = null;
    private StringBuffer mOutStringBuffer;
    public static Bitmap[] imgArray = null;

    public int cnt = 0;

    private ProgressDialog prBar;

    private final Handler mHandler = new Handler(){
        public void handleMessage(Message msg){
            switch(msg.what){
                case MESSAGE_STATE_CHANGE:
                    if(D){
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    }
                    switch (msg.arg1){
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "블루투스 연결 성공", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(),"블루투스 연결 중", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_FAIL:
                            Toast.makeText(getApplicationContext(), "블루투스 연결 실패", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    Log.d(TAG, "MESSAGE_WRITE");
                    Toast.makeText(getApplicationContext(), "메세지 쓰는 중", Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_READ:
                    int tmp = msg.arg2;
                    byte[] t2 = (byte[])msg.obj;
                    if(t2!=null){
                        Log.d(TAG, t2.toString());
                        setImg(t2);
                    }

                    break;
            }
        }
    };

    Handler prHandler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what == CNT_4){
                prBar.dismiss();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        bluetooth_btn = (Button)findViewById(R.id.bluetooth_btn);
        bluetooth_btn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(bluetoothService.getDeviceState()){
                    bluetoothService.enableBluetooth();
                }
                else{
                    finish();
                }
            }
        });

        prBar = new ProgressDialog(this);
        prBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        prBar.setMessage("이미지 로딩중입니다");

        cam_btn = (Button)findViewById(R.id.cam_btn);
        cam_btn.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                Log.i(TAG, "Start Capture");
                imgArray = new Bitmap[4];
                cnt = 0;

                if(bluetoothService.getState() == BluetoothService.STATE_CONNECTED){
                    sendMessage("1", MODE_REQUEST);
                    prBar.show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "블루투스 연결 필요", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(bluetoothService == null){
            bluetoothService = new BluetoothService(this, mHandler);
            mOutStringBuffer = new StringBuffer("");
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d(TAG, "onActivityResult" + resultCode);

        switch(requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode== Activity.RESULT_OK){
                    bluetoothService.scanDevice();
                }
                else{
                    Log.d(TAG, "Bluetooth in not enable");
                }
                break;

            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){
                    bluetoothService.getDeviceInfo(data);
                }
                break;

        }
    }

    private synchronized void sendMessage(String message, int mode){    //안드로이드 -> 아두이노 '1' 전송
        if(mSendingState == STATE_SENDING){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        mSendingState = STATE_SENDING;

        if(bluetoothService.getState() != BluetoothService.STATE_CONNECTED){
            mSendingState = STATE_NO_SENDING;
            return;
        }

        if(message.length() > 0){
            if(message == "1"){
                bluetoothService.write(message, mode);
            }

        }

        mSendingState = STATE_NO_SENDING;
        notify();
    }

    public void setImg(byte[] buf){    //카메라에서 비트맵 이미지 추출
        Bitmap bmp = BitmapFactory.decodeByteArray(buf, 0, buf.length);
        if(bmp != null){
            //Toast.makeText(getApplicationContext(), "비트맵 생성!!!!!!!!!!!!" + cnt, Toast.LENGTH_SHORT).show();
            imgArray[cnt] = bmp;
            cnt++;
            if(cnt==4){
                prHandler.sendEmptyMessage(CNT_4);

                Intent i = new Intent(getApplicationContext(), VrPanoramaActivity.class);
                startActivityForResult(i,0);
            }
        }
        else {
            //Toast.makeText(getApplicationContext(), "실패다 ㅅㅂ...", Toast.LENGTH_SHORT).show();
        }
    }

}
