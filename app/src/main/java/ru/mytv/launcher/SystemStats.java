package ru.mytv.launcher;
import android.app.ActivityManager;
import android.os.*;
import android.widget.TextView;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class SystemStats {
 final MainActivity a;final ExecutorService worker=Executors.newSingleThreadExecutor();final Handler ui=new Handler(Looper.getMainLooper());TextView view;boolean busy;long prevTotal=-1,prevIdle;long wall=SystemClock.elapsedRealtime(),cpu=android.os.Process.getElapsedCpuTime();String value="RAM / CPU — измерение…";
 SystemStats(MainActivity activity){a=activity;}
 void attach(TextView v){view=v;v.setText(value);}
 void sample(){if(busy)return;busy=true;worker.submit(()->{String result;try{ActivityManager.MemoryInfo m=new ActivityManager.MemoryInfo();((ActivityManager)a.getSystemService(android.content.Context.ACTIVITY_SERVICE)).getMemoryInfo(m);long used=Math.max(0,m.totalMem-m.availMem);String ram=m.totalMem>0?String.format(Locale.getDefault(),"RAM %.0f%% · %.2f / %.2f ГБ",100.0*used/m.totalMem,used/1073741824.0,m.totalMem/1073741824.0):"RAM —";long now=SystemClock.elapsedRealtime(),process=android.os.Process.getElapsedCpuTime();double app=100.0*Math.max(0,process-cpu)/Math.max(1,now-wall)/Math.max(1,Runtime.getRuntime().availableProcessors());wall=now;cpu=process;String load=String.format(Locale.getDefault(),"CPU приложения %.1f%%",Math.min(100,app));try(BufferedReader reader=new BufferedReader(new FileReader("/proc/stat"))){String[] p=reader.readLine().trim().split(" +");if(!p[0].equals("cpu")||p.length<9)throw new IOException();long total=0;for(int i=1;i<=8;i++)total+=Long.parseLong(p[i]);long idle=Long.parseLong(p[4])+Long.parseLong(p[5]);if(prevTotal>=0&&total>prevTotal){double percent=100.0*(1.0-(double)(idle-prevIdle)/(total-prevTotal));load=String.format(Locale.getDefault(),"CPU приставки %.0f%%",Math.max(0,Math.min(100,percent)));}prevTotal=total;prevIdle=idle;}catch(Exception ignored){}result=ram+"     |     "+load;}catch(Exception e){result="RAM / CPU — данные недоступны";}final String text=result;ui.post(()->{busy=false;if(a.isDestroyed())return;value=text;if(view!=null&&a.live())view.setText(text);});});}
 void close(){worker.shutdownNow();ui.removeCallbacksAndMessages(null);}
}
