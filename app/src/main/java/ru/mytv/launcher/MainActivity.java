package ru.mytv.launcher;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import android.service.dreams.DreamService;
import java.util.*;
public class MainActivity extends Activity {
 LinearLayout content;TextView clock,hint;SharedPreferences prefs;Updates updates;UpdateBadge badge;SystemStats stats;StartupGate gate;TileMover mover;boolean visible,waiting;View firstApp,dialogReturn,requestedApp;String focusAfterRender;ArrayList<ResolveInfo> inventory=new ArrayList<>();
 final Handler ui=new Handler(Looper.getMainLooper());
 final Runnable idle=()->startActivity(new Intent(this,Saver.class));
 final Runnable tick=new Runnable(){public void run(){if(!visible||waiting)return;clock.setText(android.text.format.DateFormat.format("HH:mm",new Date()));badge.check();stats.sample();ui.postDelayed(this,5000);}};
 int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
 @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("favorites",0);updates=new LoggedUpdates(this);badge=new UpdateBadge(this);stats=new SystemStats(this);gate=new StartupGate(this);mover=new TileMover(this);getWindow().getDecorView().setSystemUiVisibility(5894);}
 @Override protected void onResume(){super.onResume();visible=true;render();waiting=gate.showIfNeeded();if(!waiting){ui.post(tick);resetIdle();}}
 @Override protected void onPause(){visible=false;ui.removeCallbacksAndMessages(null);mover.discard();gate.finish(false);waiting=false;super.onPause();}
 @Override protected void onDestroy(){gate.finish(false);badge.close();stats.close();updates.close();super.onDestroy();}
 @Override public void onUserInteraction(){super.onUserInteraction();resetIdle();}
 @Override public void onWindowFocusChanged(boolean f){super.onWindowFocusChanged(f);if(f){if(!waiting){if(mover!=null&&mover.active)mover.focus();else{View v=dialogReturn!=null&&dialogReturn.isAttachedToWindow()?dialogReturn:requestedApp!=null?requestedApp:firstApp;dialogReturn=null;if(v!=null&&v.isAttachedToWindow())v.requestFocus();}}resetIdle();}else ui.removeCallbacks(idle);}
 @Override public boolean dispatchKeyEvent(KeyEvent e){if(gate!=null&&gate.showing){int k=e.getKeyCode();if(k==KeyEvent.KEYCODE_VOLUME_UP||k==KeyEvent.KEYCODE_VOLUME_DOWN||k==KeyEvent.KEYCODE_VOLUME_MUTE)return super.dispatchKeyEvent(e);if(e.getAction()==KeyEvent.ACTION_DOWN&&(k==KeyEvent.KEYCODE_DPAD_CENTER||k==KeyEvent.KEYCODE_ENTER||k==KeyEvent.KEYCODE_BACK))gate.finish(true);return true;}if(mover!=null&&mover.handle(e))return true;return super.dispatchKeyEvent(e);}
 void startupFinished(){waiting=false;if(live()){ui.removeCallbacks(tick);ui.post(tick);resetIdle();if(firstApp!=null)firstApp.requestFocus();}}
 void resetIdle(){ui.removeCallbacks(idle);if(visible&&!waiting&&(mover==null||!mover.active)&&getWindow().getDecorView().hasWindowFocus()&&SaverOptions.prefs(this).getBoolean("enabled",true))ui.postDelayed(idle,1000L*Math.max(60,SaverOptions.prefs(this).getInt("idle",300)));}
 boolean live(){return visible&&!isFinishing()&&!isDestroyed();}
 void message(String s){if(live())new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show();}
 TextView text(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(-1);return t;}
 GradientDrawable shape(int color){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(16));return d;}
 void focus(View v){v.setFocusable(true);v.setClickable(true);v.setBackground(shape(0xff242b3f));v.setOnFocusChangeListener((w,f)->{GradientDrawable bg=shape(f?0xff435b83:0xff242b3f);if(f)bg.setStroke(dp(2),0xffd7e6ff);w.setBackground(bg);});}
 TextView button(String s,Runnable action){TextView b=text(s,14);b.setGravity(Gravity.CENTER);b.setSingleLine(true);b.setEllipsize(android.text.TextUtils.TruncateAt.END);focus(b);HeaderButtons.style(this,b,s);b.setOnClickListener(v->{dialogReturn=v;action.run();});return b;}
 void open(Intent i){try{startActivity(i);}catch(Exception e){AppInfo.log(this,"Ошибка открытия: "+e.getClass().getSimpleName());message("Этот экран недоступен в прошивке приставки.");}}
 void saverMenu(){SaverOptions.show(this);}
 void render(){
  mover.discard();firstApp=null;requestedApp=null;LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(32),dp(16),dp(32),0);root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff182740,0xff090d18}));setContentView(root);
  LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.addView(text("Мой ТВ",28),new LinearLayout.LayoutParams(0,dp(44),1));clock=text(android.text.format.DateFormat.format("HH:mm",new Date()).toString(),24);header.addView(clock);root.addView(header);TextView metrics=text("RAM / CPU — измерение…",12);metrics.setTextColor(0xffacb9cf);metrics.setPadding(0,0,0,dp(8));root.addView(metrics);stats.attach(metrics);
  LinearLayout actions=new LinearLayout(this);TextView settings=button("Настройки",()->HomeOptions.show(this)),saver=button("Заставка",()->saverMenu()),update=button("Обновления",()->updates.check(true));badge.attach(update);int bw=Math.max(80,Math.min(156,(getResources().getConfiguration().screenWidthDp-64)/3-10));for(TextView b:new TextView[]{settings,saver,update}){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(dp(bw),dp(44));p.setMargins(dp(4),dp(5),dp(4),dp(5));actions.addView(b,p);}root.addView(actions);
  hint=text("OK — открыть · В избранном удерживай OK для перемещения",12);hint.setPadding(dp(5),dp(8),0,dp(4));root.addView(hint);ScrollView scroll=new ScrollView(this);scroll.setClipToPadding(false);scroll.setPadding(0,dp(4),0,dp(20));root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));content=new LinearLayout(this);content.setOrientation(1);scroll.addView(content);
  PackageManager pm=getPackageManager();Map<String,ResolveInfo> found=new HashMap<>();for(String cat:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER})for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(cat),0))if(!r.activityInfo.packageName.equals(getPackageName()))found.putIfAbsent(r.activityInfo.packageName,r);
  inventory=new ArrayList<>(found.values());inventory.sort((a,b)->a.loadLabel(pm).toString().compareToIgnoreCase(b.loadLabel(pm).toString()));Set<String> hidden=HomeOptions.hidden(this);ArrayList<ResolveInfo> shown=new ArrayList<>();for(ResolveInfo r:inventory)if(!hidden.contains(r.activityInfo.packageName))shown.add(r);ArrayList<ResolveInfo> fav=HomeOptions.favorites(this,false);if(!fav.isEmpty())section("Избранное",fav,pm);section("Приложения",shown,pm);if(shown.isEmpty())content.addView(text("Приложения скрыты. Вернуть их можно в настройках.",16));View initial=requestedApp!=null?requestedApp:firstApp!=null?firstApp:settings;focusAfterRender=null;root.post(()->{if(live()&&!waiting&&!gate.showing&&getWindow().getDecorView().hasWindowFocus())initial.requestFocus();});
 }
 void section(String title,ArrayList<ResolveInfo> list,PackageManager pm){
  TextView heading=text(title,20);heading.setPadding(dp(5),dp(10),0,dp(6));content.addView(heading);int width=getResources().getConfiguration().screenWidthDp;int cols=HomeOptions.prefs(this).getInt("columns",0);if(cols<3||cols>6)cols=width>=850?5:width>=650?4:3;int scale=Math.max(80,Math.min(120,HomeOptions.prefs(this).getInt("scale",100))),iconSize=Math.max(24,Math.min(60*scale/100,(width-64)/cols-24));ArrayList<LinearLayout> rows=new ArrayList<>();ArrayList<View> tiles=new ArrayList<>();
  for(int i=0;i<list.size();i+=cols){LinearLayout row=new LinearLayout(this);rows.add(row);for(int j=0;j<cols;j++){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(126*scale/100),1);p.setMargins(dp(5),dp(5),dp(5),dp(5));if(i+j>=list.size()){row.addView(new View(this),p);continue;}ResolveInfo r=list.get(i+j);String pkg=r.activityInfo.packageName;LinearLayout tile=new LinearLayout(this);tile.setOrientation(1);tile.setGravity(Gravity.CENTER);tile.setPadding(dp(6),dp(8),dp(6),dp(8));focus(tile);tiles.add(tile);if(firstApp==null)firstApp=tile;if(pkg.equals(focusAfterRender)&&requestedApp==null)requestedApp=tile;ImageView icon=new ImageView(this);icon.setImageDrawable(r.loadIcon(pm));icon.setScaleType(ImageView.ScaleType.FIT_CENTER);tile.addView(icon,new LinearLayout.LayoutParams(dp(iconSize),dp(iconSize)));TextView label=text((prefs.getBoolean(pkg,false)?"★ ":"")+r.loadLabel(pm),Math.max(12,13*scale/100));label.setGravity(Gravity.CENTER);label.setMaxLines(2);label.setEllipsize(android.text.TextUtils.TruncateAt.END);tile.addView(label,new LinearLayout.LayoutParams(-1,-2));tile.setOnClickListener(v->open(new Intent(Intent.ACTION_MAIN).setComponent(new ComponentName(pkg,r.activityInfo.name)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));tile.setOnLongClickListener(v->{dialogReturn=v;HomeOptions.tile(this,pkg);return true;});row.addView(tile,p);}content.addView(row);}if("Избранное".equals(title))mover.bind(list,rows,tiles,cols);
 }
 @Override public void onBackPressed(){if(mover!=null&&mover.active){mover.finish(false);return;}if(gate!=null&&gate.showing){gate.finish(true);return;}if(content!=null)((ScrollView)content.getParent()).smoothScrollTo(0,0);}
 public static class Saver extends Activity {
  Scenes scene;
  @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(5894);}
  @Override protected void onResume(){super.onResume();scene=new Scenes(this,getIntent().getBooleanExtra("clock",false),!getIntent().hasExtra("clock"));setContentView(scene);scene.start();}
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
