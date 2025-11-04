package com.stark.duckmodbus.models;

import java.time.LocalDate;

public class HistoricModel {
    private String request;
    private String response;
    private LocalDate date;
    private int data;

    public HistoricModel() {
        // construtor padrão
    }

    public HistoricModel(String request, String response, LocalDate date, int data) {
        this.request = request;
        this.response = response;
        this.date = date;
        this.data = data;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getData() {
        return data;
    }

    public void setData(int data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "HistoricModel{" +
                "request='" + request + '\'' +
                ", response='" + response + '\'' +
                ", date=" + date +
                ", data=" + data +
                '}';
    }
}
