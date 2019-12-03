package practica.practicalaberint;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaPlayer;

import java.util.Random;

import static practica.practicalaberint.MainGame.ALEATORIA;
import static practica.practicalaberint.MainGame.AMUNT;
import static practica.practicalaberint.MainGame.AVALL;
import static practica.practicalaberint.MainGame.CERCA;
import static practica.practicalaberint.MainGame.DRETA;
import static practica.practicalaberint.MainGame.ESQUERRA;

/**
 * Created by Ramon Mas on 15/3/16.
 * Gestió del bitxo
 */

public class Personatge
{
    private Punt posicio;      // coordenades pixels
    private Punt porta;        // casella destí final
    private Punt desplaçament; // deltas x i y
    private Punt casella;

    Cami ruta;       // Camí per recorrer

    private int  posicioCami;  // per on va del camí
    private boolean moviment;  // es mou ?
    private boolean esViu;
    private boolean pucGirar;

    private Laberint laberint;
    private double t;
    private double v;
    private int direccio;
    private int darreraDireccio;
    private int icona;
    private int tocant; // espera després d'una col·lisió
    private int tipusMoviment; // 0-aleatori, 1-cerca
    private int tipusCerca;
    private int objectesAgafats;
    private Punt desti;
    private int numIcones;
    private int color;

    private Bitmap[] imatge;


    public String toString()
    {
        String str;
        str = "Posicio:("+posicio.x+":"+posicio.y+") casella:("+casella.x+":"+casella.y+")" + esViu;
        return str;
    }

    public Personatge(Laberint l, Punt p, double velocitat, Bitmap[] imatges)
    {
        objectesAgafats = 0;
        posicioCami = 0;
        moviment = false;
        pucGirar = true;
        laberint = l;
        esViu = true;
        direccio = 0;
        icona = 0;
        ruta = null;
        tipusMoviment = 0; // per defecte, moviment aleatori
        imatge = imatges;
        v = velocitat;
        casella = new Punt(p);
        posicio = l.xy(p.x, p.y);
    }

    int getTipusCerca() {
        return tipusCerca;
    }

    void setTipusCerca(int tipusCerca) {
        this.tipusCerca = tipusCerca;
        if (tipusCerca != ALEATORIA) tipusMoviment = CERCA;
    }

    Punt getCasella()
    {
        return casella;
    }

    void setCasella(Punt c)
    {
        casella = c;
        posicio = laberint.xy(c.x, c.y);
    }

    Punt getDesti() {
        return desti;
    }

    void setDesti(Punt desti) {
        this.desti = desti;
    }

    int getX()
    {
        return posicio.x;
    }

    int getY()
    {
        return posicio.y;
    }

    int getTocant() {
        return tocant;
    }

    int getTipusMoviment() {
        return tipusMoviment;
    }

    void setTipusMoviment(int tipusMoviment) {
        this.tipusMoviment = tipusMoviment;
    }

    void setTocant(int tocant) {
        this.tocant = tocant;
    }

    int getColor() {
        return color;
    }

    void setColor(int color) {
        this.color = color;
    }

    void borraCami() {ruta = null;}

    Cami iniciaCamiA(Punt desti)
    {
        Cami c;
        c = (new Cerca(laberint)).fesCerca(tipusCerca, casella, desti);
        inicia(c);
        return c;
    }

    private void inicia(Cami c)
    {
        if (c == null) return;

        //tipusMoviment = 2;
        moviment = true;
        pucGirar = false;
        t = 0;
        posicioCami = 0;
        ruta = c;
        ruta.inverteix();
        ruta.cami[c.longitud] = new Punt(-1,-1);  // per indicar el final del recorregut
        if (MainGame.DEBUG) System.out.println("Cami des de " + c.origen().x + ":" + c.origen().y + "  fins a " + c.desti().x + ":" + c.desti().y);

        if (ruta == null || ruta.longitud == 1)
        {
            desplaçament = new Punt(0,0);
        }
        else
        {
            desplaçament = direccio(ruta.cami[posicioCami], ruta.cami[posicioCami + 1]);
        }
        posicio = new Punt(laberint.filaColumna(c.origen().x, c.origen().y));;
        porta = c.desti();
    }

