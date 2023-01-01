package car.bkrc.com.car2021.Utils.Shape;

import java.util.HashMap;

/**
 * 形状计数对象
 * 同种颜色
 */
public class ShapeCount {

    //统计形状个数
    private HashMap<String, Integer> shapeCounts = new HashMap<>();

    /**
     * 获取指定形状的数量
     *
     * @param shapeName 三角形/矩形/菱形/五角星/圆形/总计
     * @return 数量
     */
    public Integer getSameColorCounts(String shapeName) {
        return shapeCounts.get(shapeName);
    }

    /**
     * 统计形状个数
     *
     * @param shapeCounts 包含该形状的数量的对象
     */
    public void setShapeCounts(HashMap<String, Integer> shapeCounts) {
        this.shapeCounts = shapeCounts;
    }
}
