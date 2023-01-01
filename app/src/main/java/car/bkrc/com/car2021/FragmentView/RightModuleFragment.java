package car.bkrc.com.car2021.FragmentView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import car.bkrc.com.car2021.R;
import car.bkrc.com.car2021.ViewAdapter.ModuleAdapter;
import car.bkrc.com.car2021.ViewAdapter.Module_Landmark;

public class RightModuleFragment extends Fragment {

    private List<Module_Landmark> moduleList = new ArrayList<>();
    Context minStance = null;

    public static RightModuleFragment getInstance() {
        return RightModuleHolder.mInstance;
    }


    private static class RightModuleHolder {
        @SuppressLint("StaticFieldLeak")
        private static final RightModuleFragment mInstance = new RightModuleFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        minStance = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //创建Fragment
        View view = inflater.inflate(R.layout.right_module_fragment, container, false);
        //初始化视图选项
        initModule();
        //创建RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        //初始化并绑定适配器
        ModuleAdapter adapter = new ModuleAdapter(moduleList, getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initModule() {
        moduleList.clear();
        Module_Landmark m1 = new Module_Landmark("红绿灯识别", R.mipmap.traffic_light_module);
        moduleList.add(m1);
        Module_Landmark m2 = new Module_Landmark("车牌识别", R.mipmap.ocr);
        moduleList.add(m2);
        Module_Landmark m3 = new Module_Landmark("形状识别", R.mipmap.shapes);
        moduleList.add(m3);
        Module_Landmark m4 = new Module_Landmark("交通标志物识别", R.mipmap.traffic_flag);
        moduleList.add(m4);
        Module_Landmark m5 = new Module_Landmark("二维码识别", R.mipmap.qr);
        moduleList.add(m5);
        Module_Landmark m6 = new Module_Landmark("test", R.mipmap.test);
        moduleList.add(m6);
    }
}