    void atura() {esViu = false; moviment = false; mouAleatori();}

    void parteix() {esViu = true; moviment = false;}

    public void posaDesti(Punt p)
    {
        porta = p;
    }

    public void posaPosicio(Punt p)
    {
        moviment = false;
        pucGirar = true;
        posicio = p;
    }

    int getIcona() {
        return icona;
    }

    void setIcona(int icona) {
        this.icona = icona;
    }

    public boolean estaDins(Punt c)
    {
        Punt casella = laberint.filaColumna(posicio.x, posicio.y);

        if (c.x == casella.x && c.y == casella.y) return true;
        else return false;
    }

    void actualitzaIcona()
    {
        icona = (icona+1)%4;
    }

    Bitmap imatge(int d)
    {
        return imatge[d];
    }

    void alliberaImatge()
    {
        for (int i=0;i<imatge.length;i++) {
            imatge[i].recycle();
            imatge[i] = null;
        }
    }

    int getPosicioCami() {
        return posicioCami;
    }

    private void mou()
    {
        if (ruta == null || ruta.cami == null) return;

        Punt oPosicio = new Punt(posicio);

        casella = ruta.cami[posicioCami];
        Punt origen = laberint.xy(ruta.cami[posicioCami].x, ruta.cami[posicioCami].y);
        posicio.x = (int)(origen.x+desplaçament.x*t);
        posicio.y = (int)(origen.y+desplaçament.y*t);

        if (posicio.x > oPosicio.x) direccio = DRETA;
        else
        if (posicio.x < oPosicio.x) direccio = ESQUERRA;
        else
        if (posicio.y > oPosicio.y) direccio = AVALL;
        else
        if (posicio.y < oPosicio.y) direccio = AMUNT;

        t+=v;  // actualitza velocitat
    }

    boolean esMou()
    {
        return moviment;
    }

    boolean potGirar()
    {
        return pucGirar;
    }

    int getDireccio() {
        return direccio;
    }

    double getVelocitat() {
        return v;
    }

    void setDireccio(int direccio) {
        darreraDireccio = this.direccio;
        this.direccio = direccio;
    }

    boolean dinsCami(int f, int c)
    {
        int posicio = 0;

        if (ruta == null || ruta.cami == null || ruta.cami[posicio]==null) return false; // no he cercat cap camí encara

        while (ruta.cami[posicio] != null && ruta.cami[posicio].x != -1)
        {
            if (ruta.cami[posicio].x == f && ruta.cami[posicio].y == c)
                return true;
            posicio++;
        }
        return false;
    }

    void pinta(Canvas canvas, int d)
    {
        if (posicio.x<0 || posicio.y<0) return;
            //System.out.println("ERROR");
        laberint.pintaCasellaAnimada(canvas, imatge[d], posicio.x + 3, posicio.y, icona);
    }

    void pintaIcona(Canvas canvas, int x, int y)
    {
        laberint.pintaIcona(canvas, imatge[0], x, y);
    }

    void pintaIcona(Canvas canvas, int x, int y, int gran)
    {
        laberint.pintaIcona(canvas, imatge[0], x, y, gran);
    }

    void pintaCami(Canvas canvas, int c)
    {
        int posicio = posicioCami;

        if (ruta == null) return;
        ruta.pinta(canvas, laberint, c);
/*
        while (ruta.cami[posicio].x != -1)
        {
            posicio++;
            if (ruta.cami[posicio].x != -1) laberint.pintaPuntet(canvas, ruta.cami[posicio]);
        }
*/
    }

