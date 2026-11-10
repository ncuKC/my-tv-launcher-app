package ru.mytv.launcher;
import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;

public class StartupGate {
 final MainActivity a;final Handler ui=new Handler(Looper.getMainLooper());boolean showing;FrameLayout layer;TextView countdown;long deadline;
 StartupGate(MainActivity activity){a=activity;}
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("startup",0);}
 boolean showIfNeeded(){int boot;try{boot=Settings.Global.getInt(a.getContentResolver(),Settings.Global.BOOT_COUNT,-1);}catch(Exception e){return false;}if(boot<0)return false;SharedPreferences p=prefs(a);if(p.getInt("seenBoot",-1)==boot)return false;p.edit().putInt("seenBoot",boot).apply();int seconds=p.getInt("seconds",15);if(seconds<=0||SystemClock.elapsedRealtime()>180000)return false;
  showing=true;deadline=SystemClock.elapsedRealtime()+Math.min(30,seconds)*1000L;layer=new FrameLayout(a);layer.setBackgroundColor(0xff090d18);layer.setClickable(true);LinearLayout box=new LinearLayout(a);box.setOrientation(1);box.setGravity(Gravity.CENTER);box.setPadding(a.dp(32),a.dp(24),a.dp(32),a.dp(24));box.addView(a.text("Подготовка к работе…",28));ProgressBar bar=new ProgressBar(a,null,android.R.attr.progressBarStyleHorizontal);bar.setIndeterminate(true);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(a.dp(280),a.dp(16));bp.setMargins(0,a.dp(24),0,a.dp(20));box.addView(bar,bp);countdown=a.text("",14);box.addView(countdown);TextView skip=a.button("Пропустить",()->finish(true));LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(a.dp(200),a.dp(48));sp.topMargin=a.dp(20);box.addView(skip,sp);FrameLayout.LayoutParams center=new FrameLayout.LayoutParams(-2,-2,Gravity.CENTER);layer.addView(box,center);a.addContentView(layer,new ViewGroup.LayoutParams(-1,-1));skip.requestFocus();ui.post(pulse);return true;
 }
 final Runnable pulse=new Runnable(){public void run(){if(!showing)return;long remaining=deadline-SystemClock.elapsedRealtime();if(remaining<=0){finish(true);return;}countdown.setText("До рабочего экрана: "+((remaining+999)/1000)+" с · OK — пропустить");ui.postDelayed(this,250);}};
 void finish(boolean notify){if(!showing)return;showing=false;ui.removeCallbacksAndMessages(null);if(layer!=null&&layer.getParent() instanceof ViewGroup)((ViewGroup)layer.getParent()).removeView(layer);if(notify)a.startupFinished();}
 static void settings(MainActivity a){int seconds=prefs(a).getInt("seconds",15);new AlertDialog.Builder(a).setTitle("Настройки «Мой ТВ»").setItems(new String[]{"Настройки приставки","Экран запуска: "+(seconds==0?"выключен":seconds+" с"),"Настройки заставки","Обновления"},(d,n)->{if(n==0)a.open(new Intent(Settings.ACTION_SETTINGS));else if(n==1)choose(a);else if(n==2)SaverOptions.show(a);else a.updates.check(true);}).setNegativeButton("Закрыть",null).setOnDismissListener(d->a.resetIdle()).show();}
 static void choose(MainActivity a){int[] values={0,5,10,15,20,30};String[] labels={"Выключен","5 секунд","10 секунд","15 секунд","20 секунд","30 секунд"};int selected=0,current=prefs(a).getInt("seconds",15);for(int i=0;i<values.length;i++)if(values[i]==current)selected=i;new AlertDialog.Builder(a).setTitle("Ожидание после перезагрузки").setSingleChoiceItems(labels,selected,(d,n)->{prefs(a).edit().putInt("seconds",values[n]).apply();d.dismiss();settings(a);}).setNegativeButton("Назад",(d,n)->settings(a)).show();}
}
