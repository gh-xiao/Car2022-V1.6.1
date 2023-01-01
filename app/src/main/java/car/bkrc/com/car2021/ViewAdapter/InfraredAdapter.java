package car.bkrc.com.car2021.ViewAdapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import car.bkrc.com.car2021.ActivityView.FirstActivity;
import car.bkrc.com.car2021.R;
import car.bkrc.com.car2021.Utils.OtherUtil.RadiusUtil;

/**
 * 红外选项的适配器
 */
public class InfraredAdapter extends RecyclerView.Adapter<InfraredAdapter.ViewHolder> {

    private List<Infrared_Landmark> mInfraredLandmarkList;
    private final Context context;
    private short[] data = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

    static class ViewHolder extends RecyclerView.ViewHolder {
        View InfraredView;
        ImageView InfraredImage;
        TextView InfraredName;

        //https://blog.csdn.net/qq_39402590/article/details/90473268
        //通常出现在适配器里,为的是listview滚动的时候快速设置值,而不必每次都重新创建很多对象,从而提升性能
        public ViewHolder(View view) {
            super(view);
            InfraredView = view;
            InfraredImage = view.findViewById(R.id.infrared_image);
            InfraredName = view.findViewById(R.id.infrared_name);
        }
    }

    public InfraredAdapter(List<Infrared_Landmark> InfraredLandmarkList, Context context) {
        mInfraredLandmarkList = InfraredLandmarkList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.infrared_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.InfraredView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Infrared_Landmark InfraredLandmark = mInfraredLandmarkList.get(position);
            Infrared_select(InfraredLandmark);
        });
        holder.InfraredImage.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Infrared_Landmark InfraredLandmark = mInfraredLandmarkList.get(position);
            Infrared_select(InfraredLandmark);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Infrared_Landmark InfraredLandmark = mInfraredLandmarkList.get(position);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), InfraredLandmark.getImageId(), null);
        bitmap = RadiusUtil.roundBitmapByXfermode(bitmap, bitmap.getWidth(), bitmap.getHeight(), 10);
        holder.InfraredImage.setImageBitmap(bitmap);
        holder.InfraredName.setText(InfraredLandmark.getName());
    }

    @Override
    public int getItemCount() {
        return mInfraredLandmarkList.size();
    }

    private void Infrared_select(Infrared_Landmark InfraredLandmark) {
        switch (InfraredLandmark.getName()) {
            case "烽火台报警标志物":
                policeController();
                break;
            case "智能路灯标志物":
                gearController();
                break;
            case "立体显示标志物":
                threeDisplay();
                break;
            default:
                break;
        }
    }

    //烽火台报警标志物
    private void policeController() {
        //对话框构造器
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("烽火台报警标志物");
        String[] item2 = {"打开", "关闭"};
        builder.setSingleChoiceItems(item2, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                FirstActivity.Connect_Transport.infrared(
                        (byte) 0x03, (byte) 0x05, (byte) 0x14,
                        (byte) 0x45, (byte) 0xDE, (byte) 0x92);
            } else if (which == 1) {
                FirstActivity.Connect_Transport.infrared(
                        (byte) 0x67, (byte) 0x34, (byte) 0x78,
                        (byte) 0xA2, (byte) 0xFD, (byte) 0x27);
            }
        });
        builder.create().show();
    }

    //智能路灯标志物
    private void gearController() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("智能路灯标志物");
        String[] gr_item = {"光源挡位加一档", "光源挡位加二档", "光源挡位加三档"};
        builder.setSingleChoiceItems(gr_item, -1, (dialog, which) -> {
            if (which == 0) {
                // 加一档
                FirstActivity.Connect_Transport.gear(1);
            } else if (which == 1) {
                // 加二档
                FirstActivity.Connect_Transport.gear(2);
            } else if (which == 2) {
                // 加三档
                FirstActivity.Connect_Transport.gear(3);
            }
        });
        builder.create().show();
    }

    //立体显示标志物
    private void threeDisplay() {
        AlertDialog.Builder Builder = new AlertDialog.Builder(context);
        Builder.setTitle("立体显示标志物");
        String[] three_item = {"颜色信息显示模式", "图形信息显示模式", "距离信息显示模式", "车牌信息显示模式", "交通警示牌信息显示模式", "交通标志信息显示模式", "显示默认信息", "设置文字显示颜色"};
        Builder.setSingleChoiceItems(three_item, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            switch (which) {
                case 0:
                    color();
                    break;
                case 1:
                    shape();
                    break;
                case 2:
                    dis();
                    break;
                case 3:
                    lic();
                    break;
                case 4:
                    road();
                    break;
                case 5:
                    traffic_flag();
                    break;
                case 6:
                    data[0] = 0x16;
                    data[1] = 0x01;
                    FirstActivity.Connect_Transport.infrared_stereo(data);
                    break;
                case 7:
                    textColorSet();
                    break;
                default:
                    break;
            }
        });
        Builder.create().show();
    }

    //颜色信息显示模式
    private void color() {
        AlertDialog.Builder colorBuilder = new AlertDialog.Builder(context);
        colorBuilder.setTitle("颜色信息显示模式");
        String[] lg_item = {"红色", "绿色", "蓝色", "黄色", "品色", "青色", "黑色", "白色"};
        colorBuilder.setSingleChoiceItems(lg_item, -1, (dialog, which) -> {
            data[0] = 0x13;
            data[1] = (short) (which + 0x01);
            FirstActivity.Connect_Transport.infrared_stereo(data);
        });
        colorBuilder.create().show();
    }

    //图形信息显示模式
    private void shape() {
        AlertDialog.Builder shapeBuilder = new AlertDialog.Builder(context);
        shapeBuilder.setTitle("图形信息显示模式");
        String[] shape_item = {"矩形", "圆形", "三角形", "菱形", "五角星"};
        shapeBuilder.setSingleChoiceItems(shape_item, -1, (dialog, which) -> {
            data[0] = 0x12;
            data[1] = (short) (which + 0x01);
            FirstActivity.Connect_Transport.infrared_stereo(data);
        });
        shapeBuilder.create().show();
    }

    //交通警示牌信息显示模式
    private void road() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(context);
        roadBuilder.setTitle("交通警示牌信息显示模式");
        String[] road_item = {"前方学校 减速慢行", "前方施工 禁止通行", "塌方路段 注意安全", "追尾危险 保持车距", "严禁 酒后驾车", "严禁 乱扔垃圾"};
        roadBuilder.setSingleChoiceItems(road_item, -1, (dialog, which) -> {
            data[0] = 0x14;
            data[1] = (short) (which + 0x01);
            FirstActivity.Connect_Transport.infrared_stereo(data);
        });
        roadBuilder.create().show();
    }

    //交通标志信息显示模式
    private void traffic_flag() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(context);
        roadBuilder.setTitle("交通标志信息显示模式");
        String[] road_item = {"直行", "左转", "右转", "掉头", "禁止直行", "禁止通行"};
        roadBuilder.setSingleChoiceItems(road_item, -1, (dialog, which) -> {
            data[0] = 0x15;
            data[1] = (short) (which + 0x01);
            FirstActivity.Connect_Transport.infrared_stereo(data);
        });
        roadBuilder.create().show();
    }

    //设置文字显示颜色
    private void textColorSet() {
        AlertDialog.Builder roadBuilder = new AlertDialog.Builder(context);
        roadBuilder.setTitle("设置文字显示颜色");
        String[] road_item = {"中国红", "绿色", "蓝色", "自定义颜色"};
        roadBuilder.setSingleChoiceItems(road_item, -1,
                (dialog, which) -> {
                    data[0] = 0x17;
                    data[1] = 0x01;
                    switch (which) {
                        case 0: // 红
                            data[2] = 0xC8;
                            data[3] = 0x10;
                            data[4] = 0x2E;
                            break;
                        case 1: // 绿
                            data[2] = 0x00;
                            data[3] = 0xff;
                            data[4] = 0x00;
                            break;
                        case 2: // 蓝
                            data[2] = 0x00;
                            data[3] = 0x00;
                            data[4] = 0xff;
                            break;
                        case 3:
                            customColorSend();
                            break;
                        default:
                            break;
                    }
                    FirstActivity.Connect_Transport.infrared_stereo(data);
                });
        roadBuilder.create().show();
    }

    //距离信息显示模式
    private void dis() {
        AlertDialog.Builder disBuilder = new AlertDialog.Builder(context);
        disBuilder.setTitle("距离信息显示模式");
        final String[] road_item = {"10cm", "15cm", "20cm", "28cm", "39cm"};
        disBuilder.setSingleChoiceItems(road_item, -1, (dialog, which) -> {
            int disNum = Integer.parseInt(road_item[which].substring(0, 2));
            data[0] = 0x11;
            data[1] = (short) (disNum / 10 + 0x30);
            data[2] = (short) (disNum % 10 + 0x30);
            FirstActivity.Connect_Transport.infrared_stereo(data);
        });
        disBuilder.create().show();
    }

    //从string中得到short数据数组
    private short[] StringToBytes(String licString) {
        if (licString == null || licString.equals("")) return null;
        licString = licString.toUpperCase();
        int length = licString.length();
        char[] hexChars = licString.toCharArray();
        short[] d = new short[length];
        for (int i = 0; i < length; i++) {
            d[i] = (short) hexChars[i];
        }
        return d;
    }

    @SuppressLint("HandlerLeak")
    private Handler licHandler = new Handler() {
        public void handleMessage(Message msg) {
            short[] li = StringToBytes(lic_item[msg.what]);
            data[0] = 0x20;
            data[1] = (short) (li[0]);
            data[2] = (short) (li[1]);
            data[3] = (short) (li[2]);
            data[4] = (short) (li[3]);
            FirstActivity.Connect_Transport.infrared_stereo(data);
            data[0] = 0x10;
            data[1] = (short) (li[4]);
            data[2] = (short) (li[5]);
            data[3] = (short) (li[6]);
            data[4] = (short) (li[7]);
            FirstActivity.Connect_Transport.infrared_stereo(data);
        }
    };

    private int lic = -1;
    private final String[] lic_item = {"N300Y7A4", "N600H5B4", "N400Y6G6", "J888B8C8"};

    //车牌信息显示模式
    private void lic() {
        AlertDialog.Builder licBuilder = new AlertDialog.Builder(context);
        licBuilder.setTitle("车牌信息显示模式");
        licBuilder.setSingleChoiceItems(lic_item, lic, (dialog, which) -> {
            lic = which;
            licHandler.sendEmptyMessage(which);
        });
        licBuilder.create().show();
    }

    //自定义文字颜色
    @SuppressLint("SetTextI18n")
    private void customColorSend() {
        AlertDialog.Builder TFT_Hex_builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_hex, null);
        TFT_Hex_builder.setTitle("自定义文字颜色");
        TFT_Hex_builder.setView(view);
        // 下拉列表
        final EditText editText1 = view.findViewById(R.id.editText1);
        final EditText editText2 = view.findViewById(R.id.editText2);
        final EditText editText3 = view.findViewById(R.id.editText3);
        editText1.setText("FF");
        editText2.setText("00");
        editText3.setText("FF");
        data[0] = 0x17;
        data[1] = 0x01;
        TFT_Hex_builder.setPositiveButton("确定", (dialog, which) -> {
            // TODO Auto-generated method stub
            String ones = editText1.getText().toString();
            String twos = editText2.getText().toString();
            String threes = editText3.getText().toString();
            // 显示数据，一个文本编译框最多两个数据显示数目管中两个数据
            data[2] = (short) (ones.equals("") ? 0x00 : Integer.parseInt(ones, 16));
            data[3] = (short) (twos.equals("") ? 0x00 : Integer.parseInt(twos, 16));
            data[4] = (short) (threes.equals("") ? 0x00 : Integer.parseInt(threes, 16));
            FirstActivity.Connect_Transport.infrared_stereo(data);
        });
        TFT_Hex_builder.setNegativeButton("取消", (dialog, which) -> {
            // TODO Auto-generated method stub
            dialog.cancel();
        });
        TFT_Hex_builder.create().show();
    }
}