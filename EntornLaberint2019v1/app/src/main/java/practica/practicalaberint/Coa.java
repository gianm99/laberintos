package practica.practicalaberint;

/**
 * Created by Ramon Mas on 13/3/16.
 * Gestió d'elements amb una Coa de Nodes
 * Operacions: afegeix, treu, buida, consulta, elements
 */

class Coa {

    class Node {
        Object element;
        Node seguent;

        Node(Object o) {
            element = o;
            seguent = null;
        }
    }

    private Node primer;   // el primer element de la coa
    private Node darrer;   // el darrer
    private int  elements; // quants en hi ha ?

    Coa()
    {
        darrer = null;
        elements = 0;
    }

    void afegeix(Object o)   // inserta un objecte dins la coa
    {
        Node node = new Node(o);
        if (primer == null)
        {
            primer = node;
            darrer = node;
        } else
        {
            darrer.seguent = node;
            darrer = node;
        }
        elements++;
    }

    Object treu()   // extreu el primer objecte de la coa
    {
        if (primer == null)
            return null;
        Object o = primer.element;
        primer = primer.seguent;
        elements--;
        return o;
    }

    boolean buida()
    {
        return (elements == 0);
    }  // coa buida ?

    int elements()
    {
        return elements;
    }  // quants elements hi ha ?

    Object consulta()   // consulta el primer element però no el treu
    {
        if (primer == null)
            return null;
        else
            return primer.element;
    }
}
