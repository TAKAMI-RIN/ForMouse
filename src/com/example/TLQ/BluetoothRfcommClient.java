package com.example.TLQ;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;


//新定义类
public class BluetoothRfcommClient {

	private static final UUID mUUID =  UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	public static final int STATE_NONE = 0;//状态0，什么都不做
	public static final int STATE_CONNECTING = 1;//状态1，正在连接蓝牙设备
	public static final int STATE_CONNECTED = 2;//状态2，已经连接到蓝牙设备

	public BluetoothRfcommClient(Context context , Handler handler){
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = STATE_NONE;
		mHandler = handler;
	}


	//synchronized，用来给对象和方法或者代码块加锁，当它锁定一个方法或者一个代码块的时候，
	//同一时刻最多只有一个线程执行这段代码。


	//设置蓝牙当前状态，并刷新数据
	private synchronized void setState(int state){
		mState = state;
		mHandler.obtainMessage(MainActivity.MESSAGE_STATIC_CHANGE, state, -1).sendToTarget();
	}

	//返回蓝牙当前状态
	public synchronized int getState(){
		return mState;
	}

	public synchronized void start(){

		//取消正在尝试连接的线程
		if(mConnectThread != null){
			mConnectThread.cancel();
			mConnectThread = null;
		}

		//取消正在进行的线程
		if(mConnectedThread != null){
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(STATE_NONE);
	}


	public synchronized void connect(BluetoothDevice device){

		//取消正在尝试连接的线程
		if(mState == STATE_CONNECTING){
			if(mConnectThread != null){
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		//取消已连接的线程
		if(mConnectedThread != null){
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		//开始连接目标设备的线程
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
		//取消已完成连接的线程
		if (mConnectThread != null){
			mConnectThread.cancel();
			mConnectThread = null;
		}

		//取消正在进行的线程
		if (mConnectedThread != null){
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		//启动线程来管理连接和执行传输
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();

		//返回设备名称
		Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	public synchronized void stop() {
		//停止所有线程

		if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
		if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
		setState(STATE_NONE);
	}


	//蓝牙数据发送函数
	public void write(byte[] out){
		ConnectedThread r;
		synchronized (this) {
			if(mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		r.write(out);
	}

	//连接失败处理函数
	private void connectionFailed(){
		setState(STATE_NONE);
		Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(MainActivity.TOAST, "Didn't connect successfully...");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	//连接中断处理函数
	private void connectionLost(){
		setState(STATE_NONE);
		Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
		Bundle bundle =new Bundle();
		bundle.putString(MainActivity.TOAST, "connect paused");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	//该线程在尝试连接蓝牙设备时运行
	private class ConnectThread extends Thread{
		private final BluetoothSocket mSocket;
		private final BluetoothDevice mDevice;

		public ConnectThread(BluetoothDevice device){
			mDevice = device;
			BluetoothSocket socket = null;
			try{
				socket = device.createRfcommSocketToServiceRecord(mUUID);

			}catch(IOException e){}
			mSocket = socket;
		}

		public void run(){

			////取消连接线程
			setName("ConnectThread");
			mAdapter.cancelDiscovery();
			try{
				mSocket.connect();
			}catch(IOException e){
				connectionFailed();
				try{
					mSocket.close();
				}catch(IOException e1){}

				//开启服务
				BluetoothRfcommClient.this.start();
				return;
			}

			//重新设置连接线程
			synchronized(BluetoothRfcommClient.this){
				mConnectThread = null;
			}

			//开始连接线程
			connected(mSocket , mDevice);
		}

		public void cancel(){
			try {
				mSocket.close();
			} catch (IOException e){}
		}
	}//ConnectThread类结束


	private class ConnectedThread extends Thread {
		private final BluetoothSocket mSocket;
		private final InputStream mInputStream;
		private final OutputStream mOutputStream;

		public ConnectedThread(BluetoothSocket socket){
			mSocket = socket;
			InputStream input = null;
			OutputStream output =null;
			try{
				input = socket.getInputStream();
				output = socket.getOutputStream();
			}catch(IOException e){}


			mInputStream = input;
			mOutputStream = output;
		}

		//接收来自飞控的数据
		public void run(){
			byte[] buffer = new byte[1024];
			int bytes;
			while(true){
				try{
					bytes = mInputStream.read(buffer);
					MainActivity.DataAnl(buffer,bytes);
				}catch(IOException e){	connectionLost();	break;	}
			}
		}


		//app发送数据到飞控
		public void write(byte[] buffer){
			try{
				mOutputStream.write(buffer);
				mHandler.obtainMessage(MainActivity.MESSAGE_WRITE,-1,-1,buffer).sendToTarget();
			}catch(IOException e){}
		}

		public void cancel(){
			try{
				mSocket.close();
			}catch(IOException e){}
		}
	}//ConnectedThread类结束
}
