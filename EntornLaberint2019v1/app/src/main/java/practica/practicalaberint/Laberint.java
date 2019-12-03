package practica.practicalaberint;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import java.util.Random;

import static practica.practicalaberint.MainGame.AMUNT;
import static practica.practicalaberint.MainGame.AVALL;
import static practica.practicalaberint.MainGame.DRETA;
import static practica.practicalaberint.MainGame.ESQUERRA;
import static practica.practicalaberint.MainGame.INTERACTIVA;

/**
 * Created by Ramon Mas on 10/3/16.
 * Genera laberints de grandaria files x columnes i permet eliminar un percentatge aproximat de parets interiors
 * per augmentar el número de solucions
 */

public class Laberint {
    private final int MAX_FILES = 35;
    private final int MAX_COLUMNES = 35;
    private final int PARET_NORD = 1;
    private final int PARET_EST = 2;
    private final int PARET_SUD = 4;
    private final int PARET_OEST = 8;
    private final int MAX_CELLS = 20;
    private final int MAX_IMGBITXO = 4;
    private final int MAX_DOLENTS = 4;
    private final int NUM_FLETXES = 4;
    private final int NUM_PUNTETS = 5;
    private final int ALEATORI = 0;
    private final int CERCA = 1;
    private Rect src = new Rect(0, 0, 100, 100);

    int AMPLA_CASELLA = 100;
    int ALT_CASELLA = 100;

    Random aleatori;
    private Context context;

    private int windowWidth;
    private int windowHeight;
    private int PARETS;
    int nodes;
    long punts;

    private double percentatgeEliminacio;
    private String missatge = "";
    private int longitud;
    private Punt[] objectes;
    private Punt[] objectesInicials;
    private boolean mostraObjectes = false;
    private long temps;

    Punt porta = new Punt();

    private Bitmap[] cells = new Bitmap[MAX_CELLS];
    private Bitmap[] bitxoBmp = new Bitmap[MAX_IMGBITXO];
    private Bitmap[] dolentsBmp = new Bitmap[MAX_DOLENTS];
    private Bitmap[] fletxes = new Bitmap[NUM_FLETXES];
    private Bitmap[] puntets = new Bitmap[NUM_PUNTETS];
    private int[]    numPuntets = {0,1,1,1,1};
    private int[][]  numInicialPuntets = {{0,1,1,1,1},{0,2,2,2,2},{0,3,3,3,3},{0,4,4,4,4}};

    private int offsetx = 0;
    private int offsety = 0;
    private boolean portaVisible = false;
    private double velocitat;
    private long seed;

    private int nivellActual;
    int nFiles;
    int nColumnes;
    private int casellesTotals;
    private int laberint[][] = new int[MAX_FILES][MAX_COLUMNES];

    Laberint(Context cont, int files, int columnes, int percent, double v, long s)
    {
        seed = s;
        velocitat = v;
        percentatgeEliminacio = percent/(100.0 * 2);  // valors aproximats
        context = cont;
        PARETS = (PARET_NORD + PARET_EST + PARET_SUD + PARET_OEST);
        aleatori = new Random(seed);  // seed si volem sempre el mateix

        objectes = new Punt[] {new Punt(0,0), new Punt(files-1,0), new Punt(files-1, columnes-1), new Punt(0, columnes-1)};  // viatjant de comenrç, els bitxitos
        objectesInicials = new Punt[] {new Punt(0,0), new Punt(files-1,0), new Punt(files-1, columnes-1), new Punt(0, columnes-1)};  // viatjant de comenrç, els bitxitos



        DisplayMetrics metrics = cont.getResources().getDisplayMetrics();
        int w = metrics.widthPixels;
        int h = metrics.heightPixels;

        windowWidth  = w;
        windowHeight = h;

        nFiles = files;
        nColumnes = columnes;

        int dimCasella;
        if (nColumnes < nFiles)
            dimCasella = (windowHeight-200) / (nFiles + 1);
        else
            dimCasella = windowWidth  / (nColumnes + 1);

        AMPLA_CASELLA = dimCasella;
        ALT_CASELLA = dimCasella;

        for (int i = 0; i < MAX_CELLS; i++) {
            cells[i] = carregaImatgeEscalada(R.drawable.cel00 + i, AMPLA_CASELLA, ALT_CASELLA);
        }

        for (int i = 0; i < NUM_FLETXES; i++) {
            fletxes[i] = carregaImatgeEscalada(R.drawable.fletxa0+i, 200, 200);
        }

        for (int i = 0; i < NUM_PUNTETS; i++) {
            puntets[i] = carregaImatgeEscalada(R.drawable.puntet0+i, AMPLA_CASELLA, ALT_CASELLA);
        }
        offsetx = (int) ((windowWidth-dimCasella*(nColumnes+1))/2.0 +(AMPLA_CASELLA / 2));
        offsety = 10;

        inicialitza();
    }

