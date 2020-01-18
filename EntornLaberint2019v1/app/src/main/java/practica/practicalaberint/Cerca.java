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
 *					Profunditat			Amplada				Manhattan			Euclidiana			Viatjant	*
 *  Laberint	Nodes	Llargada	Nodes	Llargada	Nodes	Llargada	Nodes	Llargada	Nodes	Llargada*
 * **************************************************************************************************************
 *    Petit		23		18			62		8			27		8			27		8			86		46
 *    Mitjà		83		64			198		14			51		14			78		14			386		84
 *    Gran		188		68			576		28			520		28			814		28			663		122
 *
 * Comentari sobre els resultats obtinguts:
 * Els resultats per al laberint petit mostren que per a la cerca en profunditat es troba  
 * ràpidament la solució (no òptima) com amb les cerques amb heurística mentre que a la cerca
 * en amplada s'han de visitar més nodes. La cerca de viatjant és un cas apart que utilitza 
 * la cerca heurística amb la distància Manhattan per trobar els seus camins intermitjos.
 * Per al laberint mitjà passa el mateix, la d'amplada torna a ser la cerca que més nodes 
 * visita pero aconsegueix la solució óptima. Les cerques amb heurística troben la mateixa 
 * solució però visitant molt menys nodes. El cas de la cerca de viatjant és similar al del 
 * laberint petit.
 * Per al laberint gran passa que la cerca en profunditat troba la solució bastant més ràpid 
 * que les altres cerques però amb major longitud. També que la cerca Euclidiana en aquest 
 * laberint en concret triga visitar més nodes que el d'amplada. Aixó es un cas extrany, perquè
 *  en diferents proves realitzades a laberints aleatoris la cerca euclidiana sol treure millor 
 * resultat en termes de visites a nodes.
 * En conclusió, en general les cerques amb heurística o informades han de visitar menys nodes i 
 * treuen bons resultats en termes de longitud. La cerca en amplada i en profunditat, com són 
 * no informades, poden variar en els seus resultats depenent de cada cas.
 */

public class Cerca {
	Laberint laberint; // laberint on es cerca
	int files, columnes; // files i columnes del laberint
	Cami camiMin;          //

	public Cerca(Laberint l) {
		files = l.nFiles;
		columnes = l.nColumnes;
		laberint = l;
		camiMin= new Cami(files * columnes);
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

/* La millor estratègia per trobar el camí óptim és la de la força bruta, es a dir, trobar totes les combinacions de cerca posibles.
	 Per fer-ho, hem utilitzat permutacions de les combinacions de cada clau. Hem fet servir mètodes auxiliars recursius per calcular
	 el camí més curt. A més de mètodes que afegien un camí d'un punt a un altre o la creació d'un camí amb els punts determinats. 
 */	 
	public Cami CercaViatjant(Punt origen, Punt desti) {	
			laberint.setNodes(0);							
		int p[] = {0,1,2,3};								
		camiMin=createPath(origen, desti, p);	// El camí mes curt s'inicialitza amb els valors predeterminats.
		permutarCamins(origen, desti, p, 0);	// Es cerca el camí (més curt) òptim, recorrent les permutacions donades per l'array p, les quatre esquines.
		return camiMin;
	}

	/******************** Funcions auxiliars ********************/

	private Cami cercarCami(boolean trobat,Cami camiTrobat,Punt actual){		//Es crea el camí a partir de punts conectats, utilitzant punt.previ. 
		if (!trobat) {
			System.err.println("ERROR: No s'ha trobat el camí");
			return null;
		}
		do {
			camiTrobat.afegeix(new Punt(actual));
			actual = actual.previ;
		} while (actual != null);
		return camiTrobat;
	}

	public Cami addPath (Cami camiActual, Punt origen, Punt desti){		//Es fa una cerca amb heurística de Manhattan i s'afegeixen els punts i els nodels a un camí existent.
		Cami iterador;

		iterador = CercaAmbHeurística(origen, desti, MainGame.MANHATTAN);
		iterador.inverteix();
		for (int x = 0; x < iterador.longitud; x++) {
			camiActual.afegeix(iterador.cami[x]);
		}
		camiActual.nodesVisitats += iterador.nodesVisitats;
		return camiActual;
	}

	public Cami createPath (Punt origen, Punt desti, int [] Permutaciones){			// L'array de permutacions indica les coordenades del recorregut que ha de realitzar-se. S'inverteix per la 
		Cami camiBase = new Cami(2*files * columnes);								// naturalesa de la funció afegeix

		camiBase = addPath(camiBase, origen, laberint.getObjecte(Permutaciones[0]));
		camiBase = addPath(camiBase, laberint.getObjecte(Permutaciones[0]), laberint.getObjecte(Permutaciones[1]));
		camiBase = addPath(camiBase, laberint.getObjecte(Permutaciones[1]), laberint.getObjecte(Permutaciones[2]));
		camiBase = addPath(camiBase, laberint.getObjecte(Permutaciones[2]), laberint.getObjecte(Permutaciones[3]));
		camiBase = addPath(camiBase, laberint.getObjecte(Permutaciones[3]), desti);

		return camiBase;

	}

	public void permutarCamins(Punt origen, Punt desti, int[] arr, int index) {
		if (index >= arr.length - 1) { // SI ens trobam al darrer element, hem trobat una permutació i cercam un viatge amb aquestes coordenades.
			Cami c = createPath(origen, desti, arr);
			if (c.longitud <= camiMin.longitud) {
				c.inverteix();
				camiMin=new Cami(files*columnes);
				for (int x = 0; x < c.longitud; x++) {
					camiMin.afegeix(c.cami[x]);
				}
				camiMin.nodesVisitats = c.nodesVisitats;
			}

			return;
		}

		for (int i = index; i < arr.length; i++) { // El mètode recursiu utilitza una variable temporal per emmagatzemmar els valors i més tard tornar-los al seu lloc.

			// Swap the elements at indices index and i
			int t = arr[index];
			arr[index] = arr[i];
			arr[i] = t;

			// Feim l'algorisme recursiu en base al index. Cada iteració serà un index major

			permutarCamins(origen, desti, arr, index+1);

			// Es fixa l'array de permutacions a l'estat inical
			t = arr[index];
			arr[index] = arr[i];
			arr[i] = t;
		}
	}
	
}
