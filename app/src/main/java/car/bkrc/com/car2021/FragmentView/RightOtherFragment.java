package car.bkrc.com.car2021.FragmentView;

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
import car.bkrc.com.car2021.ViewAdapter.OtherAdapter;
import car.bkrc.com.car2021.ViewAdapter.Other_Landmark;

/**
 * 底部其他导航选项
 */
public class RightOtherFragment extends Fragment {

    private List<Other_Landmark> otherList = new ArrayList<>();
    Context minStance = null;

    //  private RightOtherFragment(){}

    public static RightOtherFragment getInstance() {
        return RightZigbeeHolder.mInstance;
    }

    private static class RightZigbeeHolder {
        private static final RightOtherFragment mInstance = new RightOtherFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        minStance = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_other_fragment, container, false);
        initFruits();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        OtherAdapter adapter = new OtherAdapter(otherList, getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initFruits() {
        otherList.clear();
        Other_Landmark apple = new Other_Landmark("摄像头控制", R.mipmap.default_position);
        otherList.add(apple);
        Other_Landmark banana = new Other_Landmark("二维码识别", R.mipmap.qr_code);
        otherList.add(banana);
        Other_Landmark orange = new Other_Landmark("蜂鸣器控制", R.mipmap.buzzer);
        otherList.add(orange);
        Other_Landmark watermelon = new Other_Landmark("转向灯控制", R.mipmap.light);
        otherList.add(watermelon);
    }
}

