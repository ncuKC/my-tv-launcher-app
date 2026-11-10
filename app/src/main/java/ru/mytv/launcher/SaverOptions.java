package ru.mytv.launcher;
import android.app.*;
import android.content.*;
import android.provider.Settings;
public class SaverOptions {
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("screensaver",0);}
 static void show(MainActivity a){SharedPreferences p=prefs(a);a.ui.removeCallbacks(a.idle);String[] labels={"Автозапуск в лаунчере: "+(p.getBoolean("enabled",true)?"вкл":"выкл"),"Режим: "+(p.getBoolean("clockMode",false)?"часы":"пейзажи"),"Ожидание в лаунчере: "+p.getInt("idle",300)/60+" мин","Смена фото: "+p.getInt("photo",60)+" с","Перемещение часов: "+p.getInt("move",15)+" с","Размер часов: "+p.getInt("size",56),"Яркость заставки: "+p.getInt("brightness",100)+"%","Часы поверх фото: "+(p.getBoolean("overlay",false)?"да":"нет"),"Подписи к фотографиям: "+(p.getBoolean("captions",true)?"вкл":"выкл"),"Предпросмотр пейзажей","Предпросмотр часов","Системная заставка и её таймер"};new AlertDialog.Builder(a).setTitle("Настройки заставки").setItems(labels,(d,n)->{switch(n){
 case 0:p.edit().putBoolean("enabled",!p.getBoolean("enabled",true)).apply();show(a);break;
 case 1:p.edit().putBoolean("clockMode",!p.getBoolean("clockMode",false)).apply();show(a);break;
 case 2:choose(a,"Ожидание в лаунчере","idle",new int[]{60,180,300,600,900,1800},300," с");break;
 case 3:choose(a,"Смена фото","photo",new int[]{30,60,120,300},60," с");break;
 case 4:choose(a,"Перемещение часов","move",new int[]{10,15,30,60},15," с");break;
 case 5:choose(a,"Размер часов","size",new int[]{40,56,72,96},56," sp");break;
 case 6:choose(a,"Яркость заставки","brightness",new int[]{30,50,70,100},100,"%");break;
 case 7:p.edit().putBoolean("overlay",!p.getBoolean("overlay",false)).apply();show(a);break;
 case 8:p.edit().putBoolean("captions",!p.getBoolean("captions",true)).apply();show(a);break;
 case 9:case 10:a.startActivity(new Intent(a,MainActivity.Saver.class).putExtra("clock",n==10));break;
 case 11:a.open(new Intent(Settings.ACTION_DREAM_SETTINGS));break;
 }}).setNegativeButton("Закрыть",null).setOnDismissListener(d->a.resetIdle()).show();}
 static void choose(MainActivity a,String title,String key,int[] values,int fallback,String suffix){String[] names=new String[values.length];int selected=-1,current=prefs(a).getInt(key,fallback);for(int i=0;i<values.length;i++){names[i]=values[i]+suffix;if(values[i]==current)selected=i;}new AlertDialog.Builder(a).setTitle(title).setSingleChoiceItems(names,selected,(d,n)->{prefs(a).edit().putInt(key,values[n]).apply();d.dismiss();show(a);}).setNegativeButton("Назад",(d,n)->show(a)).setOnDismissListener(d->a.resetIdle()).show();}
}
