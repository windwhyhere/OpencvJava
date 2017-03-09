#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/imgproc_c.h>

#include <opencv2/core/core.hpp>
using namespace std;
using namespace cv;
Mat * mCanny=NULL;
extern "C" {
jstring
Java_com_yumu_admin_opencvjava_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";

    return env->NewStringUTF(hello.c_str());

}
JNIEXPORT void JNICALL Java_com_yumu_admin_opencvjava_MainActivity_capturevideo (JNIEnv *, jobject){

}
JNIEXPORT void JNICALL Java_com_yumu_admin_opencvjava_MainActivity_framevideo
        (JNIEnv *, jobject){

}
JNIEXPORT jboolean JNICALL Java_com_yumu_admin_opencvjava_CameraPreview_ImageProcessing
        (JNIEnv *env, jobject thiz, jint width, jint height, jbyteArray NV21FrameData, jintArray outPixels){
    jbyte * pNV21FrameData = env->GetByteArrayElements(NV21FrameData, 0);
    jint * poutPixels = env->GetIntArrayElements(outPixels, 0);

    if ( mCanny == NULL )
    {
        mCanny = new Mat(height, width, CV_8UC1);
    }

    Mat mGray(height, width, CV_8UC1, (unsigned char *)pNV21FrameData);
    Mat mResult(height, width, CV_8UC4, (unsigned char *)poutPixels);

    IplImage srcImg = mGray;
    IplImage CannyImg = *mCanny;
    IplImage ResultImg = mResult;

    cvCanny(&srcImg, &CannyImg, 80, 100, 3);
    cvCvtColor(&CannyImg, &ResultImg, CV_GRAY2BGRA);

    env->ReleaseByteArrayElements(NV21FrameData, pNV21FrameData, 0);
    env->ReleaseIntArrayElements(outPixels, poutPixels, 0);
    return true;
}

JNIEXPORT jint JNICALL
Java_com_yumu_admin_opencvjava_MainActivity_main(JNIEnv *env, jobject instance) {

    // TODO
    // 打开视频文件
    cv::VideoCapture capture("/storage/emulated/0/DCIM/VUE/VUE20170218140000.mp4");
    // 检查打开是否成功
    if (!capture.isOpened())
        return 1;
    // 取得帧速率
    double rate = capture.get(CV_CAP_PROP_FPS);
    bool stop(false);
    cv::Mat frame; // 当前视频帧
    cv::namedWindow("Extracted Frame");
    // 根据帧速率计算帧之间的等待时间，单位ms
    int delay = 1000 / rate;
    // 循环遍历视频中的全部帧
    while (!stop) {
        // 读取下一帧（如果有）
        if (!capture.read(frame))
            break;
        cv::imshow("Extracted Frame", frame);
        // 等待一段时间，或者通过按键停止
        if (cv::waitKey(delay) >= 0)
            stop = true;
    }
    // 关闭视频文件
    // 不是必需的，因为类的析构函数会调用
    capture.release();
    return 0;
}
}