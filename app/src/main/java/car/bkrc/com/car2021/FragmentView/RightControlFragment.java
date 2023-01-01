package car.bkrc.com.car2021.FragmentView;

import static car.bkrc.com.car2021.ActivityView.FirstActivity.Connect_Transport;
import static car.bkrc.com.car2021.ActivityView.FirstActivity.toastUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

import car.bkrc.com.car2021.ActivityView.FirstActivity;
import car.bkrc.com.car2021.ActivityView.LoginActivity;
import car.bkrc.com.car2021.DataProcessingModule.ConnectTransport;
import car.bkrc.com.car2021.MessageBean.DataRefreshBean;
import car.bkrc.com.car2021.R;
import car.bkrc.com.car2021.Utils.CameraUtil.XcApplication;
import car.bkrc.com.car2021.Utils.OtherUtil.WiFiStateUtil;
import car.bkrc.com.car2021.Utils.RFID.Tools;

/**
 * 主页初始导航选项
 */
public class RightControlFragment extends Fragment {

    public static final String TAG = "RightFragment1";

    private TextView Data_show = null;
    private EditText speed_edit = null;
    private EditText coded_disc_edit = null;
    private EditText angle_data_edit = null;
    private TextView displayDebugIfo = null;
    private View view = null;

    // 超声波数据
    private static long ultraSonic;
    // 光照强度
    private static long light;

    public static RightControlFragment getInstance() {
        return RightFragment1Holder.sInstance;
    }

    private static class RightFragment1Holder {
        private static final RightControlFragment sInstance = new RightControlFragment();
    }

    public static long getUltraSonic() {
        return ultraSonic;
    }

    public static int getLight() {
        return (int) (light / 100);
    }

    // 接受显示设备发送的数据
    @SuppressLint("HandlerLeak")
    private final Handler rehHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                byte[] mByte = (byte[]) msg.obj;
                // 信息获取
                if (mByte[0] == 0x55) {
                    // 光敏状态
                    long psStatus = mByte[3] & 0xff;

                    ultraSonic = mByte[5] & 0xff;
                    ultraSonic = ultraSonic << 8;
                    ultraSonic += mByte[4] & 0xff;

                    light = mByte[7] & 0xff;
                    light = light << 8;
                    light += mByte[6] & 0xff;
                    // 码盘
                    long codedDisk = mByte[9] & 0xff;
                    codedDisk = codedDisk << 8;
                    codedDisk += mByte[8] & 0xff;
                    String camera_show_ip = FirstActivity.IPCamera;
                    if (mByte[1] == (byte) 0xaa) {
                        // 主车
                        if (FirstActivity.chief_status_flag) {
                            Data_show.setTextColor(getResources().getColor(R.color.color_white));
                            // 显示数据
                            Data_show.setText("超声波:" + ultraSonic + "mm  " +
                                    "光照度:" + light + "lx  " +
                                    "码盘:" + codedDisk + "  " +
                                    "运行状态:" + (String.valueOf(mByte[2])));
                            // 主车防撞功能简易实现
                            if (ultraSonic <= 100) Connect_Transport.stop();
                        }
                    }

                    if (mByte[1] == (byte) 0x02) {
                        // 从车
                        if (!FirstActivity.chief_status_flag) {
                            if (mByte[2] == -110) {
                                byte[] newData;
                                Log.e("data", "" + mByte[4]);
                                newData = Arrays.copyOfRange(mByte, 5, mByte[4] + 5);
                                Log.e("data", "" + "长度" + newData.length);
                                //第二个参数指定编码方式
                                String str = new String(newData, StandardCharsets.US_ASCII);
                                Toast.makeText(getActivity(), "" + str, Toast.LENGTH_LONG).show();
                            } else {
                                // 显示数据
                                Data_show.setTextColor(getResources().getColor(R.color.black));
                                Data_show.setText("超声波:" + ultraSonic + "mm  " +
                                        "光照度:" + light + "lx  " +
                                        "码盘:" + codedDisk + "  " +
                                        "运行状态:" + (String.valueOf(mByte[2])));
                            }
                        }
                    }
                }

