


package practica.practicalaberint;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Classe que conté els diferents algorismes de cerca que s'han d'implementar
 */

/**
 * AUTORS:__________________________________________
 */
/* S'ha d'omplenar la següent taula amb els diferents valors del nodes visitats i llargada del camí resultat
 * per les diferents grandàries de laberints proposades i comentar breument els resultats obtinguts.
 ****************************************************************************************************************
 *                  Profunditat           Amplada          Manhattan         Euclidiana         Viatjant        *
 *  Laberint     Nodes   Llargada    Nodes   Llargada   Nodes   Llargada   Nodes   Llargada  Nodes   Llargada   *
 * **************************************************************************************************************
 *    Petit
 *    Mitjà
 *    Gran
 *
 * Comentari sobre els resultats obtinguts:
 *
 *
 *
 *
 *
 *
 */


public class Cerca {
    Laberint laberint;      // laberint on es cerca
    int files, columnes;    // files i columnes del laberint

    public Cerca(Laberint l) {
        files = l.nFiles;
        columnes = l.nColumnes;
        laberint = l;
    }

    public Cami fesCerca(int tipus, Punt origen, Punt desti) {
        if (esValid(origen) && esValid(desti)) {
            switch (tipus) {
                case (MainGame.AMPLADA):
                    return CercaEnAmplada(origen, desti);

                case (MainGame.PROFUNDITAT):
                    return CercaEnProfunditat(origen, desti);

                case (MainGame.MANHATTAN):
                case (MainGame.EUCLIDEA):
                    return CercaAmbHeurística(origen, desti, tipus);

                case (MainGame.VIATJANT):
                    laberint.posaCornalonsColor();
                    // es pot agafar la posició del cornaló i
                    // amb la funció "getObjecte(i)"

                    return CercaViatjant(origen, desti);
            }
        }
        return null;
    }

    private boolean esValid(Punt p) {
        if (p.x >= 0 && p.y >= 0 && p.x < laberint.nFiles && p.y < laberint.nColumnes) return true;
        else return false;
    }

    /* Exemple gestió caselles on puc anar */

    public ArrayList <Punt> veinats(Punt current)
    {
        ArrayList <Punt> veinats = new ArrayList <Punt>();

        if (laberint.pucAnar(current.x, current.y, MainGame.ESQUERRA))
            veinats.add(new Punt(current.x, current.y - 1));
        if (laberint.pucAnar(current.x, current.y, MainGame.DRETA))
            veinats.add(new Punt(current.x, current.y + 1));
        if (laberint.pucAnar(current.x, current.y, MainGame.AMUNT))
            veinats.add(new Punt(current.x - 1, current.y));
        if (laberint.pucAnar(current.x, current.y, MainGame.AVALL))
            veinats.add(new Punt(current.x + 1, current.y));

        return veinats;
    }

    public Cami CercaEnAmplada(Punt origen, Punt desti) {
        Cami camiTrobat = new Cami(files * columnes);
        laberint.setNodes(0);

        // Implementa l'algorisme aquí
        // Exemple:

        camiTrobat.afegeix(new Punt(0, 0));
        camiTrobat.afegeix(new Punt(0, 1));
        camiTrobat.afegeix(new Punt(0, 2));

        // fi exemple

        return camiTrobat;
    }

    public Cami CercaEnProfunditat(Punt origen, Punt desti) {
        Cami camiTrobat = new Cami(files * columnes);
        laberint.setNodes(0);

        // Implementa l'algorisme aquí

        return camiTrobat;
    }

    public Cami CercaAmbHeurística(Punt origen, Punt desti, int tipus) {   // Tipus pot ser MANHATTAN o EUCLIDIA
        int i;
        Cami camiTrobat = new Cami(files * columnes);
        laberint.setNodes(0);

        // Implementa l'algorisme aquí

        return camiTrobat;
    }


    public Cami CercaViatjant(Punt origen, Punt desti) {
        Cami camiTrobat = new Cami(files * columnes);
        laberint.setNodes(0);

        // Implementa l'algorisme aquí

        return camiTrobat;
    }
}