    void actualitzaPosicio(boolean agafa)
    {
        if (moviment && esViu)  // si no es mou, es queda on està
        {
            if (t < 1)
            {
                // estic anant d'una casella a l'altra
                mou();
                pucGirar = false;
            }
            else
            {
                // he arribat a una casella
                posicioCami++;
                if (agafa && ruta.cami[posicioCami].x!=-1)
                {
                    laberint.agafaObjectes(ruta.cami[posicioCami]);
                }

                if (tocant>0) tocant--;

                pucGirar = true;
                if (ruta != null)
                {
                    if (ruta.cami[posicioCami].x == -1 || ruta.cami[posicioCami + 1].x == -1) {
                        tipusMoviment = 0; // aleatori o interactiu
                        moviment = false;
                        if (ruta.cami[posicioCami].x == -1)
                            casella = ruta.cami[posicioCami-1];
                        else
                            casella = ruta.cami[posicioCami];
                        desplaçament.x = 0;
                        desplaçament.y = 0;
                        posicio = laberint.xy(casella.x, casella.y);
                    }
                    else
                    {
                        t = 0;
                        desplaçament = direccio(ruta.cami[posicioCami], ruta.cami[posicioCami + 1]);
                        mou();
                    }
                }
            }

        }
    }

    void acabaMoviment()
    {
        if (ruta == null || ruta.cami == null) return;

        moviment = false;
        casella = ruta.cami[posicioCami];
        desplaçament.x = 0;
        desplaçament.y = 0;
        posicio = laberint.xy(casella.x, casella.y);
        tipusMoviment = 0; // aleatori o interactiu
    }

    Punt direccio(Punt o, Punt d)
    {
        Punt origen = laberint.xy(o.x, o.y);
        Punt desti  = laberint.xy(d.x, d.y);

        // Recta amb equació paramètrica per determinar velocitat
        int dx = desti.x-origen.x;
        int dy = desti.y-origen.y;

        return new Punt(dx,dy);
    }

    private Cami fesCami(Punt p, int d)
    {
        Cami posicions = new Cami(1+Math.max(laberint.nFiles,laberint.nColumnes));


        Punt tmp = casella;

        while(laberint.pucAnar(tmp.x, tmp.y, d))
        {
            posicions.afegeix(new Punt(tmp.x, tmp.y));
            switch (d) {
                case AVALL:    tmp.x++; break;
                case ESQUERRA: tmp.y--; break;
                case AMUNT:    tmp.x--; break;
                case DRETA:    tmp.y++; break;
            }
        }
        posicions.afegeix(p);
        posicions.inverteix();

        return posicions;
    }

    // mou en una certa direcció, amb velocitat v

    void mou(int d, double v)
    {
        Cami posicions = new Cami(3);

        if (laberint.pucAnar(casella.x, casella.y, d))
        {
            darreraDireccio = direccio;
            direccio = d;
            inicia(fesCami(casella, d));
        }
    }

    int direccioContraria()
    {
        int nd = direccio;

        switch(direccio)
        {
            case DRETA:    nd = ESQUERRA;
                break;
            case ESQUERRA: nd = DRETA;
                break;
            case AMUNT:    nd = AVALL;
                break;
            case AVALL:    nd = AMUNT;
                break;
        }
        return nd;
    }

    int caminsPossibles()
    {
        int camins = 0;
        for (int i=0; i<4;i++)  // 4 direccions
        {
            if (laberint.pucAnar(casella.x, casella.y, i)) camins++;
        }
        return camins;
    }

    boolean toca(Personatge p)
    {
        if (posicio.distancia(p.posicio) < laberint.AMPLA_CASELLA) return true;
        else return false;
    }


    void mouAleatori()
    {
        Cami aleat = null;

        int d = laberint.aleatori.nextInt(4);
        // mir on puc anar a partir d'aquí
        int nc = caminsPossibles();

        for (;;)  // Hi ha d'haver una direcció per força
        {
            if (d != direccioContraria() || nc == 1) {
                if (MainGame.DEBUG) System.out.println("Direccions:" + darreraDireccio + ":" + d);

                if (laberint.pucAnar(casella.x, casella.y, d)) {
                    direccio = d;
                    aleat = fesCami(casella, d);
                    darreraDireccio = d;
                    break;
                }
            }
            d = laberint.aleatori.nextInt(4);  // ves provant direccions fins que la trobis
        }
        if (aleat != null) inicia(aleat);  // només si es pot moure
    }
}
