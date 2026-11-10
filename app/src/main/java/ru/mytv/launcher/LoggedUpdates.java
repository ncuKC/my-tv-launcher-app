package ru.mytv.launcher;
import java.io.File;
public class LoggedUpdates extends ProgressUpdates {
 LoggedUpdates(MainActivity a){super(a);}
 @Override void check(boolean manual){if(manual&&!busy)AppInfo.log(activity,"Запущена ручная проверка обновлений");super.check(manual);}
 @Override void finishState(String text,File apk){AppInfo.log(activity,text);super.finishState(text,apk);}
}
