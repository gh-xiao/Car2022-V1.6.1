package car.bkrc.com.car2021.ActivityView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.king.wechat.qrcode.WeChatQRCodeDetector;
import com.xiao.baiduocr.Predictor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.OpenCVLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import car.bkrc.com.car2021.DataProcessingModule.ConnectTransport;
import car.bkrc.com.car2021.DataProcessingModule.CrashHandler;
import car.bkrc.com.car2021.FragmentView.LeftFragment;
import car.bkrc.com.car2021.FragmentView.RightControlFragment;
import car.bkrc.com.car2021.FragmentView.RightInfraredFragment;
import car.bkrc.com.car2021.FragmentView.RightModuleFragment;
import car.bkrc.com.car2021.FragmentView.RightOtherFragment;
import car.bkrc.com.car2021.FragmentView.RightZigbeeFragment;
import car.bkrc.com.car2021.MessageBean.DataRefreshBean;
import car.bkrc.com.car2021.R;
import car.bkrc.com.car2021.Utils.CameraUtil.XcApplication;
import car.bkrc.com.car2021.Utils.OtherUtil.CameraConnectUtil;
import car.bkrc.com.car2021.Utils.OtherUtil.TitleToolbar;
import car.bkrc.com.car2021.Utils.OtherUtil.ToastUtil;
import car.bkrc.com.car2021.Utils.OtherUtil.Transparent;
import car.bkrc.com.car2021.Utils.TrafficSigns.Yolov5_tflite_TSDetector;
import car.bkrc.com.car2021.ViewAdapter.ViewPagerAdapter;


/**
 * 主页Activity
 */
public class FirstActivity extends AppCompatActivity {

    //目标类的简写名称
    private final String TAG = FirstActivity.class.getSimpleName();
    //全局Context(上下文)
    private static Context mContext;
    //主页视图
    private ViewPager viewPager;
    //全自动按钮
    private Button auto_btn;
    //Toast工具类(底部弹出提示框)
    public static ToastUtil toastUtil;
    //连接传输类
    public static ConnectTransport Connect_Transport;
    //设备ip
    public static String IPCar;
    //摄像头IP
    public static String IPCamera = null;
    public static String pureCameraIP = null;
    //主从状态
    public static boolean chief_status_flag = true;
    //主从控制
//    public static boolean chief_control_flag = true;
    public static Handler recvHandler = null;
    private ViewPager mLateralViewPager;
    private CameraConnectUtil cameraConnectUtil;
    //右上角工具菜单
    private Menu toolMenu;
    //显示初始化对象状态的消息
    private final StringBuilder initMsg = new StringBuilder();

