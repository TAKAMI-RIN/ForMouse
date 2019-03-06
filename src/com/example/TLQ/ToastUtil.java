package com.example.TLQ;
//Author:TOOSAKA
//toast 工具类，用于优化toast显示
    import android.content.Context;  
    import android.os.Handler;  
    import android.os.Looper;  
    import android.widget.Toast;

    public class ToastUtil {  
  
    private static Handler handler = new Handler(Looper.getMainLooper());  
  
    private static Toast toast = null;  
      
    private static Object synObj = new Object();  
  
    public static void showMessage(final Context act, final String msg) {  
        showMessage(act, msg, Toast.LENGTH_SHORT);  
    }  
  
    public static void showMessage(final Context act, final int msg) {  
        showMessage(act, msg, Toast.LENGTH_SHORT);  
    }  
  
    public static void showMessage(final Context act, final String msg,
            final int len) {                                                                        //最终toast显示部分，解决toast bugs
        new Thread(new Runnable() {  
            public void run() {  
                handler.post(new Runnable() {  
                    @Override  
                    public void run() {  
                        synchronized (synObj) {  
                            if (toast != null) {  
                                toast.cancel();  
                                toast.setText(msg);  
                                toast.setDuration(len);  
                            } else {  
                                toast = Toast.makeText(act, msg, len);  
                            }  
                            toast.show();  
                        }  
                    }  
                });  
            }  
        }).start();  
    }  
  
  
    public static void showMessage(final Context act, final int msg,  
            final int len) {  
        new Thread(new Runnable() {  
            public void run() {  
                handler.post(new Runnable() {  
                    @Override  
                    public void run() {  
                        synchronized (synObj) {  
                            if (toast != null) {  
                                toast.cancel();  
                                toast.setText(msg);  
                                toast.setDuration(len);  
                            } else {  
                                toast = Toast.makeText(act, msg, len);  
                            }  
                            toast.show();  
                        }  
                    }  
                });  
            }  
        }).start();  
    }  


}  