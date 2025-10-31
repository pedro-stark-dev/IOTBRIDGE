package com.stark.duckmodbus.controllers;

import com.stark.duckmodbus.models.SerialModel;
import com.stark.duckmodbus.services.SerialService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class ConnectionMenuController implements Initializable {

    private final SerialService serialService = SerialService.getInstance();

    @FXML
    private ChoiceBox<String> SerialPort;
    @FXML
    private ChoiceBox<String> Velocity;
    @FXML
    private ChoiceBox<String> Parity;
    @FXML
    private ChoiceBox<String> StopBits;
    @FXML
    private ChoiceBox<String> DataBits;
    @FXML
    private Label StatusText;
    @FXML
    private Rectangle StatusLed;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Configuração das ChoiceBoxes...
        SerialPort.getItems().addAll(SerialService.getAvailablePorts());
        Velocity.getItems().addAll("110","300","600","1200","2400","4800","9600","14400","19200",
                "28800","38400","57600","115200","128000","230400","256000",
                "460800","500000","921600","1000000","1500000","2000000");
        Parity.getItems().addAll("None","Even","Odd","Mark","Space");
        StopBits.getItems().addAll("1","1.5","2");
        DataBits.getItems().addAll("5","6","7","8");

        // Seleciona valores padrão
        if (!SerialPort.getItems().isEmpty()) SerialPort.setValue(SerialPort.getItems().get(0));
        Velocity.setValue("9600");
        Parity.setValue("Even");
        StopBits.setValue("1");
        DataBits.setValue("7");

        // Atualiza o status visual conforme o estado atual
        if (SerialService.getInstance().isConnected()) {
            StatusLed.setFill(Color.GREEN);
            StatusText.setText("CONECTADO");
            StatusText.setTextFill(Color.GREEN);
        } else {
            StatusLed.setFill(Color.RED);
            StatusText.setText("DESCONECTADO");
            StatusText.setTextFill(Color.RED);
        }
    }

    // Conecta via Serial
    public void connect() {
        try {
            // Atualiza as configurações do SerialService
            SerialModel model = serialService.getSerialModel();
            model.setPort(SerialPort.getValue());
            model.setBaudRate(Integer.parseInt(Velocity.getValue()));
            model.setParity(Parity.getValue());
            model.setStopBits((int) Float.parseFloat(StopBits.getValue())); // trata "1.5" também
            model.setDataBits(Integer.parseInt(DataBits.getValue()));

            if (serialService.connect()) {
                StatusLed.setFill(Color.GREEN);
                StatusText.setText("CONECTADO");
                StatusText.setTextFill(Color.GREEN);
                System.out.println(model.getSerialConfig());
            } else {
                StatusLed.setFill(Color.RED);
                StatusText.setText("Falha na conexão");
                StatusText.setTextFill(Color.RED);
            }
        } catch (Exception e) {
            e.printStackTrace();
            StatusLed.setFill(Color.RED);
            StatusText.setText("Erro: " + e.getMessage());
            StatusText.setTextFill(Color.RED);
        }
    }

    // Desconecta
    public void disconnect() {
        serialService.disconnect();
        StatusLed.setFill(Color.RED);
        StatusText.setText("DESCONECTADO");
        StatusText.setTextFill(Color.RED);
        System.out.println("Desconectado da porta serial.");
    }
}
