package src;

/**
 * Pila genérica implementada con nodos enlazados.
 * Usada para gestionar el historial de Deshacer (Undo) y Rehacer (Redo).
 */
public class PilaHistorial {

    // Nodo interno de la pila
    private static class Nodo {
        String estado;
        Nodo siguiente;

        Nodo(String estado) {
            this.estado = estado;
            this.siguiente = null;
        }
    }

    private Nodo tope;
    private int tamaño;
    private static final int CAPACIDAD_MAXIMA = 100; // Limitar historial a 100 estados

    public PilaHistorial() {
        tope = null;
        tamaño = 0;
    }

    /**
     * Agrega un estado al tope de la pila.
     */
    public void apilar(String estado) {
        if (tamaño >= CAPACIDAD_MAXIMA) {
            eliminarFondo(); // Descartar el estado más antiguo
        }
        Nodo nuevo = new Nodo(estado);
        nuevo.siguiente = tope;
        tope = nuevo;
        tamaño++;
    }

    /**
     * Extrae y devuelve el estado del tope de la pila.
     */
    public String desapilar() {
        if (estaVacia()) {
            return null;
        }
        String estado = tope.estado;
        tope = tope.siguiente;
        tamaño--;
        return estado;
    }

    /**
     * Consulta el tope sin eliminarlo.
     */
    public String verTope() {
        if (estaVacia()) return null;
        return tope.estado;
    }

    public boolean estaVacia() {
        return tope == null;
    }

    public int getTamaño() {
        return tamaño;
    }

    /**
     * Elimina el nodo del fondo (el más antiguo) para respetar la capacidad máxima.
     */
    private void eliminarFondo() {
        if (tope == null) return;
        if (tope.siguiente == null) {
            tope = null;
            tamaño--;
            return;
        }
        Nodo actual = tope;
        while (actual.siguiente.siguiente != null) {
            actual = actual.siguiente;
        }
        actual.siguiente = null;
        tamaño--;
    }

    public void vaciar() {
        tope = null;
        tamaño = 0;
    }
}