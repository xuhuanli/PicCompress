package com.xuhuanli.piccompress;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /**
     * @return
     * @Describe 如果不做任何处理 直接调用setImage方法，在大部分低端测试机上系统会直接报OOM
     * @Author xuhuanli
     * @params
     * @Date 2018/5/31 14:04
     */
    /*public void onClick(View view) {
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.test_pic));
    }*/
    public void onClick(View view) {
        ImageView imageView = findViewById(R.id.imageView);
        Bitmap bm = decodeBitmap(getResources(), R.drawable.test_pic, imageView);
        Log.d("TAG", bm.getAllocationByteCount() + "");
        imageView.setImageBitmap(bm);
    }

    /**
     * @return
     * @Describe 对Bm二次压缩 压缩图片质量 ，注意的是质量压缩的特点是图片文件大小会减小，但是图片的像素数不会改变，加载压缩后的图片，占据的内存不会减少
     * @Author xuhuanli
     * @params
     * @Date 2018/5/31 15:56
     */
    public void onClick2(View view) {
        ImageView imageView = findViewById(R.id.imageView);
        Bitmap bitmap = decodeBitmap(getResources(), R.drawable.test_pic, imageView);
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        //主要参数是compress的第二个quality 0-100 0压缩度最大 图片失真严重  100表示不进行压缩
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, arrayOutputStream);
        Bitmap bm = BitmapFactory.decodeByteArray(arrayOutputStream.toByteArray(), 0, arrayOutputStream.toByteArray().length);
        Log.d("TAG", bm.getAllocationByteCount() + "");
        imageView.setImageBitmap(bm);
    }

    /**
     * @return
     * @Describe
     * @Author xuhuanli
     * @params
     * @Date 2018/5/31 15:34
     */
    private Bitmap decodeBitmap(Resources res, int resId, View view) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(getResources(), R.drawable.test_pic, options);
        options.inSampleSize = setSampleSize(options, view);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(getResources(), R.drawable.test_pic, options);
    }

    private void configBitmap() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //是否只取边界
        options.inSampleSize = 1; //采样尺寸 这个值最好根据控件的宽高计算出来 比如我这边的ImageView是200x200(dp)
    }

    /**
     * @return
     * @Describe 计算采样尺寸
     * @Author xuhuanli
     * @params
     * @Date 2018/5/31 14:20
     */
    private int setSampleSize(BitmapFactory.Options options, View view) {
        int outWidth = options.outWidth;
        int outHeight = options.outHeight;
        //因为是在OnClick方法里调用getWidth 此时view已经添加到了窗口 所以getWidth不会return 0 如果在OnCreat里面调用，需要注意一下
        int viewWidth = view.getWidth();
        int viewHeight = view.getHeight();
        Log.d("TAG", viewWidth + "...." + viewHeight);
        //if原图大于控件尺寸 折半处理
        return MainActivity.mathHalf(outWidth, outHeight, viewWidth, viewHeight, 1);
    }

    /**
     * @return 返回sampleSize大小
     * @Describe
     * @Author xuhuanli
     * @params w1, h1 原图的宽高 w2 h2 控件的宽高  sampleSize 初始值 =1
     * @Date 2018/5/31 14:37
     */
    public static int mathHalf(int w1, int h1, int w2, int h2, int sampleSize) {
        if (w1 <= w2 && h1 <= h2) {
            return sampleSize;
        } else {
            sampleSize *= 2;
            return mathHalf(w1 / 2, h1 / 2, w2, h2, sampleSize);
        }
    }
}
