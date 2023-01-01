package car.bkrc.com.car2021.Utils.TrafficLight;

import android.graphics.Bitmap;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TrafficLight_fix {

    private static final String TAG = TrafficLight_fix.class.getSimpleName();
    private static Bitmap Btmp;
    private static Mat srcmat;
    private static Mat red, green, yellow;
    private static Bitmap bitmapr, bitmapy, bitmapg;
    private static Bitmap bm = null;
    private static String ColorPixel;


    /**
     * 识别传入的红绿灯图片
     *
     * @param inputBitmap 传入需要识别的红绿灯图片
     * @return String – 红绿灯识别结果
     */
    public static String Identify(Bitmap inputBitmap) {
        /*在这里调整传入的图片以方便红绿灯的识别*/
        Btmp = Bitmap.createBitmap(inputBitmap,
                //开始的x轴
                (inputBitmap.getWidth() / 100) * 25,
                //开始的y轴
                (inputBitmap.getHeight() / 100) * 2,
                //从开始的x轴截取到当前位置的宽度
                (inputBitmap.getWidth() / 100) * 65,
                //从开始的y轴截取到当前位置的高度
                (inputBitmap.getHeight() / 100) * 45);
        //保存图片用
        bm = Bitmap.createBitmap(Btmp);
        TrafficLight.saveBitmap("红绿灯裁剪图片.jpg", bm);
        srcmat = new Mat();
        Utils.bitmapToMat(Btmp, srcmat);

        bitmapr = Bitmap.createBitmap(srcmat.width(), srcmat.height(), Bitmap.Config.ARGB_8888);
        bitmapy = Bitmap.createBitmap(srcmat.width(), srcmat.height(), Bitmap.Config.ARGB_8888);
        bitmapg = Bitmap.createBitmap(srcmat.width(), srcmat.height(), Bitmap.Config.ARGB_8888);

        //创建用来存储图像信息的内存对象
        red = new Mat();
        green = new Mat();
        yellow = new Mat();

        //转换为HSV
        /* 红,黄,绿 */
        Imgproc.cvtColor(srcmat, red, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(srcmat, yellow, Imgproc.COLOR_RGB2HSV);
        Imgproc.cvtColor(srcmat, green, Imgproc.COLOR_RGB2HSV);

        //颜色分割
        Log.i("start Core.inRange()", "开始颜色分割");
        /* 第一版测试 */
//            Core.inRange(red, new Scalar(0, 80, 255), new Scalar(50, 255, 255), red);
//            Core.inRange(yellow, new Scalar(0, 0, 255), new Scalar(0, 50, 255), yellow);
//            Core.inRange(green, new Scalar(70, 0, 255), new Scalar(90, 255, 255), green);

        /* 赛场上专用 */
        Core.inRange(red, new Scalar(0, 80, 230), new Scalar(15, 255, 255), red);
        Core.inRange(yellow, new Scalar(25, 0, 230), new Scalar(70, 255, 255), yellow);
        Core.inRange(green, new Scalar(70, 0, 230), new Scalar(100, 255, 255), green);

        /* 强光下使用 */
//            Core.inRange(red, new Scalar(50, 255, 255), new Scalar(0, 80, 255), red);
//            Core.inRange(yellow, new Scalar(0, 50, 255), new Scalar(0, 0, 255), green);
//            Core.inRange(green, new Scalar(90, 255, 255), new Scalar(70, 0, 255), yellow);

        /* 形态学处理 */
        //确定运算核
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        /* 开,闭运算 */
        Imgproc.morphologyEx(red, red, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(red, red, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(yellow, yellow, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(yellow, yellow, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.morphologyEx(green, green, Imgproc.MORPH_OPEN, kernel);
        Imgproc.morphologyEx(green, green, Imgproc.MORPH_CLOSE, kernel);

        Utils.matToBitmap(red, bitmapr);
        Utils.matToBitmap(green, bitmapg);
        Utils.matToBitmap(yellow, bitmapy);

        int Ir = TrafficLight.getImagePixel(bitmapr);
        int Iy = TrafficLight.getImagePixel(bitmapy);
        int Ig = TrafficLight.getImagePixel(bitmapg);

        TrafficLight.saveBitmap("r" + ".jpg", bitmapr);
        TrafficLight.saveBitmap("y" + ".jpg", bitmapy);
        TrafficLight.saveBitmap("g" + ".jpg", bitmapg);

        Log.i(TAG, "redPixel: " + Ir + "\nyellowPixel: " + Iy + "\ngreenPixel: " + Ig);
        destroy();
        if (Ig > 10) return ColorPixel = "绿灯";
        //比赛场使用 - 正常光
//        return ColorPixel = Iy > 35 ? "黄灯" : "红灯";
        //强光下使用
        return ColorPixel = Iy > 18 ? "黄灯" : "红灯";
    }

    /**
     * 释放资源
     */
    private static void destroy() {
        Btmp.recycle();
        srcmat.release();
        red.release();
        green.release();
        yellow.release();
        bitmapr.recycle();
        bitmapy.recycle();
        bitmapg.recycle();
    }


    /**
     * 只保存TrafficLight类使用的图片
     * 如需保存其他图片请添加bitmap参数
     */
    public static void saveBitmap() {
        Log.d("Save Bitmap", "Ready to save picture");
        // 指定我们想要存储文件的地址
        String TargetPath = "/storage/emulated/0/DCIM/Tess/";
        Log.d("Save Bitmap", "Save Path=" + TargetPath);
        // 判断指定文件夹的路径是否存在
        if (!fileIsExist(TargetPath)) {
            Log.d("Save Bitmap", "TargetPath isn't exist");
        } else {
            // 如果指定文件夹创建成功，那么我们则需要进行图片存储操作
            File saveFile = new File(TargetPath, ColorPixel + ".jpg");
            try {
                FileOutputStream saveImgOut = new FileOutputStream(saveFile);
                // compress - 压缩的意思
                bm.compress(Bitmap.CompressFormat.JPEG, 80, saveImgOut);
                // 存储完成后需要清除相关的进程
                saveImgOut.flush();
                saveImgOut.close();
                Log.d("Save Bitmap", "The picture is save to your phone!");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    static boolean fileIsExist(String fileName) {
        //传入指定的路径，然后判断路径是否存在
        File file = new File(fileName);
        //file.mkdirs() 创建文件夹的意思
        return file.exists() || file.mkdirs();
    }
}
