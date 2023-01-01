package car.bkrc.com.car2021.DataProcessingModule;

import static car.bkrc.com.car2021.ActivityView.FirstActivity.IPCamera;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.king.wechat.qrcode.WeChatQRCodeDetector;
import com.xiao.baiduocr.Predictor;

import org.greenrobot.eventbus.EventBus;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.objdetect.QRCodeDetector;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import car.bkrc.com.car2021.ActivityView.FirstActivity;
import car.bkrc.com.car2021.FragmentView.LeftFragment;
import car.bkrc.com.car2021.FragmentView.RightControlFragment;
import car.bkrc.com.car2021.MessageBean.DataRefreshBean;
import car.bkrc.com.car2021.Utils.BaiduOCR.Antijamming;
import car.bkrc.com.car2021.Utils.BaiduOCR.PlateDetector;
import car.bkrc.com.car2021.Utils.CameraUtil.XcApplication;
import car.bkrc.com.car2021.Utils.OtherUtil.SerialPort;
import car.bkrc.com.car2021.Utils.QRcode.GetCode;
import car.bkrc.com.car2021.Utils.QRcode.QRBitmapCutter;
import car.bkrc.com.car2021.Utils.QRcode.QR_Recognition;
import car.bkrc.com.car2021.Utils.Shape.ShapeIdentify;
import car.bkrc.com.car2021.Utils.TrafficLight.ColorProcess;
import car.bkrc.com.car2021.Utils.TrafficLight.TrafficLight;
import car.bkrc.com.car2021.Utils.TrafficLight.TrafficLight_fix;

/**
 * Socket数据处理类
 */
public class ConnectTransport {

    private final String TAG = ConnectTransport.class.getSimpleName();
    private Socket socket;
    //客户端输入流
    private DataInputStream bInputStream;
    private InputStream SerialInputStream;
    //客户端输出流
    private DataOutputStream bOutputStream;
    private OutputStream SerialOutputStream;
    //消息线程
    private Handler reHandler;
    public byte[] rByte = new byte[50];
    //串口输入字节
    byte[] serialReadByte = new byte[50];
    private boolean inputDataState = false;
    //判断FirstActivity是否已销毁了
    private boolean firstDestroy = false;
    //OCR识别对象类
    protected volatile Predictor predictor;
    //识别的车牌号
    private String plate;
    //交通标志物识别编号
    private static short getTrafficFlag = 0x03;
    //二维码解析结果对象
    private GetCode code;
    //二维码识别结果
    private String qrResult;
    //图形识别结果
    private int shapeResult = 1;
    //主从车控制判断
    public short TYPE = 0xAA;
    //从车与其他道具交互类型指令
    public short TYPE1 = 0x02;

    //构造器
    public ConnectTransport() {
        predictor = FirstActivity.getPredictor();
    }

