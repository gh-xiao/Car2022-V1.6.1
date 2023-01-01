package car.bkrc.com.car2021.FragmentView;

import static car.bkrc.com.car2021.ActivityView.FirstActivity.IPCamera;
import static car.bkrc.com.car2021.ActivityView.FirstActivity.toastUtil;
import static car.bkrc.com.car2021.FragmentView.RightControlFragment.TAG;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bkrcl.control_car_video.camerautil.CameraCommandUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Objects;

import car.bkrc.com.car2021.ActivityView.FirstActivity;
import car.bkrc.com.car2021.ActivityView.LoginActivity;
import car.bkrc.com.car2021.MessageBean.DataRefreshBean;
import car.bkrc.com.car2021.R;
import car.bkrc.com.car2021.Utils.CameraUtil.XcApplication;
import car.bkrc.com.car2021.Utils.OtherUtil.CameraConnectUtil;
import car.bkrc.com.car2021.Utils.OtherUtil.RadiusUtil;

public class LeftFragment extends Fragment implements View.OnClickListener {

    private float x1 = 0;
    private float y1 = 0;
    private ImageView image_show = null;
    private ImageButton refreshImageButton;
    private Button reference_Btn;
    // 摄像头工具
    public static CameraCommandUtil cameraCommandUtil;
    private static TextView showIP = null;
    private Button state;
    private Button control;
    private CameraConnectUtil cameraConnectUtil;
    public static Handler btChange_Handler;

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view;
        //判断设备
        if (LoginActivity.isPad(Objects.requireNonNull(getActivity())))
            view = inflater.inflate(R.layout.left_fragment, container, false);
        else
            view = inflater.inflate(R.layout.left_fragment_mobilephone, container, false);
        EventBus.getDefault().register(this);
        initView(view);
        cameraCommandUtil = new CameraCommandUtil();
        cameraConnectUtil = new CameraConnectUtil(getContext());
        getCameraPic();
        btChange_Handler = bt_handler;
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET && !IPCamera.equals("null:81")) {
            setCameraConnectState(true);
            showIP.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "Camera-IP:" + FirstActivity.pureCameraIP);
        } else if (XcApplication.isSerial == XcApplication.Mode.SOCKET && IPCamera.equals("null:81")) {
            showIP.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "请重启您的平台设备！");
        }
        return view;
    }

    /**
     * 页面控件初始化
     *
     * @param view 需要初始化的view
     */
    private void initView(View view) {
        //显示图片
        image_show = view.findViewById(R.id.img);
        //刷新按钮的图片
        refreshImageButton = view.findViewById(R.id.refresh_img_btn);
        //刷新按钮
        reference_Btn = view.findViewById(R.id.refresh_btn);
        //刷新按钮图片的监听事件
        refreshImageButton.setOnClickListener(v -> ObjectRotationAnim(refreshImageButton));
        //刷新按钮的监听事件
        reference_Btn.setOnClickListener(v -> ObjectRotationAnim(refreshImageButton));
        //下方ip展示
        showIP = view.findViewById(R.id.showip);
        //摄像头控制
        image_show.setOnTouchListener(new onTouchListener1());
        //?????废弃的控制选项?????
        state = view.findViewById(R.id.state);
        control = view.findViewById(R.id.control);
        Button clear_coded_disc = view.findViewById(R.id.clear_coded_disc);

        state.setOnClickListener(this);
        control.setOnClickListener(this);
        clear_coded_disc.setOnClickListener(this);
    }

    private void getCameraPic() {
        XcApplication.executorServicetor.execute(() -> {
            if (IPCamera.equals("null:81")) return;
            //不断刷新LeftFragment的bitmap
            while (!IPCamera.equals("null:81")) {
                getBitmap();
            }
        });
    }

    /**
     * 在以下方法onEventMainThread()进行了冗余的变量赋值
     */
    @Deprecated
    private final Bitmap newBitmap = null;

    /**
     * 接收Eventbus消息
     *
     * @param refresh -
     */
    @SuppressLint("SetTextI18n")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        switch (refresh.getRefreshState()) {
            case 1:
                Log.e(TAG, "onEventMainThread: " + IPCamera);
                if (IPCamera.equals("null:81")) {
                    Log.e(TAG, "onEventMainThread: " + IPCamera);
                }
                cameraConnectUtil.cameraStopService();
                cameraConnectUtil.cameraInit();
                cameraConnectUtil.search();
                break;
            case 2:
                getCameraPic();
                if (XcApplication.isSerial == XcApplication.Mode.SOCKET && !IPCamera.equals("null:81")) {
                    setCameraConnectState(true);
                    toastUtil.ShowToast("摄像头已连接");
                    showIP.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "Camera-IP:" + FirstActivity.pureCameraIP);
                } else if (XcApplication.isSerial == XcApplication.Mode.SOCKET && IPCamera.equals("null:81")) {
                    showIP.setText("WiFi-IP：" + FirstActivity.IPCar + "\n" + "请重启您的平台设备！");
                }
                break;
            case 4:
                connectState = false;
                if (bitmap != null) bitmap.recycle();
                setBitmap(null);
                break;
        }
    }


    /**
     * 对网络连接状态进行判断
     *
     * @return true-可用;false-不可用
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            // 获取手机所有连接管理对象(包括对wi-fi,net等连接的管理)
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            // 获取NetworkInfo对象
            assert manager != null;
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();
            //判断NetworkInfo对象是否为空
            if (networkInfo != null)
                return networkInfo.isConnected();
        }
        return false;
    }

    @SuppressLint("HandlerLeak")
    public static Handler showidHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 22) {
                showIP.setText(msg.obj + "\n" + "Camera-IP: " + IPCamera);
            }
        }
    };

    public static void setBitmap(Bitmap bitmap) {
        LeftFragment.bitmap = bitmap;
    }

    // 图片
    public static Bitmap bitmap;

    // 摄像头连接状态，默认为true
    private boolean cameraConnectState = true;

    public boolean isCameraConnectState() {
        return cameraConnectState;
    }

    public void setCameraConnectState(boolean cameraConnectState) {
        this.cameraConnectState = cameraConnectState;
    }

    private boolean connectState = true;

    // 得到当前摄像头的图片信息
    public void getBitmap() {
        setBitmap(cameraCommandUtil.httpForImage(IPCamera));
        if (bitmap != null) {
            setBitmap(RadiusUtil.roundBitmapByXfermode(bitmap, bitmap.getWidth(), bitmap.getHeight(), 8));
        } else {
            setCameraConnectState(false);
        }
        phHandler.sendEmptyMessage(10);
    }

    // 显示图片
    @SuppressLint("HandlerLeak")
    private final Handler phHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 10) {
                image_show.setImageBitmap(bitmap);
            } else if (msg.what == 11) {
            }
        }
    };

    private class onTouchListener1 implements View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO 自动生成的方法存根
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 点击位置坐标
                case MotionEvent.ACTION_DOWN:
                    x1 = event.getX();
                    y1 = event.getY();
                    break;
                // 弹起坐标
                case MotionEvent.ACTION_UP:
                    float x2 = event.getX();
                    float y2 = event.getY();
                    float xx = x1 > x2 ? x1 - x2 : x2 - x1;
                    float yy = y1 > y2 ? y1 - y2 : y2 - y1;
                    // 判断滑屏趋势
                    int MINLEN = 30;
                    if (xx > yy) {
                        // left
                        if ((x1 > x2) && (xx > MINLEN)) {
                            toastUtil.ShowToast("向左微调");
                            XcApplication.executorServicetor.execute(() -> {
                                //左
                                cameraCommandUtil.postHttp(IPCamera, 4, 1);
                            });
                        }
                        // right
                        else if ((x1 < x2) && (xx > MINLEN)) {
                            toastUtil.ShowToast("向右微调");
                            XcApplication.executorServicetor.execute(() -> {
                                //右
                                cameraCommandUtil.postHttp(IPCamera, 6, 1);
                            });
                        }
                    }
                    // up
                    else {
                        if ((y1 > y2) && (yy > MINLEN)) {
                            toastUtil.ShowToast("向上微调");
                            XcApplication.executorServicetor.execute(() -> {
                                //上
                                cameraCommandUtil.postHttp(IPCamera, 0, 1);
                            });
                        }
                        // down
                        else if ((y1 < y2) && (yy > MINLEN)) {
                            toastUtil.ShowToast("向下微调");
                            XcApplication.executorServicetor.execute(() -> {
                                //下
                                cameraCommandUtil.postHttp(IPCamera, 2, 1);
                            });
                        }
                    }
                    x1 = 0;
                    x2 = 0;
                    y1 = 0;
                    y2 = 0;
                    break;
            }
            return true;
        }
    }

    /**
     * 建议前往FirstActivity修改监听内容
     *
     * @param view -
     */
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        Button bt = (Button) view;
        String content = (String) bt.getText();

        switch (view.getId()) {
            case R.id.state:
                if (content.equals("接收主车状态")) {
                    FirstActivity.chief_status_flag = true;
                    bt.setText(getResources().getText(R.string.follow_status));
                    FirstActivity.Connect_Transport.stateChange(2);
                    FirstActivity.but_handler.obtainMessage(11).sendToTarget();
                } else if (content.equals("接收从车状态")) {
                    FirstActivity.chief_status_flag = false;
                    bt.setText(getResources().getText(R.string.main_status));
                    FirstActivity.Connect_Transport.stateChange(1);
                    FirstActivity.but_handler.obtainMessage(22).sendToTarget();
                }
                break;
            case R.id.control:
                if (content.equals("控制主车")) {
                    FirstActivity.chief_control_flag = true;
                    bt.setText(getResources().getText(R.string.follow_control));
                    FirstActivity.Connect_Transport.TYPE = 0xAA;
                    FirstActivity.but_handler.obtainMessage(33).sendToTarget();
                } else if (content.equals("控制从车")) {
                    FirstActivity.chief_control_flag = false;
                    bt.setText(getResources().getText(R.string.main_control));
                    FirstActivity.Connect_Transport.TYPE = 0x02;
                    FirstActivity.but_handler.obtainMessage(44).sendToTarget();
                }
                break;
            case R.id.clear_coded_disc:
                FirstActivity.Connect_Transport.clear();
                break;
            default:
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler bt_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 21:
                    state.setText(getResources().getText(R.string.follow_status));
                    break;
                case 22:
                    state.setText(getResources().getText(R.string.main_status));
                    break;
                case 23:
                    control.setText(getResources().getText(R.string.follow_control));
                    break;
                case 24:
                    control.setText(getResources().getText(R.string.main_control));
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 刷新按钮实现顺时针360度
     *
     * @param view -
     */
    private void ObjectRotationAnim(View view) {
        //构造ObjectAnimator对象的方法
        EventBus.getDefault().post(new DataRefreshBean(1));
        // 设置顺时针360度旋转
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0.0F, 360.0F);
        //设置旋转时间
        animator.setDuration(1500);
        //开始执行动画（顺时针旋转动画）
        animator.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
