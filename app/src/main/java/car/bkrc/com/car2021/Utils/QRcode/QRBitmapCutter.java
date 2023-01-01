package car.bkrc.com.car2021.Utils.QRcode;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * 彩色二维码截取
 */
public class QRBitmapCutter {

    public enum QRColor {RED, GREEN, BLUE}

    private static final String TAG = QRBitmapCutter.class.getSimpleName();
    //裁剪参数
    private static final int[] RedQR = {0, 150, 100, 255, 40, 255, 40};
    private static final int[] GreenQR = {0, 50, 35, 255, 75, 255, 100};
    private static final int[] BlueQR = {0, 150, 100, 255, 80, 255, 80};

    private Mat hsvmat;

    //轮廓统计
    private List<MatOfPoint> contours = new ArrayList<>();
    private Mat result;
    //裁剪后的图片
    private Bitmap RectBitmap;


    /**
     * @param inputQRBitmap 待解析裁剪的二维码原图
     * @param color         red/green/blue
     * @return 对应颜色的二维码Bitmap
     */
    public Bitmap QRCutter(@NonNull Bitmap inputQRBitmap, QRColor color) {
        /* 转换为mat对象 */
        Mat mat = new Mat();
        Utils.bitmapToMat(inputQRBitmap, mat);
        /* 红色二维码反色处理 */
        if (color.equals(QRColor.RED)) {
            //RGB转换为BGR - 红蓝色互换
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        }
        /* 设置感兴趣区域,减少误裁剪概率 */
        Rect ROI = new Rect(100, 0, mat.width() - 190, mat.height() - 35);
        Mat ROIMat = new Mat(mat, ROI);
        /* 转换为包含hsv参数的mat对象 */
        hsvmat = new Mat();
        Imgproc.cvtColor(ROIMat, hsvmat, Imgproc.COLOR_RGB2HSV);
        /* 将图像根据指定参数转换为黑白mat对象,在选定范围内的像素转换为白色 */
        switch (color) {
            case GREEN:
                Core.inRange(hsvmat, new Scalar(GreenQR[2], GreenQR[4], GreenQR[6]),
                        new Scalar(GreenQR[1], GreenQR[3], GreenQR[5]), hsvmat);
                break;
            case RED:
                Core.inRange(hsvmat, new Scalar(RedQR[2], RedQR[4], RedQR[6]),
                        new Scalar(RedQR[1], RedQR[3], RedQR[5]), hsvmat);
                break;
            /* 默认处理蓝色二维码 */
            default:
                Core.inRange(hsvmat, new Scalar(BlueQR[2], BlueQR[4], BlueQR[6]),
                        new Scalar(BlueQR[1], BlueQR[3], BlueQR[5]), hsvmat);
                break;
        }

        /* 确定运算核，类似于卷积核 */
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        //https://www.jianshu.com/p/ee72f5215e07
        /* 膨胀操作(扩大白色联通区域) */
        Imgproc.morphologyEx(hsvmat, hsvmat, Imgproc.MORPH_DILATE, kernel);
        Imgproc.morphologyEx(hsvmat, hsvmat, Imgproc.MORPH_DILATE, kernel);
        Imgproc.morphologyEx(hsvmat, hsvmat, Imgproc.MORPH_DILATE, kernel);

        contours.clear();
        /* 获取轮廓 */
        //无用但必要的Mat对象
        Mat hierarchy = new Mat();
        /*  Imgproc.findContours()方法解析:
          Mat image - 输⼊的8位单通道"二值"图像
          List<MatOfPoint> contours - 包含MatOfPoint对象的List
          Mat hierarchy - (可选)拓扑信息
          int mode  轮廓检索模式
          int method 近似方法 */
        /* int mode - 提取参数:
        RETR_EXTERNAL: 表示只提取最外面的轮廓
        RETR_LIST: 表示提取所有轮廓并将其放⼊列表
        RETR_CCOMP: 表示提取所有轮廓并将组织成⼀个两层结构，其中顶层轮廓是外部轮廓，第⼆层轮廓是“洞”的轮廓
        RETR_TREE: 表示提取所有轮廓并组织成轮廓嵌套的完整层级结构 */
        Imgproc.findContours(hsvmat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        /* (保险起见,加入判断)如果存在轮廓 */
        if (contours.size() > 0) {

            MatOfPoint contour = null;
            //最大面积
            double maxArea = 0;
            //所有轮廓的迭代器
            /* 获取最大轮廓 */
            for (MatOfPoint wrapper : contours) {
                //当前迭代的MatOfPoint对象
                //获取面积大小
                double area = Imgproc.contourArea(wrapper);
                if (area > maxArea) {
                    maxArea = area;
                    contour = wrapper;
                }
            }

            if (contour != null) {
                /* 最小外接矩形 */
                Rect rect = Imgproc.boundingRect(contour);
                /* 绘图 */
                Mat imgSource = ROIMat.clone();
                /* 微调裁剪矩形大小 */
                try {
                    result = new Mat(imgSource, rect);
                    RectBitmap = Bitmap.createBitmap(result.width(), result.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(result, RectBitmap);
                    Log.e(TAG, "二维码裁剪完毕");
                    return RectBitmap;
                } catch (Exception e) {
                    Log.e(TAG, "二维码裁剪完毕");
                    e.printStackTrace();
                    return null;
                }
            }
        }
        Log.e(TAG, "没有查找到轮廓");
        return null;
    }
}
