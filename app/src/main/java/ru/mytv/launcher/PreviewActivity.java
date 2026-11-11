package ru.mytv.launcher;
import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
public class PreviewActivity extends Activity {
 Scenes scene;
 @Override public void onCreate(Bundle state){super.onCreate(state);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);getWindow().getDecorView().setSystemUiVisibility(5894);}
 @Override protected void onResume(){super.onResume();scene=new Scenes(this,getIntent().getBooleanExtra("clock",false),false);setContentView(scene);scene.start();}
 @Override protected void onPause(){if(scene!=null)scene.stop();super.onPause();}
 @Override public void onBackPressed(){finish();}
}
