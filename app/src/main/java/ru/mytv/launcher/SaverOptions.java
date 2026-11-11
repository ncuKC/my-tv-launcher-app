package ru.mytv.launcher;
import android.app.Dialog;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import java.util.*;
public class SaverOptions {
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("screensaver",0);}
 static void show(MainActivity a){a.ui.removeCallbacks(a.idle);new Panel(a).show();}
 static class Panel extends Dialog {
  final MainActivity a;final SharedPreferences p;final ArrayList<Row> rows=new ArrayList<>();TextView photos,clocks;
  Panel(MainActivity a){super(a);this.a=a;p=prefs(a);requestWindowFeature(Window.FEATURE_NO_TITLE);LinearLayout root=new LinearLayout(a);root.setOrientation(1);root.setPadding(a.dp(24),a.dp(18),a.dp(24),a.dp(16));GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{0xff1d2c44,0xff101827});bg.setCornerRadius(a.dp(24));bg.setStroke(a.dp(1),0xff43536e);root.setBackground(bg);root.addView(a.text("Заставка",25));TextView sub=a.text("Выбери режим. Параметры меняются стрелками или OK.",13);sub.setTextColor(0xffaebed5);sub.setPadding(0,a.dp(4),0,a.dp(10));root.addView(sub);LinearLayout modes=new LinearLayout(a);photos=button("Пейзажи",()->{p.edit().putBoolean("clockMode",false).apply();refresh();});clocks=button("Часы",()->{p.edit().putBoolean("clockMode",true).apply();refresh();});add(modes,photos);add(modes,clocks);root.addView(modes);
   ScrollView scroll=new ScrollView(a);scroll.setClipToPadding(false);scroll.setPadding(0,a.dp(8),0,a.dp(8));root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));LinearLayout list=new LinearLayout(a);list.setOrientation(1);scroll.addView(list);
   row(list,"Автозапуск в лаунчере","enabled",null,1,0,"");row(list,"Ожидание до заставки","idle",new int[]{60,180,300,600,900,1800},300,0,"min");row(list,"Смена фотографий","photo",new int[]{30,60,120,300},60,1," с");row(list,"Часы поверх фото","overlay",null,0,1,"");row(list,"Подписи к фотографиям","captions",null,1,1,"");row(list,"Перемещение часов","move",new int[]{10,15,30,60},15,2," с");row(list,"Размер часов","size",new int[]{40,56,72,96},56,2," sp");row(list,"Яркость заставки","brightness",new int[]{30,50,70,100},100,0,"%");
   TextView note=a.text("Предпросмотр · Для других приложений — системные настройки",12);note.setTextColor(0xffaebed5);note.setPadding(0,a.dp(5),0,a.dp(5));root.addView(note);LinearLayout footer=new LinearLayout(a);add(footer,button("Пейзажи",()->preview(false)));add(footer,button("Часы",()->preview(true)));add(footer,button("Система",()->a.open(new Intent(Settings.ACTION_DREAM_SETTINGS))));add(footer,button("Готово",()->dismiss()));root.addView(footer);setContentView(root);setOnDismissListener(d->a.resetIdle());refresh();
  }
  @Override public void show(){super.show();Window w=getWindow();if(w!=null){w.setBackgroundDrawableResource(android.R.color.transparent);w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);WindowManager.LayoutParams lp=w.getAttributes();lp.dimAmount=.65f;w.setAttributes(lp);w.setLayout(a.dp(Math.min(860,a.getResources().getConfiguration().screenWidthDp-40)),a.dp(Math.min(640,a.getResources().getConfiguration().screenHeightDp-40)));}if(p.getBoolean("clockMode",false))clocks.requestFocus();else photos.requestFocus();}
  void preview(boolean clock){try{a.startActivity(new Intent(a,PreviewActivity.class).putExtra("clock",clock));}catch(Exception e){AppInfo.log(a,"Ошибка открытия предпросмотра: "+e.getClass().getSimpleName());}}
  void add(LinearLayout parent,TextView b){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,a.dp(44),1);lp.setMargins(a.dp(3),a.dp(3),a.dp(3),a.dp(3));parent.addView(b,lp);}
  TextView button(String s,Runnable click){TextView b=a.text(s,14);b.setGravity(Gravity.CENTER);b.setSingleLine(true);b.setEllipsize(android.text.TextUtils.TruncateAt.END);b.setPadding(a.dp(8),a.dp(8),a.dp(8),a.dp(8));b.setFocusable(true);b.setClickable(true);b.setOnClickListener(v->click.run());b.setOnFocusChangeListener((v,f)->paint(b));paint(b);return b;}
  void paint(TextView b){boolean selected=b==photos&&!p.getBoolean("clockMode",false)||b==clocks&&p.getBoolean("clockMode",false);GradientDrawable d=a.shape(b.hasFocus()?0xff4a6386:selected?0xff304b70:0xff25344b);d.setCornerRadius(a.dp(12));d.setStroke(a.dp(b.hasFocus()?2:1),b.hasFocus()?0xffdfebff:selected?0xff86addb:0xff41516b);b.setBackground(d);}
  void row(LinearLayout list,String title,String key,int[] values,int fallback,int kind,String suffix){Row r=new Row(title,key,values,fallback,kind,suffix);rows.add(r);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,a.dp(48));lp.setMargins(a.dp(3),a.dp(3),a.dp(3),a.dp(3));list.addView(r,lp);}
  void refresh(){paint(photos);paint(clocks);for(Row r:rows)r.refresh();}
  class Row extends LinearLayout {
   final String key,suffix;final int[] values;final int fallback,kind;final TextView value;long last;
   Row(String title,String key,int[] values,int fallback,int kind,String suffix){super(a);this.key=key;this.values=values;this.fallback=fallback;this.kind=kind;this.suffix=suffix;setGravity(Gravity.CENTER_VERTICAL);setPadding(a.dp(14),0,a.dp(14),0);addView(a.text(title,15),new LinearLayout.LayoutParams(0,-2,1));value=a.text("",14);value.setTextColor(0xffc5dcff);addView(value);setFocusable(true);setClickable(true);setOnClickListener(v->change(1));setOnFocusChangeListener((v,f)->refresh());setOnKeyListener((v,k,e)->{if(k!=KeyEvent.KEYCODE_DPAD_LEFT&&k!=KeyEvent.KEYCODE_DPAD_RIGHT)return false;if(e.getAction()==KeyEvent.ACTION_DOWN&&(e.getRepeatCount()==0||e.getEventTime()-last>=160)){last=e.getEventTime();change(k==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);}return true;});}
   void change(int direction){if(!isEnabled())return;if(values==null)p.edit().putBoolean(key,!p.getBoolean(key,fallback!=0)).apply();else{int cur=p.getInt(key,fallback),index=0;for(int i=0;i<values.length;i++)if(values[i]==cur)index=i;index=Math.max(0,Math.min(values.length-1,index+direction));p.edit().putInt(key,values[index]).apply();}Panel.this.refresh();}
   void refresh(){boolean clock=p.getBoolean("clockMode",false);boolean enabled=kind==0||kind==1&&!clock||kind==2&&(clock||p.getBoolean("overlay",false));setEnabled(enabled);setFocusable(enabled);setAlpha(enabled?1f:.38f);int n=values==null?0:p.getInt(key,fallback);value.setText(values==null?(p.getBoolean(key,fallback!=0)?"Вкл":"Выкл"):"‹  "+("min".equals(suffix)?n/60+" мин":n+suffix)+"  ›");GradientDrawable d=a.shape(hasFocus()?0xff3c5476:0xff202e44);d.setCornerRadius(a.dp(12));if(hasFocus())d.setStroke(a.dp(2),0xffd7e7ff);setBackground(d);}
  }
 }
}
