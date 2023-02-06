Car2022
===

## 版本号: V1.6.1

### 2022/12/21 - 12/23

    #添加TFTAutoCutter类对包含TFT标志物的图片进行智能裁剪
    #向app->java->car.bkrc.com.car2021->Utils->QRCode软件包添加QRBitmapCutter类对包含彩色二维码的图片进行智能识别

### **注意!该项目现已升级到2.0,本项目将只进行基础性维护与BUG修复(不一定进行特性同步!!!).所有日志也已迁往至2.0项目**

## 版本号: V1.6

### 2022/12/13 - 12/14

    #重新格式化FirstActivity
    #移除FirstActivity中包含百度easyDL的所有代码
    #重新格式化ConnectTransport类
    #移除ConnectTransport类中包含的easyDL代码
    ****如需参考如何使用,请选择版本低于1.5.5的项目查看****
    #向ConnectTransport中添加了几个TODO注释,以注意需要继续优化的模块以及需要注意的事项
    #向build.gradle(app)中添加

```groovy
implementation 'org.tensorflow:tensorflow-lite:2.4.0'
implementation 'org.tensorflow:tensorflow-lite-gpu:2.4.0'
implementation files('libs/Yolov5-tflite-TSDetector.aar')
```

    #向build.gradle(app)中移除easyDL模块引用
    #向assets文件夹中添加class.txt,yolov5s-fp16.tflite文件
    #向app->java->car.bkrc.com.car2021->Utils->中添加TrafficSigns软件包
    #向TrafficSigns中添加Yolov5_tflite_TSDetector类以识别交通标志物
    #向app->java->car.bkrc.com.car2021->Utils->中添加VID软件包
    #升级build.gradle(app)中的

```groovy
implementation 'com.github.jenly1314.WeChatQRCode:opencv:1.2.0'
implementation 'com.github.jenly1314.WeChatQRCode:opencv-armv64:1.2.0'
implementation 'com.github.jenly1314.WeChatQRCode:wechat-qrcode:1.2.0'
```

    #重新支持arm64-v8a架构

## 版本号: V1.5.5

### 2022/11/21

    # 移除easyDL相关模块
    # 注释easyDL相关代码

## 版本号: V1.5.4

### 2022/11/10

    #修改Shape_test()方法名为Shape_mod()
    #向Shape_mod()添加简易防干扰翻页语句,***早期测试中***
    #对PlateDetector类进行大量修改润色
    #继续向ColorHSV类添加数据

### 2022/11/11

    #修改模块参数,提高各模块精度
    #重新向app->src->main文件夹中添加assets文件夹,以存放BaiDuOCR模型文件,解决一些读取错误
    ****重要！****
      当前无法处理绿色新能源车牌的文字识别!需要紧急处理!
      形状识别模块参数需要继续调整

## 版本号: V1.5.3

### 2022/11/9

    #向app->java->car.bkrc.com.car2021->Utils->中添加PlateDetector类,以结合openCV库定位车牌,识别车牌种类和车牌号
    #向ConnectTransport类添加plate_mod_branch3()方法以调用PlateDetector类
    #向ColorHSV中添加包含车牌识别的新数据
    #向ShapeIdentify类添加识别结果统计,重写ShapeCount(形状统计对象)类

## 版本号: V1.5.2

### 2022/11/8

    #完善图形识别类ShapeIdentify
    #向app->java->car.bkrc.com.car2021->Utils中添加ColorHSV类以存放色彩数据
    #调整ColorHSV类中的数据以更好地识别形状
    #向形状识别类添加注释
    #向app->java->car.bkrc.com.car2021->Utils->Shape软件包中添加ShapeCount以统计形状识别结果
    #完善ConnectTransport类的Q2()方法以更好地完成比赛

## 版本号: V1.5.1

### 2022/11/7

    #修复一些对象传入问题
    #向新写项目添加更多注释,规范化部分旧注释的格式

## 版本号: V1.5

### 2022/11/5---11/6-0:50

    #移除