    public void setMissatge(String m)
    {
        missatge = m;
    }

    void setNodes(int n)
    {
        nodes = n;
    }
    void setLongitudCami(int l) {longitud = l;}
    void incNodes()
    {
        nodes++;
    }
    private void inicialitza() {
        initAmbNivell(1, nFiles, nColumnes);
    }

    private Bitmap carregaImatgeEscalada(int dibuix, int ampla, int alt)
    {
        Bitmap tmpBitmap;

        tmpBitmap = BitmapFactory.decodeResource(context.getResources(), dibuix);
        return Bitmap.createScaledBitmap(tmpBitmap, ampla, alt, true);
    }

    Punt getPorta()
    {
        return porta;
    }

    Punt getObjecte(int i) {return objectes[i];}

    boolean isPortaVisible() {
        return portaVisible;
    }

    Punt xy(int fila, int columna)  // converteix fila i columna del laberint a x,y (en píxels) de la pantalla
    {
        return new Punt(offsetx + columna * AMPLA_CASELLA, offsety + fila * ALT_CASELLA);
    }

    Punt filaColumna(int x, int y) // converteix x i y de la pantalla a la casella on es troba del laberint
    {
        int f, c;

        c = (x - offsetx) / AMPLA_CASELLA;
        f = (y - offsety) / ALT_CASELLA;

        if (c >= 0 && c < nColumnes && f >= 0 && f < nFiles)
            return new Punt(f, c);
        else
            return new Punt(-1, -1);
    }


    private void initAmbNivell(int n, int f, int c)
    {
        temps = 0;
        portaVisible = false;
        nivell(n);
        creaNou(f, c);  // laberint de fxc
    }

    void reinicia()
    {
        posaPuntets();
        portaVisible = false;
    }

    private void nivell(int n) {
        nivellActual = n;
    }

    private int Random_0_X(int val)   // retorna un número aleatori de 0 a X  (enter)
    {
        return (aleatori.nextInt(val));
    }

    private double Random_0_1()       // retorna un número entre 0 i 1 (double)
    {
        int x = aleatori.nextInt(100);
        return (x / 100.0);
    }

