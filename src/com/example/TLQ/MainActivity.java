package com.example.TLQ;
//Author:TOOSAKA
//MADE WITH LQresier
//do not copy
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity {
    public static int VAL_ACC_X = 0;
    public static int VAL_ACC_Y = 0;
    public static int VAL_ACC_Z = 0;
    public static int VAL_GYR_X = 0;
    public static int VAL_GYR_Y = 0;
    public static int VAL_GYR_Z = 0;
    public static float VAL_ANG_X = 0;
    public static float VAL_ANG_Y = 0;
    public static float VAL_ANG_Z = 0;

    public static final int MESSAGE_STATIC_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME =4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private String mConnectedDeviceName = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private static BluetoothRfcommClient  mRfcommClient = null;

    private TextView mStatus;

/////////////////////////////////onCreate/////////////////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);                         //锁定横屏
        this.getWindow().setNavigationBarColor(0x9966CCFF);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,                       //屏蔽通知栏
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mStatus = (TextView) findViewById(R.id.message);                                            //蓝牙状态显示用textview
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();                                   //bluetooth adapter

        if(mBluetoothAdapter == null){                                                              //检测bluetooth是否可用
            Toast.makeText(this, "Bluetooth Wrong!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if(!mBluetoothAdapter.isEnabled()){                                                         //若蓝牙未打开，则请求
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);               //此时手机将跳出请求选项
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }

        mRfcommClient = new BluetoothRfcommClient(this,mHandler);

        ImageButton mImageButton = (ImageButton) findViewById(R.id.bluetooth);                      //按下蓝牙键，跳出蓝牙配置界面
        mImageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MyBluetooth.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);

            }
        });

        mImageButton = (ImageButton) findViewById(R.id.control);                                    //按下control，进入控制界面
        mImageButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, TsControl.class);
                startActivity(intent);
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    @Override
    public synchronized void onResume(){                                                            //resume时重新开启蓝牙

        super.onResume();
        if(mRfcommClient != null){
            if(mRfcommClient.getState()==BluetoothRfcommClient.STATE_NONE ){
                //开启蓝牙
                mRfcommClient.start();
            }
        }
    }

    @Override
    public void onDestroy() {                                                                       //destroy即关闭
        if (mRfcommClient != null) mRfcommClient.stop();
        super.onDestroy();
    }


////////////////////////////////////////退出前提示///////////////////////////////////////////////////
    @Override                                                                                       //返回键UI
    public void onBackPressed(){
        Builder mBuilder = new AlertDialog.Builder(this);
        mBuilder.setTitle("TOOSAKA")
                .setMessage("close the BLT?");

        mBuilder.setPositiveButton("sure", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                finish();
            }
        });

        mBuilder.setNegativeButton("no", null).show();
    }
