package practica.practicalaberint;

/**
 * Created by Ramon Mas on 10/3/16.
 * Classe per guardar les coordenades x,y d'un píxel o d'una casella del laberint
 * Inclou atributs addicionals per si es vol utilitzar com a node en els algorismes de cerca (previ, distancias)
 */

public class Punt implements Comparable<Punt> {
    // x i y: coordenades fila, columna

    int x;
    int y;
    Punt previ;
    boolean visible = true; // variable auxiliar

    Punt()
    {
        super();
    }

    Punt(int x1, int y1, Punt f, int val)
    {
        super();
        this.x = x1;
        this.y = y1;
        this.previ = f;
    }

    Punt(Punt p1)
    {
        this.x =p1.x;
        this.y =p1.y;
    }

    Punt(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other){   //per veure si dos punts són iguals
        if (other == null) return false;
        if (((Punt)other).x == this.x && ((Punt) other).y == this.y) return true;
        else return false;
    }

    double distancia(Punt p)
    {
        return Math.sqrt((x-p.x)*(x-p.x)+(y-p.y)*(y-p.y));
    }

    public int compareTo(Punt punt2) {   // compara els valors de les heurístiques aplicades a dos punts

        return 0;
    }

}
