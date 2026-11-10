package ru.mytv.launcher;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.widget.TextView;
public class HeaderButtons {
 static void style(MainActivity a,TextView b,String label){b.setTextSize(14);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setTextColor(0xffdce7f7);b.setAlpha(.88f);b.setPadding(a.dp(14),a.dp(10),a.dp(14),a.dp(10));int icon="Настройки".equals(label)?android.R.drawable.ic_menu_manage:"Заставка".equals(label)?android.R.drawable.ic_menu_gallery:"Обновления".equals(label)?android.R.drawable.stat_sys_download:0;if(icon!=0){Drawable d=a.getDrawable(icon).mutate();d.setTint(0xffcbdcf4);d.setBounds(0,0,a.dp(18),a.dp(18));b.setCompoundDrawables(d,null,null,null);b.setCompoundDrawablePadding(a.dp(8));}b.setBackground(background(a,false));b.setOnFocusChangeListener((v,f)->{v.animate().cancel();v.setBackground(background(a,f));v.animate().alpha(f?1f:.88f).translationZ(f?a.dp(3):0).setDuration(140).start();});}
 static GradientDrawable background(MainActivity a,boolean f){GradientDrawable d=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,f?new int[]{0xff415775,0xff30435f}:new int[]{0xff26344b,0xff1b273a});d.setCornerRadius(a.dp(14));d.setStroke(a.dp(1),f?0xffd1e3ff:0xff40516a);return d;}
}