                /**
                 * 以下内容需要自己添加或修改
                 * 自定义信息指令
                 * 验证是否为自定义发送数据启动
                 */
                if (mByte[0] == (byte) 0xAA) {
                    //验证是否为自定义发送数据启动
                    if (mByte[1] == (byte) 0x11) {
                        if (mByte[2] == (byte) 0x0A) {
                            System.out.println("卡success");
                            //RFID卡识别数据传入
                            Tools.data = new Character[16];
                            for (int i = 0; i < 16; i++) {
                                Tools.data[i] = (char) mByte[i + 4];
                            }
                        }
                        if (mByte[2] == (byte) 0x00) {
//                            System.out.println("完成标志位success");
                        }
                    }
                    //启动全自动-旧方案
                    ConnectTransport.setMark(mByte[3]);
//                    new Thread(() -> Connect_Transport.half_Android_old()).start();
                    new Thread(() -> Connect_Transport.Q2_half_Android()).start();
                }
            }
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        } else {
            if (LoginActivity.isPad(Objects.requireNonNull(getActivity())))
                view = inflater.inflate(R.layout.right_control_fragment, container, false);
            else
                view = inflater.inflate(R.layout.right_control_fragment_mobilephone, container, false);
        }
        FirstActivity.recvHandler = rehHandler;
        // EventBus消息注册
        EventBus.getDefault().register(this);
        control_init();
        connect_Open();
        return view;
    }

    /**
     * 页面初始化
     */
    private void control_init() {
        Data_show = view.findViewById(R.id.rvdata);
        speed_edit = view.findViewById(R.id.speed_data);
        coded_disc_edit = view.findViewById(R.id.coded_disc_data);
        angle_data_edit = view.findViewById(R.id.angle_data);

        ImageButton up_bt = view.findViewById(R.id.up_button);
        ImageButton blew_bt = view.findViewById(R.id.below_button);
        ImageButton stop_bt = view.findViewById(R.id.stop_button);
        ImageButton left_bt = view.findViewById(R.id.left_button);
        ImageButton right_bt = view.findViewById(R.id.right_button);

        up_bt.setOnClickListener(new onClickListener2());
        blew_bt.setOnClickListener(new onClickListener2());
        stop_bt.setOnClickListener(new onClickListener2());
        left_bt.setOnClickListener(new onClickListener2());
        right_bt.setOnClickListener(new onClickListener2());
        up_bt.setOnLongClickListener(new onLongClickListener2());
    }

    /**
     * 接收Eventbus消息
     *
     * @param refresh -
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(DataRefreshBean refresh) {
        if (refresh.getRefreshState() == 1 && new WiFiStateUtil(getActivity()).wifiInit()) {
            connect_Open();
        } else if (refresh.getRefreshState() == 3) {
            toastUtil.ShowToast("平台已连接");
        } else if (refresh.getRefreshState() == 4) {
            toastUtil.ShowToast("平台连接失败！");
        } else toastUtil.ShowToast("请检查WiFi连接状态！");

    }

    private void connect_Open() {
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            //开启网络连接线程
            connect_thread();
        } else if (XcApplication.isSerial == XcApplication.Mode.SERIAL) {
            //使用纯串口uart4
            serial_thread();
        }
    }

    private void connect_thread() {
        XcApplication.executorServicetor.execute(() -> FirstActivity.Connect_Transport.connect(rehHandler, FirstActivity.IPCar));
    }

    private void serial_thread() {
        XcApplication.executorServicetor.execute(() -> FirstActivity.Connect_Transport.serial_connect(rehHandler));
    }

    /**
     * 速度
     * @return
     */
    private int getSpeed() {
        String src = speed_edit.getText().toString();
        int speed = 90;
        if (!src.equals("")) {
            speed = Integer.parseInt(src);
        } else {
            toastUtil.ShowToast("请输入设备运行速度！");
        }
        return speed;
    }

    /**
     * 码盘
     * @return
     */
    private int getEncoder() {
        String src = coded_disc_edit.getText().toString();
        int encoder = 20;
        if (!src.equals("")) {
            encoder = Integer.parseInt(src);
        } else {
            toastUtil.ShowToast("请输入码盘值！");
        }
        return encoder;
    }

    /**
     * 旋转
     * @return
     */
    private int getAngle() {
        String src = angle_data_edit.getText().toString();
        int angle = 50;
        if (!src.equals("")) {
            angle = Integer.parseInt(src);
        } else {
            toastUtil.ShowToast("请输入循迹速度值！");
        }
        return angle;
    }

    // 速度
    private int sp_n;

    private class onClickListener2 implements View.OnClickListener {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            sp_n = getSpeed();
            int en_n = getEncoder();
            switch (v.getId()) {
                case R.id.up_button:
                    FirstActivity.Connect_Transport.go(sp_n, en_n);
                    break;
                case R.id.left_button:
                    FirstActivity.Connect_Transport.left(getAngle());
                    break;
                case R.id.right_button:
                    FirstActivity.Connect_Transport.right(getAngle());
                    break;
                case R.id.below_button:
                    FirstActivity.Connect_Transport.back(sp_n, en_n);
                    break;
                case R.id.stop_button:
                    FirstActivity.Connect_Transport.stop();
                    break;
            }
        }
    }

    private class onLongClickListener2 implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View view) {
            if (view.getId() == R.id.up_button) {
                sp_n = getSpeed();
                FirstActivity.Connect_Transport.line(sp_n);
            }
            /*如果将onLongClick返回false，那么执行完长按事件后，还有执行单击事件。如果返回true，只执行长按事件*/
            return true;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}


