package practica.practicalaberint;

/**
 * Created by Ramon Mas on 10/3/16.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class MainGame extends SurfaceView {
    private SurfaceHolder holder;
    public Refresc jocLoopThread;

    static final boolean DEBUG = false;
    static final int NO_ES_MOU = -1;
    static final int AVALL = 0;
    static final int ESQUERRA = 1;
    static final int AMUNT = 2;
    static final int DRETA = 3;
    static final int ATURA = 4;
    static final int ALEATORIA = 0;
    static final int CERCA = 1;
    static final int INTERACTIVA = 0;
    static final int AMPLADA = 1;
    static final int PROFUNDITAT = 2;
    static final int MANHATTAN = 3;
    static final int EUCLIDEA = 4;
    static final int VIATJANT = 5;
    static final int MAXDOLENTS = 4;

    static final String[] nomsCerca = {"Interactiu ","Amplada    ", "Profunditat", "Manhattan  ", "Euclidea   ", "Viatjant   "};

    private int tipusCerca = AMPLADA;  // cerca per defecte
    private Cerca cerca;
    private int direccioArrossegada;
    private int darreraDireccio;

    private int colorLaberint = Color.rgb(255,105,180);


    private Context context;
    private boolean jocAturat;
    private double velocitatHomonet = 0.3;
    private long temps;
    private int numeroDolents = MAXDOLENTS;

    private Laberint laberint;
    private Personatge pacman;
    private Personatge[] fantasmes = new Personatge[4];
    private boolean mostraCamins = true;
    private boolean mataPacman = false;
    private int files, columnes, parets, velocitat;
    private long seed;
    private String posicioPersonatges, cerques;
    Punt[] posicionsCantons;
    private Random random = new Random();


    public MainGame(Context cont) {
        super(cont);
        initSurfaceView(cont);
    }

    public MainGame(Context cont, AttributeSet attrs) {
        super(cont, attrs);
        initSurfaceView(cont);
    }

    public MainGame(Context cont, AttributeSet attrs, int defStyle) {
        super(cont, attrs, defStyle);
        initSurfaceView(cont);
    }

    public void setCerca(int p, int tipus) {
        switch(p)
        {
            case 0:  // pacman
                pacman.setTipusCerca(tipus);
                pacman.setTipusMoviment(CERCA);
                break;
            default: // fantasma p-1
                if (fantasmes[p-1] != null) fantasmes[p-1].setTipusCerca(tipus);
        }
    }

    public void initSurfaceView(Context cont) {
        context = cont;
        this.setOnTouchListener(new Arrossega(context)
        {
            @Override
            public void onSwipeUp() {
                direccioArrossegada = AMUNT;
            }

            @Override
            public void onSwipeDown() {
                direccioArrossegada = AVALL;
            }

            @Override
            public void onSwipeLeft() {
                direccioArrossegada = ESQUERRA;
            }

            @Override
            public void onSwipeRight() {
                direccioArrossegada = DRETA;
            }

            @Override
            public void onStop() {
                direccioArrossegada = ATURA;
            }
        });

        jocLoopThread = new Refresc(this);
        holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            public void surfaceCreated(SurfaceHolder arg0) {
                initGame();
                jocLoopThread.setRunning(true);
                if (!jocLoopThread.isAlive())
                    jocLoopThread.start();
            }

            public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
            }

            public void surfaceDestroyed(SurfaceHolder arg0) {
                jocLoopThread.setRunning(false);
            }
        });
    }

    public void initGame() {

        direccioArrossegada = NO_ES_MOU;  // l'homonet no es mou
        jocAturat = false;         // comença el joc i el temps
        temps = 0;

        // Recuperam les preferències de l'usuari

        recupera();

        laberint = new Laberint(context, files, columnes, parets * 10, velocitatHomonet, seed);  // sum 2 perquè l'index que retorna la selecció comença per zero i el mínim de files i columnes és 2
        cerca = new Cerca(laberint);
        //laberint.setMissatge(nomsCerca[tipusCerca]);


        // Crea el pacman

        Punt posicioPacman = new Punt();
        posicioPacman.x = Integer.parseInt(posicioPersonatges.substring(0,2));
        posicioPacman.y = Integer.parseInt(posicioPersonatges.substring(2,4));

        Bitmap[] imatgePacman = new Bitmap[4];
        for (int i = 0; i < 4; i++) {
            imatgePacman[i] = carregaImatgeEscalada(R.drawable.ccocos01 +i, 400, 100);
        }

        pacman = new Personatge(laberint, posicioPacman, velocitatHomonet, imatgePacman);
        pacman.setTipusCerca(Character.getNumericValue(cerques.charAt(0)));

        /* laberint.setBitxo(pacman); */


        // Crea els fantasmes
        Bitmap[][] imatgeFantasmes = new Bitmap[MAXDOLENTS][1];
        int[] colors = {Color.RED, Color.GREEN, Color.CYAN, Color.MAGENTA};
        int index = 4;
        for (int i = 0; i < MAXDOLENTS; i++) {
            imatgeFantasmes[i][0] = carregaImatgeEscalada(R.drawable.fantasma1+i, 100, 100);
            fantasmes[i] = new Personatge(laberint, new Punt(Integer.parseInt(posicioPersonatges.substring(index,index+2)),Integer.parseInt(posicioPersonatges.substring(index+2,index+4))), velocitatHomonet/2, imatgeFantasmes[i]);
            fantasmes[i].setColor(colors[i]);
            fantasmes[i].setTipusCerca(Character.getNumericValue(cerques.charAt(i+1)));
            fantasmes[i].mouAleatori();
            index+=4;
        }

        laberint.posaPuntetsColors(numeroDolents);
        pacman.setTipusMoviment(INTERACTIVA);
        partir();
    }

    private void recupera()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        seed = sharedPref.getLong("seed",random.nextLong()%1000000);
        files = sharedPref.getInt("files", 5);             // 5x5 per defecte
        columnes = sharedPref.getInt("columnes", 5);             // 5x5 per defecte
        posicionsCantons = new Punt[] {new Punt(0,0), new Punt(files-1,0), new Punt(files-1, columnes-1), new Punt(0, columnes-1)};
        parets = sharedPref.getInt("parets", 4);             // 40% per defecte
        velocitat = sharedPref.getInt("velocitat", 5);
        velocitatHomonet = (1 + velocitat) / 10.0; // 0.2 per defecte
        numeroDolents = sharedPref.getInt("fantasmes", 4);
        mostraCamins = sharedPref.getBoolean("camins", true);
        mataPacman = sharedPref.getBoolean("mata", false);
        posicioPersonatges = sharedPref.getString("posicions",posicionsInicialsPersonatges());
        cerques = sharedPref.getString("cerques","00000");
        if (cerques.length() != 5) cerques = "00000";
    }

    private void guarda() // si pitjam ok, posa els nous valors
    {
        // Per guardar els valors
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("files", files);
        editor.putInt("columnes", columnes);
        editor.putInt("parets", parets);
        editor.putInt("velocitat", velocitat);
        editor.putInt("fantasmes", numeroDolents);
        editor.putBoolean("camins", mostraCamins);
        editor.putBoolean("mata", mataPacman);

        editor.putLong("seed",seed);

        String str = "";
        if (pacman != null) {
            editor.putString("posicions", posicionsActualsPersonatges());
            str = "" + pacman.getTipusCerca();
            for (int i = 0; i < 4; i++)
                str += fantasmes[i].getTipusCerca();
            editor.putString("cerques", str);
        }

        //commits your edits
        editor.commit();
    }

    public String posicionsInicialsPersonatges()
    {
        String str;

        // Pacman al centre per defecte
        int f = files/2;
        int c = columnes/2;
        str = dosCaracters(f)+dosCaracters(c);
        for (int i=0; i<MAXDOLENTS;i++)
        {
            str+= dosCaracters(posicionsCantons[i].x)+dosCaracters(posicionsCantons[i].y);
        }
        return str;
    }

    public String posicionsActualsPersonatges()
    {
        String str;

        str = dosCaracters(pacman.getCasella().x)+dosCaracters(pacman.getCasella().y);
        for (int i=0; i<MAXDOLENTS;i++)
        {
            str+= dosCaracters(fantasmes[i].getCasella().x)+dosCaracters(fantasmes[i].getCasella().y);
        }
        return str;
    }

    public String dosCaracters(int n)
    {
        return String.format("%02d",n);
    }

    public Bitmap carregaImatgeEscalada(int dibuix, int ampla, int alt)
    {
        Bitmap tmpBitmap;

        tmpBitmap = BitmapFactory.decodeResource(getResources(), dibuix);
        return Bitmap.createScaledBitmap(tmpBitmap, ampla, alt, true);
    }

    synchronized protected void paintScreen(Canvas canvas) {
        int direccio;

        temps++;

        if (DEBUG) System.out.println("T:"+temps+" "+canvas);

        if (canvas == null) return; // si no tenim pantalla de dibuix, no pintis res.
                                    // pot passar quan anam d'una activity a una altra !

        // Pintam el laberint

        laberint.pinta(canvas, colorLaberint, jocAturat);

        // ara pintam el pacman

        pacman.actualitzaPosicio(true);
        pacman.pinta(canvas, pacman.getDireccio());

        // i actualitzam el seu moviment si és necessari

        if (pacman.esMou()) pacman.actualitzaIcona();
        if (pacman.getTipusMoviment() == CERCA) {
            if (!pacman.esMou()) pacman.iniciaCamiA(laberint.porta);
            if (mostraCamins) pacman.pintaCami(canvas, Color.YELLOW);
        }

        // pintam els fantasmes

        for (int i=0; i<numeroDolents;i++)
        {
            if (!jocAturat)
                if (fantasmes[i] != null && pacman != null) {
                    fantasmes[i].actualitzaPosicio(false);
                    if (fantasmes[i].getCasella().equals(pacman.getCasella()))
                    {
                        if (MainGame.DEBUG) System.out.println("XOCANT amb pacman !!! "+fantasmes[i]);
                        if (mataPacman)
                            ((MainActivity) context).joc.acabaPartida();
                    }

                    if (laberint.agafatsPuntets(i) && fantasmes[i].getTipusCerca()!=ALEATORIA && fantasmes[i].getTipusMoviment() == 0) {
                        fantasmes[i].setTipusMoviment(1);
                    }

                    if (mostraCamins && fantasmes[i].getTipusCerca()!=ALEATORIA) fantasmes[i].pintaCami(canvas, fantasmes[i].getColor());

                    //if (fantasmes[i].getTipusMoviment() == 0) {
                        int j = aQuiToca(i);
                        if (j != -1)   // toca algú
                        {
                            fantasmes[i].setTocant(1);
                            fantasmes[i].setTipusMoviment(ALEATORIA);
                            fantasmes[j].setTocant(1);
                            if (MainGame.DEBUG) System.out.println("Toca: "+i+":"+j);
                            fantasmes[i].mou(fantasmes[i].direccioContraria(), fantasmes[i].getVelocitat());
                            fantasmes[i].actualitzaPosicio(false);
                            fantasmes[j].mou(fantasmes[i].direccioContraria(), fantasmes[j].getVelocitat());
                            fantasmes[j].actualitzaPosicio(false);
                        }
                    //}
                }

            if (fantasmes[i] != null) {
                    fantasmes[i].pinta(canvas, 0);
            }

            // i actualitzam el seu moviment

            if (!jocAturat)
                for (int n = 0; n < numeroDolents; n++) {
                    switch (fantasmes[n].getTipusMoviment()) {
                        case 0: // aleatori
                            if (fantasmes[n] != null && ((fantasmes[n].caminsPossibles() > 2 && fantasmes[n].getPosicioCami() >= 2) || !fantasmes[n].esMou()))
                                fantasmes[n].mouAleatori();

                            // cada 5 segons, torna a la cerca, si està activada
                            if (temps % (5*Refresc.FPS) == 0 && fantasmes[n].getTipusCerca() != 0 && fantasmes[n].getTipusMoviment() == ALEATORIA) fantasmes[n].setTipusMoviment(1);
                            break;

                        case 1: // cerca
                            fantasmes[i].iniciaCamiA(pacman.getCasella());
                            fantasmes[i].actualitzaPosicio(false);
                            fantasmes[i].setTipusMoviment(2);
                            break;
                    }
                }
        }

        // Moviment interactiu del pacman

        if (jocAturat)
        {
            pintaRectangleTransparent(canvas);
            laberint.escriuCentrat(canvas, "Joc acabat", 60, canvas.getHeight() / 2, Color.RED);
        } else
        {
            if (pacman.getTipusMoviment() == CERCA)
            {
                pacman.actualitzaPosicio(true);
            }
            else
            if (!pacman.esMou() || pacman.potGirar()) {
                direccio = direccioArrossegada;
                if (DEBUG) System.out.println("Direccio: "+pacman.getDireccio());

                if (direccio == ATURA) pacman.acabaMoviment();
                else
                if (direccio != NO_ES_MOU)
                {
                    Punt p = pacman.getCasella();
                    if (temps % 20 == 0)
                        pacman.setIcona((pacman.getIcona() + 1) % 4); // moviment de menjar

                    boolean arribat = false;
                    if (pacman.getTipusMoviment() == CERCA)
                        arribat = laberint.portaAPosicio(p.x, p.y);
                    else
                        arribat = laberint.portaAPosicio(p.x, p.y, direccio);

                    if (laberint.isPortaVisible() && arribat) {
                        acabaPartida();
                    }
                    else
                        if (laberint.pucAnar(p.x, p.y, direccio))
                        {
                            pacman.mou(direccio, velocitatHomonet);
                            pacman.actualitzaPosicio(true);
                        }
                        else direccioArrossegada = NO_ES_MOU;

                }
            }

            if (direccioArrossegada != NO_ES_MOU)
                laberint.pintaFletxaAmbFiltre(canvas, direccioArrossegada, Color.WHITE, 100);
        }

        // Escrivim la informació de les cerques i les icones dels fantasmes

        int altPantalla = canvas.getHeight();
        //int alturaText = (int)(laberint.getAltCasella()/1.7); //altPantalla/33;
        int linia = 50+(files+1)*laberint.getAltCasella(); // altPantalla - laberint.getAltCasella()*6;
        int alturaText = canvas.getWidth()/18;
        int diferenciaLinies = (altPantalla-linia)/5; //(numeroDolents+1);
        int posicioLinia = linia-alturaText/2 + diferenciaLinies;

        if (pacman != null && pacman.ruta != null) {
            pacman.pintaIcona(canvas, 10, posicioLinia-alturaText/2, alturaText);
            laberint.escriuEsquerra(canvas, nomsCerca[pacman.getTipusCerca()] + "  N: " + pacman.ruta.nodesVisitats + "  L: " + pacman.ruta.longitud, alturaText, 100, posicioLinia, Color.WHITE);
            posicioLinia+=diferenciaLinies;
        }


        for (int i = 0; i < numeroDolents; i++) {
            String nomCerca;
            if (fantasmes[i].getTipusMoviment() == ALEATORIA || fantasmes[i].getTipusCerca()==0 )
                nomCerca = "Aleatoria  ";
            else
                nomCerca = nomsCerca[fantasmes[i].getTipusCerca()];

            String txt1;
            if (fantasmes[i].ruta != null)
                txt1 = nomCerca + "  N: " + fantasmes[i].ruta.nodesVisitats + "  L: " + fantasmes[i].ruta.longitud;
            else
                txt1 = "";
            //fantasmes[i].pintaIcona(canvas, 10, linia + 2 * (i + 1) * alturaText - alturaText/2, alturaText);
            fantasmes[i].pintaIcona(canvas, 10, posicioLinia-alturaText/2, alturaText);
            //laberint.escriuEsquerra(canvas, txt1, alturaText, 100, linia + 2 * (i + 1) * alturaText, Color.WHITE);
            laberint.escriuEsquerra(canvas, txt1, alturaText, 100, posicioLinia, Color.WHITE);
            //escriuCentrat(canvas, "Long: "+longitud, alturaText, nFiles * ALT_CASELLA + primeraLinia+2*(alturaText+10));
            posicioLinia+=diferenciaLinies;
        }
    }

    public void activaPersonatge(int id)
    {
        fantasmes[id].setTipusMoviment(CERCA);
    }

    public int aQuiToca(int p)
    {
        for (int i=0; i<numeroDolents;i++)
        {
            if (MainGame.DEBUG) System.out.println("toca i,p:"+(fantasmes[i]==null)+":"+(fantasmes[p]==null));
            if (fantasmes[p]!=null && fantasmes[i]!= null && p != i
                    && fantasmes[p].toca(fantasmes[i]) && fantasmes[p].getTocant() == 0) return i;
        }
        return -1;
    }

    public void pintaRectangleTransparent(Canvas canvas)
    {
        final Rect rect = new Rect();
        final Paint paint = new Paint();
        paint.setARGB(170, 255, 255, 255);

        rect.set(0,0, getMeasuredWidth(), getMeasuredHeight());

        canvas.drawRect(rect, paint);
    }

    public void reinicia()
    {
        //laberint.aleatori.setSeed(10002220);
        //acabaPartida();
        laberint.reinicia();
        laberint.posaPuntetsColors(numeroDolents);
        reiniciaPacman();
        reiniciaDolents();
        continuaPartida();
    }

    public void reiniciaPacman()
    {
        String str = posicionsInicialsPersonatges();
        Punt posicioPacman = new Punt();
        posicioPacman.x = Integer.parseInt(str.substring(0,2));
        posicioPacman.y = Integer.parseInt(str.substring(2,4));

        pacman.atura();
        pacman.setCasella(new Punt(posicioPacman));
        pacman.ruta = null;
        pacman.setTipusMoviment(INTERACTIVA);
        pacman.actualitzaPosicio(true);
    }

    public void reiniciaDolents()
    {
        String str = posicionsInicialsPersonatges();
        for (int i = 0; i < numeroDolents; i++)
            aturaDolent(i);
        int index = 4;
        for (int i = 0; i < numeroDolents; i++) {
            fantasmes[i].setCasella(new Punt(Integer.parseInt(str.substring(index,index+2)),Integer.parseInt(str.substring(index+2,index+4))));
            fantasmes[i].ruta = null;
            fantasmes[i].setTipusMoviment(ALEATORIA);
            //fantasmes[i].setTipusCerca(ALEATORIA);
            fantasmes[i].actualitzaPosicio(false);
            index+=4;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        int lastX, lastY;

        if (jocAturat) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                return true;
            }
            case MotionEvent.ACTION_UP: {
                Cami cami;

                // per si necessitam les coordenades on es pitja
                lastX = (int) event.getX();
                lastY = (int) event.getY();
            }
        }

        return super.onTouchEvent(event);
    }

    public void aturar() {
        guarda(); jocLoopThread.setRunning(false);
    }

    public void partir() {
        recupera(); jocLoopThread.setRunning(true);
    }

    public void camina()
    {
        if (pacman != null) pacman.setTipusMoviment(CERCA);
        for (int i=0; i<numeroDolents;i++)
        {
            if (fantasmes[i] != null) fantasmes[i].setTipusMoviment(CERCA);
        }
    }

    public void acabaPartida()
    {
        jocAturat = true;
        pacman.atura();
        pacman.setCasella(new Punt(0,0));
        for (int i=0; i<numeroDolents;i++)
            aturaDolent(i);
    }

    public void continuaPartida()
    {
        jocAturat = false;
        pacman.parteix();
        for (int i=0; i<numeroDolents;i++)
            parteixDolent(i);
    }

    public void aturaDolent(int i)
    {
        if (fantasmes[i] != null) fantasmes[i].atura();
    }

    public void parteixDolent(int i)
    {
        if (fantasmes[i] != null) fantasmes[i].parteix();
    }

    public void alliberaMemoria()
    {
        pacman = null;
        laberint.alliberaMemoria();
        for (int i=0; i<MAXDOLENTS;i++)
        {
            fantasmes[i].alliberaImatge();
            fantasmes[i] = null;
        }

    }

}