    private void creaNou(int files, int columnes)
    {
        punts = 0;
        nFiles = files;
        nColumnes = columnes;
        casellesTotals = nFiles * nColumnes;
        int casellesVisitades = 1;
        int[] veinats = new int[4];

        for (int f = 0; f < nFiles; f++)
            for (int c = 0; c < nColumnes; c++)
                laberint[f][c] = PARETS;

        int casellaActualF = (int) (Random_0_1() * nFiles);
        int casellaActualC = (int) (Random_0_1() * nColumnes);

        int novaActualF;
        int novaActualC;

        int veinatsIntactes;
        int posicioAleatoria;
        int oposada;

        int[] pilaF = new int[casellesTotals];
        int[] pilaC = new int[casellesTotals];
        int pilaTop;

        pilaTop = 0;

        while (casellesTotals > casellesVisitades) {
            // veinats intactes de casellaActual
            veinatsIntactes = 0;
            if (casellaActualF > 0) {
                if (laberint[casellaActualF - 1][casellaActualC] == PARETS) {
                    veinats[veinatsIntactes++] = PARET_NORD;
                }
            }
            if (casellaActualF < nFiles - 1) {
                if (laberint[casellaActualF + 1][casellaActualC] == PARETS) {
                    veinats[veinatsIntactes++] = PARET_SUD;
                }
            }
            if (casellaActualC > 0) {
                if (laberint[casellaActualF][casellaActualC - 1] == PARETS) {
                    veinats[veinatsIntactes++] = PARET_OEST;
                }
            }
            if (casellaActualC < nColumnes - 1) {
                if (laberint[casellaActualF][casellaActualC + 1] == PARETS) {
                    veinats[veinatsIntactes++] = PARET_EST;
                }
            }

            if (veinatsIntactes > 0) {
                // tria el veinat on ens movem aleatoriament
                posicioAleatoria = (int) (Random_0_1() * veinatsIntactes);

                novaActualF = casellaActualF;
                novaActualC = casellaActualC;
                switch (veinats[posicioAleatoria]) {
                    case PARET_OEST:
                        novaActualC = casellaActualC - 1;
                        oposada = PARET_EST;
                        break;
                    case PARET_EST:
                        novaActualC = casellaActualC + 1;
                        oposada = PARET_OEST;
                        break;
                    case PARET_SUD:
                        novaActualF = casellaActualF + 1;
                        oposada = PARET_NORD;
                        break;
                    case PARET_NORD:
                        novaActualF = casellaActualF - 1;
                        oposada = PARET_SUD;
                        break;
                    default:
                        oposada = 0;
                        break;
                }

                // baixa les parets per on passam
                laberint[casellaActualF][casellaActualC] -= veinats[posicioAleatoria];
                laberint[novaActualF][novaActualC] -= oposada;

                // guarda la casella actual
                pilaF[pilaTop] = casellaActualF;
                pilaC[pilaTop] = casellaActualC;
                pilaTop++;

                casellaActualF = novaActualF;
                casellaActualC = novaActualC;

                // n'he visitada una altra
                casellesVisitades++;
            } else {
                pilaTop--;
                casellaActualF = pilaF[pilaTop];
                casellaActualC = pilaC[pilaTop];
            }
        }

        // Aleatoriament eliminam algunes parets per fer els recorreguts més curts
        // Quantes ? proporcionalmente al número de caselles (%)

        int casellesEliminades = 0;
        int fEliminar, cEliminar;
        int codi;  //codi corresponent a la paret

        while (casellesEliminades < casellesTotals * percentatgeEliminacio) {
            // casella aleatoria excepte els costats

            fEliminar = 1 + (int) (Random_0_1() * (nFiles - 2));
            cEliminar = 1 + (int) (Random_0_1() * (nColumnes - 2));

            // paret aleatoria
            int exp = (int) (Random_0_1() * 4);
            codi = (int) (Math.pow(2.0, exp));
            if ((laberint[fEliminar][cEliminar] & codi) != 0) //existeix la paret ?
            {
                // que no sigui una paret aillada

                if (teVeinats(fEliminar, cEliminar, codi)) {
                    laberint[fEliminar][cEliminar] -= codi;

                    // Ara hem de llevar la paret de la mateixa casella veinada
                    switch (codi) {
                        case PARET_OEST:
                            laberint[fEliminar][cEliminar - 1] -= PARET_EST;
                            break;
                        case PARET_EST:
                            laberint[fEliminar][cEliminar + 1] -= PARET_OEST;
                            break;
                        case PARET_SUD:
                            laberint[fEliminar + 1][cEliminar] -= PARET_NORD;
                            break;
                        case PARET_NORD:
                            laberint[fEliminar - 1][cEliminar] -= PARET_SUD;
                            break;
                    }
                    casellesEliminades++;
                }
            }

        }

        posaPorta();

        // posa objectes

        posaPuntets();
    }