```groovy
implementation project(path: ':openCVLibrary453')
```

      并添加

```groovy
implementation 'com.github.jenly1314.WeChatQRCode:opencv:1.0.0'
implementation 'com.github.jenly1314.WeChatQRCode:wechat-qrcode:1.0.0'
```

      以使用基于微信开源的二维码识别项目模块
    #移除openCVLibrary453库减少项目体积,现已用'com.github.jenly1314.WeChatQRCode:opencv:1.0.0'代替

## 版本号 : V1.4.1

### 2022/11/3-下午

    #向app->java->car.bkrc.com.car2021->DataProcessingModule软件包添加CrashHandler类以捕获全局异常
    #继续完善RightModuleFragment类以增强用户体验

## 版本号: V1.4

### 2022/11/2

        #向FirstActivity的mOnNavigationItemSelectedListener(底部导航视图)方法添加相关代码

```java
        case R.id.module_page_item:
        mLateralViewPager.setCurrentItem(4);
        return true;
```

        #修改FirstActivity中onCreate()的

```java
        viewPager.setOffscreenPageLimit(4);     //3->4
```

        #向FirstActivity中setupViewPager()添加

```java
        adapter.addFragment(RightModuleFragment.getInstance());
```

        #向app->java->car.bkrc.com.car2021->FragmentView软件包添加RightModuleFragment类
        #向app->java->car.bkrc.com.car2021->ViewAdapter软件包添加Module_Landmark类,ModuleAdapter类
        #向res->drawable添加module_page_item.xml文件
        #向res->layout添加module_item.xml,right_module_fragment.xml
        #向res->menu->navigation_menu.xml添加item: 模块测试

### 2022/11/3-上午

        #完善RightModuleFragment类中的功能
        #完善ModuleAdapter类中的功能
        #修改FirstActivity的onOptionsItemSelected()方法中的红绿灯识别测试模块为全安卓控制方案

## 版本号: V1.3.3

### 2022/10/31

        #规范化Readme.md书写
        #向easyDL()添加基础翻页功能
        #微调部分参数

## 版本号: V1.3.2

### 2022/10/25

        #升级openCV库模块至4.5.3
        #提高各个build.gradle文件中的minSdkVersion至21
            目前项目文件最低要求使用Android5.0
        #修改QR_mod()使用的二维码识别方法,目前使用的是openCV4.5+提供的二维码识别方法

```Java
        Btmp=bitmap2Gray(LeftFragment.bitmap);
        QRCodeDetector qrCodeDetector=new QRCodeDetector();
        Mat mat=new Mat();
        Utils.bitmapToMat(Btmp,mat);
        qrStr=qrCodeDetector.detectAndDecode(mat);
```

## 版本号: V1.3.1

### 2022/10/21

        #修改app->java->car.bkrc.com.car2021->Utils->test软件包名为Shape
        #重写图形识别测试类为ShapeIdentify,重命名识别方法为Identify(),旧类名为ShapeIdentify_test并与方法一同包含在ShapeIdentify类中,不推荐使用
        #重命名红绿灯图片处理模块ColorRecognition类名为ColorProcess 
        #更新FirstActivity类中部分方法的位置,使其阅读时更加自然
        #更新ConnectTransport类中部分方法的位置,使其阅读时更加自然 
        #修改ConnectTransport类中的部分方法名,使其更易于区分新旧,同时添加相关注释

### 2022/10/22

        #添加全安卓控制的Q2()方法

## 版本号: V1.3

### 2022/10/20

        #导入openCV3.4.6模块->openCVLibrary346 
            从文件->新建->Import Module中使用绝对路径(复制到的openCVSDK路径)导入到项目中
            向build.gradle(app)中添加

