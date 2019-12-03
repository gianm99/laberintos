package practica.practicalaberint;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;


public class OpcionsCercaActivity extends GeneralActivity {
    SharedPreferences mGameSettings;
    private String[] cerques = {"Aleatoria","Amplada","Profunditat","Manhattan","Euclidea","Viatjant"};                                   // per files i columnes

    private final int[] valors = new int[5];   // 0-files, 1-columnes, 2-parets, 3-velocitat, 4-fantasmes
    private String tipusCerques;

    // es crida quan es crea l'activitat
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opcions_cerca);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        tipusCerques = sharedPref.getString("cerques","00000");
        for (int i=0; i<5; i++)
            valors[i] = Character.getNumericValue(tipusCerques.charAt(i));

        initSpinner(0, R.id.Spinner_cerques1, cerques);
        initSpinner(1, R.id.Spinner_cerques2, cerques);
        initSpinner(2, R.id.Spinner_cerques3, cerques);
        initSpinner(3, R.id.Spinner_cerques4, cerques);
        initSpinner(4, R.id.Spinner_cerques5, cerques);

        initOK();
        initCANCEL();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initSpinner(final int ordre, int id, String[] opcions)
    {
        final Spinner spinner = (Spinner) findViewById(id);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, opcions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(adapter);
        spinner.setSelection(valors[ordre]);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition,
                                       long selectedId) {
                valors[ordre] = selectedItemPosition;
                //System.out.println("Valors: "+ordre+" "+selectedItemPosition);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void guarda() // si pitjam ok, posa els nous valors
    {
        // Per guardar els valors
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        String str = ""+valors[0]+valors[1]+valors[2]+valors[3]+valors[4];
        System.out.println("Valors cerques:"+str);
        editor.putString("cerques", str);

        //commits your edits
        editor.commit();
        //startActivity(new Intent(OpcionsActivity.this, MainActivity.class));
        finish();
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
