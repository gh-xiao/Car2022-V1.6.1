package car.bkrc.com.car2021.Utils.Shape;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import car.bkrc.com.car2021.Utils.ColorHSV;
import car.bkrc.com.car2021.Utils.TrafficLight.TrafficLight;

public class ShapeIdentify {

    //目标类的简写名称
    private static final String TAG = ShapeIdentify.class.getSimpleName();
    //轮廓绘制/轮廓统计
    private static List<MatOfPoint> contours = new ArrayList<>();
    /* -----形状识别结果所有图形数量统计 ----- */
    private int totals = 0;

    /**
     * 统计图中包含的图形数量(该图片所有的图形数量)
     */
    public int getTotals() {
        for (Map.Entry<String, ShapeCount> map : ShapeCounts.entrySet()) {
            totals += map.getValue().getSameColorCounts("总计");
        }
        return totals;
    }

    public void setTotals(int totals) {
        this.totals = totals;
    }
    /* -----形状识别结果所有图形数量统计 ----- */

    /* -----HashMap<颜色,该颜色统计对象HashMap<形状,数量>>----- */
    private HashMap<String, ShapeCount> ShapeCounts = new HashMap<>();

    /**
     * 获取指定颜色的统计数据<颜色,该颜色统计对象>
     *
     * @return 该颜色统计对象<形状, 数量>
     */
    public HashMap<String, ShapeCount> getShapeCounts() {
        return ShapeCounts;
    }
    /* -----HashMap<颜色,该颜色统计对象HashMap<形状,数量>>----- */

    /**
     * 形状识别 - Bitmap图片处理
     *
     * @param inputBitmap 需要处理的图片
     */
    public void shapePicProcess(Bitmap inputBitmap) {

        /* 图片截取方式1 */
//        Bitmap Btmp = Bitmap.createBitmap(inputBitmap,
//                //开始的x轴
//                (inputBitmap.getWidth() / 100) * 25,
//                //开始的y轴
//                (inputBitmap.getHeight() / 100) * 50,
//                //从开始的x轴截取到当前位置的宽度
//                (inputBitmap.getWidth() / 100) * 35,
//                //从开始的y轴截取到当前位置的高度
//                (inputBitmap.getHeight() / 100) * 50);

        /* openCV创建用来存储图像信息的内存对象 */
        Mat srcmat = new Mat();

        /* 转化为Mat对象 */
        Utils.bitmapToMat(inputBitmap, srcmat);
        shapePicProcess(srcmat);
    }

    /**
     * 形状识别 - Mat图片处理
     *
     * @param srcmat 需要识别的图片
     */
    public void shapePicProcess(Mat srcmat) {
        ShapeCounts.clear();
        /* 调整截图位置(图片截取方式2) */
        //测试图片用
//        Rect rect = new Rect(188, 81, 322, 192);
        //???
//        Rect rect = new Rect(188, 145, 290, 195);
        //主车用
//        Rect rect = new Rect(188, 175, 290, 175);
//        Rect rect = new Rect(200, 160, 290, 175);

//        Mat dstmat = new Mat(srcmat, rect);

        Mat dstmat = srcmat;

        /* 如果使用Utils.loadResource()加载图片资源,则需要转换为RGB */
//        Imgproc.cvtColor(dstmat, dstmat, Imgproc.COLOR_BGR2RGB);

        /* 保存用 */
        Bitmap save = Bitmap.createBitmap(dstmat.width(), dstmat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(dstmat, save);
        TrafficLight.saveBitmap("shape_裁剪.jpg", save);

        /* 颜色形状分析 */
        Identify(dstmat, ColorHSV.yellowHSV1, "黄色");
        Identify(dstmat, ColorHSV.greenHSV1, "绿色");
        Identify(dstmat, ColorHSV.cyanHSV, "青色");
        Identify(dstmat, ColorHSV.blueHSV2, "蓝色");
        Identify(dstmat, ColorHSV.purpleHSV1, "紫色");
        /* 红色颜色取反,方便处理 */
        Identify(dstmat, "红色");
    }

    /**
     * <p>形状识别 - 反色处理</p>
     * <p>因红色阈值问题,建议将图片进行反色处理</p>
     *
     * @param Mtmp 已经处理的Mat对象
     */
    private void Identify(Mat Mtmp, @SuppressWarnings("SameParameterValue") String colorName) {
        Mat dstmat = new Mat();
        //RGB转换为BGR - 红蓝色互换
        Imgproc.cvtColor(Mtmp, dstmat, Imgproc.COLOR_BGR2RGB);
        Identify(dstmat, ColorHSV.red2blueHSV, colorName);
    }

    /**
     * 形状识别
     *
     * @param Mtmp      已经处理的Mat对象
     * @param r         色彩数据
     * @param colorName 色彩名
     */
    private void Identify(Mat Mtmp, int[] r, String colorName) {

        /* openCV创建用来存储图像信息的内存对象 */
        Mat hsvmat = new Mat();
        Mat outma = new Mat();
        Mat mat = Mtmp.clone();
        /* 转换为HSV */
        Imgproc.cvtColor(mat, hsvmat, Imgproc.COLOR_RGB2HSV);

        /* 颜色分割 */
        Core.inRange(hsvmat, new Scalar(r[2], r[4], r[6]), new Scalar(r[1], r[3], r[5]), hsvmat);

        /* 确定运算核，类似于卷积核 */
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));

