package car.bkrc.com.car2021.ActivityView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import car.bkrc.com.car2021.Utils.CameraUtil.CameraSearchService;
import car.bkrc.com.car2021.Utils.CameraUtil.XcApplication;
import car.bkrc.com.car2021.MessageBean.DataRefreshBean;
import car.bkrc.com.car2021.Utils.OtherUtil.CameraConnectUtil;
import car.bkrc.com.car2021.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2021.Utils.OtherUtil.WiFiStateUtil;
import car.bkrc.com.car2021.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText device_edit = null;
    private EditText login_edit = null;
    private EditText passwd_edit = null;
    private ToastUtil toastUtil;

    private Button bt_connect = null;
    private CheckBox remember_box = null, wifi_box = null, uart_box = null;

    private ProgressDialog dialog = null;

    //页面创建
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        /* 判断是否是平板*/
        setContentView(isPad(this) ? R.layout.activity_login : R.layout.activity_login_mobilephone);
        // EventBus消息注册
        EventBus.getDefault().register(this);
        CameraConnectUtil cameraConnectUtil = new CameraConnectUtil(this);
        //控件初始化
        findViews();
        //摄像头初始化
        cameraConnectUtil.cameraInit();
        Request();
        //设置屏幕旋转
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
    }

    //Activity数据保留
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    //网络请求
    @RequiresApi(api = Build.VERSION_CODES.M)
    void Request() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            } else {
                if (getConnectWifiSSID().equals("<unknown ssid>")) {
                    toastUtil.ShowToast("当前连接WiFi：" + getConnectWifiSsidTwo());
                } else toastUtil.ShowToast("当前连接WiFi：" + getConnectWifiSSID());
            }
        }
    }

    //权限请求
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // TODO request success
                if (getConnectWifiSSID().equals("<unknown ssid>")) {
                    toastUtil.ShowToast("当前未连接到WiFi，请接入设备WiFi后再试！");
                } else toastUtil.ShowToast("当前连接WiFi：" + getConnectWifiSSID());
            }
        }
    }

    //重新加载实例
    @Override
    protected void onResume() {
        super.onResume();
    }

    private void findViews() {
        toastUtil = new ToastUtil(this);
        device_edit = findViewById(R.id.deviceid);
        login_edit = findViewById(R.id.loginname);
        passwd_edit = findViewById(R.id.loginpasswd);
        Button bt_reset = findViewById(R.id.reset);
        bt_connect = findViewById(R.id.connect);
        remember_box = findViewById(R.id.remember);
        wifi_box = findViewById(R.id.wifi_each);
        uart_box = findViewById(R.id.uart_each);

        bt_reset.setOnClickListener(this);
        bt_connect.setOnClickListener(this);
        remember_box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwd_edit.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                passwd_edit.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        uart_box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                uart_box.setChecked(true);
                wifi_box.setChecked(false);
                XcApplication.isSerial = XcApplication.Mode.USB_SERIAL;
                toastUtil.ShowToast("要把A72开发板的串口线接到竞赛平台哦！");
            } else {
                uart_box.setChecked(false);
            }
        });
        wifi_box.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                wifi_box.setChecked(true);
                uart_box.setChecked(false);
                XcApplication.isSerial = XcApplication.Mode.SOCKET;
                toastUtil.ShowToast("不要忘记把WiFi连接到竞赛平台哦！");
            } else {
                wifi_box.setChecked(false);
            }
        });
    }

    //获取WiFi连接SSID(服务集合标识符)
    private String getConnectWifiSSID() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        Log.d("wifiInfo", wifiInfo.toString());
        Log.d("SSID", wifiInfo.getSSID());
        return wifiInfo.getSSID();
    }

    public String getConnectWifiSsidTwo() {
        WifiManager wifiManager = ((WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE));
        assert wifiManager != null;

        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        String SSID = wifiInfo.getSSID();

        int networkId = wifiInfo.getNetworkId();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfiguration : configuredNetworks) {
            if (wifiConfiguration.networkId == networkId) {
                SSID = wifiConfiguration.SSID;
            }
        }
        return SSID.replace("\"", "");
    }

    //点击事件
    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.reset) {
            device_edit.setText("");
            login_edit.setText("");
            passwd_edit.setText("");
            remember_box.setChecked(false);
        } else if (view.equals(bt_connect)) {
            dialog = new ProgressDialog(this);
            dialog.setMessage("撸起袖子加载中...");
            dialog.show();
            if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
                useNetwork();
            } else {
                useUart();
            }
        }
    }

    /**
     * 接收Eventbus消息
     *
     * @param refresh -
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        if (refresh.getRefreshState() == 2) {
            startFirstActivity();
        }
    }

    // 搜索摄像cameraIP
    private void search() {
        Intent intent = new Intent(LoginActivity.this, CameraSearchService.class);
        startService(intent);
    }

    private void useUart() {
        // 搜索摄像头然后启动摄像头
        search();
    }

    private void useNetwork() {
        if (new WiFiStateUtil(this).wifiInit()) {
            //WiFi初始化成功
            search();
        } else {
            dialog.cancel();
            toastUtil.ShowToast("请确认设备已通过WiFi接入竞赛平台！");
        }
    }

    private void startFirstActivity() {
        dialog.cancel();
        startActivity(new Intent(LoginActivity.this, FirstActivity.class));
        if (FirstActivity.IPCamera.equals("null:81")) {
            toastUtil.ShowToast("摄像头没有找到，快去找找它吧");
        }
        finish();
    }

    //Activity销毁
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // EventBus消息注销
        EventBus.getDefault().unregister(this);
        if (dialog != null) {
            dialog.cancel();
        }
        Log.e("LoginActivity", "onDestroy");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("LoginActivity", "onRestart");
    }

    /**
     * 判断当前设备是手机还是平板，代码来自 Google I/O App for Android
     *
     * @param context -
     * @return 平板返回 True，手机返回 False
     */
    public static boolean isPad(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}

