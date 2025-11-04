package com.stark.duckmodbus.controllers;

import com.stark.duckmodbus.models.HistoricModel;
import com.stark.duckmodbus.services.HistoricService;
import com.stark.duckmodbus.services.SerialService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    private static final Logger logger = Logger.getLogger(MainController.class.getName());

    private Stage parameterStage;
    private Stage commandsStage;
    private Stage historicStage;
    private Stage configurationStage;
    private volatile String lastSentRequest = "";
    @FXML private Label statusText;
    @FXML private Rectangle statusLed;
    @FXML private TextArea receiver;
    @FXML private TextArea sender;

    private Thread serialThread;
    private volatile boolean running = false;

    @FXML
    public void initialize() {
        logger.info("MainController inicializado");
        startSerialReader();
    }

    // -----------------------------
    //        ABERTURA DE MENUS
    // -----------------------------

    @FXML
    public void openConnectionMenu() {
        openNewStageSafe("/ConnectionMenu.fxml", "Conexão", 450, 700,
                () -> parameterStage, s -> parameterStage = s);
    }

    @FXML
    public void openCommandsMenu() {
        openNewStageSafe("/CommandsMenu.fxml", "Comandos", 800, 600,
                () -> commandsStage, s -> commandsStage = s);
    }

    @FXML
    public void openHistoricMenu() {
        openNewStageSafe("/HistoricMenu.fxml", "Histórico", 700, 500,
                () -> historicStage, s -> historicStage = s);
    }

    @FXML
    public void openConfigurationMenu() {
        openNewStageSafe("/ConfigurationMenu.fxml", "Configuração", 600, 400,
                () -> configurationStage, s -> configurationStage = s);
    }

    private void openNewStageSafe(String fxmlPath, String title, int w, int h,
                                  Supplier<Stage> getter, Consumer<Stage> setter) {
        try {
            Stage existing = getter.get();
            if (existing != null && existing.isShowing()) {
                existing.toFront();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, w, h));
            stage.initOwner(statusText.getScene().getWindow());
            setter.accept(stage);
            stage.setOnHidden(e -> setter.accept(null));
            stage.show();

            logger.info("Janela aberta: " + title);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erro ao abrir janela: " + title, e);
            showStatus("Erro ao abrir " + title, "red");
        }
    }

    // -----------------------------
    //       LEITURA SERIAL
    // -----------------------------

    private void startSerialReader() {
        running = true;
        serialThread = new Thread(() -> {
            logger.info("Iniciando thread de leitura serial...");
            SerialService serial = SerialService.getInstance();
            StringBuilder buffer = new StringBuilder(); // Acumula fragmentos

            while (running) {
                try {
                    if (serial != null && serial.isConnected()) {
                        String data = serial.read(); // Pode ser parcial
                        if (data != null && !data.isEmpty()) {
                            buffer.append(data);

                            // Supondo que cada mensagem termina com '\n'
                            int index;
                            while ((index = buffer.indexOf("\n")) != -1) {
                                String completeMessage = buffer.substring(0, index).trim();
                                buffer.delete(0, index + 1); // Remove mensagem processada

                                String finalMessage = completeMessage;
                                Platform.runLater(() -> receiver.appendText(finalMessage + "\n"));

                                // Salvar histórico
                                try {
                                    HistoricModel model = new HistoricModel(
                                            lastSentRequest,
                                            finalMessage,
                                            LocalDate.now(),
                                            lastSentRequest.length()
                                    );
                                    HistoricService.saveHistoric(model);
                                } catch (Exception e) {
                                    logger.warning("Falha ao salvar histórico: " + e.getMessage());
                                }

                                logger.fine("Recebido: " + finalMessage.replace("\n", "\\n"));
                            }
                        }
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Erro na leitura serial", e);
                }
            }

            logger.info("Thread de leitura serial encerrada.");
        }, "Serial-Reader");

        serialThread.setDaemon(true);
        serialThread.start();
    }


    // -----------------------------
    //         ENVIO SERIAL
    // -----------------------------


    @FXML
    public void sendRequest() {
        String text = sender.getText();
        if (text == null || text.trim().isEmpty()) return;

        final String msg = text.trim();
        lastSentRequest = msg;
        SerialService serial = SerialService.getInstance();

        if (serial == null || !serial.isConnected()) {
            logger.warning("Tentativa de envio sem conexão serial.");
            showStatus("Sem conexão serial", "orange");
            return;
        }

        // Envio assíncrono para não travar o JavaFX thread
        new Thread(() -> {
            try {
                serial.send(msg);
                logger.info("Enviado: " + msg);

                Platform.runLater(() -> {
                    receiver.appendText("> " + msg + "\n");
                    sender.clear();
                    showStatus("Enviado", "green");
                });
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Falha ao enviar mensagem", e);
                Platform.runLater(() -> showStatus("Erro ao enviar", "red"));
            }
        }, "Serial-Sender").start();
    }

    // -----------------------------
    //         STATUS UI
    // -----------------------------

    private void showStatus(String text, String color) {
        Platform.runLater(() -> {
            statusText.setText(text);
            statusText.setStyle("-fx-text-fill: " + color + ";");
            statusLed.setStyle("-fx-fill: " + color + ";");
        });
    }
    // -----------------------------
    //       ENCERRAMENTO LIMPO
    // -----------------------------

    public void stop() {
        running = false;
        if (serialThread != null) {
            serialThread.interrupt();
            try {
                serialThread.join(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Fecha e flush nos handlers configurados globalmente
        for (Handler h : logger.getHandlers()) {
            try {
                h.flush();
                h.close();
            } catch (Exception ignored) {}
        }

        logger.info("MainController parado com segurança.");
    }
    public void clean(){
        receiver.setText("");
        logger.info("Receiver terminal cleared");
    }
    public void export(){
        logger.info("export()");
    }
    public void pause(){
        logger.info("pause()");
    }
}
