/*
 * Proyecto: Proyecto 3 - Simulación de Asteroides
 * Nombre: Kaleb Yael De La Rosa Gutiérrez
 * Grupo: 7CM1
 */

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.GridLayout;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.Separator;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.util.Arrays;

public class AsteroidClientUI {
    private final AsteroidHttpClient httpClient;
    private Screen screen;
    private MultiWindowTextGUI gui;
    private BasicWindow mainWindow;

    private Label lblConnectionStatus;
    private Label lblAsteroidCounter;
    private Label lblFeedbackMessage;

    private TextBox txtSpeed;
    private TextBox txtDirX;
    private TextBox txtDirY;
    private TextBox txtAngularVelocity;

    private TextBox txtRandomCount;

    public AsteroidClientUI(AsteroidHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public void start() throws IOException {
        DefaultTerminalFactory terminalFactory = new DefaultTerminalFactory();
        terminalFactory.setTerminalEmulatorTitle("PROYECTO 3: CLIENTE LANTERNA - Kaleb Yael De La Rosa Gutiérrez (7CM1)");

        Terminal terminal;
        try {
            terminal = terminalFactory.createTerminal();
        } catch (IOException e) {
            // En Windows estándar (sin Cygwin/stty), Lanterna utiliza su emulador de terminal integrado
            terminalFactory.setPreferTerminalEmulator(true);
            terminal = terminalFactory.createTerminalEmulator();
        }

        screen = new TerminalScreen(terminal);
        screen.startScreen();

        gui = new MultiWindowTextGUI(screen);

        mainWindow = new BasicWindow("PROYECTO 3: CONTROL DE ASTEROIDES EN RED");
        mainWindow.setHints(Arrays.asList(Window.Hint.CENTERED));

        Panel rootPanel = new Panel(new LinearLayout(Direction.VERTICAL));

        // 1. Encabezado institucional
        Panel headerPanel = new Panel(new LinearLayout(Direction.VERTICAL));
        headerPanel.addComponent(new Label("SISTEMAS DISTRIBUIDOS - SIMULACIÓN DE ASTEROIDES"));
        headerPanel.addComponent(new Label("Alumno: Kaleb Yael De La Rosa Gutiérrez | Grupo: 7CM1"));
        headerPanel.addComponent(new Separator(Direction.HORIZONTAL));
        rootPanel.addComponent(headerPanel);

        // 2. Estado de conexión y Contador
        Panel statusPanel = new Panel(new GridLayout(2));
        statusPanel.addComponent(new Label("Servidor HTTP:"));
        lblConnectionStatus = new Label("Comprobando conexión...");
        statusPanel.addComponent(lblConnectionStatus);

        statusPanel.addComponent(new Label("Total de Asteroides Creados:"));
        lblAsteroidCounter = new Label("0");
        lblAsteroidCounter.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT);
        statusPanel.addComponent(lblAsteroidCounter);

        rootPanel.addComponent(statusPanel);
        rootPanel.addComponent(new Separator(Direction.HORIZONTAL));

        // 3. Sección: Creación Manual de Asteroide
        Label lblManualTitle = new Label("--- CREAR UN ASTEROIDE MANUAL ---");
        lblManualTitle.setForegroundColor(TextColor.ANSI.CYAN_BRIGHT);
        rootPanel.addComponent(lblManualTitle);

        Panel manualGrid = new Panel(new GridLayout(2));

        manualGrid.addComponent(new Label("Rapidez (v):"));
        txtSpeed = new TextBox("3.0").setPreferredSize(new TerminalSize(16, 1));
        manualGrid.addComponent(txtSpeed);

        manualGrid.addComponent(new Label("Dirección X (dx):"));
        txtDirX = new TextBox("1.0").setPreferredSize(new TerminalSize(16, 1));
        manualGrid.addComponent(txtDirX);

        manualGrid.addComponent(new Label("Dirección Y (dy):"));
        txtDirY = new TextBox("1.0").setPreferredSize(new TerminalSize(16, 1));
        manualGrid.addComponent(txtDirY);

        manualGrid.addComponent(new Label("Velocidad Angular (+/- ω):"));
        txtAngularVelocity = new TextBox("2.5").setPreferredSize(new TerminalSize(16, 1));
        manualGrid.addComponent(txtAngularVelocity);

        rootPanel.addComponent(manualGrid);

        Panel manualButtonPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        Button btnCreateManual = new Button("Crear Asteroide Individual", new Runnable() {
            @Override
            public void run() {
                handleCreateManual();
            }
        });
        manualButtonPanel.addComponent(btnCreateManual);
        rootPanel.addComponent(manualButtonPanel);

        rootPanel.addComponent(new Separator(Direction.HORIZONTAL));

        // 4. Sección: Creación Masiva de N Asteroides Aleatorios
        Label lblRandomTitle = new Label("--- CREAR N ASTEROIDES ALEATORIOS ---");
        lblRandomTitle.setForegroundColor(TextColor.ANSI.YELLOW_BRIGHT);
        rootPanel.addComponent(lblRandomTitle);

        Panel randomGrid = new Panel(new GridLayout(2));
        randomGrid.addComponent(new Label("Cantidad (n):"));
        txtRandomCount = new TextBox("5").setPreferredSize(new TerminalSize(16, 1));
        randomGrid.addComponent(txtRandomCount);
        rootPanel.addComponent(randomGrid);

        Panel randomButtonsPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        randomButtonsPanel.addComponent(new Button("Crear N Aleatorios", new Runnable() {
            @Override
            public void run() {
                handleCreateRandom();
            }
        }));

        // Atajos para la secuencia exacta del video demostrativo
        randomButtonsPanel.addComponent(new Button("[+5]", new Runnable() {
            @Override
            public void run() {
                triggerBatch(5);
            }
        }));
        randomButtonsPanel.addComponent(new Button("[+10]", new Runnable() {
            @Override
            public void run() {
                triggerBatch(10);
            }
        }));
        randomButtonsPanel.addComponent(new Button("[+1000]", new Runnable() {
            @Override
            public void run() {
                triggerBatch(1000);
            }
        }));

        rootPanel.addComponent(randomButtonsPanel);
        rootPanel.addComponent(new Separator(Direction.HORIZONTAL));

        // 5. Mensaje de retroalimentación
        lblFeedbackMessage = new Label("Listo. Seleccione una opción o ingrese datos.");
        lblFeedbackMessage.setForegroundColor(TextColor.ANSI.WHITE);
        rootPanel.addComponent(lblFeedbackMessage);

        rootPanel.addComponent(new EmptySpace(new TerminalSize(1, 1)));

        // 6. Barra de acciones inferiores
        Panel bottomPanel = new Panel(new LinearLayout(Direction.HORIZONTAL));
        bottomPanel.addComponent(new Button("Actualizar Contador", new Runnable() {
            @Override
            public void run() {
                refreshCounter();
            }
        }));

        bottomPanel.addComponent(new Button("Salir", new Runnable() {
            @Override
            public void run() {
                mainWindow.close();
            }
        }));
        rootPanel.addComponent(bottomPanel);

        mainWindow.setComponent(rootPanel);

        // Inicializar estado y abrir ventana
        refreshConnectionStatus();
        refreshCounter();

        gui.addWindowAndWait(mainWindow);

        // Al salir de la interfaz
        screen.stopScreen();
    }