    private boolean teVeinats(int f, int c, int paret)
    {
        switch (paret) {
            case PARET_OEST:
                if ((laberint[f][c] & PARET_NORD) != 0 || (laberint[f][c-1] & PARET_NORD) != 0) return true;
                if ((laberint[f][c] & PARET_SUD) != 0 || (laberint[f][c-1] & PARET_SUD) != 0) return true;
                if ((laberint[f+1][c] & PARET_OEST) != 0 || (laberint[f-1][c] & PARET_OEST) != 0) return true;
                break;
            case PARET_EST:
                if ((laberint[f][c] & PARET_NORD) != 0 || (laberint[f][c+1] & PARET_NORD) != 0) return true;
                if ((laberint[f][c] & PARET_SUD) != 0 || (laberint[f][c+1] & PARET_SUD) != 0) return true;
                if ((laberint[f+1][c] & PARET_EST) != 0 || (laberint[f-1][c] & PARET_EST) != 0) return true;
                break;
            case PARET_SUD:
                if ((laberint[f][c] & PARET_OEST) != 0 || (laberint[f+1][c] & PARET_OEST) != 0) return true;
                if ((laberint[f][c] & PARET_EST) != 0 || (laberint[f+1][c] & PARET_EST) != 0) return true;
                if ((laberint[f][c-1] & PARET_SUD) != 0 || (laberint[f][c+1] & PARET_SUD) != 0) return true;
                break;
            case PARET_NORD:
                if ((laberint[f][c] & PARET_OEST) != 0 || (laberint[f-1][c] & PARET_OEST) != 0) return true;
                if ((laberint[f][c] & PARET_EST) != 0 || (laberint[f-1][c] & PARET_EST) != 0) return true;
                if ((laberint[f][c-1] & PARET_NORD) != 0 || (laberint[f][c+1] & PARET_NORD) != 0) return true;
                break;
        }
        return false;
    }
    /*
    public void reiniciaDolents()
    {
        for (int i = 0; i < numeroDolents; i++) {
            dolents[i].setCasella(objectes[i]);
            dolents[i].setTipusMoviment(ALEATORI);
        }
    }
    */

    private void posaPuntets()
    {
        for (int i=0; i<NUM_PUNTETS; i++)
            numPuntets[i] = numInicialPuntets[nivellActual-1][i];
        if (MainGame.DEBUG) System.out.println("Num puntets = "+numPuntets[1]);
        for (int f=0; f<nFiles; f++)
            for (int c=0; c<nColumnes; c++)
            {
                laberint[f][c] &= 255;  // borra altres puntets
                laberint[f][c] += 256;  // inicialment tots puntets normals
            }
    }



    void posaPuntetsColors(int n)
    {
        int posicio = 0;

        for (int i=2; i<=MAX_DOLENTS+1; i++)  // puntents de cada color dels dolents
        {
            if (i <= n+1)
                puntetsAleatoris(numPuntets[i-1], i, posicio++);
            else
                numPuntets[i-1] = 0;
        }
    }

    public void posaAleatorisColor()
    {
        posaPuntets();
        posaPuntetsColors(4);
    }

    void posaCornalonsColor()
    {
        posaPuntets();
        for (int i=0; i<4; i++)
        {
            objectes[i].x = objectesInicials[i].x;
            objectes[i].y = objectesInicials[i].y;
            laberint[objectes[i].x][objectes[i].y] = (laberint[objectes[i].x][objectes[i].y] & 255) + (i+2)*256;
        }
    }

    private void puntetsAleatoris(int n, int tipus, int posicio)
    {
        int f;
        int c;
        int colocats = 0;

        while (colocats < n)
        {
            f = aleatori.nextInt(nFiles);
            c = aleatori.nextInt(nColumnes);

            int v = laberint[f][c]>>8;  // quin objecte hi tenim ?

            if (v < 2) // no hi pot haver un altre objecte de color
            {
                laberint[f][c] = (laberint[f][c] & 255) + tipus*256;
                objectes[posicio].x = f;
                objectes[posicio].y = c;
                if (MainGame.DEBUG) System.out.println("Puntets aleatoris: "+n+" "+f+":"+c);
                colocats++;
                if (MainGame.DEBUG) System.out.println("Puntet: "+ tipus+ " a posició: ("+f+","+c+")");
            }

        }
    }