    //底部导航视图
    private final BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.home_page_item:
                    mLateralViewPager.setCurrentItem(0);
                    return true;
                case R.id.scene_setting_item:
                    mLateralViewPager.setCurrentItem(1);
                    return true;
                case R.id.device_manage_item:
                    mLateralViewPager.setCurrentItem(2);
                    return true;
                case R.id.personal_center_item:
                    mLateralViewPager.setCurrentItem(3);
                    return true;
                case R.id.module_page_item:
                    mLateralViewPager.setCurrentItem(4);
                    return true;
            }
            return false;
        }
    };

    /**
     * 页面创建
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 获取Context */
        mContext = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        /* 绑定内容视图 */
        setContentView(R.layout.activity_first1);
        toastUtil = new ToastUtil(this);
        /* 竞赛平台和A72通过usb转串口通信 */
        //TODO 修改连接方式
        if (XcApplication.isSerial == XcApplication.Mode.USB_SERIAL) {
            /* 启动usb的识别和获取 */
            mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
            /* 启动旋转效果的对话框，实现usb的识别和获取 */
            Transparent.showLoadingMessage(this, "正在拼命追赶串口……", false);
        }
        /* EventBus消息注册 */
        /* 关于EventBus的介绍:https://blog.csdn.net/qq_34902522/article/details/84890474 */
        EventBus.getDefault().register(this);
        TitleToolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        /* 全自动按钮注册 */
        auto_btn = findViewById(R.id.auto_drive_btn);
        /* 全自动按钮设置监听事件 */
        auto_btn.setOnClickListener(v -> autoDriveAction());
        /* 使用viewPager实现页面滑动效果 */
        viewPager = findViewById(R.id.viewpager);
        /* 设置预加载页面数量 */
        /* https://blog.csdn.net/qq_30885821/article/details/109842128 */
        viewPager.setOffscreenPageLimit(4);
        /* 底部导航栏 */
        nativeView();
        /* 实例化连接类 */
        Connect_Transport = new ConnectTransport();
        /* 摄像头连接工具类 */
        cameraConnectUtil = new CameraConnectUtil(this);
        //++++++++++++++++++++++++++++++++++++++++++++++++++在下面添加自己需要初始化的项目++++++++++++++++++++++++++++++++++++++++++++++++++
        /* 设置屏幕旋转 */
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        /* 申请权限,如果没有的话 */
        requestPermission();
        /* 初始化全局异常捕获 */
        /* 全局异常捕获对象 */
        CrashHandler.getInstance().init(this);
        initMsg.append("初始化全局异常捕获完毕\n");
        try {
            /* openCV库初始化 */
            inLoadOpenCV();
            /* WeChat二维码识别对象初始化 */
            WeChatQRCodeDetector.init(this);
            /* 百度OCR模型初始化 */
            initMsg.append(onLoadOCRModel() ? "车牌识别模型初始化成功\n" : "车牌识别模型初始化失败\n");
            /* Yolov5s-tflite-trafficSign模型初始化 */
            initMsg.append(onLoadYTModel() ? "交通标志物识别模型创建成功" : "交通标志物识别模型创建失败");
        } catch (Exception e) {
            e.printStackTrace();
        }
        /* 显示初始化消息 */
        toastUtil.ShowToast(initMsg.toString());
    }

    /**
     * <p>设备配置信息更改</p>
     * <p>比如屏幕方向发生改变时</p>
     *
     * @param newConfig 新的设备配置信息
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 导航视图初始化
     */
    private void nativeView() {
        //底部导航视图
        BottomNavigationView navigation = findViewById(R.id.bottomNavigation);
        //设置项目图标颜色列表
        navigation.setItemIconTintList(null);
        //设置所选监听事件的导航项
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //获取到ViewPager(FirstActivity)
        mLateralViewPager = findViewById(R.id.viewpager);
        //加载fragment
        setupViewPager(viewPager);
        //ViewPager的监听
        final BottomNavigationView finalNavigation = navigation;
        mLateralViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //写滑动页面后做的事，使每一个fragment与一个page相对应
                finalNavigation.getMenu().getItem(position).setChecked(true);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * 设置适配器
     */
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        //底部导航栏的四个项目
        adapter.addFragment(RightControlFragment.getInstance());
        adapter.addFragment(RightZigbeeFragment.getInstance());
        adapter.addFragment(RightInfraredFragment.getInstance());
        adapter.addFragment(RightOtherFragment.getInstance());
        adapter.addFragment(RightModuleFragment.getInstance());
        viewPager.setAdapter(adapter);
    }

    /**
     * activity创建时创建菜单Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.tool_rightitem, menu);
        toolMenu = menu;
        return true;
    }

    /**
     * 菜单项监听
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Toast.makeText(FirstActivity.this,item.getTitle(),Toast.LENGTH_SHORT).show();
        switch (id) {
            case R.id.car_status:
                if (item.getTitle().equals("接收实训平台状态")) {
                    chief_status_flag = true;
                    item.setTitle(getResources().getText(R.string.follow_status));
                    Connect_Transport.stateChange(2);

                } else if (item.getTitle().equals("接收移动机器人状态")) {
                    chief_status_flag = false;
                    item.setTitle(getResources().getText(R.string.main_status));
                    Connect_Transport.stateChange(1);

                }
                break;
            case R.id.car_control:
                if (item.getTitle().equals("控制实训平台")) {
//                    chief_control_flag = true;
                    item.setTitle(getResources().getText(R.string.follow_control));
                    Connect_Transport.TYPE = 0xAA;
                } else if (item.getTitle().equals("控制移动机器人")) {
//                    chief_control_flag = false;
                    item.setTitle(getResources().getText(R.string.main_control));
                    Connect_Transport.TYPE = 0x02;
                }
                break;
            case R.id.clear_coded_disc:
                Connect_Transport.clear();
                break;
            case android.R.id.home:
                //https://blog.csdn.net/chen493072/article/details/86159298
                finish();
                break;
            case R.id.set_ConnectTransport_mark:
                ConnectTransport.setMark(1);
                ConnectTransport.setTemp(1);
                break;
            case R.id.Android_Control:
                Connect_Transport.module(0xB4);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * <p>+++++++++++++++++++++++++全自动方法按钮+++++++++++++++++++++++++</p>
     */
    private void autoDriveAction() {
        //对话框构造器
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // 设置Title的内容
        builder.setIcon(R.mipmap.rc_logo);
        builder.setTitle("温馨提示");
        // 设置Content(内容)来显示一个信息
        builder.setMessage("请确认是否开始自动驾驶！");
        // 设置一个PositiveButton(确认按钮)
        builder.setPositiveButton("开始", (dialog, which) -> {
//            Connect_Transport.autoDrive();
            new Thread(() -> Connect_Transport.autoDrive()).start();
            toastUtil.ShowToast("开始自动驾驶，请检查车辆周围环境！");
        });
        // 设置一个NegativeButton
        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    /**
     * <p>+++++++++++++++++++++++++++++++++++++++++++++++++++++++++</p>
     */


    /**
     * 接收Eventbus消息
     *
     * @param refresh
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        if (refresh.getRefreshState() == 4) {
        }
    }

    /**
     * Activity销毁
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraConnectUtil.destroy();
        if (XcApplication.isSerial == XcApplication.Mode.USB_SERIAL) {
            try {
                unregisterReceiver(mUsbPermissionActionReceiver);
                sPort.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException ignored) {
            }
            sPort = null;
        } else if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            Connect_Transport.destroy();
        }
    }

    /**
     * ---------------------------------------------以下大致为串口通讯的实现---------------------------------------------
     */
    //获取和实现usb转串口的通信，实现A72和竞赛平台的串口通信
    public static UsbSerialPort sPort = null;
    //创建大小为1的固定线程池
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    //串行输入输出管理器(串口通讯管理器)
    private SerialInputOutputManager mSerialIoManager;
    //监听事件
    private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() {
        @Override
        public void onRunError(Exception e) {
            Log.e(TAG, "Runner stopped.");
        }

        @Override
        //新的数据
        public void onNewData(final byte[] data) {
            FirstActivity.this.runOnUiThread(() -> {
                Message msg = recvHandler.obtainMessage(1, data);
                msg.sendToTarget();
                FirstActivity.this.updateReceivedData(data);
            });
        }
    };

    //usb控制器
    protected void controlusb() {
        Log.e(TAG, "Resumed, port=" + sPort);
        if (sPort == null) toastUtil.ShowToast("没有串口驱动！");
        else {
            //在打开usb设备前，弹出选择对话框，尝试获取usb权限
            tryGetUsbPermission();
            if (connection == null) {
                mHandler.sendEmptyMessageDelayed(MESSAGE_REFRESH, REFRESH_TIMEOUT_MILLIS);
                toastUtil.ShowToast("串口驱动失败！");
                return;
            }
            try {
                sPort.open(connection);
                sPort.setParameters(115200, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e) {
                toastUtil.ShowToast("串口驱动错误！");
                try {
                    sPort.close();
                } catch (IOException ignored) {
                }
                sPort = null;
                return;
            }
        }
        //设备状态更改时
        onDeviceStateChange();
        //关闭加载对话框
        Transparent.dismiss();
    }

    //获取usb权限
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final int MESSAGE_REFRESH = 101;
    private static final long REFRESH_TIMEOUT_MILLIS = 5000;
    private UsbDeviceConnection connection;
    private UsbManager mUsbManager;

    private void tryGetUsbPermission() {

        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mUsbPermissionActionReceiver, filter);
        PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        //here do emulation to ask all connected usb device for permission
        for (final UsbDevice usbDevice : mUsbManager.getDeviceList().values()) {
            //add some conditional check if necessary
            if (mUsbManager.hasPermission(usbDevice)) {
                //if has already got permission, just goto connect it
                //that means: user has choose yes for your previously popup window asking for grant perssion for this usb device
                //and also choose option: not ask again
                afterGetUsbPermission(usbDevice);
            } else {
                //this line will let android popup window, ask user whether to allow this app to have permission to operate this usb device
                mUsbManager.requestPermission(usbDevice, mPermissionIntent);
            }
        }
    }

    private void afterGetUsbPermission(UsbDevice usbDevice) {
        toastUtil.ShowToast("Found USB device: VID=" + usbDevice.getVendorId() + " PID=" + usbDevice.getProductId());

        connection = mUsbManager.openDevice(usbDevice);
    }

//    private void doYourOpenUsbDevice(UsbDevice usbDevice) {
//        connection = mUsbManager.openDevice(usbDevice);
//    }

    private final BroadcastReceiver mUsbPermissionActionReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        //user choose YES for your previously popup window asking for grant perssion for this usb device
                        if (null != usbDevice) {
                            afterGetUsbPermission(usbDevice);
                        }
                    } else {
                        //user choose NO for your previously popup window asking for grant perssion for this usb device
                        toastUtil.ShowToast("Permission denied for device" + usbDevice);
                    }
                }
            }
        }
    };

    //停止串行IO管理器
    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.e(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    //启动串行IO管理器
    private void startIoManager() {
        if (sPort != null) {
            Log.e(TAG, "Starting io manager ..");
            //添加监听
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            //在新的线程中监听串口的数据变化
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void updateReceivedData(byte[] data) {
        final String message = "Read " + data.length + " bytes: \n" + HexDump.dumpHexString(data) + "\n\n";
        //  Log.e("read data is ：：","   "+message);
    }

    //usb转串口列表
    private final List<UsbSerialPort> mEntries = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_REFRESH) refreshDeviceList();
            else super.handleMessage(msg);
        }
    };

