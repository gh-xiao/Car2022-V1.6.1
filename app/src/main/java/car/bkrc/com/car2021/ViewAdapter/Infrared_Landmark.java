package car.bkrc.com.car2021.ViewAdapter;

/**
 * 红外选项的标记对象
 */
public class Infrared_Landmark {

    private final String name;

    private final int imageId;

    public Infrared_Landmark(String name, int imageId) {
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