    void pintaLinia(Canvas canvas, Punt p1, Punt p2, int c)
    {
        Punt inici = xy(p1.x, p1.y);
        Punt fi = xy(p2.x, p2.y);
        int red = (c & 0xFF0000) / 0xFFFF;
        int green = (c & 0xFF00) / 0xFF;
        int blue = c & 0xFF;
        int meitat = (int)(AMPLA_CASELLA*0.55);

        Paint paint = new Paint();
        paint.setStrokeWidth(4);
        paint.setColor(c);
        canvas.drawLine(inici.x+meitat, inici.y+meitat,fi.x+meitat,fi.y+meitat, paint);
    }

    public void pintaRectangle(Canvas canvas, int x, int y, int c)
    {
        Punt lloc = xy(x, y);
        int red = (c & 0xFF0000) / 0xFFFF;
        int green = (c & 0xFF00) / 0xFF;
        int blue = c & 0xFF;

        Paint paint = new Paint();
        paint.setARGB(128,red , green, blue);
        canvas.drawRect(lloc.x+10, lloc.y+10,lloc.x+AMPLA_CASELLA-10,lloc.y+ALT_CASELLA-10, paint);
    }

    Paint paint = new Paint();

    private void pintaRectangle(Canvas canvas, Bitmap bitmap, int x, int y, Rect r, int t)
    {
        Rect dst = new Rect(x, y, (x + AMPLA_CASELLA), (y + ALT_CASELLA));
        paint.setAlpha(t);
        canvas.drawBitmap(bitmap, r, dst, paint);
    }

    void pintaCasellaAnimada(Canvas canvas, Bitmap bitmap, int x, int y, int clau)  // pinta una imatge a una posició x,y
    {
        Rect src;

        src = new Rect(clau*100, 0, clau*100+100, 100);
        pintaRectangle(canvas, bitmap, x, y, src, 255);
    }

    void pintaIcona(Canvas canvas, Bitmap bitmap, int x, int y)  // pinta una imatge a una posició x,y
    {
        Rect dst = new Rect(x, y, (x + AMPLA_CASELLA/2), (y + AMPLA_CASELLA/2));
        Paint paint = new Paint();
        canvas.drawBitmap(bitmap, src, dst, paint);
    }

    void pintaIcona(Canvas canvas, Bitmap bitmap, int x, int y, int gran)  // pinta una imatge a una posició x,y
    {
        Rect dst = new Rect(x, y, (x + gran), (y + gran));
        Paint paint = new Paint();
        canvas.drawBitmap(bitmap, src, dst, paint);
    }



    void pintaCasella(Canvas canvas, Bitmap bitmap, int x, int y)  // pinta una imatge a una posició x,y
    {
        canvas.drawBitmap(bitmap, x, y, paint);
    }

    private void pintaCasellaAmbFiltreF(Canvas canvas, Bitmap bitmap, int x, int y, int c, int transparencia)
    {
        Paint paint = new Paint();

        canvas.drawBitmap(bitmap, x, y, paint);
        paint.setAlpha(transparencia);
        paint.setColorFilter(filtreDeColor(c));
        canvas.drawBitmap(bitmap, x, y, paint);
    }

    public void pintaCasellaTransparent(Canvas canvas, Bitmap bitmap, int x, int y, int t)  // pinta una imatge a una posició x,y
    {
        paint.setAlpha(t);
        canvas.drawBitmap(bitmap, x, y, paint);
    }

    void pinta(Canvas canvas, int color, boolean aturat)  // pinta tota la pantalla
    {
        if (canvas == null) return;
        temps++;

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, windowWidth, windowHeight, paint);

        // Ara tenc totes ses caselles, les he de pintar

        int fila = 0;
        int columna = 0;
        int imatge, costats;
        Boolean oNord, nOest, eNord;

        imatge = 0;

