package com.stark.duckmodbus.services;

import com.fazecast.jSerialComm.SerialPort;
import com.stark.duckmodbus.models.SerialModel;

public class SerialService {
    private SerialPort serialPort;
    private SerialModel serialModel;

    private static SerialService instance;

    private SerialService() {
        this.serialModel = new SerialModel();
    }

    public static SerialService getInstance() {
        if (instance == null) {
            instance = new SerialService();
        }
        return instance;
    }

    // Lista todas as portas disponíveis
    public static String[] getAvailablePorts() {
        SerialPort[] ports = SerialPort.getCommPorts();
        String[] portNames = new String[ports.length];
        for (int i = 0; i < ports.length; i++) {
            portNames[i] = ports[i].getSystemPortName();
        }
        return portNames;
    }

    // Conecta usando os dados do SerialModel
    public boolean connect() {
        serialPort = SerialPort.getCommPort(serialModel.getPort());
        serialPort.setComPortParameters(
                serialModel.getBaudRate(),
                serialModel.getDataBits(),
                serialModel.getStopBits(),
                getParityCode(serialModel.getParity())
        );
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 1000, 1000);

        boolean connected = serialPort.openPort();
        if (connected) {
            System.out.println("Conectado com sucesso -> " + serialModel.getSerialConfig());
        } else {
            System.err.println("Falha ao conectar na porta " + serialModel.getPort());
        }
        return connected;
    }

    // Desconecta da porta atual
    public void disconnect() {
        if (serialPort != null && serialPort.isOpen()) {
            serialPort.closePort();
            System.out.println("Porta serial desconectada.");
        }
    }

    // Verifica se há conexão
    public boolean isConnected() {
        return serialPort != null && serialPort.isOpen();
    }

    // Envia dados
    public void send(String data) {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                serialPort.writeBytes(data.getBytes(), data.length());
                System.out.println(data);
                System.out.println(data.length());
            } catch (Exception e) {
                System.err.println("Erro ao enviar dados: " + e.getMessage());
                disconnect(); // desconecta se houver problema
            }
        }
    }

    // Lê dados
    public String read() {
        if (serialPort != null && serialPort.isOpen()) {
            try {
                byte[] buffer = new byte[1024];
                int bytesRead = serialPort.readBytes(buffer, buffer.length);
                if (bytesRead > 0) {
                    return new String(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                System.err.println("Erro ao ler dados: " + e.getMessage());
                disconnect();
            }
        }
        return null;
    }

    // Retorna o modelo atual
    public SerialModel getSerialModel() {
        return serialModel;
    }

    // Converte o nome da paridade para código jSerialComm
    private int getParityCode(String parity) {
        return switch (parity.toLowerCase()) {
            case "even" -> SerialPort.EVEN_PARITY;
            case "odd" -> SerialPort.ODD_PARITY;
            case "mark" -> SerialPort.MARK_PARITY;
            case "space" -> SerialPort.SPACE_PARITY;
            default -> SerialPort.NO_PARITY;
        };
    }
}
