package com.stark.duckmodbus.models;

public class SerialModel {
    private String Port = "COM1";
    private String Parity = "Even";
    private int BaudRate = 9600;
    private int StopBits = 1;
    private int DataBits = 7;

    public void setPort(String port) {
        this.Port = port;
    }

    public void setParity(String parity) {
        this.Parity = parity;
    }

    public void setBaudRate(int baudRate) {
        this.BaudRate = baudRate;
    }

    public void setStopBits(int stopBits) {
        this.StopBits = stopBits;
    }

    public void setDataBits(int dataBits) {
        this.DataBits = dataBits;
    }

    public String getPort() {
        return Port;
    }

    public String getParity() {
        return Parity;
    }

    public int getBaudRate() {
        return BaudRate;
    }

    public int getStopBits() {
        return StopBits;
    }

    public int getDataBits() {
        return DataBits;
    }

    public String getSerialConfig() {
        return String.format(
                "Port: %s | BaudRate: %d | DataBits: %d | StopBits: %d | Parity: %s",
                Port, BaudRate, DataBits, StopBits, Parity
        );
    }
}