    private void refreshCounter() {
        int count = httpClient.fetchTotalCreatedCount();
        if (count >= 0) {
            lblAsteroidCounter.setText(String.valueOf(count));
            System.out.println("[Cliente Lanterna] Número de asteroides creados hasta el momento: " + count);
        } else {
            lblAsteroidCounter.setText("Error al consultar");
        }
    }

    private void handleCreateManual() {
        try {
            double speed = Double.parseDouble(txtSpeed.getText().trim());
            double dirX = Double.parseDouble(txtDirX.getText().trim());
            double dirY = Double.parseDouble(txtDirY.getText().trim());
            double angularVel = Double.parseDouble(txtAngularVelocity.getText().trim());

            if (speed <= 0) {
                setFeedback("La rapidez (v) debe ser mayor a 0.", TextColor.ANSI.RED_BRIGHT);
                return;
            }
            if (Math.hypot(dirX, dirY) < 1e-6) {
                setFeedback("El vector (dx, dy) no puede ser (0, 0).", TextColor.ANSI.RED_BRIGHT);
                return;
            }

            boolean ok = httpClient.createManualAsteroid(speed, dirX, dirY, angularVel);
            if (ok) {
                int count = httpClient.fetchTotalCreatedCount();
                lblAsteroidCounter.setText(String.valueOf(count));
                setFeedback("Asteroide individual creado. Total creados hasta el momento: " + count, TextColor.ANSI.GREEN_BRIGHT);
                System.out.println("[Cliente Lanterna] Asteroide manual creado. Total creados hasta el momento: " + count);
            } else {
                setFeedback("Error al contactar al servidor HTTP.", TextColor.ANSI.RED_BRIGHT);
            }
        } catch (NumberFormatException e) {
            setFeedback("Error: Verifique que todos los campos sean numéricos.", TextColor.ANSI.RED_BRIGHT);
        }
    }

    private void handleCreateRandom() {
        try {
            int count = Integer.parseInt(txtRandomCount.getText().trim());
            if (count < 1) {
                setFeedback("La cantidad (n) debe ser al menos 1.", TextColor.ANSI.RED_BRIGHT);
                return;
            }
            triggerBatch(count);
        } catch (NumberFormatException e) {
            setFeedback("Error: Ingrese un número entero válido para n.", TextColor.ANSI.RED_BRIGHT);
        }
    }

    private void triggerBatch(int count) {
        setFeedback("Enviando " + count + " asteroides...", TextColor.ANSI.YELLOW);
        int added = httpClient.createRandomAsteroids(count);
        if (added > 0) {
            int total = httpClient.fetchTotalCreatedCount();
            lblAsteroidCounter.setText(String.valueOf(total));
            setFeedback("¡Se agregaron " + added + " asteroides! Total creados hasta el momento: " + total, TextColor.ANSI.GREEN_BRIGHT);
            System.out.println("[Cliente Lanterna] Se agregaron " + added + " asteroides. Total creados hasta el momento: " + total);
        } else {
            setFeedback("Fallo al agregar asteroides. Verifique el servidor.", TextColor.ANSI.RED_BRIGHT);
        }
    }

    private void refreshConnectionStatus() {
        boolean ok = httpClient.testConnection();
        if (ok) {
            lblConnectionStatus.setText("CONECTADO (" + httpClient.getBaseUrl() + ")");
            lblConnectionStatus.setForegroundColor(TextColor.ANSI.GREEN);
        } else {
            lblConnectionStatus.setText("DESCONECTADO (" + httpClient.getBaseUrl() + ")");
            lblConnectionStatus.setForegroundColor(TextColor.ANSI.RED_BRIGHT);
        }
    }

    private void setFeedback(String msg, TextColor color) {
        lblFeedbackMessage.setText(msg);
        lblFeedbackMessage.setForegroundColor(color);
    }
}
