package practica.practicalaberint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * Classe que conté els diferents algorismes de cerca que s'han d'implementar
 */

/**
 * AUTORS: Gian Lucas Martín Chamorro y Tomás Bordoy García-Carpintero
 */
/* S'ha d'emplenar la següent taula amb els diferents valors del nodes visitats i llargada del camí resultat
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
	Laberint laberint; // laberint on es cerca
	int files, columnes; // files i columnes del laberint
	Cami camiMin;          //

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
		if (p.x >= 0 && p.y >= 0 && p.x < laberint.nFiles && p.y < laberint.nColumnes)
			return true;
		else
			return false;
	}

	/* Exemple gestió caselles on puc anar */

	public ArrayList<Punt> veinats(Punt current) {
		ArrayList<Punt> veinats = new ArrayList<Punt>();

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
		boolean trobat = false;
		Punt actual = new Punt();
		LinkedList<Punt> obert = new LinkedList<Punt>(); // Llista obert
		LinkedList<Punt> tancat = new LinkedList<Punt>(); // Llista tancat

		obert.addLast(origen);
		while (!obert.isEmpty()) {
			// visitar el node
			actual = obert.poll();
			camiTrobat.nodesVisitats++;
			if (actual.equals(desti)) {
				trobat = true;
				break;
			} else {
				// generar succesors de "actual"
				ArrayList<Punt> succesors = veinats(actual);
				// posar "actual" a tancat
				tancat.add(actual);
				// afegir succesors no visitats a OBERT (al final)
				for (Punt p : succesors) {
					// eliminar si està en obert o tancat
					if (!tancat.contains(p) && !obert.contains(p)) {
						p.previ = actual;
						obert.addLast(p);
					}
				}
			}
		}
		return cercarCami(trobat, camiTrobat,actual);
	}

	public Cami CercaEnProfunditat(Punt origen, Punt desti) {
		Cami camiTrobat = new Cami(files * columnes);
		boolean trobat = false;
		Punt actual = new Punt();
		LinkedList<Punt> obert = new LinkedList<Punt>(); // Llista obert
		LinkedList<Punt> tancat = new LinkedList<Punt>(); // Llista tancat

		obert.addLast(origen);
		while (!obert.isEmpty()) {
			// visitar el node
			actual = obert.pop();
			camiTrobat.nodesVisitats++;
			if (actual.equals(desti)) {
				trobat = true;
				break;
			}
			// generar succesors de "actual"
			ArrayList<Punt> succesors = veinats(actual);
			// posar "actual" a tancat
			tancat.add(actual);
			// afegir succesors no visitats a OBERT (al final)
			for (Punt p : succesors) {
				// // eliminar si està en obert, tancat o en el camí
				// if (!tancat.contains(p) && !obert.contains(p) && p.previ==null) {
				// eliminar si està en obert o tancat
				if (!tancat.contains(p) && !obert.contains(p)) {
					p.previ = actual;
					obert.push(p);
				}
			}

		}
		return cercarCami(trobat, camiTrobat,actual);
	}

	public Cami CercaAmbHeurística(Punt origen, Punt desti, int tipus) { // Tipus pot ser MANHATTAN o EUCLIDIA
		Cami camiTrobat = new Cami(files * columnes);
		boolean trobat = false;
		Punt actual = new Punt();
		LinkedList<Punt> obert = new LinkedList<Punt>(); // Llista obert
		LinkedList<Punt> tancat = new LinkedList<Punt>(); // Llista tancat
		// inicialitzar el punt origen
		origen.f = 0;
		origen.g = 0;
		origen.h = 0;
		obert.addLast(origen);
		while (!obert.isEmpty()) {
			// trobar el node amb menor "f"
			actual = obert.get(0);
			int i = 0;
			while (i < obert.size()) {
				if (obert.get(i).f < actual.f) {
					actual = obert.get(i);
				}
				i++;
			}
			// visitar el node
			obert.remove(actual);
			camiTrobat.nodesVisitats++;
			if (actual.equals(desti)) {
				trobat = true;
				break;
			}
			// posar "actual" a tancat
			tancat.add(actual);
			// generar succesors
			ArrayList<Punt> succesors = veinats(actual);
			for (Punt p : succesors) {
				double f, g, h;
				g = actual.g + 1;
				switch (tipus) {
				case MainGame.MANHATTAN:
					h = p.distanciaManhattan(desti);
					break;
				default:
					h = p.distancia(desti);
					break;
				}
				f = g + h;
				if (p.f == 0 || f < p.f) {
					p.f = f;
					p.g = g;
					p.h = h;
					p.previ = actual;
					tancat.remove(p);
					if(!obert.contains(p)){
						obert.add(p);
					}
				}
			}
		}
		return cercarCami(trobat, camiTrobat, actual);
	}

	public Cami CercaViatjant(Punt origen, Punt desti) {
		Cami camiTrobat = new Cami(files * columnes);
		Cami camiActual = new Cami(files * columnes);
		Cami camiMesCurt = new Cami(files * columnes);
		Cami iterador;


		laberint.setNodes(0);
		int p[] ={0,1,2,3};
		int nKeys = 4;
		int nodesVisitats = 0;
		int bestTour = 0;
		int longIterador = 0;
		int longTotal[] = new int[4];
		int longMin = 0, longCam = 0;


		permuteHelper(origen, desti, p, 0);


		return camiMin;
	}


	/******************** Funcions auxiliars ********************/

	private Cami cercarCami(boolean trobat,Cami camiTrobat,Punt actual){
		if (!trobat) {
			System.err.println("ERROR: No s'ha trobat el camí");
			return null;
		}
		do {
			camiTrobat.afegeix(new Punt(actual));
			actual = actual.previ;
		} while (actual != null);
		return camiTrobat;
	}	Cami cami;

	public Cami addPath (Cami camiActual, Punt origen, Punt desti){
		Cami iterador;

		iterador = CercaAmbHeurística(origen, desti, MainGame.MANHATTAN);
		iterador.inverteix();
		for (int x = 0; x < iterador.longitud; x++) {
			camiActual.afegeix(iterador.cami[x]);
		}
		camiActual.nodesVisitats += iterador.nodesVisitats;
		return camiActual;
	}

	public void permuteHelper(Punt origen, Punt desti, int[] arr, int index) {
		if (index >= arr.length - 1) { // If we are at the last element - nothing left to permute
			Cami c = createPath(origen, desti, arr);
			if (c.longitud < camiMin.longitud) {
				c.inverteix();
				for (int x = 0; x < c.longitud; x++) {
					camiMin.afegeix(c.cami[x]);
				}
				camiMin.nodesVisitats = c.nodesVisitats;
			}

			return;
		}

		for (int i = index; i < arr.length; i++) { // For each index in the sub array arr[index...end]

			// Swap the elements at indices index and i
			int t = arr[index];
			arr[index] = arr[i];
			arr[i] = t;

			// Recurse on the sub array arr[index+1...end]

			permuteHelper(origen, desti, arr, index + 1);

			// Swap the elements back
			t = arr[index];
			arr[index] = arr[i];
			arr[i] = t;
		}
	}




	public Cami createPath (Punt origen, Punt desti, int [] Permutaciones){
		Cami camiBase = new Cami(files * columnes);

		camiBase = addPath(camiBase, origen, laberint.getObjecte(Permutaciones[0]));
		camiBase = addPath(camiBase, laberint.getObjecte(Permutaciones[0]), laberint.getObjecte(Permutaciones[1]));
		camiBase = addPath(camiBase, laberint.getObjecte(Permutaciones[1]), laberint.getObjecte(Permutaciones[2]));
		camiBase = addPath(camiBase, laberint.getObjecte(Permutaciones[2]), laberint.getObjecte(Permutaciones[3]));
		camiBase = addPath(camiBase, laberint.getObjecte(Permutaciones[3]), desti);

		camiBase.inverteix();

		return camiBase;

	}

}
