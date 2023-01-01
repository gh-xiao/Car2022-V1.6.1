package car.bkrc.com.car2021.ViewAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import car.bkrc.com.car2021.ActivityView.FirstActivity;
import car.bkrc.com.car2021.FragmentView.LeftFragment;
import car.bkrc.com.car2021.R;
import car.bkrc.com.car2021.Utils.OtherUtil.RadiusUtil;

/**
 * 模块测试选项的适配器
 */
public class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.ViewHolder> {

    private List<Module_Landmark> mModuleLandmarksList;
    private final Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View moduleView;
        ImageView moduleImage;
        TextView moduleName;

        public ViewHolder(View view) {
            super(view);
            moduleView = view;
            moduleImage = (ImageView) view.findViewById(R.id.landmark_image);
            moduleName = (TextView) view.findViewById(R.id.landmark_name);
        }
    }

    public ModuleAdapter(List<Module_Landmark> ModuleLandmarksList, Context context) {
        mModuleLandmarksList = ModuleLandmarksList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.module_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.moduleView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Module_Landmark moduleLandmark = mModuleLandmarksList.get(position);
            module_select(moduleLandmark);
        });

        holder.moduleImage.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Module_Landmark moduleLandmark = mModuleLandmarksList.get(position);
            module_select(moduleLandmark);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Module_Landmark moduleLandmark = mModuleLandmarksList.get(position);
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), moduleLandmark.getImageId(), null);
        bitmap = RadiusUtil.roundBitmapByXfermode(bitmap, bitmap.getWidth(), bitmap.getHeight(), 10);
        holder.moduleImage.setImageBitmap(bitmap);
        holder.moduleName.setText(moduleLandmark.getName());
    }

    @Override
    public int getItemCount() {
        return mModuleLandmarksList.size();
    }

    /**
     * 添加新模块时需要在{@link car.bkrc.com.car2021.FragmentView.RightModuleFragment}中的initModule()方法添加相应名字的Module_Landmark对象
     *
     * @param moduleLandmark -
     */
    private void module_select(Module_Landmark moduleLandmark) {
        //测试模块拥有最高优先级
        if (moduleLandmark.getName().equals("test")) {
            FirstActivity.Connect_Transport.module(6);
            FirstActivity.toastUtil.ShowToast("想要测试的项目模块可以写在这哦!");
            return;
        }
        //禁止在未正常连接主车wifi获得摄像头图像的时候使用模块
        if (LeftFragment.bitmap == null) {
            FirstActivity.toastUtil.ShowToast("没有图片可以进行识别!");
            return;
        }
        switch (moduleLandmark.getName()) {
            case "红绿灯识别":
                FirstActivity.Connect_Transport.module(1);
                FirstActivity.toastUtil.ShowToast("红绿灯识别");
                break;
            case "车牌识别":
                FirstActivity.Connect_Transport.module(2);
                FirstActivity.toastUtil.ShowToast("车牌识别");
                break;
            case "形状识别":
                FirstActivity.Connect_Transport.module(3);
                FirstActivity.toastUtil.ShowToast("形状识别");
                break;
            case "交通标志物识别":
                FirstActivity.Connect_Transport.module(4);
                FirstActivity.toastUtil.ShowToast("交通标志物识别");
//                FirstActivity.toastUtil.ShowToast("模块暂时关闭!");
                break;
            case "二维码识别":
                FirstActivity.Connect_Transport.module(5);
                FirstActivity.toastUtil.ShowToast("二维码识别");
                break;
            default:
                break;
        }
    }


}