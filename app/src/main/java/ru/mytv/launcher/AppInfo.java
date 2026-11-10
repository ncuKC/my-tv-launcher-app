package ru.mytv.launcher;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.os.Build;
import android.widget.*;
import java.util.*;

public class AppInfo {
 static void log(Context c,String text){SharedPreferences.Editor e=c.getSharedPreferences("diagnostics",0).edit().putString("result",text).putLong("at",System.currentTimeMillis());if(text.startsWith("Ошибка"))e.putString("error",text).putLong("errorAt",System.currentTimeMillis());e.apply();}
 static String date(long t){return t==0?"нет данных":android.text.format.DateFormat.format("dd.MM.yyyy HH:mm:ss",new Date(t)).toString();}
 static String report(Context c){String version="неизвестна";try{PackageInfo p=c.getPackageManager().getPackageInfo(c.getPackageName(),0);version=p.versionName+" ("+Updates.version(p)+")";}catch(Exception ignored){}SharedPreferences d=c.getSharedPreferences("diagnostics",0),s=SaverOptions.prefs(c),h=HomeOptions.prefs(c);return "Мой ТВ "+version+"\nПакет: "+c.getPackageName()+"\nУстройство: "+Build.MANUFACTURER+" "+Build.MODEL+"\nAndroid: "+Build.VERSION.RELEASE+" / API "+Build.VERSION.SDK_INT+"\nПоследняя ручная проверка: "+date(c.getSharedPreferences("updateStatus",0).getLong("checked",0))+"\nПоследний результат: "+d.getString("result","нет данных")+"\nВремя результата: "+date(d.getLong("at",0))+"\nПоследняя ошибка: "+d.getString("error","нет данных")+"\nВремя ошибки: "+date(d.getLong("errorAt",0))+"\nКолонки: "+h.getInt("columns",0)+" (0 — авто)\nРазмер плиток: "+h.getInt("scale",100)+"%\nЗаставка: "+(s.getBoolean("clockMode",false)?"часы":"пейзажи")+"\nАвтозапуск в лаунчере: "+s.getBoolean("enabled",true)+"\nОжидание заставки: "+s.getInt("idle",300)+" с\nАвтопроверка обновлений: раз в 20 минут при открытом лаунчере\nДиагностика хранится локально и не отправляется автоматически.";}
 static void show(MainActivity a){String report=report(a);ScrollView scroll=new ScrollView(a);TextView text=a.text(report,14);text.setTextIsSelectable(true);text.setPadding(a.dp(20),a.dp(12),a.dp(20),a.dp(12));scroll.addView(text);new AlertDialog.Builder(a).setTitle("О приложении и диагностика").setView(scroll).setPositiveButton("Скопировать",(d,n)->{ClipboardManager clipboard=(ClipboardManager)a.getSystemService(Context.CLIPBOARD_SERVICE);clipboard.setPrimaryClip(ClipData.newPlainText("Диагностика «Мой ТВ»",report));Toast.makeText(a,"Диагностика скопирована",Toast.LENGTH_SHORT).show();}).setNegativeButton("Закрыть",null).show();}
}
