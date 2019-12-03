package practica.practicalaberint;

/**
 * Created by Ramon Mas on 10/3/16.
 * Gestiona la pantalla d'opcions per crear un nou laberint
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import java.util.Random;


public class OpcionsActivity extends GeneralActivity {
    SharedPreferences mGameSettings;
    private String[] laberint = {"Aleatori","Petit","Mitjà","Gran"};
    private String[] numeros  = {"2","3","4","5","6","7","8","9","10","11","12","13","14",
                                "15","16","17","18","19","20","21","22","23","24","25",
                                "26","27","28","29","30"};                                   // per files i columnes
    private String[] numeros2 = {"0","10","20","30","40","50","60","70","80","90","100"};    // per % parets llevades
    private String[] numeros3 = {"1","2","3","4","5","6","7","8","9","10"};                  // per velocitat
    private String[] numeros4 = {"0","1","2","3","4"};

    private final int[] valors = new int[6];   // 0-files, 1-columnes, 2-parets, 3-velocitat, 4-fantasmes, 6-laberints predefinits
    private boolean[] opcions = new boolean[2];
    private String tipusCerques;
    private Spinner[] spinners = new Spinner[6];
    private long seed = 0;

    private Random random = new Random();

    // es crida quan es crea l'activitat
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opcions);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        valors[0] = sharedPref.getInt("files", 5)-2;     // 5x5 per defecte
        valors[1] = sharedPref.getInt("columnes", 5)-2;  // 5x5 per defecte
        valors[2]   = sharedPref.getInt("parets", 6);   // 60 %  (6x10)
        valors[3]   = sharedPref.getInt("velocitat", 3);   // 0.3
        valors[4]   = sharedPref.getInt("fantasmes", 4);
        opcions[0] = sharedPref.getBoolean("camins",true);  // mostra els camins
        opcions[1] = sharedPref.getBoolean("mata",true);    // mata en pacman
        tipusCerques = sharedPref.getString("cerques","00000");

        initSpinner(0, R.id.Spinner_files, numeros);
        initSpinner(1, R.id.Spinner_columnes, numeros);
        initSpinner(2, R.id.Spinner_parets, numeros2);
        initSpinner(3, R.id.Spinner_velocitat, numeros3);
        initSpinner(4, R.id.Spinner_fantasmes, numeros4);
        initSpinner(5, R.id.Spinner_tipuslaberint, laberint);

        initCheckBox(0, R.id.Check_camins);
        initCheckBox(1, R.id.Check_mata);
        initOK();
        initCANCEL();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initCheckBox(final int n, int id)
    {
        CheckBox chk;

        chk = (CheckBox) findViewById(id);
        chk.setChecked(opcions[n]);

        chk.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                opcions[n] = ((CheckBox) v).isChecked();
            }
        });
    }

    private void initSpinner(final int ordre, int id, String[] opcions)
    {
        final Spinner spinner = (Spinner) findViewById(id);

        spinners[ordre] = spinner;

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opcions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(adapter);
        spinner.setSelection(valors[ordre]);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition,
                                       long selectedId) {
                valors[ordre] = selectedItemPosition;
                //System.out.println("Valors: "+ordre+" "+selectedItemPosition);
                if (ordre == 5)
                {
                    if (selectedItemPosition != 0) // no el volem aleatori
                    {
                        valors[2] = 6; // 60 % de parets llevades
                        switch(selectedItemPosition)
                        {
                            case 1: // petit
                                valors[0] = 6;
                                valors[1] = 6;
                                seed = 231111;
                                break;
                            case 2: // mitjà
                                valors[0] = 13;
                                valors[1] = 13;
                                seed = 344122;
                                break;
                            case 3: // gran
                                valors[0] = 22;
                                valors[1] = 22;
                                seed = 512345;
                                break;
                        }
                        spinners[0].setSelection(valors[0]);
                        spinners[1].setSelection(valors[1]);
                        spinners[2].setSelection(valors[2]);
                    }
                }
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void guarda() // si pitjam ok, posa els nous valors
    {
        // Per guardar els valors
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        Editor editor = sharedPref.edit();
        editor.putInt("files", valors[0]+2);
        editor.putInt("columnes", valors[1]+2);
        editor.putInt("parets", valors[2]);
        editor.putInt("velocitat", valors[3]);
        editor.putInt("fantasmes", valors[4]);
        editor.putBoolean("camins", opcions[0]);
        editor.putBoolean("mata", opcions[1]);
        editor.putLong("seed",(seed == 0)? random.nextLong()%1000000:seed);
        tipusCerques = "0"+tipusCerques.substring(1);
        editor.putString("cerques", tipusCerques);

        String str;
        int files = valors[0]+2;
        int columnes = valors[1]+2;
        int f = files/2;
        int c = columnes/2;
        str = dosCaracters(f)+dosCaracters(c);
        Punt[] posicionsCantons = new Punt[] {new Punt(0,0), new Punt(files-1,0), new Punt(files-1, columnes-1), new Punt(0, columnes-1)};

        for (int i=0; i<4;i++)
        {
            str+= dosCaracters(posicionsCantons[i].x)+dosCaracters(posicionsCantons[i].y);
        }

        editor.putString("posicions", str);
        //commits your edits
        editor.commit();
        //startActivity(new Intent(OpcionsActivity.this, MainActivity.class));
        finish();
    }

    public String dosCaracters(int n)
    {
        return String.format("%02d",n);
    }

    private void ignora()  // si pitjam cancel·lar, no canviam res
    {
        finish();
    }

    private void initOK() {
        // Boto OK
        Button okButton = (Button) findViewById(R.id.buttonOK);
        okButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                guarda();

            }
        });
    }

    private void initCANCEL() {
        // Boto Cancel·lar
        Button cancelButton = (Button) findViewById(R.id.buttonCANCEL);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ignora();
            }
        });
    }
}