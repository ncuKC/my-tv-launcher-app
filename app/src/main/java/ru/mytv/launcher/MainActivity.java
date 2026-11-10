package ru.mytv.launcher;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import android.service.dreams.DreamService;
import java.util.*;

public class MainActivity extends Activity {
 LinearLayout content; TextView clock; SharedPreferences prefs; Updates updates; UpdateBadge badge; SystemStats stats; boolean visible;
 final Handler ui=new Handler(Looper.getMainLooper());
 final Runnable idle=()->startActivity(new Intent(this,Saver.class));
 final Runnable tick=new Runnable(){public void run(){if(!visible)return;clock.setText(android.text.format.DateFormat.format("HH:mm",new Date()));badge.check();stats.sample();ui.postDelayed(this,5000);}};
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("favorites",0);updates=new Updates(this);badge=new UpdateBadge(this);stats=new SystemStats(this);getWindow().getDecorView().setSystemUiVisibility(5894);}
 @Override protected void onResume(){super.onResume();visible=true;render();ui.post(tick);resetIdle();}
 @Override protected void onPause(){visible=false;ui.removeCallbacksAndMessages(null);super.onPause();}
 @Override protected void onDestroy(){badge.close();stats.close();updates.close();super.onDestroy();}
 @Override public void onUserInteraction(){super.onUserInteraction();resetIdle();}
 void resetIdle(){ui.removeCallbacks(idle);if(visible)ui.postDelayed(idle,300000);}
 boolean live(){return visible&&!isFinishing()&&!isDestroyed();}
 void message(String s){if(live())new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show();}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(-1);return t;}
 GradientDrawable shape(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));return d;}
 void focus(View v){v.setFocusable(true);v.setClickable(true);v.setBackground(shape(0xff242b3f));v.setOnFocusChangeListener((w,f)->{GradientDrawable bg=shape(f?0xff435b83:0xff242b3f);if(f)bg.setStroke(dp(2),0xffd7e6ff);w.setBackground(bg);});}
 TextView button(String s,Runnable action){TextView b=text(s,15);b.setGravity(Gravity.CENTER);b.setSingleLine(true);b.setEllipsize(android.text.TextUtils.TruncateAt.END);b.setPadding(dp(12),dp(10),dp(12),dp(10));focus(b);b.setOnClickListener(v->action.run());return b;}
 void open(Intent i){try{startActivity(i);}catch(Exception e){message("Этот экран недоступен в прошивке приставки.");}}
 void saverMenu(){ui.removeCallbacks(idle);new AlertDialog.Builder(this).setTitle("Заставка").setItems(new String[]{"Пейзажи из интернета","Только плавающие часы","Выбрать системную заставку"},(d,n)->{if(n<2)startActivity(new Intent(this,Saver.class).putExtra("clock",n==1));else open(new Intent(Settings.ACTION_DREAM_SETTINGS));}).setNegativeButton("Закрыть",null).setOnDismissListener(d->resetIdle()).show();}
 void render(){
  LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(32),dp(16),dp(32),0);root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff182740,0xff090d18}));setContentView(root);
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.addView(text("Мой ТВ",28),new LinearLayout.LayoutParams(0,dp(44),1));clock=text(android.text.format.DateFormat.format("HH:mm",new Date()).toString(),24);header.addView(clock);root.addView(header);
  TextView metrics=text("RAM / CPU — измерение…",12);metrics.setTextColor(0xffacb9cf);metrics.setPadding(0,0,0,dp(8));root.addView(metrics);stats.attach(metrics);
  LinearLayout actions=new LinearLayout(this);
  TextView settings=button("Настройки",()->open(new Intent(Settings.ACTION_SETTINGS)));TextView saver=button("Заставка",()->saverMenu());TextView update=button("Обновления",()->updates.check(true));badge.attach(update);
  for(TextView b:new TextView[]{settings,saver,update}){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(5),dp(4),dp(5),dp(4));actions.addView(b,p);}root.addView(actions);
  TextView hint=text("OK — открыть  •  Удержание OK — избранное",12);hint.setPadding(dp(5),dp(8),0,dp(4));root.addView(hint);
  ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setPadding(0,dp(4),0,dp(20));root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));content=new LinearLayout(this);content.setOrientation(1);scroll.addView(content);
  PackageManager pm=getPackageManager();Map<String,ResolveInfo> found=new HashMap<>();for(String cat:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}){Intent q=new Intent(Intent.ACTION_MAIN).addCategory(cat);for(ResolveInfo r:pm.queryIntentActivities(q,0))if(!r.activityInfo.packageName.equals(getPackageName()))found.putIfAbsent(r.activityInfo.packageName,r);}
  ArrayList<ResolveInfo> all=new ArrayList<>(found.values());all.sort((a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));ArrayList<ResolveInfo> fav=new ArrayList<>();for(ResolveInfo r:all)if(prefs.getBoolean(r.activityInfo.packageName,false))fav.add(r);if(!fav.isEmpty())section("Избранное",fav,pm);section("Приложения",all,pm);
 }
 void section(String title,ArrayList<ResolveInfo> list,PackageManager pm){
  TextView heading=text(title,20);heading.setPadding(dp(5),dp(10),0,dp(6));content.addView(heading);int width=getResources().getConfiguration().screenWidthDp;int cols=width>=850?5:width>=650?4:3;
  for(int i=0;i<list.size();i+=cols){LinearLayout row=new LinearLayout(this);for(int j=0;j<cols;j++){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(126),1);lp.setMargins(dp(5),dp(5),dp(5),dp(5));if(i+j>=list.size()){row.addView(new View(this),lp);continue;}ResolveInfo r=list.get(i+j);String pkg=r.activityInfo.packageName;String name=r.loadLabel(pm).toString();LinearLayout tile=new LinearLayout(this);tile.setOrientation(1);tile.setGravity(Gravity.CENTER);tile.setPadding(dp(6),dp(8),dp(6),dp(8));focus(tile);ImageView icon=new ImageView(this);icon.setImageDrawable(r.loadIcon(pm));icon.setScaleType(ImageView.ScaleType.FIT_CENTER);tile.addView(icon,new LinearLayout.LayoutParams(dp(60),dp(60)));TextView label=text((prefs.getBoolean(pkg,false)?"★ ":"")+name,13);label.setGravity(Gravity.CENTER);label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);tile.addView(label,new LinearLayout.LayoutParams(-1,-2));tile.setOnClickListener(v->open(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(pkg,r.activityInfo.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));tile.setOnLongClickListener(v->{prefs.edit().putBoolean(pkg,!prefs.getBoolean(pkg,false)).apply();render();return true;});row.addView(tile,lp);}content.addView(row);}
 }
 @Override public void onBackPressed(){if(content!=null)((ScrollView)content.getParent()).smoothScrollTo(0,0);}
 public static class Saver extends Activity {
  Scenes scene;
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(5894);}
  @Override protected void onResume(){super.onResume();scene=new Scenes(this,getIntent().getBooleanExtra("clock",false));setContentView(scene);scene.start();}
  @Override protected void onPause(){if(scene!=null)scene.stop();super.onPause();}
  @Override public boolean dispatchKeyEvent(KeyEvent e){if(e.getAction()==KeyEvent.ACTION_DOWN)finish();return true;}
  @Override public boolean dispatchTouchEvent(MotionEvent e){if(e.getAction()==0)finish();return true;}
 }
 public static class Dream extends DreamService {
  Scenes scene;
  @Override public void onAttachedToWindow(){super.onAttachedToWindow();setInteractive(false);setFullscreen(true);scene=new Scenes(this,false);setContentView(scene);}
  @Override public void onDreamingStarted(){super.onDreamingStarted();scene.start();}
  @Override public void onDreamingStopped(){scene.stop();super.onDreamingStopped();}
  @Override public void onDetachedFromWindow(){if(scene!=null)scene.stop();super.onDetachedFromWindow();}
 }
}
