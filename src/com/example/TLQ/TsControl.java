package com.example.TLQ;
//Author:TOOSAKA
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


//        android:textColor="#4afaff"
public class TsControl extends Activity {


	private Matrix mMatrix = new Matrix();
	private float mAngle_thr = 0;
	Bitmap mBitmap;
	Paint paint = new Paint();

	private int thrTF = 0;//

	mImageListenter mImageButtonListener = new mImageListenter();

	private int VAL_THR = 1500;
	private int VAL_YAW = 1500, VAL_ROL = 1500, VAL_PIT = 1500;
	private int TSVal = 50;

    private TextView TSValll;

	private ImageButton image;
	private int LOCK = 1;
	Timer send_timer = new Timer();


	private final Handler myHandler = new Handler();
	//handler可以分发Message对象和Runnable对象到主线程中
	//handler中 的执行时间过长会出错！！！！！！！！！！
////////////////////////////////////////////////////////////////////////////////////////////////////
	private final Runnable myRunable = new Runnable() {
		@Override
		public void run() {                                                      //控制界面数据显示
			// TODO Auto-generated method stub
			//0.1秒后调用此Runnable对象,用于控制数据的更新
			myHandler.postDelayed(this,100);                                          //等待100ms后进行操作
			//	TSValll = (TextView) findViewById(R.id.textViewww);
			if(MainActivity.CheckTheBluetooth() == 1){
				TSValll.setText("Lost Synch");
			}
			else {
				TSValll.setText("Everything Fine!");
			}
		}
	};
////////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void onCreate(Bundle savedInstanceState) {
////////////////////////////////////////////////////////////////////////////////////////////////////
		super.onCreate(savedInstanceState);                                                         //横屏，并要求全屏显示，且保持屏幕常亮
		//横屏模式
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_ts_control);
////////////////////////////////////////////////////////////////////////////////////////////////////
		if(MainActivity.CheckTheBluetooth() == 1){                                                  //check for bluetooth status
			Toast.makeText(this,"NotConnected...", Toast.LENGTH_SHORT).show();
			this.finish();                         //back to the main activity
			//onDestroy();
		}
