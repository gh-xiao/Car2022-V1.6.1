package car.bkrc.com.car2021.ViewAdapter;

import static car.bkrc.com.car2021.ActivityView.FirstActivity.toastUtil;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import car.bkrc.com.car2021.ActivityView.FirstActivity;
import car.bkrc.com.car2021.Utils.QRcode.QR_Recognition;
import car.bkrc.com.car2021.FragmentView.LeftFragment;
import car.bkrc.com.car2021.R;
import car.bkrc.com.car2021.Utils.CameraUtil.XcApplication;
import car.bkrc.com.car2021.Utils.OtherUtil.RadiusUtil;

/**
 * 其他选项的适配器
 */
public class OtherAdapter extends RecyclerView.Adapter<OtherAdapter.ViewHolder> {

    private List<Other_Landmark> mOtherLandmarkList;
    Context context;
    private String result_qr;
    private Bitmap qrBitmap;
    private boolean qrRecState = false;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View InfraredView;
        ImageView OtherImage;
        TextView OtherName;

        public ViewHolder(View view) {
            super(view);
            InfraredView = view;
            OtherImage = view.findViewById(R.id.landmark_image);
            OtherName = view.findViewById(R.id.landmark_name);
        }
    }

    public OtherAdapter(List<Other_Landmark> InfraredLandmarkList, Context context) {
        mOtherLandmarkList = InfraredLandmarkList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.OtherName.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Other_Landmark otherLandmark = mOtherLandmarkList.get(position);
            Other_select(otherLandmark);
        });
        holder.OtherImage.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Other_Landmark otherLandmark = mOtherLandmarkList.get(position);
            Other_select(otherLandmark);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Other_Landmark InfraredLandmark = mOtherLandmarkList.get(position);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), InfraredLandmark.getImageId(), null);
        bitmap = RadiusUtil.roundBitmapByXfermode(bitmap, bitmap.getWidth(), bitmap.getHeight(), 10);
        holder.OtherImage.setImageBitmap(bitmap);
        holder.OtherName.setText(InfraredLandmark.getName());
    }

    @Override
    public int getItemCount() {
        return mOtherLandmarkList.size();
    }

    private void Other_select(Other_Landmark InfraredLandmark) {
        switch (InfraredLandmark.getName()) {
            case "摄像头控制":
                position_Dialog();
                break;
            case "二维码识别":
                QR_Dialog();
                break;
            case "蜂鸣器控制":
                buzzerController();
                break;
            case "转向灯控制":
                lightController();
                break;
            default:
                break;
        }
    }

    // 二维码、车牌处理
    @SuppressLint("HandlerLeak")
    Handler qrHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10:
                    qrRecState = true;
                    qrBitmap = LeftFragment.bitmap;
                    if (qrBitmap != null) {
                        QRRecon();
                    } else toastUtil.ShowToast("没有连接到摄像头，请连接到摄像头后再试！");
                    break;
                case 20:
                    toastUtil.ShowToast(result_qr);
                    break;
                case 30:
                    toastUtil.ShowToast("未检测到二维码！");
                    break;
                default:
                    break;
            }
        }
    };

    private void QRRecon() {
        new Thread(() -> {
            Result result;
            //灰度化图片
            QR_Recognition rSource = new QR_Recognition(bitmap2Gray(qrBitmap));
            try {
                //生成二进制位图
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(rSource));
                Map<DecodeHintType, String> hint = new HashMap<>();
                hint.put(DecodeHintType.CHARACTER_SET, "utf-8");
                //生成二维码识别对象
                QRCodeReader reader = new QRCodeReader();
                result = reader.decode(binaryBitmap, hint);
                result_qr = result.toString();
                qrHandler.sendEmptyMessage(20);
                System.out.println("正在识别");
            } catch (NotFoundException e) {
                e.printStackTrace();
                qrHandler.sendEmptyMessage(30);
            } catch (ChecksumException | FormatException e) {
                e.printStackTrace();
            }
            qrRecState = false;
        }).start();
    }

    /**
     * 图像灰度化
     *
     * @param bmSrc 传入Bitmap
     * @return 返回灰度化的bmSrc
     */
    public Bitmap bitmap2Gray(Bitmap bmSrc) {
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

    private int state_camera = 0;

    // 摄像头预设位对话框
    private void position_Dialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("摄像头角度预设位调节");
        String[] set_item = {"预设位 ①", "预设位 ②", "预设位 ③", "预设位 ④", "预设位 ⑤", "预设位 ⑥"};
        builder.setSingleChoiceItems(set_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            state_camera = which + 5;
            cameraState_control();
        });
        builder.create().show();
    }

    // 二维码识别对话框
    private void QR_Dialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("二维码识别");
        String[] set_item = {"智能嵌入式实训平台", "智能移动机器人"};
        builder.setSingleChoiceItems(set_item, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                // 主车识别二维码
                if (!qrRecState) {
                    qrHandler.sendEmptyMessage(10);
                }
            } else if (which == 1) {
                // 从车识别二维码
                agv_QR_Dialog();
            }
        });
        builder.create().show();
    }

    // 智能移动机器人二维码识别对话框
    private void agv_QR_Dialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("智能移动机器人二维码识别");
        String[] set_item = {"开始识别", "取消识别"};
        builder.setSingleChoiceItems(set_item, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                FirstActivity.Connect_Transport.qr_rec(1);
            } else if (which == 1) {
                FirstActivity.Connect_Transport.qr_rec(2);
            }
        });
        builder.create().show();
    }

    // 主车摄像头控制
    private void cameraState_control() {
        XcApplication.executorServicetor.execute(() -> {
            switch (state_camera) {
                //上下左右转动
                case 1:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 0, 1);  //向上
                    break;
                case 2:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 2, 1);  //向下
                    break;
                case 3:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 4, 1);  //向左
                    break;
                case 4:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 6, 1);  //向右
                    break;
                // / 5-7   设置预设位1到3
                case 5:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 30, 0);
                    break;
                case 6:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 32, 0);
                    break;
                case 7:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 34, 0);
                    break;
                //调用预设位1-3
                case 8:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 31, 0);
                    break;
                case 9:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 33, 0);
                    break;
                case 10:
                    LeftFragment.cameraCommandUtil.postHttp(FirstActivity.IPCamera, 35, 0);
                    break;
                default:
                    break;
            }
            state_camera = 0;
        });
    }

    // 蜂鸣器
    private void buzzerController() {
        AlertDialog.Builder build = new AlertDialog.Builder(context);
        build.setTitle("蜂鸣器控制");
        String[] im = {"打开", "关闭"};
        build.setSingleChoiceItems(im, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                // 打开蜂鸣器
                FirstActivity.Connect_Transport.buzzer(1);
            } else if (which == 1) {
                // 关闭蜂鸣器
                FirstActivity.Connect_Transport.buzzer(0);
            }
        });
        build.create().show();
    }

    // 指示灯遥控器
    private void lightController() {
        AlertDialog.Builder lt_builder = new AlertDialog.Builder(context);
        lt_builder.setTitle("转向灯控制");
        String[] item = {"左转", "右转", "停车", "临时停车"};
        lt_builder.setSingleChoiceItems(item, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                FirstActivity.Connect_Transport.light(1, 0);
            } else if (which == 1) {
                FirstActivity.Connect_Transport.light(0, 1);
            } else if (which == 2) {
                FirstActivity.Connect_Transport.light(0, 0);
            } else if (which == 3) {
                FirstActivity.Connect_Transport.light(1, 1);
            }
        });
        lt_builder.create().show();
    }
}