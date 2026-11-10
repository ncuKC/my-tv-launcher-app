package ru.mytv.launcher;
import android.content.Context;
import android.graphics.*;
import android.os.*;
import android.widget.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Scenes extends FrameLayout {
 final Handler ui=new Handler(Looper.getMainLooper());final Random random=new Random();final ImageView photo;final TextView clock;final boolean onlyClock;
 volatile boolean running;volatile HttpURLConnection connection;ExecutorService pool;int index;
 final String[] urls={"https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=1920&h=1080&q=80","https://images.unsplash.com/photo-1470770841072-f978cf4d019e?auto=format&fit=crop&w=1920&h=1080&q=80","https://images.unsplash.com/photo-1501785888041-af3ef285b470?auto=format&fit=crop&w=1920&h=1080&q=80"};
 public Scenes(Context c,boolean clocks){super(c);onlyClock=clocks;setBackgroundColor(Color.BLACK);photo=new ImageView(c);photo.setScaleType(ImageView.ScaleType.CENTER_CROP);addView(photo,new LayoutParams(-1,-1));clock=new TextView(c);clock.setTextSize(64);clock.setTextColor(0xffbbbbbb);addView(clock,new LayoutParams(-2,-2));index=random.nextInt(urls.length);}
 final Runnable move=new Runnable(){public void run(){if(!running)return;clock.setText(android.text.format.DateFormat.format("HH:mm",new Date()));clock.measure(MeasureSpec.makeMeasureSpec(getWidth(),MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(getHeight(),MeasureSpec.AT_MOST));int x=Math.max(0,getWidth()-clock.getMeasuredWidth()-40),y=Math.max(0,getHeight()-clock.getMeasuredHeight()-40);clock.animate().translationX(20+random.nextInt(x+1)).translationY(20+random.nextInt(y+1)).setDuration(1200).start();ui.postDelayed(this,15000);}};
 final Runnable next=new Runnable(){public void run(){if(!running)return;load();ui.postDelayed(this,60000);}};
 void start(){if(running)return;running=true;pool=Executors.newSingleThreadExecutor();ui.post(move);if(!onlyClock)ui.post(next);}
 void stop(){running=false;ui.removeCallbacksAndMessages(null);if(connection!=null)connection.disconnect();if(pool!=null)pool.shutdownNow();photo.animate().cancel();clock.animate().cancel();photo.setImageDrawable(null);}
 void load(){final String url=urls[index++%urls.length];pool.submit(()->{Bitmap result=null;HttpURLConnection c=null;try{
  c=(HttpURLConnection)new URL(url).openConnection();connection=c;c.setConnectTimeout(10000);c.setReadTimeout(10000);if(c.getResponseCode()!=200)throw new IOException("HTTP "+c.getResponseCode());
  ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] buf=new byte[8192];int n,total=0;while((n=in.read(buf))!=-1){total+=n;if(!running||total>8388608)throw new IOException("Cancelled or too large");out.write(buf,0,n);}}
  byte[] bytes=out.toByteArray();BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);if(options.outWidth<1||options.outHeight<1)throw new IOException("Invalid image");options.inSampleSize=1;while(options.outWidth/options.inSampleSize>1920||options.outHeight/options.inSampleSize>1080)options.inSampleSize*=2;options.inJustDecodeBounds=false;options.inPreferredConfig=Bitmap.Config.RGB_565;result=BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
 }catch(Exception e){android.util.Log.w("MyTV","Landscape unavailable",e);}finally{if(c!=null)c.disconnect();connection=null;}
 final Bitmap bitmap=result;ui.post(()->{if(!running){if(bitmap!=null)bitmap.recycle();return;}photo.animate().cancel();if(bitmap==null){photo.setImageDrawable(null);clock.setVisibility(VISIBLE);}else{photo.setAlpha(0);photo.setImageBitmap(bitmap);photo.animate().alpha(1).setDuration(1500).start();clock.setVisibility(GONE);}});});}
}
