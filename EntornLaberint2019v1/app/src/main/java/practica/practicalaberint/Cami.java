package practica.practicalaberint;

import android.graphics.Canvas;

/**
 * Created by Ramon Mas.
 * Manages a path of points. Used to store a path solution
 * A path is an ordered array of consecutive points
 */

public class Cami
{
    Punt[] cami;   // conté els punts del camí
    int longitud;  // número real de punts que conté: llargada del camí
    int nodesVisitats;  // Nodes visitats per trobar aquest camí

    Cami(int l)
    {
        cami = new Punt[l];
        longitud = 0;
        cami[longitud]= new Punt(-1,-1);
        nodesVisitats = 0;
    }

    void afegeix(Punt p)   // posa un nou punt dins el cami
    {
        cami[longitud++] = new Punt(p);
    }

    Punt origen()  // inici del camí
    {
        if (longitud == 0) return new Punt(-1,-1);
        else return cami[0];
    }

    Punt desti()  // acabament del camí
    {
        if (longitud == 0) return new Punt(-1,-1);
        else return cami[longitud-1];
    }

    public String toString() // escriu els punts del camí: System.out.println("Cami:"+alguncami);
    {
        String res = "";

        for (int i=longitud-1; i>=0; i--)
            res += " ("+cami[i].x+":"+cami[i].y+")";
        return res;
    }

    void pinta(Canvas canvas, Laberint l, int c)
    {
        for (int i=0; i<longitud-1; i++)
        {
            l.pintaLinia(canvas, cami[i], cami[i+1], c);
        }
    }

    void inverteix()
    {
        Punt tmp;

        for (int i = 0; i < longitud/2; i++)
        {
            tmp = cami[i];
            cami[i] = cami[longitud-1 - i];
            cami[longitud-1 - i] = tmp;
        }
    }
}