//    @SuppressLint("HandlerLeak")
//    private final Handler usbHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            if (msg.what == 2) {
//                try {
//                    useUsbtoserial();
//                } catch (IndexOutOfBoundsException e) {
//                    //关闭加载对话框
//                    Transparent.dismiss();
//                    toastUtil.ShowToast("串口通信失败，请检查设备连接状态！");
//                }
//            }
//        }
//    };

    private void useUsbtoserial() {
        //A72上只有一个 usb转串口，用position =0即可
        final UsbSerialPort port = mEntries.get(0);
        final UsbSerialDriver driver = port.getDriver();
        final UsbDevice device = driver.getDevice();
        final String usbid = String.format("Vendor %s  ，Product %s",
                HexDump.toHexString((short) device.getVendorId()),
                HexDump.toHexString((short) device.getProductId()));
        Message msg = LeftFragment.showidHandler.obtainMessage(22, usbid);
        msg.sendToTarget();
        sPort = port;
        if (sPort != null) {
            //使用usb功能
            controlusb();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void refreshDeviceList() {
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params) {
                Log.e(TAG, "Refreshing device list ...");
                Log.e("mUsbManager is :", "  " + mUsbManager);
                final List<UsbSerialDriver> drivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
                final List<UsbSerialPort> result = new ArrayList<>();
                for (final UsbSerialDriver driver : drivers) {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    Log.e(TAG, String.format("+ %s: %s port%s", driver, ports.size(), ports.size() == 1 ? "" : "s"));
                    result.addAll(ports);
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result) {
                mEntries.clear();
                mEntries.addAll(result);
//                usbHandler.sendEmptyMessage(2);
                try {
                    useUsbtoserial();
                } catch (IndexOutOfBoundsException e) {
                    //关闭加载对话框
                    Transparent.dismiss();
                    toastUtil.ShowToast("串口通信失败，请检查设备连接状态！");
                }
                Log.e(TAG, "Done refreshing, " + mEntries.size() + " entries found.");
            }
        }.execute((Void) null);
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++在这里添加自己编写的部分代码++++++++++++++++++++++++++++++++++++++++++++++++++

    /**
     * 创建一个静态方法,以便获取Context对象
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * 初始化OpenCV库
     */
    private void inLoadOpenCV() {
        boolean success = OpenCVLoader.initDebug();
        if (success) {
            Log.i(TAG, "OpenCV库加载成功\n");
            initMsg.append("OpenCV库加载成功\n");
        } else {
            Log.i(TAG, "OpenCV库加载失败\n");
            initMsg.append("OpenCV库加载失败\n");
        }
    }

    /**
     * 预测对象类-即控制OCR识别的关键类
     */
    protected static Predictor predictor = new Predictor();

    /**
     * 获取predictor对象方法
     *
     * @return Predictor对象
     */
    public static Predictor getPredictor() {
        return predictor;
    }

    /**
     * <p>调用onCreate()方法初始化百度OCR模型</p>
     * <p>call in Identify, model init</p><
     *
     * @return Boolean - 模型是否初始化成功
     */
    private boolean onLoadOCRModel() {
        if (predictor == null) {
            predictor = new Predictor();
        }
        // Model settings of object detection
        //目标检测的模型设置
        String assetModelDirPath = "models/ocr_v2_for_cpu";
        String assetLabelFilePath = "labels/ppocr_keys_v1.txt";
        return predictor.init(this, assetModelDirPath, assetLabelFilePath);
    }

    private static final Yolov5_tflite_TSDetector yolov5_tflite_tsDetector = new Yolov5_tflite_TSDetector();

    public static Yolov5_tflite_TSDetector getYolov5_tflite_tsDetector() {
        return yolov5_tflite_tsDetector;
    }

    /**
     * 调用onCreate()方法初始化基于Yolov5的tflite模型
     */
    private boolean onLoadYTModel() {
        return yolov5_tflite_tsDetector.LoadModel("CPU", 4, this.getAssets());
    }

    /**
     * 动态权限申请
     */
    private void requestPermission() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.READ_PHONE_STATE
        };
        ArrayList<String> toApplyList = new ArrayList<>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    /**
     * Android 11 跳转到设置获取SD卡根目录写入权限
     * 仅实现了Androidx特性时,以下语句使用才不会报错
     */
//    private void requestAllFilesAccess() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
//            allPermissionsGranted = false;
//
//            android.support.v7.app.AlertDialog.Builder alertBuilder = new android.support.v7.app.AlertDialog.Builder(
//                    this,
//                    R.style.Theme_AppCompat_Light_Dialog_Alert);
//            alertBuilder.setMessage("需授权访问SD卡文件");
//            alertBuilder.setCancelable(false);
//            alertBuilder.setPositiveButton("去设置", (dialog, which) -> {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(intent);
//            });
//            alertBuilder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());
//            alertBuilder.show();
//        }
//    }

    /**
     * 参考来的代码,未在其他地方使用
     */
    @Deprecated
    @SuppressLint("HandlerLeak")
    public Handler phHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 11) {
//                Toast.makeText(FirstActivity.this, Connect_Transport.openmv1_text, Toast.LENGTH_LONG).show();
            }
            //拍照保存
            if (msg.what == 55) {
//                new FileService().savePhoto(LeftFragment.bitmap, Global.PaiZhao + ".png");
                Toast.makeText(FirstActivity.this, "保存照片完成", Toast.LENGTH_LONG).show();
            }
            //识别指示灯颜色
            if (msg.what == 44) {
//                color = getImageColorPixel(LeftFragment.bitmap);
//                Connect_Transport.color = color;
//                Toast.makeText(FirstActivity.this, color, Toast.LENGTH_LONG).show();
            }
            if (msg.what == 100) {
                String yy = "123";
//                new FileService().savePhoto(LeftFragment.bitmap, Global.ENCODE1 + ".png");
                Toast.makeText(FirstActivity.this, yy, Toast.LENGTH_LONG).show();
            }
            //单次扫描二维码
            if (msg.what == 181) {
//                qrHandler.sendEmptyMessage(10);
//                codeflag = true;
            }
            //多次扫描二维码
            if (msg.what == 184) {
//                codeflag = true;
//                qrHandler2.sendEmptyMessage(11);
            }
            //关闭--扫描二维码--线程
            if (msg.what == 183) {
//                codeflag = false;
            }
            //扫描图形形状
            if (msg.what == 180) {
//                shapeColor(LeftFragment.bitmap);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                Toast.makeText(FirstActivity.this, shapeColor + "==" + shapeResult, Toast.LENGTH_LONG).show();
            }
        }
    };
}