////////////////////////////////////////////////////////////////////////////////////////////////////



    public void onActivityResult(int requestCode, int resultCode, Intent data){
        switch(requestCode){
            case REQUEST_CONNECT_DEVICE:
                if(resultCode == Activity.RESULT_OK){                                               //返回为连接成功
                    String address = data.getExtras().getString(MyBluetooth.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    mRfcommClient.connect(device);

                }
                break;

            case REQUEST_ENABLE_BT:
                if(resultCode != Activity.RESULT_OK){                                               //若连接不成功
                    Toast.makeText(this, "Connect Fail...", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message mMessage){

            switch(mMessage.what){

                case MESSAGE_STATIC_CHANGE:

                    switch(mMessage.arg1){//用arg1、arg2这两个成员变量传递消息                        //连接状态
                        case BluetoothRfcommClient.STATE_CONNECTED:                                 //连接成功时，显示connected to 从机名
                            mStatus.setText("Connected To ");
                            mStatus.append("" + mConnectedDeviceName);
                            break;

                        case BluetoothRfcommClient.STATE_CONNECTING:                                //若正在连接，则显示connecting
                            mStatus.setText("Connecting...");
                            break;

                        case BluetoothRfcommClient.STATE_NONE:                                      //若无连接，则显示fail
                            mStatus.setText("Connect Fail...");
                            break;

                    }
                    break;

                case MESSAGE_READ:                                                                  //
                    byte[] mRead = (byte[]) mMessage.obj;
                    DataAnl(mRead , mMessage.arg1);
                    break;

                case MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = mMessage.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected:"+mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;

                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), mMessage.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    static void SendData(String message){                                                           //SendData函数，用于发送字符串 //used to be static
        if(mRfcommClient.getState()!= BluetoothRfcommClient.STATE_CONNECTED){                       //发送前进行确认
//            Looper.prepare();
//// 此处执行UI操作
//            Toast.makeText(mContext, "text", Toast.LENGTH_SHORT).show();
//            Looper.loop();
            return;
        }

        if(message.length() >0 ){                                                                   //message长度不是0时才能发送
            byte[] send = message.getBytes();
            mRfcommClient.write(send);
        }
    }



    static void SendData_Byte(byte[] data){                                                         //字符数据发送函数
        if(mRfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED) {

            return;
        }
        mRfcommClient.write(data);
    }



    static void Send_Command(byte data){                                                            //控制命令发送函数
        byte[] bytes = new byte[6];
        byte sum = 0;

        if(mRfcommClient.getState() != BluetoothRfcommClient.STATE_CONNECTED)
            return;

        bytes[0] = (byte)0xaa;
        bytes[1] = (byte)0xaf;
        bytes[2] = (byte)0x01;
        bytes[3] = (byte)0x01;
        bytes[4] = (byte)data;

        for(int i=0; i<5; i++)
            sum += bytes[i];

        bytes[5] = sum;
        SendData_Byte(bytes);

    }
    static int Buffer_Length = 1000;
    static byte[] Read = new byte[Buffer_Length];
    static int ReadLength = 0;//读取的数据长度
    static int ReadState = 0;//读取数据的状态
    static int ReadCount = 0;//计数

////////////////////////////////////////////////////////////////////////////////////////////////////
    static void DataAnl(byte[] data, int length){                                                   //数据接收处理函数  //work point 4 me
        for(int i=0; i<length; i++){
            //读第一个AA
            if(ReadState == 0){
                if(data[i] == (byte)0xaa){
                    ReadState = 1;
                    Read[0] = (byte)0xaa;
                }
            }

            //读第二个AA
            else if(ReadState == 1){
                if(data[i] == (byte)0xaa){
                    ReadState = 2;
                    Read[1] = (byte)0xaa;
                }
                else
                    ReadState = 0;
            }

            else if(ReadState == 2){
                ReadState = 3;
                Read[2] = data[i];
            }

            else if(ReadState == 3){
                if(data[i] > 45)
                    ReadState = 0;
                else{
                    ReadState = 4;
                    Read[3] = data[i];
                    ReadLength = data[i];
                    if(ReadLength < 0)
                        ReadLength = -ReadLength;
                    ReadCount = 4;
                }
            }

            else if(ReadState == 4){
                ReadLength--;
                Read[ReadCount] = data[i];
                ReadCount++;
                if(ReadLength <= 0)
                    ReadState = 5;
            }

            else if(ReadState == 5){
                Read[ReadCount] = data[i];
                if(ReadCount <= (Buffer_Length-1))
                    FrameAnl(ReadCount+1);
                ReadState = 0;
            }
        }


    }
////////////////////////////////////////////////////////////////////////////////////////////////////



////////////////////////////////////////////////////////////////////////////////////////////////////
    static void FrameAnl(int length){                                                               //数据帧接收，并转为程序变量
        byte sum = 0;
        for(int i=0; i<(length-1); i++)
            sum += Read[i];
        if(sum==Read[length-1])//两个总和值相等
        {

            if(Read[2]==1)//返回的是加计
            {
                VAL_ANG_X = ((float)(BytetoUint(4)))/100;
                VAL_ANG_Y = ((float)(BytetoUint(6)))/100;
                VAL_ANG_Z = ((float)(BytetoUint(8)))/100;
            }
            if(Read[2]==2)//返回的有加计和陀螺仪
            {
                VAL_ACC_X = BytetoUint(4);
                VAL_ACC_Y = BytetoUint(6);
                VAL_ACC_Z = BytetoUint(8);
                VAL_GYR_X = BytetoUint(10);
                VAL_GYR_Y = BytetoUint(12);
                VAL_GYR_Z = BytetoUint(14);
            }

        }
    }
////////////////////////////////////////////////////////////////////////////////////////////////////



    static short BytetoUint(int count)
    {
        short r = 0;
        r <<= 8;  //r左移8位
        r |= (Read[count] & 0x00ff);
        r <<= 8;
        r |= (Read[count+1] & 0x00ff);
        return r;
    }

    static int CheckTheBluetooth(){
        if(mRfcommClient.getState()!= BluetoothRfcommClient.STATE_CONNECTED){
            return 1;
        }
        else{
            return 0;
        }
    }
}
