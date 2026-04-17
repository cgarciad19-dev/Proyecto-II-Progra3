package src;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del Árbol Binario de Búsqueda (ABB).
 * Almacena una palabra como clave y la lista de posiciones donde aparece en el texto.
 */
public class NodoABB {

    String palabra;               // Clave del nodo (palabra en minúsculas para comparación uniforme)
    List<Integer> posiciones;     // Posiciones (índices) donde aparece la palabra en el texto
    NodoABB izquierdo;
    NodoABB derecho;

    public NodoABB(String palabra, int posicion) {
        this.palabra = palabra.toLowerCase();
        this.posiciones = new ArrayList<>();
        this.posiciones.add(posicion);
        this.izquierdo = null;
        this.derecho = null;
    }

    /**
     * Agrega una nueva posición para esta palabra.
     */
    public void agregarPosicion(int posicion) {
        posiciones.add(posicion);
    }

    @Override
    public String toString() {
        return "\"" + palabra + "\" en posiciones: " + posiciones;
    }
}