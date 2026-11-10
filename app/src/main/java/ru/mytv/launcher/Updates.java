package ru.mytv.launcher;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.Toast;
import java.net.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.concurrent.*;
import org.json.*;

public class Updates {
 static final String REPO="ncuKC/my-tv-launcher-app";final MainActivity activity;final ExecutorService pool=Executors.newSingleThreadExecutor();final Handler ui=new Handler(Looper.getMainLooper());boolean busy;
 Updates(MainActivity a){activity=a;}
 void close(){pool.shutdownNow();ui.removeCallbacksAndMessages(null);}
 void check(boolean manual){
  if(busy){if(manual)activity.message("Проверка или загрузка уже выполняется.");return;}
  if(activity.getPackageName().endsWith(".test")){if(manual)activity.message("Это тестовая сборка. Обновления доступны в релизной версии.");return;}
  SharedPreferences prefs=activity.getSharedPreferences("updates",0);long last=prefs.getLong("check",0);if(!manual&&System.currentTimeMillis()-last<43200000)return;prefs.edit().putLong("check",System.currentTimeMillis()).apply();busy=true;if(manual)Toast.makeText(activity,"Проверяю GitHub…",Toast.LENGTH_SHORT).show();
  pool.submit(()->{String error=null;File file=null;try{
   byte[] json=fetch("https://api.github.com/repos/"+REPO+"/releases/latest",1048576);JSONObject release=new JSONObject(new String(json,java.nio.charset.StandardCharsets.UTF_8));String tag=release.getString("tag_name");String[] parts=tag.replaceFirst("^v","").split("\\.");
   if(parts.length!=3)throw new IOException("Неподдерживаемая версия релиза");long expected=Long.parseLong(parts[0])*10000+Long.parseLong(parts[1])*100+Long.parseLong(parts[2]);PackageInfo own=activity.getPackageManager().getPackageInfo(activity.getPackageName(),PackageManager.GET_SIGNATURES);
   if(expected>version(own)){JSONArray assets=release.getJSONArray("assets");String url=null;for(int i=0;i<assets.length();i++)if("My-TV.apk".equals(assets.getJSONObject(i).getString("name")))url=assets.getJSONObject(i).getString("browser_download_url");if(url==null||!url.startsWith("https://github.com/"+REPO+"/releases/download/"))throw new IOException("APK релиза не найден");byte[] bytes=fetch(url,33554432);file=new File(activity.getCacheDir(),"update.apk");try(FileOutputStream out=new FileOutputStream(file)){out.write(bytes);}verify(file,own,expected);}
  }catch(Exception e){error=e.getMessage();}
  final String message=error;final File apk=file;ui.post(()->{busy=false;if(!activity.live())return;if(message!=null){if(manual)activity.message("Обновление недоступно: "+message);return;}if(apk==null){if(manual)activity.message("Установлена актуальная версия.");return;}new AlertDialog.Builder(activity).setTitle("Обновление загружено").setMessage("Подпись проверена. Установить новую версию?").setPositiveButton("Установить",(d,n)->install(apk)).setNegativeButton("Позже",null).show();});});
 }
 static long version(PackageInfo p){return Build.VERSION.SDK_INT>=28?p.getLongVersionCode():p.versionCode;}
 void verify(File file,PackageInfo own,long expected)throws Exception{
  PackageInfo update=activity.getPackageManager().getPackageArchiveInfo(file.getAbsolutePath(),PackageManager.GET_SIGNATURES);if(update==null||!own.packageName.equals(update.packageName)||version(update)!=expected||version(update)<=version(own))throw new IOException("Неверный пакет или номер версии");if(own.signatures==null||update.signatures==null||own.signatures.length!=1||update.signatures.length!=1)throw new IOException("Неподдерживаемая подпись");MessageDigest digest=MessageDigest.getInstance("SHA-256");if(!MessageDigest.isEqual(digest.digest(own.signatures[0].toByteArray()),digest.digest(update.signatures[0].toByteArray())))throw new IOException("Подпись APK отличается — установка отменена");
 }
 static byte[] fetch(String value,int limit)throws Exception{URL url=new URL(value);HttpURLConnection c=null;try{
  for(int redirect=0;;redirect++){if(!"https".equals(url.getProtocol()))throw new IOException("Only HTTPS allowed");c=(HttpURLConnection)url.openConnection();c.setInstanceFollowRedirects(false);c.setConnectTimeout(15000);c.setReadTimeout(20000);c.setRequestProperty("User-Agent","MyTVLauncher/0.3");int status=c.getResponseCode();if(status>=300&&status<400){String location=c.getHeaderField("Location");c.disconnect();if(location==null||redirect>=5)throw new IOException("Redirect error");url=new URL(url,location);continue;}if(status!=200)throw new IOException("HTTP "+status);break;}
  ByteArrayOutputStream out=new ByteArrayOutputStream();try(InputStream in=c.getInputStream()){byte[] b=new byte[8192];int n,total=0;while((n=in.read(b))!=-1){total+=n;if(total>limit)throw new IOException("File too large");out.write(b,0,n);}}return out.toByteArray();
 }finally{if(c!=null)c.disconnect();}}
 void install(File apk){try{
  if(!activity.getPackageManager().canRequestPackageInstalls()){new AlertDialog.Builder(activity).setMessage("Разреши установку из этого приложения, затем снова нажми «Обновления».").setPositiveButton("Открыть настройки",(d,n)->activity.open(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+activity.getPackageName())))).setNegativeButton("Отмена",null).show();return;}
  PackageInfo own=activity.getPackageManager().getPackageInfo(activity.getPackageName(),PackageManager.GET_SIGNATURES);PackageInfo archived=activity.getPackageManager().getPackageArchiveInfo(apk.getAbsolutePath(),0);if(archived==null)throw new IOException("APK не читается");verify(apk,own,version(archived));
  PackageInstaller installer=activity.getPackageManager().getPackageInstaller();PackageInstaller.SessionParams params=new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);params.setAppPackageName(activity.getPackageName());params.setSize(apk.length());if(Build.VERSION.SDK_INT>=31)params.setRequireUserAction(PackageInstaller.SessionParams.USER_ACTION_NOT_REQUIRED);int id=installer.createSession(params);
  try(PackageInstaller.Session session=installer.openSession(id)){try(InputStream in=new FileInputStream(apk);OutputStream out=session.openWrite("base.apk",0,apk.length())){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);session.fsync(out);}Intent intent=new Intent(activity,Result.class).setAction("ru.mytv.launcher.INSTALL_RESULT");int flags=PendingIntent.FLAG_UPDATE_CURRENT;if(Build.VERSION.SDK_INT>=31)flags|=PendingIntent.FLAG_MUTABLE;session.commit(PendingIntent.getActivity(activity,id,intent,flags).getIntentSender());}catch(Exception e){installer.abandonSession(id);throw e;}
 }catch(Exception e){activity.message("Установка не началась: "+e.getMessage());}}
 public static class Result extends Activity {
  @Override public void onCreate(Bundle state){super.onCreate(state);int status=getIntent().getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);if(status==PackageInstaller.STATUS_PENDING_USER_ACTION){Intent confirm=getIntent().getParcelableExtra(Intent.EXTRA_INTENT);if(confirm!=null)try{startActivity(confirm);}catch(Exception e){Toast.makeText(this,"Открой установку повторно из лаунчера",Toast.LENGTH_LONG).show();}}else Toast.makeText(this,status==PackageInstaller.STATUS_SUCCESS?"Приложение обновлено":"Установка отменена или не выполнена",Toast.LENGTH_LONG).show();finish();}
 }
}
