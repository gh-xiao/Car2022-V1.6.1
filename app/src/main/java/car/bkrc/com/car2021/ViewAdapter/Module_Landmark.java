package car.bkrc.com.car2021.ViewAdapter;

/**
 * 模块测试选项的标记对象
 */
public class Module_Landmark {
    private String name;
    private int imageId;

    public Module_Landmark(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }


    public int getImageId() {
        return imageId;
    }
}