        for (int i = 0; i < casellesTotals + nFiles + nColumnes + 1; i++) {
            fila = i / (nColumnes + 1);
            columna = i % (nColumnes + 1);

            if (fila == nFiles || columna == nColumnes) {
                if (columna == nColumnes) {
                    if (fila == 0) imatge = 6;
                    else if (fila == nFiles) imatge = 7;
                    else
                        imatge = 5;
                } else if (fila == nFiles) {
                    imatge = 1;
                }
            } else {
                imatge = laberint[fila][columna]; // & 255;

                // ara posam els costats que facin falta
                costats = imatge & (PARET_NORD | PARET_OEST);

                oNord = columna > 0 && ((laberint[fila][columna - 1] & PARET_NORD) != 0);
                nOest = fila > 0 && ((laberint[fila - 1][columna] & PARET_OEST) != 0);
                eNord = columna < nColumnes - 1 && ((laberint[fila][columna + 1] & PARET_NORD) != 0);

                imatge = 0;

                switch (costats) {
                    case 1:
                        imatge = 1;
                        if (oNord && eNord) imatge = 1;
                        else if (!oNord && !nOest) imatge = 2;
                        else if (oNord && !eNord && !nOest) imatge = 3;
                        else if (!oNord && nOest && eNord) imatge = 4;
                        break;
                    case 8:
                        imatge = 5;
                        if (!nOest) imatge = 6;
                        break;
                    case 9:
                        if (!nOest && !oNord) imatge = 10;
                        else imatge = 11;
                        break;

                    default: // no hi ha parets al nord ni a l'oest, mira el trocet que hi ha d'haver
                        if (oNord && nOest) imatge = 7;
                        else if (oNord) imatge = 8;
                        else if (nOest) imatge = 7;
                        break;
                }
            }


            Punt lloc = xy(fila, columna);

            pintaCasella(canvas, cells[imatge], lloc.x, lloc.y);

            // pinta el puntet si en hi ha
            int tipusPuntet = laberint[fila][columna] >> 8;

            if (tipusPuntet != 0)
            {
                pintaPuntet(canvas, fila, columna, tipusPuntet-1);
            }

        }

        Punt pos = xy(porta.x, porta.y);

        // ara pintam la porta, si tenim tots els puntets
        int colorPorta = Color.WHITE;

        if (tenimTotsElsPuntets() || portaVisible) {
            portaVisible = true;
            colorPorta = Color.RED;
        }
        if (porta.x == 0) pintaCasellaAmbFiltreF(canvas, cells[14], pos.x, pos.y, colorPorta, 255);
        if (porta.x == nFiles - 1) pintaCasellaAmbFiltreF(canvas, cells[14], pos.x, pos.y + ALT_CASELLA, colorPorta, 255);
        if (porta.y == 0) pintaCasellaAmbFiltreF(canvas, cells[13], pos.x, pos.y, colorPorta, 255);
        if (porta.y == nColumnes - 1)
            pintaCasellaAmbFiltreF(canvas, cells[13], pos.x + AMPLA_CASELLA, pos.y, colorPorta, 255);


