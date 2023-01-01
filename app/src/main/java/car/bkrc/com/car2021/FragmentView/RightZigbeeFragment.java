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
import car.bkrc.com.car2021.ViewAdapter.ZigbeeAdapter;
import car.bkrc.com.car2021.ViewAdapter.Zigbee_Landmark;

/**
 * 底部Zigbee导航选项
 */
public class RightZigbeeFragment extends Fragment {

    private List<Zigbee_Landmark> ZigbeeList = new ArrayList<>();
    Context minStance = null;

    // private RightZigbeeFragment(){}

    public static RightZigbeeFragment getInstance() {
        return RightZigbeeHolder.mInstance;
    }

    private static class RightZigbeeHolder {
        private static final RightZigbeeFragment mInstance = new RightZigbeeFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        minStance = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_zigbee_fragment, container, false);
        initZigBees();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        ZigbeeAdapter adapter = new ZigbeeAdapter(ZigbeeList, getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initZigBees() {
        ZigbeeList.clear();
        Zigbee_Landmark apple = new Zigbee_Landmark("道闸标志物", R.mipmap.barrier_gate);
        ZigbeeList.add(apple);
        Zigbee_Landmark banana = new Zigbee_Landmark("LED显示标志物", R.mipmap.nixie_tube);
        ZigbeeList.add(banana);
        Zigbee_Landmark orange = new Zigbee_Landmark("语音播报标志物", R.mipmap.voice_broadcast);
        ZigbeeList.add(orange);
        Zigbee_Landmark watermelon = new Zigbee_Landmark("无线充电标志物", R.mipmap.maglev);
        ZigbeeList.add(watermelon);
        Zigbee_Landmark pear_A = new Zigbee_Landmark("智能TFT显示标志物", R.mipmap.tft_lcd);
        ZigbeeList.add(pear_A);
        Zigbee_Landmark traffic_light_A = new Zigbee_Landmark("智能交通灯标志物", R.mipmap.traffic_light);
        ZigbeeList.add(traffic_light_A);
        Zigbee_Landmark stereo_garage_A = new Zigbee_Landmark("立体车库标志物", R.mipmap.cheku);
        ZigbeeList.add(stereo_garage_A);
        Zigbee_Landmark etc_A = new Zigbee_Landmark("ETC系统标志物", R.mipmap.etc_pic);
        ZigbeeList.add(etc_A);
    }

}
