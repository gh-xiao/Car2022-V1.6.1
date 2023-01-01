package car.bkrc.com.car2021.Utils.OtherUtil;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import car.bkrc.com.car2021.Utils.CameraUtil.CameraSearchService;
import car.bkrc.com.car2021.Utils.CameraUtil.XcApplication;
import car.bkrc.com.car2021.MessageBean.DataRefreshBean;
import car.bkrc.com.car2021.ActivityView.FirstActivity;

public class CameraConnectUtil {

    public CameraConnectUtil(Context context){
        this.context = context;
    }

    private Context context;

    public void cameraInit() {
        //广播接收器注册
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(A_S);
        context.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    public void cameraStopService(){
        Intent intent = new Intent(context, CameraSearchService.class);
        context.stopService(intent);
    }

    public static final String A_S = "com.a_s";
    public BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context arg0, Intent arg1) {
            FirstActivity.IPCamera = arg1.getStringExtra("IP");
            FirstActivity.pureCameraIP = arg1.getStringExtra("pureip");
            Log.e("camera ip::", "  " + FirstActivity.IPCamera);

            // 如果是串口配置在这里提前启动摄像头驱动，否则是WiFi的话到下个界面再连接
            if (XcApplication.isSerial != XcApplication.Mode.SOCKET) {
                useUartCamera();
            }
            EventBus.getDefault().post(new DataRefreshBean(2));
            context.unregisterReceiver(this);
        }
    };

    // 启动摄像头
    public void useUartCamera() {
        Intent ipintent = new Intent();
        //ComponentName的参数1:目标app的包名,参数2:目标app的Service完整类名
        ipintent.setComponent(new ComponentName("com.android.settings", "com.android.settings.ethernet.CameraInitService"));
        //设置要传送的数据
        ipintent.putExtra("pureCamerAIP", FirstActivity.pureCameraIP);
        context.startService(ipintent);   //摄像头设为静态192.168.16.20时，可以不用发送
    }

    // 搜索摄像cameraIP
    public void search() {
        Intent intent = new Intent(context, CameraSearchService.class);
        context.startService(intent);
    }

    public void destroy(){
        try {
            context.unregisterReceiver(myBroadcastReceiver);
        }catch (RuntimeException ignored){

        }
    }

}
