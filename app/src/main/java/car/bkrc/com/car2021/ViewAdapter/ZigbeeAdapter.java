package car.bkrc.com.car2021.ViewAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.util.List;

import car.bkrc.com.car2021.Utils.OtherUtil.RadiusUtil;
import car.bkrc.com.car2021.ActivityView.FirstActivity;
import car.bkrc.com.car2021.R;

/**
 * Zigbee选项的适配器
 */
public class ZigbeeAdapter extends RecyclerView.Adapter<ZigbeeAdapter.ViewHolder> {

    private List<Zigbee_Landmark> mZigbeeLandmarkList;
    private final Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View zigbeeView;
        ImageView zigbeeImage;
        TextView zigbeeName;

        public ViewHolder(View view) {
            super(view);
            zigbeeView = view;
            zigbeeImage = (ImageView) view.findViewById(R.id.landmark_image);
            zigbeeName = (TextView) view.findViewById(R.id.landmark_name);
        }
    }

    public ZigbeeAdapter(List<Zigbee_Landmark> zigbeeLandmarkList, Context context) {
        mZigbeeLandmarkList = zigbeeLandmarkList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.zigbee_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.zigbeeView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Zigbee_Landmark zigbeeLandmark = mZigbeeLandmarkList.get(position);
            zigbee_select(zigbeeLandmark);
        });
        holder.zigbeeImage.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Zigbee_Landmark zigbeeLandmark = mZigbeeLandmarkList.get(position);
            zigbee_select(zigbeeLandmark);
        });
        return holder;
    }

    private void zigbee_select(Zigbee_Landmark zigbeeLandmark) {
        switch (zigbeeLandmark.getName()) {
            case "道闸标志物":
                gateController();
                break;
            case "LED显示标志物":
                digital();
                break;
            case "语音播报标志物":
                voiceController();
                break;
            case "无线充电标志物":
                magnetic_suspension();
                break;
            case "智能TFT显示标志物":
                TFT_Control();
                break;
            case "智能交通灯标志物":
                Traffic_Control();
                break;
            case "立体车库标志物":
                stereo_garage_Control();
                break;
            case "ETC系统标志物":
                etc_Control();
                break;
            default:
                break;
        }
    }

    //ETC标志物
    private void etc_Control() {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        garage_builder.setTitle("ETC系统标志物舵机角度调节");
        String[] ga = {"左侧舵机调节", "右侧舵机调节"};
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            switch (i) {
                case 0: // 左侧
                    etc_SteeringEngine_Adjust(0);
                    break;
                case 1: // 右侧
                    etc_SteeringEngine_Adjust(1);
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }

    //立体车库标志物
    private void stereo_garage_Control() {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        garage_builder.setTitle("立体车库标志物");
        String[] ga = {"立体车库标志物（A）", "立体车库标志物（B）"};
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            switch (i) {
                case 0: // A
                    stereo_garage_A();
                    break;
                case 1: // B
                    stereo_garage_B();
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }

    //智能交通灯标志物
    private void Traffic_Control() {
        AlertDialog.Builder traffic_builder = new AlertDialog.Builder(context);
        traffic_builder.setTitle("智能交通灯标志物");
        String[] ga = {"智能交通灯标志物（A）", "智能交通灯标志物（B）"};
        traffic_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            switch (i) {
                case 0: // A
                    Traffic_light_A();
                    break;
                case 1: // B
                    Traffic_light_B();
                    break;
                default:
                    break;
            }
        });
        traffic_builder.create().show();
    }

    //智能TFT显示标志物
    private void TFT_Control() {
        AlertDialog.Builder tft_builder = new AlertDialog.Builder(context);
        tft_builder.setTitle("智能TFT显示标志物");
        String[] ga = {"TFT显示标志物（A）", "TFT显示标志物（B）"};
        tft_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            switch (i) {
                case 0: // A
                    TFT_LCD_A();
                    break;
                case 1: // B
                    TFT_LCD_B();
                    break;
                default:
                    break;
            }
        });
        tft_builder.create().show();
    }

    /**
     * ETC系统标志物舵机初始角度调节
     *
     * @param rudder 选择舵机，0为左侧，1为右侧
     */
    private void etc_SteeringEngine_Adjust(final int rudder) {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        String[] ga = {"上升", "下降"};
        if (rudder != 0) {
            garage_builder.setTitle("右侧舵机");
        } else {
            garage_builder.setTitle("左侧舵机");
        }
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            switch (i) {
                case 0:  // 上调
                    if (rudder != 0) {
                        FirstActivity.Connect_Transport.rudder_control(0x00, 0x01);
                    } else FirstActivity.Connect_Transport.rudder_control(0x01, 0x00);
                    break;
                case 1:  // 下调
                    if (rudder != 0) {
                        FirstActivity.Connect_Transport.rudder_control(0x00, 0x02);
                    } else FirstActivity.Connect_Transport.rudder_control(0x02, 0x00);
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }

    //TFT显示标志物_B
    private void stereo_garage_B() {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        garage_builder.setTitle("智能TFT显示标志物（B）");
        String[] ga = {"复位（第一层）", "到达第二层", "到达第三层", "到达第四层", "请求返回立体车库当前层数", "请求返回立体车库前/后侧\n" + "红外状态"};
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            switch (i) {
                case 0:  //到达第一层
                    FirstActivity.Connect_Transport.garage_control(0x05, 0x01, 0x01);
                    break;
                case 1:  //到达第二层
                    FirstActivity.Connect_Transport.garage_control(0x05, 0x01, 0x02);
                    break;
                case 2:  //到达第三层
                    FirstActivity.Connect_Transport.garage_control(0x05, 0x01, 0x03);
                    break;
                case 3:  //到达第四层
                    FirstActivity.Connect_Transport.garage_control(0x05, 0x01, 0x04);
                    break;
                case 4:  //请求返回车库位于第几层
                    FirstActivity.Connect_Transport.garage_control(0x05, 0x02, 0x01);
                    break;
                case 5:  //请求返回前后侧红外状态
                    FirstActivity.Connect_Transport.garage_control(0x05, 0x02, 0x02);
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }

    //智能交通灯标志物_B
    private void Traffic_light_B() {
        AlertDialog.Builder traffic_builder = new AlertDialog.Builder(context);
        traffic_builder.setTitle("智能交通灯标志物（B）");
        String[] tr_light = {"进入识别模式", "识别结果为红色，请求确认", "识别结果为绿色，请求确认", "识别结果为黄色，请求确认"};
        traffic_builder.setSingleChoiceItems(tr_light, -1, (dialog, i) -> {
            switch (i) {
                case 0:
                    FirstActivity.Connect_Transport.traffic_control(0x0F, 0x01, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.traffic_control(0x0F, 0x02, 0x01);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.traffic_control(0x0F, 0x02, 0x02);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.traffic_control(0x0F, 0x02, 0x03);
                    break;
                default:
                    break;
            }

        });
        traffic_builder.create().show();
    }

    //TFT显示标志物_B
    private void TFT_LCD_B() {
        AlertDialog.Builder TFTBuilder = new AlertDialog.Builder(context);
        TFTBuilder.setTitle("TFT显示标志物（B）");
        String[] TFTItem = {"图片显示模式", "车牌显示", "计时模式", "距离显示", "HEX显示模式"};
        TFTBuilder.setSingleChoiceItems(TFTItem, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            switch (which) {
                case 0:
                    TFT_Image_B();
                    break;
                case 1:
                    TFT_plate_number_B();
                    break;
                case 2:
                    TFT_Timer_B();
                    break;
                case 3:
                    Distance_B();
                    break;
                case 4:
                    Hex_show_B();
                    break;
                case 5:
                    TFT_traffic(0x08);
                    break;
            }
        });
        TFTBuilder.create().show();
    }

    //TFT显示标志物_B
    private void Hex_show_B() {
        AlertDialog.Builder TFT_Hex_builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_hex, null);
        TFT_Hex_builder.setTitle("HEX显示模式");
        TFT_Hex_builder.setView(view);
        // 下拉列表
        final EditText editText1 = (EditText) view.findViewById(R.id.editText1);
        final EditText editText2 = (EditText) view.findViewById(R.id.editText2);
        final EditText editText3 = (EditText) view.findViewById(R.id.editText3);
        TFT_Hex_builder.setPositiveButton("确定",
                (dialog, which) -> {
                    // TODO Auto-generated method stub
                    String ones = editText1.getText().toString();
                    String twos = editText2.getText().toString();
                    String threes = editText3.getText().toString();
                    // 显示数据，一个文本编译框最多两个数据显示数目管中两个数据
                    one = ones.equals("") ? 0x00 : Integer.parseInt(ones, 16);
                    two = twos.equals("") ? 0x00 : Integer.parseInt(twos, 16);
                    three = threes.equals("") ? 0x00 : Integer.parseInt(threes, 16);
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x40, one, two, three);
                });
        TFT_Hex_builder.setNegativeButton("取消", (dialog, which) -> {
            // TODO Auto-generated method stub
            dialog.cancel();
        });
        TFT_Hex_builder.create().show();
    }

    //TFT显示标志物_B
    private void Distance_B() {
        AlertDialog.Builder TFT_Distance_builder = new AlertDialog.Builder(context);
        TFT_Distance_builder.setTitle("距离显示模式");
        String[] TFT_Image_item = {"400mm", "500mm", "600mm"};
        TFT_Distance_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            if (which == 0) {
                FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x50, 0x00, 0x04, 0x00);
            }
            if (which == 1) {
                FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x50, 0x00, 0x05, 0x00);
            }
            if (which == 2) {
                FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x50, 0x00, 0x06, 0x00);
            }
        });
        TFT_Distance_builder.create().show();
    }

    //TFT显示标志物_B
    private void TFT_plate_number_B() {
        AlertDialog.Builder TFT_plate_builder = new AlertDialog.Builder(context);
        TFT_plate_builder.setTitle("车牌显示模式");
        final String[] TFT_Image_item = {"Z799C4", "B554H1", "D888B8"};
        TFT_plate_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x20, 'Z', '7', '9');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x21, '9', 'C', '4');
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x20, 'B', '5', '5');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x21, '4', 'H', '1');
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x20, 'D', '8', '8');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x21, '8', 'B', '8');
                    break;
            }
        });
        TFT_plate_builder.create().show();
    }

    //TFT显示标志物_B
    private void TFT_Timer_B() {
        AlertDialog.Builder TFT_Timer_builder = new AlertDialog.Builder(context);
        TFT_Timer_builder.setTitle("计时模式");
        String[] TFT_Image_item = {"开始", "关闭", "停止"};
        TFT_Timer_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x30, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x30, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x30, 0x00, 0x00, 0x00);
                    break;
            }
        });
        TFT_Timer_builder.create().show();
    }

    //TFT显示标志物_B
    private void TFT_Image_B() {
        AlertDialog.Builder TFT_Image_builder = new AlertDialog.Builder(context);
        TFT_Image_builder.setTitle("图片显示模式");
        String[] TFT_Image_item = {"指定显示", "上翻一页", "下翻一页", "自动翻页"};
        TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    TFT_B_show();
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x01, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x02, 0x00, 0x00);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x03, 0x00, 0x00);
                    break;
            }
        });
        TFT_Image_builder.create().show();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Zigbee_Landmark zigbeeLandmark = mZigbeeLandmarkList.get(position);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), zigbeeLandmark.getImageId(), null);
        bitmap = RadiusUtil.roundBitmapByXfermode(bitmap, bitmap.getWidth(), bitmap.getHeight(), 10);
        holder.zigbeeImage.setImageBitmap(bitmap);
        holder.zigbeeName.setText(zigbeeLandmark.getName());
    }

    @Override
    public int getItemCount() {
        return mZigbeeLandmarkList.size();
    }

    //智能交通灯标志物控制数据结构
    private void Traffic_light_A() {
        AlertDialog.Builder traffic_builder = new AlertDialog.Builder(context);
        traffic_builder.setTitle("智能交通灯标志物（A）");
        String[] tr_light = {"进入识别模式", "识别结果为红色，请求确认", "识别结果为绿色，请求确认", "识别结果为黄色，请求确认"};
        traffic_builder.setSingleChoiceItems(tr_light, -1, (dialog, i) -> {
            switch (i) {
                case 0:
                    FirstActivity.Connect_Transport.traffic_control(0x0E, 0x01, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.traffic_control(0x0E, 0x02, 0x01);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.traffic_control(0x0E, 0x02, 0x02);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.traffic_control(0x0E, 0x02, 0x03);
                    break;
                default:
                    break;
            }

        });
        traffic_builder.create().show();
    }

    //立体车库_A
    private void stereo_garage_A() {
        AlertDialog.Builder garage_builder = new AlertDialog.Builder(context);
        garage_builder.setTitle("立体车库标志物（A）");
        String[] ga = {"复位（第一层）", "到达第二层", "到达第三层", "到达第四层", "请求返回立体车库当前层数", "请求返回立体车库前/后侧\n" +
                "红外状态"};
        garage_builder.setSingleChoiceItems(ga, -1, (dialog, i) -> {
            switch (i) {
                case 0:  //到达第一层
                    FirstActivity.Connect_Transport.garage_control(0x0D, 0x01, 0x01);
                    break;
                case 1:  //到达第二层
                    FirstActivity.Connect_Transport.garage_control(0x0D, 0x01, 0x02);
                    break;
                case 2:  //到达第三层
                    FirstActivity.Connect_Transport.garage_control(0x0D, 0x01, 0x03);
                    break;
                case 3:  //到达第四层
                    FirstActivity.Connect_Transport.garage_control(0x0D, 0x01, 0x04);
                    break;
                case 4:  //请求返回车库位于第几层
                    FirstActivity.Connect_Transport.garage_control(0x0D, 0x02, 0x01);
                    break;
                case 5:  //请求返回前后侧红外状态
                    FirstActivity.Connect_Transport.garage_control(0x0D, 0x02, 0x02);
                    break;
                default:
                    break;
            }
        });
        garage_builder.create().show();
    }

    //道闸标志物
    private void gateController() {
        AlertDialog.Builder gt_builder = new AlertDialog.Builder(context);
        gt_builder.setTitle("道闸标志物");
        String[] gt = {"开启", "关闭", "车牌显示模式", "道闸初始角度调节", "请求返回道闸状态"};
        gt_builder.setSingleChoiceItems(gt, -1,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            // 打开道闸标志物
                            FirstActivity.Connect_Transport.gate(0x01, 0x01, 0x00, 0x00);
                            break;
                        case 1:
                            // 关闭道闸标志物
                            FirstActivity.Connect_Transport.gate(0x01, 0x02, 0x00, 0x00);
                            break;
                        case 2:
                            //显示车牌
                            gate_plate_number();
                            break;
                        case 3:
                            //调节初始角度
                            gate_angle_number();
                            break;
                        case 4:
                            //请求返回道闸标志物状态
                            FirstActivity.Connect_Transport.gate(0x20, 0x01, 0x00, 0x00);
                            break;
                        default:
                            break;
                    }
                });
        gt_builder.create().show();
    }

    //道闸显示车牌
    private void gate_plate_number() {
        AlertDialog.Builder gate_plate_builder = new AlertDialog.Builder(context);
        gate_plate_builder.setTitle("道闸显示车牌");
        final String[] gate_Image_item = {"A123B4", "B567C8", "D910E1"};
        gate_plate_builder.setSingleChoiceItems(gate_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.gate(0x10, 'A', '1', '2');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.gate(0x11, '3', 'B', '4');
                    break;
                case 1:
                    FirstActivity.Connect_Transport.gate(0x10, 'B', '5', '6');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.gate(0x11, '7', 'C', '8');
                    break;
                case 2:
                    FirstActivity.Connect_Transport.gate(0x10, 'D', '9', '1');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.gate(0x11, '0', 'E', '1');
                    break;
            }
        });
        gate_plate_builder.create().show();
    }

    //道闸初始角度调节
    private void gate_angle_number() {
        AlertDialog.Builder gate_plate_builder = new AlertDialog.Builder(context);
        gate_plate_builder.setTitle("道闸初始角度调节");
        final String[] gate_Image_item = {"上升", "下降"};
        gate_plate_builder.setSingleChoiceItems(gate_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.gate(0x09, 0x01, 0, 0);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.gate(0x09, 0x02, 0, 0);
                    break;
                default:
                    break;
            }
        });
        gate_plate_builder.create().show();
    }

    // LED显示标志物
    private void digital() {
        AlertDialog.Builder dig_timeBuilder = new AlertDialog.Builder(context);
        dig_timeBuilder.setTitle("LED显示标志物");
        String[] dig_item = {"数码管显示指定数据", "数码管显示计时模式", "数码管显示距离模式"};
        dig_timeBuilder.setSingleChoiceItems(dig_item, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                // LED显示标志物显示
                digitalController();
            } else if (which == 1) {
                // LED显示标志物计时
                digital_time();
            } else if (which == 2) {
                // 显示距离
                digital_dis();
            }
        });
        dig_timeBuilder.create().show();
    }

    // LED显示标志物显示方法
    private String[] items = {"第一行", "第二行"};
    int main, one, two, three;

    //数码管显示指定数据
    private void digitalController() {
        AlertDialog.Builder dg_Builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_digital, null);
        dg_Builder.setTitle("数码管显示指定数据");
        dg_Builder.setView(view);
        // 下拉列表
        Spinner spinner = (Spinner) view.findViewById(R.id.spinner);
        final EditText editText1 = (EditText) view.findViewById(R.id.editText1);
        final EditText editText2 = (EditText) view.findViewById(R.id.editText2);
        final EditText editText3 = (EditText) view.findViewById(R.id.editText3);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, items);
        spinner.setAdapter(adapter);
        // 下拉列表选择监听
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                main = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        dg_Builder.setPositiveButton("确定", (dialog, which) -> {
            // TODO Auto-generated method stub
            String ones = editText1.getText().toString();
            String twos = editText2.getText().toString();
            String threes = editText3.getText().toString();
            // 显示数据，一个文本编译框最多两个数据显示数目管中两个数据
            one = ones.equals("") ? 0x00 : Integer.parseInt(ones, 16);
            two = twos.equals("") ? 0x00 : Integer.parseInt(twos, 16);
            three = threes.equals("") ? 0x00 : Integer.parseInt(threes, 16);
            FirstActivity.Connect_Transport.digital(main, one, two, three);
        });

        dg_Builder.setNegativeButton("取消", (dialog, which) -> {
            // TODO Auto-generated method stub
            dialog.cancel();
        });
        dg_Builder.create().show();
    }

    private int dgtime_index = -1;

    // LED显示标志物计时
    private void digital_time() {
        AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(context);
        dg_timeBuilder.setTitle("数码管显示计时模式");
        String[] dgtime_item = {"计时结束", "计时开始", "清零"};
        dg_timeBuilder.setSingleChoiceItems(dgtime_item, dgtime_index, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                // 计时结束
                FirstActivity.Connect_Transport.digital_close();
            } else if (which == 1) {
                // 计时开启
                FirstActivity.Connect_Transport.digital_open();
            } else if (which == 2) {
                // 计时清零
                FirstActivity.Connect_Transport.digital_clear();
            }
        });
        dg_timeBuilder.create().show();
    }

    //数码管显示距离模式
    private void digital_dis() {
        AlertDialog.Builder dis_timeBuilder = new AlertDialog.Builder(context);
        dis_timeBuilder.setTitle("数码管显示距离模式");
        final String[] dis_item = {"100mm", "200mm", "400mm"};
        int digits_index = -1;
        dis_timeBuilder.setSingleChoiceItems(dis_item, digits_index, (dialog, which) -> {
            if (which == 0) {
                // 距离100mm
                FirstActivity.Connect_Transport.digital_dic(Integer.parseInt(dis_item[which].substring(0, 3)));
            } else if (which == 1) {
                // 距离200mmm
                FirstActivity.Connect_Transport.digital_dic(Integer.parseInt(dis_item[which].substring(0, 3)));
            } else if (which == 2) {
                // 距离400mm
                FirstActivity.Connect_Transport.digital_dic(Integer.parseInt(dis_item[which].substring(0, 3)));
            }
        });
        dis_timeBuilder.create().show();
    }

    private TextView voiceText;

    //语音播报标志物
    private void voiceController() {
        AlertDialog.Builder dg_timeBuilder = new AlertDialog.Builder(context);
        dg_timeBuilder.setTitle("语音播报标志物");
        String[] dgtime_item = {"语音播报随机指令", "语音播报指定内容"};
        dg_timeBuilder.setSingleChoiceItems(dgtime_item, dgtime_index, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                // 语音播报随机指令
                FirstActivity.Connect_Transport.VoiceBroadcast();
            } else if (which == 1) {
                // 语音播报指定内容
                View view = LayoutInflater.from(context).inflate(R.layout.item_car, null);
                voiceText = (EditText) view.findViewById(R.id.voiceText);
                AlertDialog.Builder voiceBuilder = new AlertDialog.Builder(context);
                voiceBuilder.setTitle("语音播报标志物");
                voiceBuilder.setView(view);
                voiceBuilder.setPositiveButton("播报", (dialog1, which1) -> {
                    // TODO Auto-generated method stub
                    String src = voiceText.getText().toString();
                    if (src.equals("")) {
                        src = "请输入你要播报的内容";
                    }
                    try {
                        byte[] sbyte = byteSend(src.getBytes("GBK"));
                        FirstActivity.Connect_Transport.send_voice(sbyte);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    dialog1.cancel();
                });
                voiceBuilder.setNegativeButton("取消", null);
                voiceBuilder.create().show();
            }
        });
        dg_timeBuilder.create().show();
    }

    private byte[] byteSend(byte[] sByte) {
        byte[] textByte = new byte[sByte.length + 5];
        textByte[0] = (byte) 0xFD;
        textByte[1] = (byte) (((sByte.length + 2) >> 8) & 0xff);
        textByte[2] = (byte) ((sByte.length + 2) & 0xff);
        // 合成语音命令
        textByte[3] = 0x01;
        // 编码格式
        textByte[4] = (byte) 0x01;
        System.arraycopy(sByte, 0, textByte, 5, sByte.length);
        return textByte;
    }

    //无线充电标志物
    private void magnetic_suspension() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("无线充电标志物");
        String[] item2 = {"开"};
        builder.setSingleChoiceItems(item2, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            if (which == 0) {
                FirstActivity.Connect_Transport.magnetic_suspension(0x01, 0x01, 0x00, 0x00);
            }
        });
        builder.create().show();
    }

    //TFT显示标志物_A
    private void TFT_LCD_A() {
        AlertDialog.Builder TFTBuilder = new AlertDialog.Builder(context);
        TFTBuilder.setTitle("TFT显示标志物（A）");
        String[] TFTitem = {"图片显示模式", "车牌显示模式", "计时模式模式", "距离显示模式", "HEX显示模式", "交通标志显示模式"};
        TFTBuilder.setSingleChoiceItems(TFTitem, -1, (dialog, which) -> {
            // TODO Auto-generated method stub
            switch (which) {
                case 0:
                    TFT_Image_A();
                    break;
                case 1:
                    TFT_plate_number_A();
                    break;
                case 2:
                    TFT_Timer_A();
                    break;
                case 3:
                    Distance_A();
                    break;
                case 4:
                    Hex_show_A();
                    break;
                case 5:
                    TFT_traffic(0x0B);
                    break;
            }
        });
        TFTBuilder.create().show();
    }

    //TFT显示标志物_A
    private void TFT_Image_A() {
        AlertDialog.Builder TFT_Image_builder = new AlertDialog.Builder(context);
        TFT_Image_builder.setTitle("图片显示模式");
        String[] TFT_Image_item = {"指定显示", "上翻一页", "下翻一页", "自动翻页"};
        TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    TFT_A_show();
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x10, 0x01, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x10, 0x03, 0x00, 0x00);
                    break;
            }
        });
        TFT_Image_builder.create().show();
    }

    //TFT显示标志物_B
    private void TFT_B_show() {
        AlertDialog.Builder TFT_Image_builder = new AlertDialog.Builder(context);
        TFT_Image_builder.setTitle("指定图片显示");
        String[] TFT_Image_item = {"1", "2", "3", "4", "5"};
        TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x03, 0x00, 0x00);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x04, 0x00, 0x00);
                    break;
                case 4:
                    FirstActivity.Connect_Transport.TFT_LCD(0x08, 0x10, 0x05, 0x00, 0x00);
                    break;
            }
        });
        TFT_Image_builder.create().show();
    }

    //TFT显示标志物_A
    private void TFT_A_show() {
        AlertDialog.Builder TFT_Image_builder = new AlertDialog.Builder(context);
        TFT_Image_builder.setTitle("指定图片显示");
        String[] TFT_Image_item = {"1", "2", "3", "4", "5"};
        TFT_Image_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x10, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x10, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x10, 0x03, 0x00, 0x00);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x10, 0x04, 0x00, 0x00);
                    break;
                case 4:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x10, 0x05, 0x00, 0x00);
                    break;
            }
        });
        TFT_Image_builder.create().show();
    }

    //TFT显示标志物_A
    private void TFT_plate_number_A() {
        AlertDialog.Builder TFT_plate_builder = new AlertDialog.Builder(context);
        TFT_plate_builder.setTitle("车牌显示模式");
        final String[] TFT_Image_item = {"A123B4", "B567C8", "D910E1"};
        TFT_plate_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x20, 'A', '1', '2');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x21, '3', 'B', '4');
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x20, 'B', '5', '6');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x21, '7', 'C', '8');
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x20, 'D', '9', '1');
                    FirstActivity.Connect_Transport.YanChi(500);
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x21, '0', 'E', '1');
                    break;
            }
        });
        TFT_plate_builder.create().show();
    }

    //TFT显示标志物_A
    private void TFT_Timer_A() {
        AlertDialog.Builder TFT_Timer_builder = new AlertDialog.Builder(context);
        TFT_Timer_builder.setTitle("计时显示模式");
        String[] TFT_Image_item = {"开始", "关闭", "停止"};
        TFT_Timer_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x30, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x30, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x30, 0x00, 0x00, 0x00);
                    break;
            }
        });
        TFT_Timer_builder.create().show();
    }

    //TFT显示标志物_A
    private void Distance_A() {
        AlertDialog.Builder TFT_Distance_builder = new AlertDialog.Builder(context);
        TFT_Distance_builder.setTitle("距离显示模式");
        String[] TFT_Image_item = {"100mm", "200mm", "300mm"};
        TFT_Distance_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            if (which == 0) {
                FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x50, 0x00, 0x01, 0x00);
            }
            if (which == 1) {
                FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x50, 0x00, 0x02, 0x00);
            }
            if (which == 2) {
                FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x50, 0x00, 0x03, 0x00);
            }
        });
        TFT_Distance_builder.create().show();
    }

    //TFT显示标志物_A
    private void Hex_show_A() {

        AlertDialog.Builder TFT_Hex_builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.item_hex, null);
        TFT_Hex_builder.setTitle("HEX显示模式");
        TFT_Hex_builder.setView(view);
        // 下拉列表
        final EditText editText1 = (EditText) view.findViewById(R.id.editText1);
        final EditText editText2 = (EditText) view.findViewById(R.id.editText2);
        final EditText editText3 = (EditText) view.findViewById(R.id.editText3);
        TFT_Hex_builder.setPositiveButton("确定", (dialog, which) -> {
            // TODO Auto-generated method stub
            String ones = editText1.getText().toString();
            String twos = editText2.getText().toString();
            String threes = editText3.getText().toString();
            // 显示数据，一个文本编译框最多两个数据显示数目管中两个数据
            one = ones.equals("") ? 0x00 : Integer.parseInt(ones, 16);
            two = twos.equals("") ? 0x00 : Integer.parseInt(twos, 16);
            three = threes.equals("") ? 0x00 : Integer.parseInt(threes, 16);
            FirstActivity.Connect_Transport.TFT_LCD(0x0B, 0x40, one, two, three);
        });
        TFT_Hex_builder.setNegativeButton("取消", (dialog, which) -> {
            // TODO Auto-generated method stub
            dialog.cancel();
        });
        TFT_Hex_builder.create().show();
    }

    private void TFT_traffic(final int type) {
        AlertDialog.Builder TFT_Items_builder = new AlertDialog.Builder(context);
        if (type != 0x0B)
            TFT_Items_builder.setTitle("TFT-B 交通标志显示模式");
        else
            TFT_Items_builder.setTitle("TFT-A 交通标志显示模式）");
        String[] TFT_Image_item = {"直行", "左转", "右转", "掉头", "禁止直行", "禁止通行"};
        TFT_Items_builder.setSingleChoiceItems(TFT_Image_item, -1, (dialog, which) -> {
            // TODO 自动生成的方法存根
            switch (which) {
                case 0:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x01, 0x00, 0x00);
                    break;
                case 1:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x02, 0x00, 0x00);
                    break;
                case 2:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x03, 0x00, 0x00);
                    break;
                case 3:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x04, 0x00, 0x00);
                    break;
                case 4:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x05, 0x00, 0x00);
                    break;
                case 5:
                    FirstActivity.Connect_Transport.TFT_LCD(type, 0x60, 0x06, 0x00, 0x00);
                    break;
            }
        });
        TFT_Items_builder.create().show();
    }

}