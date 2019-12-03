package practica.practicalaberint;

/**
 * Created by Ramon Mas on 10/3/16.
 * Gestiona el cicle de vida de l'aplicació i el menú principal d'opcions
 */

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.content.res.Configuration;

public class MainActivity extends GeneralActivity {
    public MainGame joc;
    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.init();
        setContentView(R.layout.joc);
        joc = (MainGame) findViewById(R.id.mySurfaceView);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void onResume()
    {
        super.onResume();
        joc.partir();
    }

    protected void onRestart()
    {
        super.onRestart();
    }


    public void activaPersonatge(int id){
        joc.activaPersonatge(id);
    }

    protected void onPause()
    {
        super.onPause();
        joc.aturar();
    }

    protected void onStop()
    {
        super.onStop();
        if (mediaPlayer != null) mediaPlayer.release();
        joc.aturar();
    }

    protected void onDestroy()
    {
        super.onDestroy();
        joc.aturar();
        joc.alliberaMemoria();
        //System.gc();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_newgame:  // genera un nou laberint amb les opcions triades
                startActivity(new Intent(MainActivity.this, OpcionsActivity.class));
                break;
            case R.id.action_cerques:  // mostra el submenu de cerques
                startActivity(new Intent(MainActivity.this, OpcionsCercaActivity.class));
                break;
            case R.id.action_reset:
                joc.reinicia();
                break;
            case R.id.action_run:
                joc.camina();
                break;
        }
        return true;
    }
}

