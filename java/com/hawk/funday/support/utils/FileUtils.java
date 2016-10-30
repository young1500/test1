package com.hawk.funday.support.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import org.aisen.android.common.utils.Logger;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * @Description: FileUtils 文件处理工具类
 * @author  qiangtai.huang
 * @date  2016/8/23
 * @copyright TCL-HAWK
 */
public class FileUtils {

    public boolean fileIsExists(String filePath){
        try{
            File f=new File(filePath);
            if(!f.exists()){
                return false;
            }

        }catch (Exception e) {
            return false;
        }
        return true;
    }
    /**
      * @Description:  文件转字节数组
      * @params filePath 文件路径
      * @return  
      */
    
    public  byte[] file2byte(String filePath)
    {
        byte[] buffer = null;
        try
        {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1)
            {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return buffer;
    }
    public long getFileLength( String filePath){
        File file=new File(filePath);
        if (file.exists()&&file.isFile())
        {
            return file.length();
        }else {
           return 0;
        }
    }

    public  void byte2File(byte[] buf, String filePath, String fileName)
    {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try
        {
            File dir = new File(filePath);
            if (!dir.exists() && dir.isDirectory())
            {
                dir.mkdirs();
            }
            file = new File(filePath + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bos != null)
            {
                try
                {
                    bos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            if (fos != null)
            {
                try
                {
                    fos.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    private byte [] compressBitmapWithMaxSize(Bitmap bitmap, long max,Bitmap.CompressFormat format){
        int quality = 100;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(format,quality,byteArrayOutputStream);
        while (byteArrayOutputStream.toByteArray().length > max){
            byteArrayOutputStream.reset();
            quality = quality -10;
            if (quality<1)
                quality=1;
            else if (quality>100)
                   quality=100;
            bitmap.compress(format,quality,byteArrayOutputStream);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
      * @Description:
      * @param srcPath 文件路径
     * @param  hh  预期高度
     * @param  ww  预期宽度
     * @param  isWH 是否以高宽两个维度进行压缩
      * @return
      */

    public Bitmap getBitmap(String srcPath,float hh,float ww,boolean isWH){
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);//此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        int width = newOpts.outWidth;
        int height = newOpts.outHeight;
        Logger.e("getBitmap","图片原始宽度："+width+"高度："+height);
        float be = 1;//be=1表示不缩放
        if (isWH) {
            if (width > height && width > ww) {//如果宽度大的话根据宽度固定大小缩放
                be = (width / ww);
            } else if (width < height &&height > hh) {//如果高度高的话根据宽度固定大小缩放
                be = (height / hh);
            }
        }else  if (width > ww){
            be = width / ww;
        }
        if (be <= 0)
            be = 1;
        BigDecimal bigDecimal= new BigDecimal(be);
        newOpts.inSampleSize= bigDecimal.setScale(0,BigDecimal.ROUND_HALF_UP).intValue();



        //重新读入图片此时已经把options.inJustDecodeBounds 设回false
        try {
            //bitmap = BitmapFactory.decodeFile(srcPath,newOpts); ///decodeFIle 比decodeStream 产生null概率高
            bitmap =BitmapFactory.decodeStream(new FileInputStream(new File(srcPath)),null,newOpts);
        } catch (Exception e){
            e.printStackTrace();
            bitmap=null;
        }catch (OutOfMemoryError error)
        {
            error.printStackTrace();
            try{
                newOpts.inSampleSize= newOpts.inSampleSize*2;
                bitmap =BitmapFactory.decodeStream(new FileInputStream(new File(srcPath)),null,newOpts);
            }catch (OutOfMemoryError er){

            }catch (FileNotFoundException e1){
                e1.printStackTrace();
                bitmap=null;
            }
        }
        if (bitmap!=null)
            Logger.e("getBitmap","图片压缩后宽度："+bitmap.getWidth()+"高度："+bitmap.getHeight());
        return bitmap;
    }
    private  long calculatedByteCount(int width,int height,Bitmap.Config config){
        long result=width*height;
        if (config.equals( Bitmap.Config.ARGB_4444)||config.equals(Bitmap.Config.RGB_565))
        {
            result=result*2;
        }else if (config.equals( Bitmap.Config.ARGB_8888))
        {
            result=result*4;
        }
        return  result+8;
    }
    public byte[] compressImage(String imgPath){
        int maxSize=2097000;///1024*1024*2=2M
        Bitmap bitmap=getBitmap(imgPath,720f,720f,false);
        if(bitmap==null)
            return null;
        byte[] result= compressBitmapWithMaxSize(bitmap,maxSize,getPictureType(imgPath));
        if (bitmap!=null &&!bitmap.isRecycled()) {
            bitmap.recycle();

        }
        return result;
    }
    public Bitmap.CompressFormat getPictureType(String imgPath)
    {
        String tmp=imgPath.toLowerCase();
        if (tmp.endsWith("png"))
            return Bitmap.CompressFormat.PNG;
        else if (tmp.endsWith("jpg")||tmp.endsWith("jpeg"))
            return Bitmap.CompressFormat.JPEG;
        else
            return Bitmap.CompressFormat.WEBP;
    }
    public   Bitmap small(Bitmap bitmap,float maxSize) {
        Matrix matrix = new Matrix();
        int w=bitmap.getWidth();
        int h=bitmap.getHeight();
        float scale=w>h?maxSize/w:maxSize/h;
        matrix.postScale(scale,scale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap,0,0,w,h,matrix,true);
        return resizeBmp;
    }
    /**
     * @param bitmap      原图
     * @param edgeLength  希望得到的正方形部分的边长
     * @return  缩放截取正中部分后的位图。
     */
    public  Bitmap centerSquareScaleBitmap(Bitmap bitmap, int edgeLength)
    {
        if(null == bitmap || edgeLength <= 0)
        {
            return  null;
        }
        Bitmap result = bitmap;
        int widthOrg = bitmap.getWidth();
        int heightOrg = bitmap.getHeight();
        /////原图中心点
       /* int centerX=widthOrg/2;
        int centerY=heightOrg/2;*/
        /////重新计算边长：如果原长宽都大于预期长度取预期长度，如果长宽有一条小于预期长度，取长宽中最小值
        edgeLength=widthOrg<heightOrg?(widthOrg>edgeLength?edgeLength:widthOrg):(heightOrg>edgeLength?edgeLength:heightOrg);
        /////截取的图片起始坐标
        int newStartX=(widthOrg-edgeLength)/2;
        int newStartY=(heightOrg-edgeLength)/2;
        try{
            result = Bitmap.createBitmap(bitmap, newStartX<0?0:newStartX, newStartY<0?0:newStartY, edgeLength, edgeLength);
        }
        catch(Exception e){
            return null;
        }
        return result;
    }
}