////////////////////////////////////////////////////////////////////////////////////////////////////
        TSValll = (TextView) findViewById(R.id.textViewww);


		ImageButton mImageButton;
		DisplayMetrics dm =new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		//按钮监听器
		mImageButton = (ImageButton) findViewById(R.id.thr_up1);
		mImageButton.setOnTouchListener(mImageButtonListener);

		mImageButton = (ImageButton) findViewById(R.id.thr_down1);
		mImageButton.setOnTouchListener(mImageButtonListener);

		mImageButton = (ImageButton) findViewById(R.id.rol_you1);
		mImageButton.setOnTouchListener(mImageButtonListener);

		mImageButton = (ImageButton) findViewById(R.id.rol_zuo1);
		mImageButton.setOnTouchListener(mImageButtonListener);

		mImageButton = (ImageButton) findViewById(R.id.pit_qian1);
		mImageButton.setOnTouchListener(mImageButtonListener);

		mImageButton = (ImageButton) findViewById(R.id.suo1);
		mImageButton.setOnTouchListener(mImageButtonListener);
		//延迟1秒后执行任务send_task,然后经过0.005秒再次执行send_task，
		send_timer.schedule(send_task,1000,5);

		//每0.1秒执行一次runnable
		myHandler.postDelayed(myRunable, 100);

	}

	class mImageListenter implements OnTouchListener{                              //控制键响应

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub

			if(LOCK == 1){
				if(v.getId() == R.id.pit_qian1){                                                    //
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{VAL_THR = 2500;	TSVal = 120;}
					if(event.getAction() == MotionEvent.ACTION_CANCEL)
					{VAL_THR = 1500;	TSVal = 50;}
					if(event.getAction() == MotionEvent.ACTION_UP)
					{VAL_THR = 1500;	TSVal = 50;}

				}

				if(v.getId() == R.id.suo1){                                                         // space button
					if(event.getAction() == MotionEvent.ACTION_DOWN)
					{VAL_THR = 500;		TSVal = 0;}
					if(event.getAction() == MotionEvent.ACTION_CANCEL)
					{VAL_THR = 1500;		TSVal = 50;}
					if(event.getAction() == MotionEvent.ACTION_UP)
					{VAL_THR = 1500;		TSVal = 50;}

				}


				if(v.getId() == R.id.rol_zuo1){
					if(event.getAction() == MotionEvent.ACTION_DOWN)
						VAL_ROL = 1200;
					if(event.getAction() == MotionEvent.ACTION_CANCEL)
						VAL_ROL = 1500;
					if(event.getAction() == MotionEvent.ACTION_UP)
						VAL_ROL = 1500;

				}

				if(v.getId() == R.id.rol_you1){
					if(event.getAction() == MotionEvent.ACTION_DOWN)
						VAL_ROL = 1800;
					if(event.getAction() == MotionEvent.ACTION_CANCEL)
						VAL_ROL = 1500;
					if(event.getAction() == MotionEvent.ACTION_UP)
						VAL_ROL = 1500;
				}


				if(v.getId() == R.id.thr_up1){
						if(event.getAction() == MotionEvent.ACTION_DOWN)
						{VAL_THR = 2000;	TSVal = 80;}
						//{VAL_THR += 15;	mRotate_thr_up();}
						if(event.getAction() == MotionEvent.ACTION_CANCEL)
						{VAL_THR = 1500;	TSVal = 50;}
						if(event.getAction() == MotionEvent.ACTION_UP)
						{VAL_THR = 1500;	TSVal = 50;}

				}

				if(v.getId() == R.id.thr_down1){
						if(event.getAction() == MotionEvent.ACTION_DOWN)
						{VAL_THR = 1000;	TSVal = 20;}
						if(event.getAction() == MotionEvent.ACTION_CANCEL)
						{VAL_THR = 1500;	TSVal = 50;}
						if(event.getAction() == MotionEvent.ACTION_UP)
						{VAL_THR = 1500;	TSVal = 50;}
				}

			}
			return false;
		}
	}

////////////needed:new task for decoding AV port signals//////////////////////////

	TimerTask send_task = new TimerTask(){
		byte[] bytes = new byte[27];
		public void run ()
		{
			//TSValll.setText("test");
			byte sum=0;

			bytes[0] = (byte) 0xaa;
			bytes[1] = (byte) 0xaf;
			bytes[2] = (byte) 0x03;
			bytes[3] = (byte) 0x14;
			bytes[4] = (byte) (VAL_THR/0xff);
			bytes[5] = (byte) (VAL_THR%0xff);//取余
			bytes[6] = (byte) (VAL_YAW/0xff);
			bytes[7] = (byte) (VAL_YAW%0xff);
			bytes[8] = (byte) (VAL_ROL/0xff);
			bytes[9] = (byte) (VAL_ROL%0xff);
			bytes[10] = (byte) (VAL_PIT/0xff);
			bytes[11] = (byte) (VAL_PIT%0xff);
			bytes[12] = 0;
			bytes[13] = 0;
			bytes[14] = 0;
			bytes[15] = 0;
			bytes[16] = 0;
			bytes[17] = 0;
			bytes[18] = 0;
			bytes[19] = 0;
			bytes[20] = 0;
			bytes[21] = 0;
			bytes[22] = 0;
			bytes[23] = 0;
			for(int i=0;i<24;i++) sum += bytes[i];
			bytes[24] = sum;     //1A
			bytes[25] = 0x0d;
			bytes[26] = 0x0a;

			MainActivity.SendData_Byte(bytes);
		}
	};

	protected void onDestroy(){                                                                     //退出 activity 时的动作，注销定时器便可停止数据发送
		if (send_timer != null)
		{
			send_timer.cancel();
			send_timer = null;
		}
		super.onDestroy();
	}

}

