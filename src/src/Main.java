package src;

import javax.swing.*;

/**
 * Punto de entrada del Editor de Texto Avanzado.
 *
 * Estructuras de datos implementadas:
 *  - PilaHistorial  → Deshacer (Undo) y Rehacer (Redo)
 *  - ArbolBusqueda  → Árbol Binario de Búsqueda (ABB) para búsqueda eficiente O(log n)
 */
public class Main {
    public static void main(String[] args) {
        // Usar el look & feel del sistema operativo para mejor apariencia
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Lanzar en el Event Dispatch Thread (EDT) de Swing
        SwingUtilities.invokeLater(EditorTexto::new);
    }
}