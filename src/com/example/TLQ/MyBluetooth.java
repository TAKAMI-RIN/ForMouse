package com.example.TLQ;
//MyBluetooth：蓝牙连接部分控件
import java.util.Set;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MyBluetooth extends Activity {

	//其他设备地址
	public static String EXTRA_DEVICE_ADDRESS = "device_addres";

	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String>mPairedDevicesArrayAdapter;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;                       //使用三个adapter 显示蓝牙连接dialog

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);//开启窗口并添加不确定进度条
		setContentView(R.layout.activity_my_bluetooth);


		setResult(Activity.RESULT_CANCELED);
		Button mButton = (Button) findViewById(R.id.Scan);
		mButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doDiscovery();
				v.setVisibility(View.GONE);//隐藏控件
			}
		});

		//以view布局的方式显示设备名称
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(this,R.layout.device_name);

		//设置列表框监听器
		ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDevicesArrayAdapter);
		pairedListView.setOnItemClickListener(mClickListener);

		ListView newDeviceListView = (ListView) findViewById(R.id.new_devices);
		newDeviceListView.setAdapter(mNewDevicesArrayAdapter);
		newDeviceListView.setOnItemClickListener(mClickListener);

		//注册新发现的蓝牙设备
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);

		//本地蓝牙适配器
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		//显示已配对设备
		Set<BluetoothDevice>paireDevices = mBtAdapter.getBondedDevices();
		if(paireDevices.size() >0 ){
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for(BluetoothDevice device : paireDevices){
				mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
		else
			mPairedDevicesArrayAdapter.add("No Paired Device");
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();

		//防止重复搜索蓝牙设备
		if(mBtAdapter != null)
			mBtAdapter.cancelDiscovery();

		this.unregisterReceiver(mReceiver);
	}

	private void doDiscovery(){
		setProgressBarIndeterminate(true);
		setTitle("Searching...");

		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

		if(mBtAdapter.isDiscovering())
			mBtAdapter.cancelDiscovery();

		mBtAdapter.startDiscovery();
	}

	//该函数返回蓝牙设备的名字和地址
	private OnItemClickListener mClickListener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?>arg0, View arg1, int arg2, long arg3){
			mBtAdapter.cancelDiscovery();
			String info = ((TextView)arg1).getText().toString();
			String address = info.substring(info.length()-17);

			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

			setResult(Activity.RESULT_OK,intent);
			finish();
		}
	};

	//广播接收器
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent){
			String action = intent.getAction();

			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


				if(device.getBondState() != BluetoothDevice.BOND_BONDED)
					mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
			else
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				setProgressBarIndeterminateVisibility(false);
				setTitle("Choose Device");

				if (mNewDevicesArrayAdapter.getCount() == 0)
					mNewDevicesArrayAdapter.add("No New Devices");

			}
		}
	};
}