```groovy
implementation project(':openCVLibrary346')
```

        #向FirstActivity添加inLoadOpenCV()方法初始化OpenCV库
    `   #添加使用OpenCV库的红绿灯识别类TrafficLight_fix,同时弃用原红绿灯识别类TrafficLight

## 版本号: V1.2.2

### 2022/10/9

        #向所有文件添加注释

## 版本号: V1.2.1

### 2022/9/27

        #向AndroidManifest.xml添加声明
            tools:node="replace"
            代表此节点优先级较高,将替换其它第三方库中的相同节点 修复了应用图标不显示的bug 
        #向旧的ConnectTransport1添加注解@Deprecated声明该类为废弃类
        #暂时注释串口通讯线程SerialRunnable方法的部分语句 

```java
        try{Thread.sleep(1);}
        catch(InterruptedException e){e.printStackTrace();}
```

            因为这样做会导致线程处于忙等待,导致程序资源开销极大

## 版本号: V1.2

### 2022/9/26

        #提取百度easyDL图像分类项目模块至jniLibs/EasyDL-Mod-debug.aar 
        #注释部分重复的easyDL图像分类项目模块代码
        #向ConnectTransport类的autoDrive()方法添加自定义发送指令 测试二维码解密读取RFID卡有效数据地址并向主车发送

## 版本号: V1.1

### 2022/9/22

        #提取百度OCR文字识别项目模块至jniLibs/BaiDuOCR-Mod-debug.aar 
        #删除冗余重复的百度OCR文字识别项目模块代码 
        #修改部分类名以匹配功能

## 版本号: V1.0

### 2022/9/22

        #继续向项目添加注释

## 版本号: betaV0.4.1

### 2022/8/25

        #修改RightFragment1中的rehHandler方法,添加自定义信息指令接收 
        #重写autoDrive(),测试全自动方法Q1 
        #修改模块参数,提高精度

### 2022/8/26

        #为autoDrive()大部分指令添加

```java
        for(int J=0;J< 3;J++)
