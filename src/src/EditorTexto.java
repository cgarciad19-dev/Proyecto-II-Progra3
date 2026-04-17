package src;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;

/**
 * Ventana principal del Editor de Texto avanzado.
 * Incluye: Deshacer, Rehacer y Buscar con ABB.
 */
public class EditorTexto extends JFrame {

    // ─── Componentes gráficos ───────────────────
    private JTextArea areaTexto;
    private JLabel lblEstado;
    private JLabel lblPosicion;

    // ─── Estructuras de datos ───────────────────
    private final PilaHistorial pilaDeshacer = new PilaHistorial();
    private final PilaHistorial pilaRehacer  = new PilaHistorial();
    private final ArbolBusqueda arbolBusqueda = new ArbolBusqueda();

    // ─── Estado interno ─────────────────────────
    private String estadoAnterior = "";    // Snapshot antes de cada cambio
    private boolean grabandoCambio = false; // Evita ciclos al restaurar texto
    private File archivoActual = null;
    private boolean textoModificado = false;

    // ─── Diálogo de búsqueda ────────────────────
    private BuscarDialog dialogoBuscar;

    // ─── Acciones del menú ──────────────────────
    private JMenuItem mnuDeshacer;
    private JMenuItem mnuRehacer;

    public EditorTexto() {
        super("Editor de Texto Avanzado");
        initUI();
        initMenu();
        initListeners();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                salirConConfirmacion();
            }
        });
        setSize(900, 650);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // ─────────────────────────────────────────────
    //  CONSTRUCCIÓN DE LA INTERFAZ
    // ─────────────────────────────────────────────

    private void initUI() {
        setLayout(new BorderLayout());

        // Barra de herramientas rápida
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

        JButton btnDeshacer = crearBotonToolbar("Deshacer", "Deshacer último cambio (Ctrl+Z)");
        JButton btnRehacer  = crearBotonToolbar("Rehacer",  "Rehacer cambio (Ctrl+Y)");
        JButton btnBuscar   = crearBotonToolbar("Buscar",   "Buscar en el texto (Ctrl+F)");
        JButton btnNuevo    = crearBotonToolbar("Nuevo",    "Nuevo archivo (Ctrl+N)");
        JButton btnAbrir    = crearBotonToolbar("Abrir",    "Abrir archivo (Ctrl+O)");
        JButton btnGuardar  = crearBotonToolbar("Guardar",  "Guardar archivo (Ctrl+S)");

        toolbar.add(btnNuevo);
        toolbar.add(btnAbrir);
        toolbar.add(btnGuardar);
        toolbar.addSeparator();
        toolbar.add(btnDeshacer);
        toolbar.add(btnRehacer);
        toolbar.addSeparator();
        toolbar.add(btnBuscar);

        btnDeshacer.addActionListener(e -> deshacer());
        btnRehacer.addActionListener(e  -> rehacer());
        btnBuscar.addActionListener(e   -> abrirBuscar());
        btnNuevo.addActionListener(e    -> nuevoArchivo());
        btnAbrir.addActionListener(e    -> abrirArchivo());
        btnGuardar.addActionListener(e  -> guardarArchivo());

        // Área de texto
        areaTexto = new JTextArea();
        areaTexto.setFont(new Font("Consolas", Font.PLAIN, 14));
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setMargin(new Insets(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(areaTexto);

        // Barra de estado
        JPanel barraEstado = new JPanel(new BorderLayout());
        barraEstado.setBorder(BorderFactory.createEtchedBorder());
        lblEstado   = new JLabel("  Listo");
        lblPosicion = new JLabel("Ln 1, Col 1  ");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPosicion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        barraEstado.add(lblEstado,   BorderLayout.WEST);
        barraEstado.add(lblPosicion, BorderLayout.EAST);

        add(toolbar,     BorderLayout.NORTH);
        add(scroll,      BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);
    }

    private JButton crearBotonToolbar(String texto, String tooltip) {
        JButton btn = new JButton(texto);
        btn.setToolTipText(tooltip);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return btn;
    }

    // ─────────────────────────────────────────────
    //  MENÚ
    // ─────────────────────────────────────────────

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        // ── Archivo ──
        JMenu mnuArchivo = new JMenu("Archivo");
        mnuArchivo.setMnemonic(KeyEvent.VK_A);
        agregarItem(mnuArchivo, "Nuevo",    KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), e -> nuevoArchivo());
        agregarItem(mnuArchivo, "Abrir…",   KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK), e -> abrirArchivo());
        agregarItem(mnuArchivo, "Guardar",  KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), e -> guardarArchivo());
        agregarItem(mnuArchivo, "Guardar como…", null, e -> guardarComo());
        mnuArchivo.addSeparator();
        agregarItem(mnuArchivo, "Salir", KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK), e -> salirConConfirmacion());

        // ── Editar ──
        JMenu mnuEditar = new JMenu("Editar");
        mnuEditar.setMnemonic(KeyEvent.VK_E);
        mnuDeshacer = agregarItem(mnuEditar, "Deshacer", KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), e -> deshacer());
        mnuRehacer  = agregarItem(mnuEditar, "Rehacer",  KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), e -> rehacer());
        mnuEditar.addSeparator();
        agregarItem(mnuEditar, "Seleccionar todo", KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK), e -> areaTexto.selectAll());

        // ── Buscar ──
        JMenu mnuBuscar = new JMenu("Buscar");
        mnuBuscar.setMnemonic(KeyEvent.VK_B);
        agregarItem(mnuBuscar, "Buscar…", KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK), e -> abrirBuscar());

        // ── Ayuda ──
        JMenu mnuAyuda = new JMenu("Ayuda");
        agregarItem(mnuAyuda, "Acerca de…", null, e -> mostrarAcercaDe());

        menuBar.add(mnuArchivo);
        menuBar.add(mnuEditar);
        menuBar.add(mnuBuscar);
        menuBar.add(mnuAyuda);
        setJMenuBar(menuBar);

        actualizarEstadoMenus();
    }

    private JMenuItem agregarItem(JMenu menu, String nombre, KeyStroke atajo, ActionListener accion) {
        JMenuItem item = new JMenuItem(nombre);
        if (atajo != null) item.setAccelerator(atajo);
        item.addActionListener(accion);
        menu.add(item);
        return item;
    }

    // ─────────────────────────────────────────────
    //  LISTENERS DEL ÁREA DE TEXTO
    // ─────────────────────────────────────────────

    private void initListeners() {
        // DocumentListener: detecta cada cambio en el texto
        areaTexto.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { alCambiar(); }
            @Override public void removeUpdate(DocumentEvent e)  { alCambiar(); }
            @Override public void changedUpdate(DocumentEvent e) { /* atributos de formato */ }
        });

        // CaretListener: actualizar Ln/Col en la barra de estado
        areaTexto.addCaretListener(e -> actualizarPosicionCaret());
    }

    /**
     * Llamado por el DocumentListener ante cada cambio.
     * Guarda el snapshot ANTERIOR en la pila de deshacer.
     */
    private void alCambiar() {
        if (grabandoCambio) return; // Evitar bucle al restaurar

        String textoNuevo = areaTexto.getText();

        // Solo apilar si realmente cambió algo
        if (!textoNuevo.equals(estadoAnterior)) {
            pilaDeshacer.apilar(estadoAnterior);
            pilaRehacer.vaciar(); // Un cambio nuevo invalida el historial de rehacer
            estadoAnterior = textoNuevo;
            textoModificado = true;
            actualizarEstadoMenus();
            actualizarTitulo();
            lblEstado.setText("  Editando…");
        }
    }

    // ─────────────────────────────────────────────
    //  DESHACER / REHACER
    // ─────────────────────────────────────────────

    private void deshacer() {
        if (pilaDeshacer.estaVacia()) {
            lblEstado.setText("  Nada que deshacer.");
            return;
        }
        // Guardar estado actual en pila de rehacer ANTES de restaurar
        pilaRehacer.apilar(areaTexto.getText());

        grabandoCambio = true;
        String estadoPrevio = pilaDeshacer.desapilar();
        areaTexto.setText(estadoPrevio);
        estadoAnterior = estadoPrevio;
        grabandoCambio = false;

        actualizarEstadoMenus();
        lblEstado.setText("  Deshecho. (Deshacer: " + pilaDeshacer.getTamaño() + " | Rehacer: " + pilaRehacer.getTamaño() + ")");
    }

    private void rehacer() {
        if (pilaRehacer.estaVacia()) {
            lblEstado.setText("  Nada que rehacer.");
            return;
        }
        // Guardar estado actual en pila de deshacer antes de avanzar
        pilaDeshacer.apilar(areaTexto.getText());

        grabandoCambio = true;
        String estadoSiguiente = pilaRehacer.desapilar();
        areaTexto.setText(estadoSiguiente);
        estadoAnterior = estadoSiguiente;
        grabandoCambio = false;

        actualizarEstadoMenus();
        lblEstado.setText("  Rehecho. (Deshacer: " + pilaDeshacer.getTamaño() + " | Rehacer: " + pilaRehacer.getTamaño() + ")");
    }

    // ─────────────────────────────────────────────
    //  BUSCAR
    // ─────────────────────────────────────────────

    private void abrirBuscar() {
        if (dialogoBuscar == null || !dialogoBuscar.isDisplayable()) {
            dialogoBuscar = new BuscarDialog(this, areaTexto, arbolBusqueda);
        }
        dialogoBuscar.setVisible(true);
        dialogoBuscar.toFront();
    }

    // ─────────────────────────────────────────────
    //  MANEJO DE ARCHIVOS
    // ─────────────────────────────────────────────

    private void nuevoArchivo() {
        if (textoModificado && !confirmarDescartar()) return;
        grabandoCambio = true;
        areaTexto.setText("");
        grabandoCambio = false;
        estadoAnterior = "";
        pilaDeshacer.vaciar();
        pilaRehacer.vaciar();
        archivoActual = null;
        textoModificado = false;
        actualizarTitulo();
        actualizarEstadoMenus();
        lblEstado.setText("  Nuevo archivo.");
    }

    private void abrirArchivo() {
        if (textoModificado && !confirmarDescartar()) return;
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File archivo = fc.getSelectedFile();
                String contenido = Files.readString(archivo.toPath());
                grabandoCambio = true;
                areaTexto.setText(contenido);
                grabandoCambio = false;
                estadoAnterior = contenido;
                archivoActual = archivo;
                textoModificado = false;
                pilaDeshacer.vaciar();
                pilaRehacer.vaciar();
                actualizarTitulo();
                actualizarEstadoMenus();
                lblEstado.setText("  Archivo abierto: " + archivo.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al abrir el archivo:\n" + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void guardarArchivo() {
        if (archivoActual == null) {
            guardarComo();
        } else {
            guardarEn(archivoActual);
        }
    }

    private void guardarComo() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();
            if (!archivo.getName().endsWith(".txt")) {
                archivo = new File(archivo.getAbsolutePath() + ".txt");
            }
            guardarEn(archivo);
        }
    }

    private void guardarEn(File archivo) {
        try {
            Files.writeString(archivo.toPath(), areaTexto.getText());
            archivoActual = archivo;
            textoModificado = false;
            actualizarTitulo();
            lblEstado.setText("  Guardado: " + archivo.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ─────────────────────────────────────────────
    //  UTILIDADES DE INTERFAZ
    // ─────────────────────────────────────────────

    private void actualizarEstadoMenus() {
        if (mnuDeshacer != null) mnuDeshacer.setEnabled(!pilaDeshacer.estaVacia());
        if (mnuRehacer  != null) mnuRehacer.setEnabled(!pilaRehacer.estaVacia());
    }

    private void actualizarTitulo() {
        String nombre = archivoActual != null ? archivoActual.getName() : "Sin título";
        setTitle((textoModificado ? "* " : "") + nombre + " — Editor de Texto Avanzado");
    }

    private void actualizarPosicionCaret() {
        try {
            int pos = areaTexto.getCaretPosition();
            int fila = 0;
            int col = 0;
            String texto = areaTexto.getText(0, pos);
            for (char c : texto.toCharArray()) {
                if (c == '\n') { fila++; col = 0; }
                else col++;
            }
            lblPosicion.setText("Ln " + (fila + 1) + ", Col " + (col + 1) + "  ");
        } catch (Exception ignored) {}
    }

    private boolean confirmarDescartar() {
        int op = JOptionPane.showConfirmDialog(this,
                "Hay cambios sin guardar. ¿Deseas continuar sin guardar?",
                "Cambios sin guardar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        return op == JOptionPane.YES_OPTION;
    }

    private void salirConConfirmacion() {
        if (textoModificado && !confirmarDescartar()) return;
        System.exit(0);
    }

    private void mostrarAcercaDe() {
        JOptionPane.showMessageDialog(this,
                "Editor de Texto Avanzado\n\n" +
                        "Estructuras de datos utilizadas:\n" +
                        "  • Pila (Stack) — Deshacer / Rehacer\n" +
                        "  • Árbol Binario de Búsqueda (ABB) — Búsqueda de palabras\n\n" +
                        "Universidad Mariano Gálvez de Guatemala\n" +
                        "Programación 3",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE);
    }
}