        int linia = (nFiles+1)*ALT_CASELLA;
        escriuCentrat(canvas, "Punts: " + punts+"  ("+seed+")", windowHeight/33, linia+10, Color.WHITE);

    }

    private boolean tenimTotsElsPuntets()
    {
        int npunts = 0;
        for (int i=1;i<4+1;i++)
        {
            npunts+= numPuntets[i];
        }
        return npunts == 0;
    }



    public void pintaFletxa(Canvas canvas, int i)
    {
        Rect src;
        int x = windowWidth/2-100;
        int y = windowHeight-500;

        src = new Rect(0, 0, 200, 200);
        Rect dst = new Rect(x, y, (x + 200), (y + 200));
        Paint paint = new Paint();
        canvas.drawBitmap(fletxes[i], src, dst, paint);
    }

    void agafaObjectes(Punt p)   // per agafar els puntets
    {
        int f = p.x;
        int c = p.y;

        for (int i = 0; i < 4; i++) {
            int objecte = laberint[f][c] >> 8;
            if (objecte != 0) {
                //System.out.println("Agafa objecte: "+objecte);
                switch(objecte)
                {
                    case 1:
                        punts+= 10;
                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        //dolents[objecte-2].setTipusMoviment(1);
                        ((MainActivity)(context)).activaPersonatge(objecte-2);
                        numPuntets[objecte-1]--;
                        break;
                }
                laberint[f][c] = laberint[f][c] & 255;  // borra del laberint
            }
        }
    }

    boolean agafatsPuntets(int n)
    {
        if (numPuntets[n+1] == 0) return true;
        else return false;
    }

    public void mostraObjectes()
    {
        mostraObjectes = true;
        for (int i=0; i<4;i++)
            objectes[i].visible = true;
    }

    public void amagaObjectes()
    {
        mostraObjectes = false;
    }


    // Aleatòriament col·loca la porta a un costat del laberint
    private void posaPorta()
    {
        // primer triam el quadrant (de 0 a 3)

        int quadrant = Random_0_X(4);
        Punt lloc = new Punt();
        Punt centre = new Punt();

        centre.x = nColumnes / 2;
        centre.y = nFiles / 2;

        switch (quadrant) {
            case 0:// AdaltEsquerra
                if (Random_0_X(50) < 25) {
                    lloc.x = 0;
                    lloc.y = 1 + (int) (centre.y * Random_0_1());
                } else {
                    lloc.x = 1 + (int) (centre.x * Random_0_1());
                    lloc.y = 0;
                }
                break;
            case 1: // AdaltDreta
                if (Random_0_X(50) < 25) {
                    lloc.x = 0;
                    lloc.y = centre.y + (int) ((centre.y - 2) * Random_0_1());
                } else {
                    lloc.x = 1 + (int) (centre.x * Random_0_1());
                    lloc.y = nFiles - 1;
                }
                break;
            case 2: // AbaixEsquerra
                if (Random_0_X(50) < 25) {
                    lloc.x = 1 + (int) (centre.x * Random_0_1());
                    lloc.y = 0;
                } else {
                    lloc.x = nColumnes - 1;
                    lloc.y = 1 + (int) (centre.y * Random_0_1());
                }
                break;
            case 3: // AbaixDreta
                if (Random_0_X(50) < 25) {
                    lloc.x = nColumnes - 1;
                    lloc.y = centre.y + (int) ((centre.y - 2) * Random_0_1());
                } else {
                    lloc.x = centre.x + (int) ((centre.x - 2) * Random_0_1());
                    lloc.y = nFiles - 1;
                }
                break;
        }

        porta.x = lloc.y;
        porta.y = lloc.x;
    }

    // Mira si la porta està a la posició (f,c)
    boolean portaAPosicio(int f, int c)
    {
        if (porta.x == f && porta.y == c) return true;
        else return false;
    }

    // Mira si la porta està a la posició (f,c) en direcció (dir)
    boolean portaAPosicio(int f, int c, int dir)
    {
        if (porta.x == f && porta.y == c)
            switch (dir) {
                case ESQUERRA:
                    if (c == 0) return true;
                    break;
                case DRETA:
                    if (c == nColumnes - 1) return true;
                    break;
                case AMUNT:
                    if (f == 0) return true;
                    break;
                case AVALL:
                    if (f == nFiles - 1) return true;
                    break;
            }
        return false;
    }

    // Mira si puc anar cap a una direccio tenint en compte d'on venc

    boolean pucAnar(int f, int c, int dir)
    {
        if (f<0 || c<0) return  false;

        //System.out.println("Fila columna = "+f+":"+c);
        switch (dir)
        {
            case ESQUERRA:  if ((laberint[f][c] & PARET_OEST) == 0 && c>0) return true;
                            break;
            case DRETA:     if ((laberint[f][c] & PARET_EST) == 0 && c<nColumnes+1) return true;
                            break;
            case AMUNT:     if ((laberint[f][c] & PARET_NORD) == 0 && f>0) return true;
                            break;
            case AVALL:     if ((laberint[f][c] & PARET_SUD) == 0 && f<nFiles+1) return true;
                            break;
        }
        return false;
    }

    // pinta un puntet

    public void pintaPuntet(Canvas canvas, Punt p)
    {
        Punt lloc = xy(p.x, p.y);
        pintaCasella(canvas, puntets[1], lloc.x, lloc.y);
    }


    private void pintaPuntet(Canvas canvas, int f, int c, int color)
    {
        Punt lloc = xy(f, c);
        pintaCasella(canvas, puntets[color], lloc.x, lloc.y);
    }


    void escriuCentrat(Canvas canvas, String missatge, int size, int y, int color)
    {
        Paint paint = new Paint();
        //paint.setTypeface(((GeneralActivity) context).fontJoc);
        paint.setTextSize(size);

        // Centrat horitzontal

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(color);
        paint.setAlpha(255);

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) (y - ((paint.descent() + paint.ascent()))/4) ;

        canvas.drawText(missatge, xPos, yPos, paint);
    }


    void escriuEsquerra(Canvas canvas, String missatge, int size, int x, int y, int color)
    {
        Paint paint = new Paint();
        //paint.setTypeface(((GeneralActivity) context).fontJoc);
        paint.setTextSize(size);

        // Centrat horitzontal

        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(color);
        paint.setAlpha(255);

        int xPos = x;
        int yPos = (int) (y - ((paint.descent() + paint.ascent()))/4) ;

        canvas.drawText(missatge, xPos, yPos, paint);
    }


    void alliberaMemoria()
    {
        for (int i=0; i<MAX_CELLS;i++)
        {
            cells[i].recycle();
            cells[i] = null;
        }
        for (int i=0; i<NUM_FLETXES;i++)
        {
            fletxes[i].recycle();
            fletxes[i] = null;
        }

        for (int i=0; i<NUM_PUNTETS;i++)
        {
            if (MainGame.DEBUG) System.out.println("Puntets: "+i+" "+puntets[i]);
            if (puntets[i] != null)
            {
                puntets[i].recycle();
                puntets[i] = null;
            }
        }
    }

    void pintaFletxaAmbFiltre(Canvas canvas, int i, int c, int transparencia)

    {
        if (i >= NUM_FLETXES) return;

        int x = windowWidth/2-100;
        int y = windowHeight-500;

        Rect src = new Rect(0, 0, 200, 200);
        Rect dst = new Rect(x, y, (x + 200), (y + 200));

        Paint paint = new Paint();
        paint.setAlpha(transparencia);
        canvas.drawBitmap(fletxes[i], src, dst, paint);
        paint.setAlpha(transparencia);
        paint.setColorFilter(filtreDeColor(c));
        canvas.drawBitmap(fletxes[i], src, dst, paint);
    }

    private void pintaCasellaAmbFiltre(Canvas canvas, Bitmap bitmap, int x, int y, int c, int transparencia)
    {
        Rect dst = new Rect(x, y, x + AMPLA_CASELLA, y + ALT_CASELLA);
        Paint paint = new Paint();

        canvas.drawBitmap(bitmap, src, dst, paint);
        paint.setAlpha(transparencia);
        paint.setColorFilter(filtreDeColor(c));
        canvas.drawBitmap(bitmap, src, dst, paint);
    }

    private ColorMatrixColorFilter filtreDeColor(int c)
    {
        int red = (c & 0xFF0000) / 0xFFFF;
        int green = (c & 0xFF00) / 0xFF;
        int blue = c & 0xFF;

        float[] colorTransform = {
                0, 1f, 0, 0, red,
                0, 0, 0f, 0, green,
                0, 0, 0, 0f, blue,
                0, 0, 0, 1f, 0};

        ColorMatrix colorMatrix = new ColorMatrix();
        colorMatrix.setSaturation(0f); //Remove Colour
        colorMatrix.set(colorTransform); //Apply the Red
        return new ColorMatrixColorFilter(colorMatrix);
    }

    int getAmplaCasella() {
        return AMPLA_CASELLA;
    }

    int getAltCasella() {
        return ALT_CASELLA;
    }
}