```

            防止主车zigbee模块未处理安卓传入的指令
        #*****添加车牌防干扰模块*****
            早期测试中

### 2022/8/27

        #添加RGB转HSV的类进行测试

### 2022/8/29

        #向Utils添加QuanZiDong1类,准备简化autoDrive()方法

### 2022/8/30

        #向右上角菜单添加重置全自动mark值,方便无需重启应用即可开启下一次全自动执行

## 版本号: betaV0.4

### 2022/8/22

        #重写ConnectTransport类,提高可读性 
        #向ConnectTransport类中添加注释

### 2022/8/23

        #修复无法切换接收主从车状态的BUG 
        #向所有可能需要理解的类添加注释 
        #检查并统一所有代码样式,解决部分拼写错误

## 版本号: alphaV0.3

### 2022/8/18

        #增加在项目的gradle.properties的jvmargs所需内存，现在为-Xmx4608M 
        #在AndroidManifest添加允许用户旋转屏幕的配置:
            android:configChanges="orientation|screenSize|keyboardHidden"
        #在FirstActivity中的onCreate()中添加 

```java
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
```

            以允许用户旋转屏幕 
        #在FirstActivity中添加重写方法onConfigurationChanged(),以允许用户旋转屏幕后不重新绘制Activity

### 2022/8/19

        #在AndroidManifest添加允许管理用户所有文件的权限:
            <uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" tools:ignore="ScopedStorage"/>
        #在FirstActivity中添加了有关百度easyDL图像分类的字段 
        #向FirstActivity中添加初始化easyDL模型的语句,相关更改已在相关位置进行了注释
        #向app->java->car.bkrc.com.car2021->Utils中添加EasyDL文件夹以存放百度easyDL图像分类的关键类
        #向assets文件夹中添加demo,infer两个有关百度easyDL的图像分类资源文件夹
        #在项目视图中的app->libs中添加easyedge-sdk.jar,arm64-v8a,armeabi-v7a三个文件 
        #取消build.gradle(app)中"arm64-v8a"的支持,因为一旦运行图像识别模块将会导致程序崩
        #临时更改应用包名为"car.bkrc.com.car2021"以允许激活百度easyDL图像分类模块
        #向app的build.gradle(app)中添加了
            1. implementation files('libs/easyedge-sdk.jar')
            以导入easyDL的交通标志物识别SDK
            2. sourceSets { 
                main { jniLibs.srcDirs = ['libs']} 
                }
            重定向jinLibs为libs路径
            3. dexOptions { 
                    javaMaxHeapSize "4g"
                }
            增加Java虚拟内存大小,提高编译速度 
            4. packagingOptions { 
                    //pickFirst 'lib/arm64-v8a/libc++_shared.so' 
                    pickFirst 'lib/armeabi-v7a/libc++_shared.so' 
                }
            防止资源冲突
        #提高两个build.gradle中compileSdkVersion,targetSdkVersion等级至28
        #提高两个build.gradle中minSdkVersion等级至19 
        #移除了在local.properties中的cmake路径
        *****$$$$$-----#####-----alpha测试结束,接下来将对整个项目的模块配合进行测试-----#####-----$$$$$*****

## 版本号: alphaV0.2

### 2022/8/15

        #向app->java->car.bkrc.com.car2021->Utils中添加baidu.OCR软件包与相关Java类以识别项目以识别车牌
        #在项目视图app->src->main文件夹中添加assets文件夹
        #向assets文件夹中添加images,labels,models.ocr_v2_for_cpu,ocr_v2_for_cpu文件夹
        #当前assets文件夹中只包含百度OCR文字识别资源
        #向app->java->car.bkrc.com.car2021->ActivityView->FirstActivity添加onLoadModel()方法初始化百度OCR文字识别模型,相关注释已写在相应位置
        #向app中添加cpp软件包,这是有关百度OCR文字识别的关键C语言文件
        #向res->values->strings.xml中添加百度OCR所使用的string资源 
        #向app的build.gradle(app)中添加了对C文件支持的语句,相关更改已在相关位置进行了注释
        #移除了对armeabi设备的支持,因为百度OCR识别项目不支持该设备
        #移除了项目的gradle.properties中android.useDeprecatedNdk=true命令行,因为在将来版本ndk不再被支持使用,需要使用Cmake或ndk-bundle
        #在local.properties中添加了cmake的路径,cmake使用的版本为3.10.2.4988404
        #在gradle-wrapper.properties中升级了构建版本,当前的构建版本为gradle-6.5.1 
        #在项目结构中升级了AGP的版本，当前版本为4.1.3
        ****重要!****
            注意OCRPredictorNative类中的init(),forward(),release()三个方法,相关注释已写在相应位置

### 2022/8/16

        #向app->java->car.bkrc.com.car2021->Utils中添加test文件夹对某些新增的类进行测试 
        #添加形状识别类,识别图形形状,图形颜色,图形个数————早期测试中

## 版本号: alphaV0.1

### 2022/8/13

        #向app->java->car.bkrc.com.car2021->ActivityView中添加注释
        #优化部分代码

### 2022/8/14

        #向app->java->car.bkrc.com.car2021->Utils中添加TrafficLight软件包
        #向TrafficLight软件包添加TrafficLight类和ColorRecognition类,TrafficLight类处理红绿灯识别结果,ColorRecognition类处理传入图片
        #重写红绿灯识别类的识别方法,提高红绿灯识别的准确率

# 其他资料:

* [Android的Context](https://www.jianshu.com/p/57220504efd2)
* 有关AGP与gradle版本之间的问题请前往[这里](https://developer.android.google.cn/studio/releases/gradle-plugin.html)
* 全局异常捕获处理可以前往[这里](https://www.jianshu.com/p/9b2f43d87c9f)
  ,或者[这里](https://blog.csdn.net/shankezh/article/details/79332004)
  ,以及[这里](https://blog.csdn.net/cqn2bd2b/article/details/126435256)
* 了解RecyclerView如何使用请前往[这里](https://www.jianshu.com/p/0bd4bc12c170)
  或者[这里](https://blog.csdn.net/qq_29882585/article/details/108818849)
* 有关WeChatQRCode的学习资料可参阅[这里](https://www.wanandroid.com/blog/show/3041)
* [morphologyEx(形态学操作)](https://www.jianshu.com/p/ee72f5215e07)