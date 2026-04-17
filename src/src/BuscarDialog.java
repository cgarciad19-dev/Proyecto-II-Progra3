package src;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Diálogo de búsqueda.
 * Usa el ABB para búsqueda eficiente O(log n) y resalta resultados en el JTextArea.
 */
public class BuscarDialog extends JDialog {

    private final JTextArea areTexto;
    private final ArbolBusqueda arbol;

    private JTextField campoBusqueda;
    private JLabel lblResultado;
    private JButton btnBuscar;
    private JButton btnSiguiente;
    private JButton btnAnterior;
    private JButton btnLimpiar;

    private List<Integer> posicionesEncontradas;
    private int indiceActual = -1;

    // Color de resaltado
    private static final Color COLOR_RESALTADO      = new Color(255, 220, 50);
    private static final Color COLOR_RESALTADO_ACT  = new Color(255, 120, 0);

    public BuscarDialog(JFrame padre, JTextArea areTexto, ArbolBusqueda arbol) {
        super(padre, "Buscar", false); // No modal para poder editar mientras se busca
        this.areTexto = areTexto;
        this.arbol = arbol;
        initUI();
        setSize(420, 160);
        setLocationRelativeTo(padre);
        setResizable(false);
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Panel superior: campo de texto
        JPanel panelBusqueda = new JPanel(new BorderLayout(6, 0));
        panelBusqueda.add(new JLabel("Buscar:"), BorderLayout.WEST);
        campoBusqueda = new JTextField();
        campoBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panelBusqueda.add(campoBusqueda, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        btnBuscar    = new JButton("Buscar");
        btnAnterior  = new JButton("◀ Anterior");
        btnSiguiente = new JButton("Siguiente ▶");
        btnLimpiar   = new JButton("Limpiar");

        btnAnterior.setEnabled(false);
        btnSiguiente.setEnabled(false);
        btnLimpiar.setEnabled(false);

        panelBotones.add(btnBuscar);
        panelBotones.add(btnAnterior);
        panelBotones.add(btnSiguiente);
        panelBotones.add(btnLimpiar);

        // Etiqueta de resultado
        lblResultado = new JLabel(" ");
        lblResultado.setHorizontalAlignment(SwingConstants.CENTER);
        lblResultado.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        add(panelBusqueda, BorderLayout.NORTH);
        add(panelBotones,  BorderLayout.CENTER);
        add(lblResultado,  BorderLayout.SOUTH);

        // Acciones
        btnBuscar.addActionListener(e -> realizarBusqueda());
        btnSiguiente.addActionListener(e -> navegarSiguiente());
        btnAnterior.addActionListener(e -> navegarAnterior());
        btnLimpiar.addActionListener(e -> limpiarResaltado());

        // Enter en el campo dispara búsqueda
        campoBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) realizarBusqueda();
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
            }
        });
    }

    // ─────────────────────────────────────────────
    //  LÓGICA DE BÚSQUEDA
    // ─────────────────────────────────────────────

    private void realizarBusqueda() {
        String termino = campoBusqueda.getText().trim();
        if (termino.isEmpty()) {
            lblResultado.setForeground(Color.RED);
            lblResultado.setText("Ingresa una palabra para buscar.");
            return;
        }

        limpiarResaltado();

        // 1. Usar el ABB para verificar si la palabra existe (O log n)
        String textoActual = areTexto.getText();
        arbol.reconstruir(textoActual);
        List<Integer> posicionesABB = arbol.buscar(termino);

        if (posicionesABB.isEmpty()) {
            lblResultado.setForeground(Color.RED);
            lblResultado.setText("No se encontró \"" + termino + "\".");
            btnSiguiente.setEnabled(false);
            btnAnterior.setEnabled(false);
            btnLimpiar.setEnabled(false);
            return;
        }

        // 2. Recolectar posiciones exactas con indexOf (case-insensitive)
        //    para que el resaltado funcione con mayúsculas y minúsculas
        posicionesEncontradas = new ArrayList<>();
        String textoLower  = textoActual.toLowerCase();
        String terminoLower = termino.toLowerCase();
        int idx = 0;
        while ((idx = textoLower.indexOf(terminoLower, idx)) != -1) {
            posicionesEncontradas.add(idx);
            idx += terminoLower.length();
        }

        if (posicionesEncontradas.isEmpty()) {
            lblResultado.setForeground(Color.RED);
            lblResultado.setText("No se encontró \"" + termino + "\".");
            return;
        }

        // 3. Resaltar todas las ocurrencias
        resaltarTodas(termino.length());
        indiceActual = 0;
        resaltarActiva(termino.length(), indiceActual);

        lblResultado.setForeground(new Color(0, 120, 0));
        lblResultado.setText(posicionesEncontradas.size() + " resultado(s) — Resultado 1 de " + posicionesEncontradas.size());

        btnSiguiente.setEnabled(posicionesEncontradas.size() > 1);
        btnAnterior.setEnabled(posicionesEncontradas.size() > 1);
        btnLimpiar.setEnabled(true);
    }

    private void navegarSiguiente() {
        if (posicionesEncontradas == null || posicionesEncontradas.isEmpty()) return;
        indiceActual = (indiceActual + 1) % posicionesEncontradas.size();
        resaltarActiva(campoBusqueda.getText().trim().length(), indiceActual);
        actualizarEtiqueta();
    }

    private void navegarAnterior() {
        if (posicionesEncontradas == null || posicionesEncontradas.isEmpty()) return;
        indiceActual = (indiceActual - 1 + posicionesEncontradas.size()) % posicionesEncontradas.size();
        resaltarActiva(campoBusqueda.getText().trim().length(), indiceActual);
        actualizarEtiqueta();
    }

    private void actualizarEtiqueta() {
        lblResultado.setForeground(new Color(0, 120, 0));
        lblResultado.setText(posicionesEncontradas.size() + " resultado(s) — Resultado "
                + (indiceActual + 1) + " de " + posicionesEncontradas.size());
    }

    // ─────────────────────────────────────────────
    //  RESALTADO EN EL TEXTO
    // ─────────────────────────────────────────────

    private void resaltarTodas(int len) {
        Highlighter hl = areTexto.getHighlighter();
        Highlighter.HighlightPainter pintor = new DefaultHighlighter.DefaultHighlightPainter(COLOR_RESALTADO);
        for (int pos : posicionesEncontradas) {
            try {
                hl.addHighlight(pos, pos + len, pintor);
            } catch (BadLocationException ignored) {}
        }
    }

    private void resaltarActiva(int len, int indice) {
        // Limpiar solo los highlights, sin tocar posicionesEncontradas
        areTexto.getHighlighter().removeAllHighlights();
        resaltarTodas(len);

        int pos = posicionesEncontradas.get(indice);
        Highlighter hl = areTexto.getHighlighter();
        Highlighter.HighlightPainter pintorActivo = new DefaultHighlighter.DefaultHighlightPainter(COLOR_RESALTADO_ACT);
        try {
            hl.addHighlight(pos, pos + len, pintorActivo);
            areTexto.setCaretPosition(pos);
        } catch (BadLocationException ignored) {}
    }

    public void limpiarResaltado() {
        areTexto.getHighlighter().removeAllHighlights();
        posicionesEncontradas = null;
        indiceActual = -1;
        lblResultado.setText(" ");
        btnSiguiente.setEnabled(false);
        btnAnterior.setEnabled(false);
        btnLimpiar.setEnabled(false);
    }
}