    //销毁socket
    public void destroy() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                bInputStream.close();
                bOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //建立通讯
    public void connect(Handler reHandler, String IP) {
        try {
            this.reHandler = reHandler;
            firstDestroy = false;
            int port = 60000;
            socket = new Socket(IP, port);
            bInputStream = new DataInputStream(socket.getInputStream());
            bOutputStream = new DataOutputStream(socket.getOutputStream());
            if (!inputDataState) reThread();
            EventBus.getDefault().post(new DataRefreshBean(3));
        } catch (SocketException ignored) {
            EventBus.getDefault().post(new DataRefreshBean(4));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //串口通讯
    public void serial_connect(Handler reHandler) {
        this.reHandler = reHandler;
        try {
            int baudrate = 115200;
            String path = "/dev/ttyS4";
            SerialPort mSerialPort = new SerialPort(new File(path), baudrate, 0);
            SerialOutputStream = mSerialPort.getOutputStream();
            SerialInputStream = mSerialPort.getInputStream();
            //new Thread(new SerialRunnable()).start();
            //reThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        XcApplication.executorServicetor.execute(new SerialRunnable());
//        new Thread(new SerialRunnable()).start();
    }

    //串口通讯线程
    class SerialRunnable implements Runnable {
        @Override
        public void run() {
            while (SerialInputStream != null) {
                try {
                    int num = SerialInputStream.read(serialReadByte);
                    // String  readSerialStr =new String(serialReadByte);
                    String readSerialStr = new String(serialReadByte, 0, num, "utf-8");
                    Log.e("----serialReadByte----", "******" + readSerialStr);
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = serialReadByte;
                    reHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                try {
//                    Thread.sleep(1);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }
    }

    //Activity消息通讯线程
    private void reThread() {
        new Thread(() -> {
            // TODO Auto1-generated method stub
            while (socket != null && !socket.isClosed()) {
                //FirstActivity 已销毁了
                if (firstDestroy) break;
                try {
                    inputDataState = true;
                    bInputStream.read(rByte);
                    Message msg = new Message();
                    msg.what = 1;
                    msg.obj = rByte;
                    reHandler.sendMessage(msg);
                } catch (SocketException ignored) {
                    EventBus.getDefault().post(new DataRefreshBean(4));
                    destroy();
                    inputDataState = false;
                } catch (IOException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new DataRefreshBean(4));
                    destroy();
                    inputDataState = false;
                } catch (UnsupportedOperationException ignored) {
                    inputDataState = false;
                }
            }
        }).start();

    }

    /**
     * <p>zigbee主车与从车通讯方法</p>
     * <p>TYPE = 0xAA -> 主车</p>
     * <p>TYPE = 0x02 -> 从车</p>
     *
     * @param MAJOR  操作指令-在主车上也被称为包头
     * @param FIRST  指令1
     * @param SECOND 指令2
     * @param THIRD  指令3
     */
    private void send(short MAJOR, short FIRST, short SECOND, short THIRD) {
        short CHECKSUM = (short) ((MAJOR + FIRST + SECOND + THIRD) % 256);
        // 发送数据字节数组
        final byte[] sByte = {0x55, (byte) TYPE, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THIRD, (byte) CHECKSUM, (byte) 0xBB};
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(() -> {
                // TODO Auto-generated method stub
                try {
                    if (socket != null && !socket.isClosed()) {
                        bOutputStream.write(sByte, 0, sByte.length);
                        bOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.SERIAL) {
            XcApplication.executorServicetor.execute(() -> {
                try {
                    SerialOutputStream.write(sByte, 0, sByte.length);
                    SerialOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.USB_SERIAL)
            try {
                FirstActivity.sPort.write(sByte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            }
//        FirstActivity.toastUtil.ShowToast("成功发送指令" + Arrays.toString(sByte));
    }

    /**
     * <p>zigbee其他通讯方法</p>
     * <p>[ 0x55, TYPE1, MAJOR, FIRST, SECOND, THIRD, CHECKSUM, 0xBB ]</p>
     *
     * @param MAJOR  操作指令-在主车上也被称为包头
     * @param FIRST  指令1
     * @param SECOND 指令2
     * @param THIRD  指令3
     */
    private void sendOther(short MAJOR, short FIRST, short SECOND, short THIRD) {
        short CHECKSUM = (short) ((MAJOR + FIRST + SECOND + THIRD) % 256);
        // 发送数据字节数组
        final byte[] sByte = {0x55, (byte) TYPE1, (byte) MAJOR, (byte) FIRST, (byte) SECOND, (byte) THIRD, (byte) CHECKSUM, (byte) 0xBB};
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(() -> {
                // TODO Auto-generated method stub
                try {
                    if (socket != null && !socket.isClosed()) {
                        bOutputStream.write(sByte, 0, sByte.length);
                        bOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.SERIAL) {
            XcApplication.executorServicetor.execute(() -> {
                try {
                    SerialOutputStream.write(sByte, 0, sByte.length);
                    SerialOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.USB_SERIAL)
            try {
                FirstActivity.sPort.write(sByte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            }
//        FirstActivity.toastUtil.ShowToast("成功发送指令" + Arrays.toString(sByte));
    }

    /**
     * 语音播报
     *
     * @param textByte -
     */
    public void send_voice(final byte[] textByte) {
        if (XcApplication.isSerial == XcApplication.Mode.SOCKET) {
            XcApplication.executorServicetor.execute(() -> {
                // TODO Auto-generated method stub
                try {
                    if (socket != null && !socket.isClosed()) {
                        bOutputStream.write(textByte, 0, textByte.length);
                        bOutputStream.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.SERIAL) {
            XcApplication.executorServicetor.execute(() -> {
                try {
                    SerialOutputStream.write(textByte, 0, textByte.length);
                    SerialOutputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else if (XcApplication.isSerial == XcApplication.Mode.USB_SERIAL)
            try {
                FirstActivity.sPort.write(textByte, 5000);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException ignored) {
                Log.e("UART:", "offline");
            }
    }

    /**
     * 前进
     *
     * @param sp_n 速度
     * @param en_n 码盘
     */
    public void go(int sp_n, int en_n) {
        short MAJOR = 0x02;
        send(MAJOR, (byte) (sp_n & 0xFF), (byte) (en_n & 0xff), (byte) (en_n >> 8));
    }

    /**
     * 后退
     *
     * @param sp_n 速度
     * @param en_n 码盘
     */
    public void back(int sp_n, int en_n) {
        short MAJOR = 0x03;
        send(MAJOR, (byte) (sp_n & 0xFF), (byte) (en_n & 0xff), (byte) (en_n >> 8));
    }

    /**
     * 左转
     *
     * @param sp_n 速度
     */
    public void left(int sp_n) {
        short MAJOR = 0x04;
        send(MAJOR, (byte) (sp_n & 0xFF), (byte) 0x00, (byte) 0x00);
    }

    /**
     * 右转
     *
     * @param sp_n 速度
     */
    public void right(int sp_n) {
        short MAJOR = 0x05;
        send(MAJOR, (byte) (sp_n & 0xFF), (byte) 0x00, (byte) 0x00);
    }

    /**
     * 停车
     */
    public void stop() {
        short MAJOR = 0x01;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 循迹
     *
     * @param sp_n 速度
     */
    public void line(int sp_n) {
        short MAJOR = 0x06;
        send(MAJOR, (byte) (sp_n & 0xFF), (short) 0x00, (short) 0x00);
    }

    /**
     * 清除码盘值
     */
    public void clear() {
        short MAJOR = 0x07;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 主从车状态转换
     *
     * @param i =1:从车/=2:主车
     */
    public void stateChange(final int i) {
        final short temp = TYPE;
        short MAJOR = 0x80;
        new Thread(() -> {
            //从车状态
            if (i == 1) {
                //接收从车数据
                TYPE = 0x02;
                send(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
                YanChi(500);
                //关闭接送主车数据
                TYPE = 0xAA;
                send(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
            }
            // 主车状态
            else if (i == 2) {
                //关闭接送从车数据
                TYPE = 0x02;
                send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
                YanChi(500);
                //接收主车数据
                TYPE = 0xAA;
                send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
            }
            TYPE = temp;
        }).start();
    }

    /**
     * 红外
     *
     * @param one   -
     * @param two   -
     * @param third -
     * @param four  -
     * @param five  -
     * @param six   -
     */
    public void infrared(final byte one, final byte two, final byte third, final byte four, final byte five, final byte six) {
        new Thread(() -> {
            short MAJOR = 0x10;
            send(MAJOR, one, two, third);
            YanChi(500);
            MAJOR = 0x11;
            send(MAJOR, four, five, six);
            YanChi(500);
            MAJOR = 0x12;
            send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
            YanChi(1000);
        }).start();
    }

    //双色led灯
    public void lamp(byte command) {
        short MAJOR = 0x40;
        send(MAJOR, command, (short) 0x00, (short) 0x00);
    }

    //小车方向指示灯
    public void light(int left, int right) {
        short MAJOR = 0x20;
        if (left == 1 && right == 1) {
            send(MAJOR, (short) 0x01, (short) 0x01, (short) 0x00);
        } else if (left == 1 && right == 0) {
            send(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
        } else if (left == 0 && right == 1) {
            send(MAJOR, (short) 0x00, (short) 0x01, (short) 0x00);
        } else if (left == 0 && right == 0) {
            send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
        }
    }

    //蜂鸣器
    public void buzzer(int i) {
        short MAJOR = 0x30;
        short FIRST = (short) (i == 1 ? 0x01 : 0x00);
        send(MAJOR, FIRST, (short) 0x00, (short) 0x00);
//        小车内置硬件,如需要让从车启动,则将TYPE设为0x02
//        sendSecond(MAJOR, FIRST, (short) 0x00, (short) 0x00);
    }

    /**
     * 从车二维码识别
     * TODO 注意查看从车重新修改过的指令
     *
     * @param state 开启/关闭识别
     */
    public void qr_rec(int state) {
//        sendOther()调用的TYPE1默认值为0x02,即从车通讯
        short MAJOR = (byte) state;
        sendOther(MAJOR, (short) 0x92, (short) 0x00, (short) 0x00);
    }

    //加光照档位
    public void gear(int i) {
        short MAJOR = 0x61;
        if (i == 1) {
        } else if (i == 2)
            MAJOR = 0x62;
        else if (i == 3)
            MAJOR = 0x63;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
//        红外通讯,如需要让从车启动,则将TYPE设为0x02
//        sendSecond(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    //立体显示-新
    public void infrared_stereo(final short[] data) {
        short MAJOR = 0x10;
        send(MAJOR, (short) 0xff, data[0], data[1]);
        YanChi(500);
        MAJOR = 0x11;
        send(MAJOR, data[2], data[3], data[4]);
        YanChi(500);
        MAJOR = 0x12;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(700);
    }

    //立体显示-旧-废弃通讯
    @Deprecated
    public void infrared_dis(final short[] data) {
        new Thread(() -> {
            short MAJOR = 0x10;
            send(MAJOR, (short) 0xff, data[0], data[1]);
            YanChi(500);
            MAJOR = 0x11;
            send(MAJOR, data[2], data[3], data[4]);
            YanChi(500);
            MAJOR = 0x12;
            send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
            YanChi(500);
        }).start();
    }

    //智能交通灯
    public void traffic_control(int type, int major, int first) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) type;
        sendOther((byte) major, (byte) first, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    /**
     * 舵机角度控制
     *
     * @param major 左侧舵机
     * @param first 右侧舵机
     */
    public void rudder_control(int major, int first) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) 0x0C;
        sendOther((byte) 0x08, (byte) major, (byte) first, (short) 0x00);
        TYPE1 = temp;
    }

    //立体车库控制
    public void garage_control(int type, int major, int first) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) type;
        sendOther((byte) major, (byte) first, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    //闸门
    public void gate(int major, int first, int second, int third) {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x03;
        sendOther((byte) major, (byte) first, (byte) second, (byte) third);
        TYPE1 = temp;
    }

    /**
     * LCD 显示标志物进入计时模式
     */
    //数码管停止计时
    public void digital_close() {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x04;
        short MAJOR = 0x03;
        sendOther(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    //数码管开始计时
    public void digital_open() {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x04;
        short MAJOR = 0x03;
        sendOther(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    //数码管清零
    public void digital_clear() {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x04;
        short MAJOR = 0x03;
        sendOther(MAJOR, (short) 0x02, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    //LCD显示标志物第二排显示距离
    public void digital_dic(int dis) {
        byte temp = (byte) TYPE1;
        int a, b, c;
        a = (dis / 100) & (0xF);
        b = (dis % 100 / 10) & (0xF);
        c = (dis % 10) & (0xF);
        b = b << 4;
        b = b | c;
        TYPE1 = 0x04;
        short MAJOR = 0x04;
        sendOther(MAJOR, (short) 0x00, (short) a, (short) b);
        TYPE1 = temp;
    }

    //数码管
    public void digital(int i, int one, int two, int three) {
        byte temp = (byte) TYPE1;
        TYPE1 = 0x04;
        //i==1数据写入第一排数码管//i==2数据写入第二排数码管
        short MAJOR = (short) (i == 1 ? 0x01 : 0x02);
        sendOther(MAJOR, (byte) one, (byte) two, (byte) three);
        TYPE1 = temp;
    }

    //语音播报随机指令
    public void VoiceBroadcast() {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) 0x06;
        sendOther((short) 0x20, (short) 0x01, (short) 0x00, (short) 0x00);
        TYPE1 = temp;
    }

    //tft lcd
    public void TFT_LCD(int type, int MAIN, int KIND, int COMMAND, int DEPUTY) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) type;
        sendOther((short) MAIN, (byte) KIND, (byte) COMMAND, (byte) DEPUTY);
        TYPE1 = temp;
    }

    //磁悬浮
    public void magnetic_suspension(int MAIN, int KIND, int COMMAD, int DEPUTY) {
        byte temp = (byte) TYPE1;
        TYPE1 = (short) 0x0A;
        sendOther((short) MAIN, (byte) KIND, (byte) COMMAD, (byte) DEPUTY);
        TYPE1 = temp;
    }

    //线程延迟
    public void YanChi(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /******************************************************************************************/

    /**
     * <p>程序自动执行</p>
     * <p>(也可以从这里修改需要启动的自动驾驶方案或需要测试的模块) - 当前已经不建议这样做,你可以选择从{@link car.bkrc.com.car2021.ViewAdapter.ModuleAdapter}中的module_select()方法添加并测试相应模块</p>
     */
    public void autoDrive() {
        /* 通讯测试 */
//        final short temp = TYPE;
//        short MAJOR = 0x00;
//        TYPE = 0xEE;
//        send(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
//        YanChi(500);
//        TYPE = temp;
        /* 全安卓1方案 */
//        Q1();
        /* 半安卓2方案 */
        Q2_half_Android();
        /* 全安卓2方案 */
//        Q2();
        /* 二维码模块测试 */
//        QR_mod();
        /* 车牌OCR识别模块测试 */
//        plate_mod_branch2();
        /* 形状识别测试 */
//        Shape_mod();
        /* 立体显示标志物 */
//        infrared_stereo(new short[]{0x11, getTrafficFlag, getTrafficFlag, 0x00, 0x00});
        /* ----- */
//        ReadCard_long2crossroads();
        /* ????? */
//        YanChi(1500);
//        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
//        YanChi(2500);
//        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
//        ReadCard_short2crossroads();
//        YanChi(1000);
//        left(90);
//        YanChi(2500);
//        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
//        YanChi(4000);
//        ReadCard_longLine();
//        YanChi(1000);
//        ReadCard_short2crossroads();
    }

    /**
     * <p>程序自动执行</p>
     * 官方原方法 - 弃用
     */
    @Deprecated
    public void Deprecated_autoDrive() {
        short MAJOR = 0xA0;
        send(MAJOR, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * +++++++++++++++++++++++++
     */

    /**
     * 半安卓控制方案-旧-V1.0
     */
    //半安卓控制的处理模块值
    private static int mark = 1;

    public static void setMark(int mark) {
        ConnectTransport.mark = mark;
    }

    @SuppressWarnings("DanglingJavadoc")
    @Deprecated
    public void half_Android_old() {
        switch (mark) {
            /**
             * 二维码
             */
            case 1:
                String qrStr = null;
                int i = 1;
                while (qrStr == null && i <= 10) {
                    YanChi(2500);
                    qrStr = QRRecon(LeftFragment.bitmap);
                    System.out.println("第" + i + "次识别二维码: \n");
                    System.out.println(qrStr);
                    i++;
                }
                //对应主车需要运行的第三模块
                send((short) 0xA3, (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            /**
             * 红绿灯
             */
            case 2:
                YanChi(500);
                //摄像头向上微调
                LeftFragment.cameraCommandUtil.postHttp(IPCamera, 0, 1);
                System.out.println("延迟成功");
                for (int J = 0; J < 3; J++) {
                    YanChi(100);
                    traffic_control(0x0E, 0x01, 0x00);
                }
                System.out.println("进入识别模式");
                ColorProcess c = new ColorProcess(FirstActivity.getContext());
                YanChi(2000);
                //处理图片
                c.PictureProcessing(LeftFragment.bitmap);
                //生成结果
                String color = TrafficLight.getImageColorPixel(c.getResult());
                //保存识别的图片
                TrafficLight.saveBitmap();
                System.out.println(color);
                YanChi(1000);
                switch (color) {
                    case "红灯":
                        for (int J = 0; J < 10; J++) {
                            YanChi(100);
                            traffic_control(0x0E, 0x02, 0x01);
                        }
                        System.out.println("识别为红灯");
                        break;
                    case "绿灯":
                        for (int J = 0; J < 10; J++) {
                            YanChi(100);
                            traffic_control(0x0E, 0x02, 0x02);
                        }
                        System.out.println("识别为绿灯");
                        break;
                    case "黄灯":
                        for (int J = 0; J < 10; J++) {
                            YanChi(100);
                            traffic_control(0x0E, 0x02, 0x03);
                        }
                        System.out.println("识别为黄灯");
                        break;
                }
                YanChi(1000);
                //摄像头向下微调
                LeftFragment.cameraCommandUtil.postHttp(IPCamera, 2, 1);
                YanChi(1000);
                //对应主车需要运行的第二模块
                send((short) 0xA2, (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            /**
             * 解密
             */
            case 3:
                break;
            /**
             * 主车全自动
             */
            case 4:
                //启动主车全自动程序
                send((short) 0xA1, (short) 0x00, (short) 0x00, (short) 0x00);
                //关闭接收主车信息数据
//                short MAJOR = 0x80;
//                for (int J = 0; J < 5; J++) send(MAJOR, (short) 0x01, (short) 0x00, (short) 0x00);
                break;
            /**
             * 车牌
             */
            case 5:
                //重新识别车牌号的次数
                int fre = 1;
                //车牌号
                String plate;
                YanChi(1500);
                do {
                    //做基本判断,输入图片主题色是否为蓝色
                    while (Antijamming.ColorTask(LeftFragment.bitmap)) {
                        //TFT_A
                        for (int J = 0; J < 3; J++) {
                            YanChi(500);
                            TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                        }
                        System.out.println("TFT_A翻页成功");
                        YanChi(6000);
                    }
                    plate = plate();
                    plate = completion(plate);
                    System.out.print("*****这里是车牌号*****" + plate + "\n");
                } while (all0(plate, fre++));
                //发送车牌给TFT
                YanChi(2000);
                for (int J = 0; J < 5; J++) {
                    YanChi(500);
                    TFT_LCD(0x0B, 0x20, plate.charAt(0), plate.charAt(1), plate.charAt(2));
                }
                System.out.println("第一次发送成功");
                YanChi(1500);
                for (int J = 0; J < 5; J++)
                    TFT_LCD(0x0B, 0x21, plate.charAt(3), plate.charAt(4), plate.charAt(5));
                System.out.println("第二次发送成功");
                YanChi(500);
                //启动主车模块
                send((short) 0xA4, (byte) 0x00, (byte) 0x00, (byte) 0x00);
                break;
            /**
             * 从车启动
             */
            case 6:
//                stateChange(1);
                YanChi(1000);
                for (int J = 0; J < 3; J++)
                    sendOther((short) 160, (short) 162, (short) 0x00, (short) 0x00);
                System.out.println("从车启动成功");
                YanChi(1000);
//                stateChange(2);
                YanChi(1500);
                send((short) 0xA6, (short) 0x00, (short) 0x00, (short) 0x00);
                System.out.println("运行第6模块");
                break;
            /**
             * 交通标志物识别
             */
            case 7:
                break;
            /**
             * 自动拍照
             */
            case 98:
                Thread autoTakePhotos = new Thread(() -> {
                    for (int j = 1; j <= 10; j++)
                        TrafficLight.saveBitmap("允许掉头" + j + ".jpg", LeftFragment.bitmap);
                });
                autoTakePhotos.start();
                break;
            /**
             * 测试摄像头传入图片裁剪效果
             */
            case 99:
                Bitmap b = LeftFragment.bitmap;
                Bitmap save = Bitmap.createBitmap(b,
                        (b.getWidth() / 100) * 30,
                        (b.getHeight() / 100) * 69,
                        (b.getWidth() / 100) * 45,
                        (b.getHeight() / 100) * 37);
                TrafficLight.saveBitmap("车牌.jpg", save);
                break;
        }
    }

    /**
     * 半安卓控制方案-Q2-V2.0
     */
    //半安卓控制主车行进路线的字段
    private static int temp = 1;

    /**
     * 重置主车行进路线模块的方法
     *
     * @param temp 默认设为1,从头开始
     */
    public static void setTemp(int temp) {
        ConnectTransport.temp = temp;
    }

    public void Q2_half_Android() {
        switch (mark) {
            //开始半自动
            case 1:
                //回发握手指令
                send((short) 0, (short) 0, (short) 0, (short) 0);

                send((short) (0xA0 + temp++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //车牌识别/图形识别/交通标志物识别+发送道闸车牌
            case 2:
                send((short) (0xA0 + temp++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //二维码识别
            case 3:
                WeChatQR_mod();
                send((short) (0xA0 + temp++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //红绿灯识别
            case 4:
                trafficLight_mod(1);
                send((short) (0xA0 + temp++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //立体显示 - 安卓控制
            case 5:
                send((short) (0xA0 + temp++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //TFT合并项目 - TFTA
            case 6:
                System.out.println("TFT");

                plate_mod_branch3();
                Shape_mod();

                YanChi(1500);
                short i10 = (short) (0xA0 + temp++);
                for (int J = 0; J < 3; J++) {
                    send(i10, (short) 0x00, (short) 0x00, (short) 0x00);
                }
                System.out.println("启动主车: " + i10);
                break;
            //TFT合并项目 - TFTB
            case 7:
                send((short) (0xA0 + temp++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            //-----
            case 8:
                send((short) (0xA0 + temp++), (short) 0x00, (short) 0x00, (short) 0x00);
                break;
            case 9:
                break;
        }
    }

    /**
     * +++++++++++++++++++++++++
     */
    /**
     * <p>全安卓控制方案</p>
     * 指令协议:
     * B1前进
     * B2短前进
     * B3左转
     * B4右转
     * B5倒车入库
     * B6读卡(未测试)(停车版本)
     * B7读卡2(未测试)(无需停车版本)
     * B8左45
     * B9右45
     * C0语音识别
     * C1短后退
     * C2返回语音标志物识别信息至立体显示标志物
     * C3(仅)获取智能路灯目标初始挡位
     * TODO C_获取智能路灯目标初始挡位并调灯-----
     * C4初始化赛场LED数码显示管和车库
     * C5(仅)倒车入库
     */
    private void Q1() {

        digital_clear();
        YanChi(500);
        digital_open();

        // B8->B6->右转
        YanChi(1000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("B8->B6->右转-----模块完成");

        // 过ETC->D6->右转面向智能语音播报系统
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("过ETC->D6->右转面向智能语音播报系统-----模块完成");
        /**
         * 语音播报控制
         */
        //----------

        //----------

        // 左转->过障碍物通道->左转面向红绿灯
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        /**
         * 红绿灯
         */
        //----------
        trafficLight_mod(1);
        WeChatQR_mod();
        //----------
        System.out.println("左转->过障碍物通道->左转->红绿灯识别完成-----模块完成");

        // 前进到F4->识别二维码
        YanChi(1500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        /**
         * 二维码识别
         */
        YanChi(1500);
        //---------

        //---------
        System.out.println("前进到F4->识别二维码-----模块完成");

        // 左转->前进到D4
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转->前进到D4-----模块完成");

        // 右转->前进到D2->OCR识别/车牌识别
        YanChi(2500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        ReadCard_short2crossroads();
        YanChi(1000);

        /**
         * OCR识别/车牌识别
         */
        //----------
        plate_mod_branch1();
        //----------
        System.out.println("右转->前进到D2->OCR识别/车牌识别-----模块完成");

        // 左转->读卡并前进到B2
        YanChi(2500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        /**
         * 这里改为循迹读卡
         */
        YanChi(2500);
        //----------
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3000);
        //长线-----半路程
        ReadCard_longLine();
        YanChi(2500);
        //B2卡位
        line(90);
        YanChi(800);
        stop();
        YanChi(500);
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        System.out.println("左转->读卡并前进到B2-----模块完成");

//        System.out.println(easyDL());

        //倒车入库&&启动从车
        YanChi(3500);
        sendOther((short) 160, (short) 162, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xA6, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("倒车入库启动");

    }

    private void Q2() {

        YanChi(1000);
        FirstActivity.Connect_Transport.garage_control(0x0D, 0x01, 0x01);
        YanChi(1000);
        FirstActivity.Connect_Transport.digital_clear();
        YanChi(1000);
        digital_open();

        // F7->F6->右转45
        YanChi(1000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB9, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        //向TFT前进减少干扰
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        System.out.println("F7->F6->右转45-----模块完成");
        //----------
        //识别图形
        Shape_mod();
        //识别车牌
        plate_mod_branch3();
        //识别交通标志物

        //----------
        System.out.println("识别完成");
        //左转面向E6道闸
        //后退到原位置
        YanChi(1000);
        send((short) 0xC1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转面向E6道闸完成");

        //F6->打开道闸->D6
        YanChi(3500);
        //发送信息给道闸
        for (int J = 0; J < 3; J++) {
            gate(0x10, plate.charAt(0), plate.charAt(1), plate.charAt(2));
            YanChi(500);
        }
        //发送信息给道闸
        for (int J = 0; J < 3; J++) {
            gate(0x11, plate.charAt(3), plate.charAt(4), plate.charAt(5));
            YanChi(100);
        }
        YanChi(500);
        gate(0x01, 0x01, 0x00, 0x00);
        YanChi(1000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F6->打开道闸->D6-----模块完成");

        // D6->B6->左转面向静态标志物
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("D6->B6->左转面向静态标志物-----模块完成");

        // 识别二维码/LCD发送测距信息->右转180->识别红绿灯
        WeChatQR_mod();
        YanChi(500);
        for (int J = 0; J < 3; J++) {
            digital_dic((int) RightControlFragment.getUltraSonic());
            YanChi(100);
        }
        YanChi(1500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        trafficLight_mod(1);
        System.out.println("识别二维码->左转/右转180->识别红绿灯-----模块完成");

        // B6->B4->左转面向语音播报
        YanChi(1500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        //----------
        send((short) 0xC0, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        System.out.println("B6->B4->左转面向语音播报-----模块完成");

        // 右转面向立体显示->发送数据
        YanChi(35000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB9, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        YanChi(2000);
        infrared_stereo(new short[]{0x15, getTrafficFlag, getTrafficFlag, 0x00, 0x00});
        //----------
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("右转面向立体显示->发送数据-----模块完成");

        // B4->寻卡->F4
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);

//        YanChi(2500);
//        ReadCard_longLine();
//        YanChi(2500);
//        ReadCard_short2crossroads();
//        YanChi(2500);
//        ReadCard_longLine();
//        YanChi(2500);
//        ReadCard_short2crossroads();


        System.out.println("B4->寻卡->F4-----模块完成");

        // F4->左转->F2
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F4->左转->F2-----模块完成");

        // 右转->调整智能路灯->左转面向报警台->左转面向特殊地形
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        YanChi(3500);
        //RFID卡解密结果
        int k = 2;
        //图形解析结果 - 红色图形数量
        int r = shapeResult;
        //灯光初始挡位
        int n = RightControlFragment.getLight();
        Log.i(TAG, "RFID解密结果: " + k + " 图形解析结果: " + r + " 灯光初始挡位: " + n);
        int i = (int) (Math.pow((k + r), n) % 4 + 1);
        gear(i);
        //----------
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB8, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        //开启报警台
        //----------
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);

        // F2->特殊地形->B2
        YanChi(3500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);

        //倒车入库&&启动从车
        YanChi(9000);
//        sendOther((short) 160, (short) 162, (short) 0x00, (short) 0x00);
//        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB5, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("倒车入库启动");


    }

    private void Q3() {

        YanChi(1000);
        for (int J = 0; J < 3; J++) {
            FirstActivity.Connect_Transport.digital_clear();
            YanChi(50);
        }
        YanChi(1500);
        digital_open();

        // F7->F6
        YanChi(1000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F7->F6-----模块完成");

        //左转面向ETC
        YanChi(2500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转面向ETC完成");

        //F6->打开ETC->B6
        YanChi(3500);
        //-----?????-----
//        rudder_control(0x01, 0x01);
        //-----?????-----
        line(90);
        YanChi(800);
        stop();
        //等待ETC开启
        YanChi(1500);
        //通过ETC到D6
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F6->打开ETC->D6-----模块完成");

        // D6->B6->左面向静态标志物
        YanChi(1500);
        line(90);
        YanChi(3500);
        System.out.println("D6->B6->面向静态标志物-----模块完成");

        // 识别二维码/LCD发送测距信息->右转180->识别红绿灯
        WeChatQR_mod();
        for (int J = 0; J < 3; J++) {
            digital_dic((int) RightControlFragment.getUltraSonic());
            YanChi(100);
        }
        YanChi(1000);
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("识别二维码->右转180-----模块完成");

        // B6->B4->左转面向TFT(A)
        YanChi(2500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        //----------
        plate_mod_branch3();
        Shape_mod();
        sendOther((short) 160, (short) 162, (short) 0x00, (short) 0x00);
        //----------
        System.out.println("B6->B4->左转面向TFT(A)-----模块完成");

        // 右转面向烽火台->发送数据
        YanChi(2000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB9, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        YanChi(2000);
        //开启烽火台
        //----------
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("右转面向烽火台->发送数据-----模块完成");

        // B4->红绿灯识别->D4->开启道闸->F4
        //----------
        trafficLight_mod(2);
        //----------
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        //----------
        //发送信息给道闸
        for (int J = 0; J < 3; J++) {
            gate(0x10, plate.charAt(0), plate.charAt(1), plate.charAt(2));
            YanChi(500);
        }
        //发送信息给道闸
        for (int J = 0; J < 3; J++) {
            gate(0x11, plate.charAt(3), plate.charAt(4), plate.charAt(5));
            YanChi(100);
        }
        YanChi(500);
        gate(0x01, 0x01, 0x00, 0x00);
        //----------
        YanChi(3000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("B4->红绿灯识别->D4->开启道闸->F4-----模块完成");

        // F4->识别交通标志物->左转->F2
        //----------
//        easyDL();
        //----------
        YanChi(1500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F4->左转->F2-----模块完成");

        // 左转45面向报警台->左转面向特殊地形
        YanChi(1500);
        send((short) 0xB8, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        //向立体显示物发数据
        //----------
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);

        // F2->特殊地形->B2->语音识别
        YanChi(2500);
        FirstActivity.Connect_Transport.line(90);
        YanChi(1300);
        FirstActivity.Connect_Transport.go(50, 1200);
        YanChi(2500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        //----------
        send((short) 0xC0, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        YanChi(30000);

        //倒车入库
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(1500);
        send((short) 0xB5, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("倒车入库启动");

    }

    private void Q4() {

        YanChi(1000);
        for (int J = 0; J < 3; J++) {
            FirstActivity.Connect_Transport.digital_clear();
            YanChi(50);
        }
        YanChi(1500);
        digital_open();


        // B7->B6
        YanChi(500);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("B7->B6-----模块完成");

        //左转面向静态标志物A
        YanChi(3500);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);

        System.out.println("右转面向静态标志物A完成");

        //B6->测距
        YanChi(3500);
        for (int J = 0; J < 3; J++) {
            digital_dic((int) RightControlFragment.getUltraSonic());
            YanChi(100);
        }
        YanChi(1000);
        System.out.println("B6->测距-----模块完成");

        // B6->左转/右转面向F6->D6->F6
        YanChi(2500);
        //右转
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3000);
        //右转
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(3500);
        System.out.println("B6->左转/右转面向F6->D6->F6-----模块完成");

        // 语音播报->左转向立体显示物->向立体显示物发送语音播报数据->右转面向F2
        //-----语音播报-----
        send((short) 0xC0, (short) 0x00, (short) 0x00, (short) 0x00);
        //-----语音播报-----
        YanChi(21000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        //左转45
        YanChi(3500);
        send((short) 0xB8, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        //-----向立体显示物发送数据-----
        send((short) 0xC2, (short) 0x00, (short) 0x00, (short) 0x00);
        //-----向立体显示物发送数据-----
        YanChi(3500);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("语音播报->向立体显示物发送语音播报数据->有转面向F2-----模块完成");

        // F6->F4->F2
        YanChi(2000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("F6->F4->F2-----模块完成");

        // 获取智能路灯标志物初始档位->调灯->左转面向特殊地形
        YanChi(3500);
        //-----获取智能路灯标志物初始档位-----
        send((short) 0xC3, (short) 0x00, (short) 0x00, (short) 0x00);
        //-----获取智能路灯标志物初始档位-----
        YanChi(15000);
        //-----从车启动-----
        //TODO 从车启动
        //-----从车启动-----
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("获取智能路灯标志物初始档位->调灯->左转面向特殊地形-----模块完成");

        //F2->B2
        YanChi(3000);
        //-----读卡前进并通过地形标志物-----
        send((short) 0xB7, (short) 0x00, (short) 0x00, (short) 0x00);
        //-----读卡前进并通过地形标志物-----
        YanChi(15000);
        System.out.println("F2->B2-----模块完成");

        // 左转面向B4->B4->--左转面向ETC->通过ETC到D4
        YanChi(2000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        //通过ETC
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转面向B4->B4->--左转面向ETC->通过ETC到D4-----模块完成");

        //左转45面向烽火台->发送数据->右转面向车库
        YanChi(4000);
        send((short) 0xB8, (short) 0x00, (short) 0x00, (short) 0x00);
        //----------
        //TODO 开启烽火台

        //----------
        YanChi(4000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(2000);
        send((short) 0xB4, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("右转面向烽火台->发送数据->右转面向车库-----模块完成");

        // D4->D6
        YanChi(3000);
        send((short) 0xB1, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("D4->D6-----模块完成");

        //左转->左转面向特殊地形
        YanChi(4000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        YanChi(4000);
        send((short) 0xB3, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("左转->左转面向特殊地形-----模块完成");

        //倒车入库
        YanChi(1500);
        send((short) 0xB5, (short) 0x00, (short) 0x00, (short) 0x00);
        System.out.println("倒车入库启动");

    }

    /**
     * +++++++++++++++++++++++++
     */
    //红绿灯识别模块测试 - 功能性单独测试 - 旧版
    public String trafficLight() {
        String color = TrafficLight.getImageColorPixel(LeftFragment.bitmap);
        TrafficLight.saveBitmap();
        return color;
//        return TrafficLight.getImageColorPixel(LeftFragment.bitmap);
    }

    /**
     * 红绿灯模块
     *
     * @param i 需要识别的智能交通灯标志物
     *          1 - A/2 - B
     */
    public void trafficLight_mod(int i) {
        YanChi(1500);
        //摄像头向上微调
        LeftFragment.cameraCommandUtil.postHttp(IPCamera, 0, 1);
        System.out.println("延迟成功");
        for (int J = 0; J < 3; J++) {
            YanChi(100);
            traffic_control(0x0D + i, 0x01, 0x00);
        }
        System.out.println("进入识别模式");
        ColorProcess c = new ColorProcess(FirstActivity.getContext());
        YanChi(3500);
        //处理图片
        c.PictureProcessing(LeftFragment.bitmap);
        //生成结果
//        String color = TrafficLight.getImageColorPixel(c.getResult());
        String color = TrafficLight_fix.Identify(c.getResult());
        //保存识别的图片
//        TrafficLight.saveBitmap(color + ".jpg", c.getResult());
        TrafficLight_fix.saveBitmap();
        System.out.println(color);
        //发送结果
        sendToTrafficLight(color, i);
        YanChi(1000);
        //摄像头向下微调
        LeftFragment.cameraCommandUtil.postHttp(IPCamera, 2, 1);
        YanChi(1000);
    }

    /**
     * 给智能交通灯标志物发送信息
     *
     * @param color 识别的颜色
     * @param i     智能交通灯标志物A/B
     */
    private void sendToTrafficLight(String color, int i) {
        switch (color) {
            case "红灯":
                for (int J = 0; J < 10; J++) {
                    YanChi(100);
                    traffic_control(0x0D + i, 0x02, 0x01);
                }
                System.out.println("识别为红灯");
                break;
            case "绿灯":
                for (int J = 0; J < 10; J++) {
                    YanChi(100);
                    traffic_control(0x0D + i, 0x02, 0x02);
                }
                System.out.println("识别为绿灯");
                break;
            case "黄灯":
                for (int J = 0; J < 10; J++) {
                    YanChi(100);
                    traffic_control(0x0D + i, 0x02, 0x03);
                }
                System.out.println("识别为黄灯");
                break;
        }
    }

    /**
     * +++++++++++++++++++++++++
     */
    /**
     * 形状识别模块
     */
    //TODO 仍需要优化
    private void Shape_mod() {
        Log.i(TAG, "----------形状识别开始----------");
        boolean success = true;
        int fre = 1;
        do {
            YanChi(6000);
            ShapeIdentify task = new ShapeIdentify();
            task.shapePicProcess(LeftFragment.bitmap);
            int tmp = task.getTotals();
            if (tmp <= 3 /*|| tmp >= 6*/) {
                for (int J = 0; J < 3; J++) {
                    YanChi(100);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                System.out.println("TFT_A翻页成功");
                task.setTotals(0);
            } else {
                success = false;
                shapeResult = Objects.requireNonNull(task.getShapeCounts().get("红色")).getSameColorCounts("总计");
                System.out.println("检测出的图形数量: " + tmp);
            }

        } while (success && fre++ < 5);
        Log.i(TAG, "----------形状识别完成----------");
    }

    /**
     * +++++++++++++++++++++++++
     */
    /**
     * 图像灰度化(方便二维码识别)
     *
     * @param bmSrc 需要灰度化的Bitmap
     * @return 灰度化的bmSrc
     */
    public static Bitmap bitmap2Gray(Bitmap bmSrc) {
        // 得到图片的长和宽
        if (bmSrc == null) return null;
        int width = bmSrc.getWidth();
        int height = bmSrc.getHeight();
        // 创建目标灰度图像
        Bitmap bmpGray;
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        // 创建画布
        Canvas c = new Canvas(bmpGray);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmSrc, 0, 0, paint);
        return bmpGray;
    }

    /**
     * 基于Google.zxing库的二维码识别模块
     * 已弃用,已有更好的替代方案
     */
    @Deprecated
    private String QRRecon(Bitmap bitmap) {
        Result result;
        String result_qr = null;
        //灰度化图片
//        QR_Recognition rSource = new QR_Recognition(bitmap2Gray(bitmap));
        QR_Recognition rSource = new QR_Recognition(bitmap);
        try {
            //生成二进制位图
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rSource));
            Map<DecodeHintType, String> hint = new HashMap<>();
            hint.put(DecodeHintType.CHARACTER_SET, "utf-8");
            //生成二维码识别对象
            QRCodeReader reader = new QRCodeReader();
            result = reader.decode(binaryBitmap, hint);
            //取得结果
            result_qr = result.toString();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            e.printStackTrace();
        }
        return result_qr;
    }

    /**
     * 基于openCV基本库的二维码识别模块
     * 已弃用,已有更好的替代方案
     */
    @Deprecated
    private void openCVQR() {
        Bitmap Btmp;
        String qrStr = null;
        int i = 1;
        while ((qrStr == null || qrStr.isEmpty()) && i <= 10) {
            YanChi(2500);
            //Google提供的二维码识别方法(弃用,识别率不如openCV)
//            qrStr = QRRecon(LeftFragment.bitmap);
            Btmp = bitmap2Gray(LeftFragment.bitmap);
            QRCodeDetector qrCodeDetector = new QRCodeDetector();
            Mat mat = new Mat();
            Utils.bitmapToMat(Btmp, mat);
            qrStr = qrCodeDetector.detectAndDecode(mat);
            System.out.println("第" + i + "次识别二维码: \n");
            System.out.println(qrStr);
            Log.i("QRcode", qrStr);
            i++;
        }
        if (qrStr == null || qrStr.isEmpty()) qrStr = "A1B2C3D4E5";
        code = new GetCode();
        code.parsing(qrStr);
    }

    /**
     * WeChat二维码扫描
     */
    private void WeChatQR_mod() {
        YanChi(3500);
        Bitmap Btmp = LeftFragment.bitmap;
        if (Btmp == null) return;
        //彩色二维码裁剪
        QRBitmapCutter cutter = new QRBitmapCutter();
        //识别结果
        String qrStr = null;
        //重新识别次数
        int i = 1;
        while ((qrStr == null || qrStr.isEmpty()) && i <= 6) {
            Btmp = cutter.QRCutter(Btmp, QRBitmapCutter.QRColor.RED);
            try {
                qrStr = WeChatQRCodeDetector.detectAndDecode(Btmp).get(0);
                Log.i(TAG, "第" + i + "次识别二维码: " + "***" + qrStr + "***");
            } catch (IndexOutOfBoundsException | NullPointerException e) {
                e.printStackTrace();
            }
            YanChi(2500);
            Btmp = LeftFragment.bitmap;
            i++;
        }
        if (qrStr == null || qrStr.isEmpty()) qrStr = "A1B2C3D4E5";
        //根据题意解析二维码数据获得有效信息
        code = new GetCode();
        qrResult = code.parsing(qrStr);
    }

    /**
     * +++++++++++++++++++++++++
     */
    /**
     * 车牌识别/OCR文字识别模块
     * 不推荐使用
     */
    @Deprecated
    public String plate() {
        predictor.setInputImage(LeftFragment.bitmap);
        if (predictor.isLoaded()) predictor.runModel();
        return predictor.outputResult();
//        FirstActivity.toastUtil.ShowToast(predictor.isLoaded() && predictor.runModel() ? "车牌识别成功" : "识别错误");
    }

    public String plate(Bitmap inputBitmap) {
        predictor.setInputImage(inputBitmap);
        if (predictor.isLoaded()) predictor.runModel();
        return predictor.outputResult();
//        FirstActivity.toastUtil.ShowToast(predictor.isLoaded() && predictor.runModel() ? "车牌识别成功" : "识别错误");
    }

    /**
     * 过滤与补全
     *
     * @param str 车牌识别结果
     * @return 过滤后的车牌
     */
    private String completion(String str) {
        StringBuilder sb = new StringBuilder();
        for (char ch : str.toCharArray()) {
            //如果为数字或字母则添加进sb中
            if (Character.isDigit(ch) || Character.isUpperCase(ch) || Character.isLowerCase(ch))
                sb.append(ch);
        }
        //不满6个数则补全到6
        while (sb.toString().length() < 6) sb.append(0);
        return sb.toString().toUpperCase(Locale.ROOT);
    }

    /**
     * 判断是否为正常的车牌号
     *
     * @param s 车牌号
     * @param i 已经识别次数
     * @return -
     */
    private boolean all0(String s, int i) {
        if (i >= 5 || s.isEmpty()) return false;
        int total = 0;
        for (char c : s.toCharArray()) if (c == '0') total++;
        return total >= 4;
    }

    /**
     * <p>车牌识别模块 - 分支1</p>
     * <p>使用颜色识别 - 可能不稳定</p>
     * <p>针对于赛场使用干扰颜色车牌</p>
     * <p>建议使用3分支</p>
     */
    @Deprecated
    private void plate_mod_branch1() {
        //重新识别车牌号的次数
        int fre = 1;
        //翻页次数
        int flip = 1;

        YanChi(1500);
        do {
            //做基本判断,输入图片主题色是否为蓝色
            while (Antijamming.ColorTask(LeftFragment.bitmap) && flip++ <= 8) {
                //TFT_A
                for (int J = 0; J < 3; J++) {
                    YanChi(500);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                System.out.println("TFT_A翻页成功");
                YanChi(6000);
            }
            plate = plate();
            plate = completion(plate);
            System.out.print("*****这里是车牌号*****" + plate + "\n");
        } while (all0(plate, fre++));
        //发送车牌给TFT
        YanChi(2000);
//        for (int J = 0; J < 5; J++) {
//            YanChi(500);
//            TFT_LCD(0x0B, 0x20, plate.charAt(0), plate.charAt(1), plate.charAt(2));
//        }
//        System.out.println("第一次发送成功");
//        YanChi(1500);
//        for (int J = 0; J < 5; J++)
//            TFT_LCD(0x0B, 0x21, plate.charAt(3), plate.charAt(4), plate.charAt(5));
//        System.out.println("第二次发送成功");
        YanChi(500);
    }

    /**
     * <p>车牌识别模块 - 分支2</p>
     * <p>使用车牌号判断 - 无法区分颜色,但遇号既出结果</p>
     * <p>针对于赛场不使用干扰颜色车牌</p>
     */
    private void plate_mod_branch2() {
        //重新识别车牌号的次数
        int fre = 1;
        YanChi(2000);
        do {
            //生成结果
            plate = plate();
            //过滤与补全
            plate = completion(plate);
            System.out.print("*****这里是车牌号*****" + plate + "\n");
            //翻页,使用车牌判断
            if (all0(plate, fre)) {
                for (int J = 0; J < 3; J++) {
                    YanChi(500);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                System.out.println("TFT_A翻页成功");
                Log.i("plate", plate);
                YanChi(6000);
            }
        } while (all0(plate, fre++));
        //发送车牌给TFT
        YanChi(2000);
//        for (int J = 0; J < 5; J++) {
//            YanChi(500);
//            TFT_LCD(0x0B, 0x20, plate.charAt(0), plate.charAt(1), plate.charAt(2));
//        }
//        System.out.println("第一次发送成功");
//        YanChi(1500);
//        for (int J = 0; J < 5; J++)
//            TFT_LCD(0x0B, 0x21, plate.charAt(3), plate.charAt(4), plate.charAt(5));
//        System.out.println("第二次发送成功");
        YanChi(500);

    }

    /**
     * <p>车牌识别模块 - 分支3</p>
     * 结合openCV库(仅)定位车牌,识别车牌种类和车牌号
     */
    private void plate_mod_branch3() {
        //重新识别车牌号的次数
        int fre = 1;
        YanChi(2000);
        plate = null;
        do {
            /* 车牌识别图片处理 */
            PlateDetector plateDetector = new PlateDetector();
            String plateType = plateDetector.plateOnlyDetector(LeftFragment.bitmap);
            /* 翻页,使用车牌种类判断 */
            //plateType.equals("填入需要屏蔽的车牌颜色")
            //比如:蓝/绿/...
            if ((plateType == null || plateType.equals("绿")) && fre < 5) {
                for (int J = 0; J < 3; J++) {
                    YanChi(100);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                System.out.println("检测车牌翻页成功");
            } else {
                if (plateDetector.getRectBitmap() == null) continue;
                //生成结果
                //TODO 车牌处理2,灰度化处理,将黑字替换成白字


                plate = plate(plateDetector.getRectBitmap());
                /* 保存图片 */
                TrafficLight.saveBitmap("裁剪后的车牌.jpg", plateDetector.getRectBitmap());
                plate = plateDetector.completion(plate);
//                System.out.println("车牌种类: " + plateType + "\n车牌号: " + plate);
                Log.i(TAG, "车牌种类: " + plateType + "\n车牌号: " + plate);
            }
            if (plate == null || plate.equals("D000D0")) {
                for (int J = 0; J < 3; J++) {
                    YanChi(100);
                    TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                }
                System.out.println("非法车牌-----TFT_A翻页成功");
            }
            YanChi(6000);
        } while ((plate == null || plate.equals("D000D0")) && fre++ < 5);
        //发送车牌给TFT
        YanChi(2000);
//        for (int J = 0; J < 5; J++) {
//            YanChi(500);
//            TFT_LCD(0x0B, 0x20, plate.charAt(0), plate.charAt(1), plate.charAt(2));
//        }
//        System.out.println("第一次发送成功");
//        YanChi(1500);
//        for (int J = 0; J < 5; J++)
//            TFT_LCD(0x0B, 0x21, plate.charAt(3), plate.charAt(4), plate.charAt(5));
//        System.out.println("第二次发送成功");
        YanChi(500);
    }

    /**
     * +++++++++++++++++++++++++
     */
    /**
     * 解密
     */ {
    }

    /**
     * +++++++++++++++++++++++++
     */
    /**
     * 交通标志物识别
     */
    private void trafficSign_mod() {
        YanChi(3500);
        try {
            String result = "";
            Bitmap bitmap;
            //车牌识别后进行翻页
            for (int J = 0; J < 3; J++) {
                YanChi(500);
                TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
            }
            System.out.println("TFT_A翻页成功");
            YanChi(6000);
            for (int i = 1; i <= 6; i++) {
                Bitmap tmp = LeftFragment.bitmap;
                bitmap = Bitmap.createBitmap(tmp,
                        (tmp.getWidth() / 100) * 25,
                        (tmp.getHeight() / 100) * 50,
                        (tmp.getWidth() / 100) * 35,
                        (tmp.getHeight() / 100) * 60);

//                result = FirstActivity.TrafficFlag.TrafficFlag(bitmap);
            }

            switch (result) {
                case "直行":
                    getTrafficFlag = 0x01;
                    break;
                case "向左转弯":
                    getTrafficFlag = 0x02;
                    break;
                case "允许掉头":
                    getTrafficFlag = 0x04;
                    break;
                case "禁止直行":
                    getTrafficFlag = 0x05;
                    break;
                case "禁止通行":
                    getTrafficFlag = 0x06;
                    break;
                case "向右转弯":
                default:
                    getTrafficFlag = 0x03;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * 以下全安卓控制主车读卡循迹未必完全有效,仅作为备用手段使用
     */
    /**
     * 读卡-----沙盘横向长线-----半路程
     */
    private void ReadCard_longLine() {
        YanChi(1000);
        line(90);
        YanChi(800);
        stop();
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 读卡-----沙盘竖向短线-----半路程
     */
    private void ReadCard_shortLine() {
        YanChi(1000);
        line(90);
        YanChi(500);
        stop();
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 读卡-----沙盘十字路口-----短线行驶
     */
    private void ReadCard_short2crossroads() {
        YanChi(1000);
        line(90);
        YanChi(1050);
        stop();
        YanChi(1000);
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    /**
     * 读卡-----沙盘十字路口-----长线行驶
     */
    private void ReadCard_long2crossroads() {
        YanChi(1000);
        line(90);
        YanChi(1300);
        stop();
        YanChi(500);
        send((short) 0xB2, (short) 0x00, (short) 0x00, (short) 0x00);
        //读卡
        YanChi(1500);
        send((short) 0xB6, (short) 0x00, (short) 0x00, (short) 0x00);
    }

    private int testint = 1;

    /**
     * <p>{@link car.bkrc.com.car2021.ViewAdapter.ModuleAdapter}调用函数</p>
     * <p>在这里添加新模块的启动线程</p>
     *
     * @param i 选择的模块
     */
    public void module(int i) {
        switch (i) {
            //红绿灯
            case 1:
                new Thread(() -> trafficLight_mod(2)).start();
                break;
            //车牌 - 分支2
            case 2:
                new Thread(this::plate_mod_branch3).start();
                break;
            //形状
            case 3:
                new Thread(this::Shape_mod).start();
                break;
            //交通标志物
            case 4:
                new Thread(() -> {
                    String str = FirstActivity.getYolov5_tflite_tsDetector().processImage(LeftFragment.bitmap);
                    System.out.println(str != null ? str : "null");
                }).start();
                break;
            //二维码
            case 5:
                new Thread(this::WeChatQR_mod).start();
                break;
            case 6:
//                new Thread(() -> {
//                    /* 保存图片 */
//                    TrafficLight.saveBitmap("TFT图片" + testint++ + ".jpg", LeftFragment.bitmap);
//                }).start();
                break;
            //全安卓控制2
            case 0xB2:
//                new Thread(this::Q2).start();
                break;
            //全安卓控制4
            case 0xB4:
                new Thread(this::Q4).start();
                break;
        }
    }
}

