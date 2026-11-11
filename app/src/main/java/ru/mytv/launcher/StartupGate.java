package ru.mytv.launcher;
import android.content.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
public class StartupGate {
 final MainActivity a;final Handler ui=new Handler(Looper.getMainLooper());boolean showing;FrameLayout layer;TextView countdown;long deadline;
 StartupGate(MainActivity a){this.a=a;}
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("startup",0);}
 boolean showIfNeeded(){int boot;try{boot=Settings.Global.getInt(a.getContentResolver(),Settings.Global.BOOT_COUNT,-1);}catch(Exception e){return false;}if(boot<0)return false;SharedPreferences p=prefs(a);if(p.getInt("seenBoot",-1)==boot)return false;p.edit().putInt("seenBoot",boot).apply();int seconds=p.getInt("seconds",15);if(seconds<=0||SystemClock.elapsedRealtime()>180000)return false;showing=true;deadline=SystemClock.elapsedRealtime()+Math.min(30,seconds)*1000L;layer=new FrameLayout(a);layer.setBackgroundColor(0xff090d18);layer.setClickable(true);layer.setFocusableInTouchMode(true);LinearLayout box=new LinearLayout(a);box.setOrientation(1);box.setGravity(Gravity.CENTER);box.setPadding(a.dp(32),a.dp(24),a.dp(32),a.dp(24));box.addView(a.text("Подготовка к работе…",28));ProgressBar bar=new ProgressBar(a,null,android.R.attr.progressBarStyleHorizontal);bar.setIndeterminate(true);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(a.dp(280),a.dp(16));bp.setMargins(0,a.dp(24),0,a.dp(20));box.addView(bar,bp);countdown=a.text("",14);box.addView(countdown);layer.addView(box,new FrameLayout.LayoutParams(-2,-2,Gravity.CENTER));a.addContentView(layer,new ViewGroup.LayoutParams(-1,-1));layer.requestFocus();ui.post(pulse);return true;}
 final Runnable pulse=new Runnable(){public void run(){if(!showing)return;long remaining=deadline-SystemClock.elapsedRealtime();if(remaining<=0){finish(true);return;}countdown.setText("До рабочего экрана: "+((remaining+999)/1000)+" с");ui.postDelayed(this,250);}};
 void finish(boolean notify){if(!showing)return;if(notify&&SystemClock.elapsedRealtime()<deadline)return;showing=false;ui.removeCallbacksAndMessages(null);if(layer!=null&&layer.getParent() instanceof ViewGroup)((ViewGroup)layer.getParent()).removeView(layer);if(notify)a.startupFinished();}
 static void settings(MainActivity a){HomeOptions.show(a);}
 static void choose(MainActivity a){HomeOptions.delay(a);}
}
