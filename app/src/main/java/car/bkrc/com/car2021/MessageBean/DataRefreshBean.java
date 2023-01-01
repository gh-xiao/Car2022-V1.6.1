package car.bkrc.com.car2021.MessageBean;

/**
 * 数据刷新实例对象
 */
public class DataRefreshBean {

    private int refreshState;

    public DataRefreshBean(int refreshState){
        this.refreshState = refreshState;
    }

    public int getRefreshState() {
        return refreshState;
    }

    public void setRefreshState(int refreshState) {
        this.refreshState = refreshState;
    }
}
