package car.bkrc.com.car2021.FragmentView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import car.bkrc.com.car2021.R;
import car.bkrc.com.car2021.ViewAdapter.InfraredAdapter;
import car.bkrc.com.car2021.ViewAdapter.Infrared_Landmark;

/**
 * 底部红外导航选项
 */
public class RightInfraredFragment extends Fragment {

    public static final String TAG = "RightInfraredFragment";
    private final Infrared_Landmark[] infrared = {
            new Infrared_Landmark("烽火台报警标志物", R.mipmap.alarm),
            new Infrared_Landmark("智能路灯标志物", R.mipmap.gear_position),
            new Infrared_Landmark("立体显示标志物", R.mipmap.stereo_display)};

    private List<Infrared_Landmark> InfraredList = new ArrayList<>();
    private static RightInfraredFragment mInstance = null;

    //   private RightInfraredFragment(){}
    //单例模式初始化
    public static RightInfraredFragment getInstance() {
        if (mInstance == null) {
            synchronized (RightInfraredFragment.class) {
                if (mInstance == null) {
                    mInstance = new RightInfraredFragment();
                }
            }
        }
        return mInstance;
    }

    /**
     * 附加到对应的上下文
     * @param context 上下文
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach");
    }

    /**
     * -
     * @param savedInstanceState 保存当前实例状态
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Identify");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.right_infrared_fragment, container, false);
        initInfrared();
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(manager);
        InfraredAdapter adapter = new InfraredAdapter(InfraredList, getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void initInfrared() {
        InfraredList.clear();
        Collections.addAll(InfraredList, infrared);
    }
}