        /* 开运算(除去白噪点) */
        Imgproc.morphologyEx(hsvmat, hsvmat, Imgproc.MORPH_OPEN, kernel);
        /* 闭运算(除去黑噪点) */
        Imgproc.morphologyEx(hsvmat, hsvmat, Imgproc.MORPH_CLOSE, kernel);

        /* 轮廓提取,用于提取图像的轮廓 */
        contours.clear();
        Imgproc.findContours(hsvmat, contours, outma, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        /* 轮廓数量统计 */
        int contoursCounts = contours.size();

        /* 绘制轮廓,用于绘制找到的图像轮廓 */
        Imgproc.drawContours(mat, contours, -1, new Scalar(0, 255, 0), 4);

        /* 形状统计 */
        /* 核心统计代码,参数已调整 */
        //轮廓
        MatOfPoint2f contour2f;
        //近似曲线(多边形拟合)
        MatOfPoint2f approxCurve;
        double epsilon;
        int tri, irect, circle, star, rhombus;
        tri = irect = circle = star = rhombus = 0;
        /* 遍历轮廓 */
        for (int i = 0; i < contoursCounts; i++) {
            /* 判断面积是否大于阈值 */
            Log.i(TAG, "这是轮廓面积: " + Imgproc.contourArea(contours.get(i)));
            if (Imgproc.contourArea(contours.get(i)) > 250) {
                /* 某一个点的集合(当前对象的轮廓) */
                contour2f = new MatOfPoint2f(contours.get(i).toArray());
                /* 计算轮廓的周长 */
                epsilon = 0.035 * Imgproc.arcLength(contour2f, true);
                /* 多边形拟合 */
                approxCurve = new MatOfPoint2f();
                Imgproc.approxPolyDP(contour2f, approxCurve, epsilon, true);
//                System.out.println("数量: " + approxCurve.rows());
                /* 返回包含旋转矩形的最小矩形 */
                Rect rect1 = Imgproc.boundingRect(approxCurve);
                /* 计算轮廓中心 */
                Imgproc.rectangle(mat, new Point(rect1.x, rect1.y), new Point(rect1.x + rect1.width, rect1.y + rect1.height), new Scalar(255, 255, 0), 4);
                if (approxCurve.rows() == 3) tri++;
                    /* 判断矩形和菱形 */
//                else if (approxCurve.rows() == 4) irect++;
                else if (approxCurve.rows() == 4) {
                    double area, minArea;
                    /* 四边形拟合的面积 */
                    area = Imgproc.contourArea(contour2f);
                    /* 包含旋转矩形的最小矩形的面积 */
                    RotatedRect rect2 = Imgproc.minAreaRect(contour2f);
                    minArea = rect2.size.area();
                    double rec = area / minArea;
                    Log.i(TAG, "这是area / minArea得到的阈值: " + rec);
                    if (rec >= 0.78 && rec < 1.15) irect++;
                    else rhombus++;
                }
                /* 判断五角星和圆形 */
                else if (approxCurve.rows() > 4) {
                    int mianji1 = rect1.height * rect1.width;
                    double mianji2 = Imgproc.contourArea(contours.get(i));
                    if ((mianji2 / mianji1) > 0.5) {
                        circle++;
                    } else {
                        star++;
                    }
                }
            }
        }

        /* 引用ShapeCount对象存放识别数据 */
        SaveResult(colorName, circle, tri, irect, star, rhombus);

        /* 输出结果 */
        String msg = colorName + "轮廓: " + contoursCounts + "\n圆形: " + circle + " 三角形: " + tri + " 矩形: " + irect + " 菱形: " + rhombus + " 五角星: " + star;
        Log.i(TAG, msg);

        /* 保存图片 */
        Bitmap save = Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, save);
        TrafficLight.saveBitmap(colorName + ".jpg", save);
    }

    private void SaveResult(String colorName, int circle, int tri, int rect, int star, int diamond) {
        /* 统计相同颜色的所有形状个数 */
        /* 同色不同形 */
        HashMap<String, Integer> sameColorCounts = new HashMap<>();
        sameColorCounts.put("三角形", tri);
        sameColorCounts.put("矩形", rect);
        sameColorCounts.put("菱形", diamond);
        sameColorCounts.put("五角星", star);
        sameColorCounts.put("圆形", circle);
        sameColorCounts.put("总计", tri + rect + diamond + star + circle);
        /* 形状计数对象 */
        ShapeCount shapeCount = new ShapeCount();
        /* 保存在该对象上 */
        shapeCount.setShapeCounts(sameColorCounts);
        ShapeCounts.put(colorName, shapeCount);
    }
}