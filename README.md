# Android Bitmap的简单压缩 #

## 官方更推荐Glide去获取 解码，加载，图片 ##

### 如果不做任何处理 直接调用setImage方法，在大部分低端测试机上系统会直接报OOM 

    /*public void onClick(View view) {
        ImageView imageView = findViewById(R.id.imageView);
        imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.test_pic));
    }*/

### 对图片进行尺寸压缩
	
	通常我们获取到的图片，比如从相册获取的照片，大小都以M为单位，整个图片的宽高也比较大，如我的这张图片

![](https://i.imgur.com/LF0hNnr.jpg)

尺寸为3880×5184的JPG格式  直接加载进内存就OOM了（724101132的内存需求）

![](https://i.imgur.com/NhxF9vH.jpg)

先对图片做一次大小的压缩，需要加载到App中的ImageView大小设定为200×200DP，转成px大约也是600×600，完全没有必要将这么大的图片完全加载到内存。

	BitmapFactory.Options 可以对Bitmap做出一些参数设置，如果options.inJustDecodeBounds = true
	在解码过程中返回的是一个不加载到内存空的Bitmap，可以获取图片宽高。简单的说就是获取图片的边界。

	options.inSampleSize 表示图片长/宽的大小控制， size = 1 表示长/宽和原图相等， size = 2，表示需要原图的1/（2*2）,即 长的1/2，宽的1/2. 这个值的大小最好根据ImageView的大小来计算。

	
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


在这里用递归反复计算intSampleSize的大小，直到原图的宽高小于等于ImageView的宽高。


有了图片尺寸的约束，现在可以把Bitmap 显示到ImageView了

	public void onClick(View view) {
        ImageView imageView = findViewById(R.id.imageView);
        Bitmap bm = decodeBitmap(getResources(), R.drawable.test_pic, imageView);
        Log.d("TAG", bm.getAllocationByteCount() + "");
        imageView.setImageBitmap(bm);
    }

![](https://i.imgur.com/9KJencj.jpg)

Logcat显示控件宽高为600×600(dp转到了px)，在mathHalf方法里，可以获取到返回的intSampleSize = 16，所以在加载处理后的bitmap时占用的内存为 724101132/（16×16） = 2828520  跟日志的分配大小差不多。


### 对图片进行质量压缩 ###

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

质量压缩需要注意的是，它压缩的是图片存放在硬盘上的大小，而不是在运行时，占用的内存大小，内存大小是不会因为质量压缩减小的。图像的占内存大小根据图像的像素以及它的Bitmap.Config所决定

如图

![](https://i.imgur.com/MMWEYOH.jpg)

图片质量压缩是为了让你在保存图片到手机的时候 可以少占用一些硬盘空间。个人没怎么对图片做过这个压缩

![](https://i.imgur.com/UEOsFwz.jpg)    ![](https://i.imgur.com/uRBTJ7q.jpg)

左边是只经过尺寸压缩 ，右边经过尺寸压缩 又做了一次质量压缩 图像失真严重 。

查看Logcat 发现右图的占内存大小依然和左图一样

![](https://i.imgur.com/gz6Cwln.jpg)

### 另外 ###

图片的压缩还是比较耗时的，最好放到子线程处理，这里仅作演示。

矩阵压缩 调用Bitmap.createBitmap() 里面有一个重载方法 可以传入一个数学矩阵，也可以达到减少占内存效果，实际还是通过矩阵变换将图片进行了尺寸缩小。



