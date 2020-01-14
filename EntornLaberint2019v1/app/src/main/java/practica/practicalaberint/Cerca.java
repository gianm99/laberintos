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
/*
 * S'ha d'emplenar la següent taula amb els diferents valors del nodes visitats
 * i llargada del camí resultat per les diferents grandàries de laberints
 * proposades i comentar breument els resultats obtinguts.
 ****************************************************************************************************************
 * Profunditat Amplada Manhattan Euclidiana Viatjant * Laberint Nodes Llargada
 * Nodes Llargada Nodes Llargada Nodes Llargada Nodes Llargada *
 * *****************************************************************************
 * ********************************* Petit Mitjà Gran
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
		laberint.setNodes(0);
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
		if (!trobat) {
			System.err.println("ERROR: No s'ha trobat el camí");
		} else {
			do {
				camiTrobat.afegeix(new Punt(actual));
				actual = actual.previ;
			} while (actual != null);
		}
		return camiTrobat;
	}

	public Cami CercaEnProfunditat(Punt origen, Punt desti) {
		Cami camiTrobat = new Cami(files * columnes);
		laberint.setNodes(0);
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
		if (!trobat) {
			System.err.println("ERROR: No s'ha trobat el camí");
		} else {
			do {
				camiTrobat.afegeix(new Punt(actual));
				actual = actual.previ;
			} while (actual != null);
		}
		return camiTrobat;
	}

	public Cami CercaAmbHeurística(Punt origen, Punt desti, int tipus) { // Tipus pot ser MANHATTAN o EUCLIDIA
		Cami camiTrobat = new Cami(files * columnes);
		laberint.setNodes(0);
		boolean trobat = false;
		Punt actual = new Punt();
		LinkedList<Punt> obert = new LinkedList<Punt>(); // Llista obert
		LinkedList<Punt> tancat = new LinkedList<Punt>(); // Llista tancat
		double f, g, h;
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
				if (!tancat.contains(p)) {
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
						obert.add(p);
					}
				}
			}
		}
		if (!trobat) {
			System.err.println("ERROR: No s'ha trobat el camí");
		} else {
			do {
				camiTrobat.afegeix(new Punt(actual));
				actual = actual.previ;
			} while (actual != null);
		}

		return camiTrobat;

	}

	public Cami CercaViatjant(Punt origen, Punt desti) {
		Cami camiTrobat = new Cami(files * columnes);
		laberint.setNodes(0);

		// Implementa l'algorisme aquí

		return camiTrobat;
	}

}
