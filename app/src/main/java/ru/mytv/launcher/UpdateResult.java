package ru.mytv.launcher;
import android.app.*;
import android.content.*;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.widget.Toast;

public class UpdateResult extends Activity {
 @Override public void onCreate(Bundle state){super.onCreate(state);handle(getIntent());}
 void handle(Intent result){int status=result.getIntExtra(PackageInstaller.EXTRA_STATUS,PackageInstaller.STATUS_FAILURE);if(status==PackageInstaller.STATUS_PENDING_USER_ACTION){Intent confirm=result.getParcelableExtra(Intent.EXTRA_INTENT);if(confirm==null){failure("Нет системного подтверждения установки");return;}try{startActivity(confirm);finish();}catch(Exception e){failure("Не удалось открыть установщик: "+e.getClass().getSimpleName());}return;}if(status==PackageInstaller.STATUS_SUCCESS){AppInfo.log(this,"Приложение успешно обновлено");Toast.makeText(this,"Приложение обновлено",Toast.LENGTH_SHORT).show();home();}else failure("Установка отменена или не выполнена. Код: "+status);}
 void home(){try{startActivity(new Intent(this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));finish();}catch(Exception e){AppInfo.log(this,"Ошибка возврата в лаунчер: "+e.getClass().getSimpleName());new AlertDialog.Builder(this).setTitle("Обновление установлено").setMessage("Автоматически открыть лаунчер не удалось. Нажми Home или попробуй снова.").setPositiveButton("Открыть",(d,n)->home()).setNegativeButton("Закрыть",(d,n)->finish()).show();}}
 void failure(String text){AppInfo.log(this,"Ошибка: "+text);new AlertDialog.Builder(this).setTitle("Результат установки").setMessage(text).setPositiveButton("Открыть лаунчер",(d,n)->home()).setNegativeButton("Закрыть",(d,n)->finish()).show();}
}
