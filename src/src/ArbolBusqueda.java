package src;

import java.util.ArrayList;
import java.util.List;

/**
 * Árbol Binario de Búsqueda (ABB) que indexa palabras del texto y sus posiciones.
 * Permite búsquedas en O(log n) en el caso promedio.
 */
public class ArbolBusqueda {

    private NodoABB raiz;

    public ArbolBusqueda() {
        raiz = null;
    }

    // ─────────────────────────────────────────────
    //  INSERCIÓN
    // ─────────────────────────────────────────────

    /**
     * Inserta una palabra con su posición en el árbol.
     * Si la palabra ya existe, solo agrega la posición.
     */
    public void insertar(String palabra, int posicion) {
        raiz = insertarRecursivo(raiz, palabra.toLowerCase(), posicion);
    }

    private NodoABB insertarRecursivo(NodoABB nodo, String palabra, int posicion) {
        if (nodo == null) {
            return new NodoABB(palabra, posicion);
        }
        int cmp = palabra.compareTo(nodo.palabra);
        if (cmp < 0) {
            nodo.izquierdo = insertarRecursivo(nodo.izquierdo, palabra, posicion);
        } else if (cmp > 0) {
            nodo.derecho = insertarRecursivo(nodo.derecho, palabra, posicion);
        } else {
            // La palabra ya existe: agregar la nueva posición
            nodo.agregarPosicion(posicion);
        }
        return nodo;
    }

    // ─────────────────────────────────────────────
    //  BÚSQUEDA
    // ─────────────────────────────────────────────

    /**
     * Busca una palabra en el árbol y devuelve la lista de posiciones donde aparece.
     * Retorna lista vacía si no se encuentra.
     * Complejidad: O(log n) caso promedio.
     */
    public List<Integer> buscar(String palabra) {
        NodoABB resultado = buscarRecursivo(raiz, palabra.toLowerCase());
        if (resultado != null) {
            return resultado.posiciones;
        }
        return new ArrayList<>();
    }

    private NodoABB buscarRecursivo(NodoABB nodo, String palabra) {
        if (nodo == null) return null;
        int cmp = palabra.compareTo(nodo.palabra);
        if (cmp == 0) return nodo;
        if (cmp < 0) return buscarRecursivo(nodo.izquierdo, palabra);
        return buscarRecursivo(nodo.derecho, palabra);
    }

    /**
     * Verifica si una palabra existe en el árbol.
     */
    public boolean contiene(String palabra) {
        return buscarRecursivo(raiz, palabra.toLowerCase()) != null;
    }

    // ─────────────────────────────────────────────
    //  RECONSTRUCCIÓN
    // ─────────────────────────────────────────────

    /**
     * Vacía el árbol y lo reconstruye a partir del texto completo.
     * Divide el texto en palabras y registra las posiciones de cada una.
     */
    public void reconstruir(String texto) {
        raiz = null;
        if (texto == null || texto.isEmpty()) return;

        // Recorrer el texto carácter a carácter para registrar posiciones exactas
        int i = 0;
        int len = texto.length();

        while (i < len) {
            // Saltar espacios/puntuación hasta encontrar letra o dígito
            if (!Character.isLetterOrDigit(texto.charAt(i))) {
                i++;
                continue;
            }

            int inicio = i;
            // Leer la palabra completa
            while (i < len && Character.isLetterOrDigit(texto.charAt(i))) {
                i++;
            }
            String palabra = texto.substring(inicio, i);
            insertar(palabra, inicio);
        }
    }

    /**
     * Devuelve todas las palabras del árbol en orden alfabético (recorrido inorden).
     */
    public List<String> obtenerPalabrasOrdenadas() {
        List<String> lista = new ArrayList<>();
        inorden(raiz, lista);
        return lista;
    }

    private void inorden(NodoABB nodo, List<String> lista) {
        if (nodo == null) return;
        inorden(nodo.izquierdo, lista);
        lista.add(nodo.palabra);
        inorden(nodo.derecho, lista);
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public void vaciar() {
        raiz = null;
    }
}