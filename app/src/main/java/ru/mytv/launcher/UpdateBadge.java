package ru.mytv.launcher;
import android.content.*;
import android.content.pm.*;
import android.os.*;
import android.widget.TextView;
import java.util.concurrent.*;
import org.json.*;

public class UpdateBadge {
 final MainActivity a;final ExecutorService worker=Executors.newSingleThreadExecutor();final Handler ui=new Handler(Looper.getMainLooper());final SharedPreferences prefs;TextView view;long last;boolean busy,available;
 UpdateBadge(MainActivity activity){a=activity;prefs=a.getSharedPreferences("badge",0);try{available=prefs.getLong("version",0)>Updates.version(a.getPackageManager().getPackageInfo(a.getPackageName(),0));}catch(Exception ignored){}}
 void attach(TextView v){view=v;paint();}
 void paint(){if(view!=null){view.setText(available?"↑ Обновления":"Обновления");view.setTextColor(available?0xffb4edba:0xffffffff);view.setContentDescription(available?"Доступно обновление. Нажмите для установки":"Проверить обновления");}}
 void check(){long now=SystemClock.elapsedRealtime();if(busy||a.getPackageName().endsWith(".test")||(last!=0&&now-last<1200000))return;busy=true;last=now;worker.submit(()->{long found=-1;try{JSONObject r=new JSONObject(new String(Updates.fetch("https://api.github.com/repos/"+Updates.REPO+"/releases/latest",1048576),java.nio.charset.StandardCharsets.UTF_8));String tag=r.getString("tag_name");if(tag.startsWith("v"))tag=tag.substring(1);String[] p=tag.split("[.]");if(p.length!=3)throw new Exception("Bad version");int major=Integer.parseInt(p[0]),minor=Integer.parseInt(p[1]),patch=Integer.parseInt(p[2]);if(major<0||minor<0||minor>99||patch<0||patch>99)throw new Exception("Bad version");long code=(long)major*10000+minor*100+patch;long own=Updates.version(a.getPackageManager().getPackageInfo(a.getPackageName(),0));boolean apk=false;JSONArray assets=r.getJSONArray("assets");for(int i=0;i<assets.length();i++)if("My-TV.apk".equals(assets.getJSONObject(i).getString("name")))apk=true;found=apk&&code>own?code:0;}catch(Exception ignored){}final long result=found;ui.post(()->{busy=false;if(a.isDestroyed())return;if(result>=0){prefs.edit().putLong("version",result).apply();available=result>0;paint();}});});}
 void close(){worker.shutdownNow();ui.removeCallbacksAndMessages(null);}
}
