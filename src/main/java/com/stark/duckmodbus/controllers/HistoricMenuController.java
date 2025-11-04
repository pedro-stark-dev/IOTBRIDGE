package com.stark.duckmodbus.controllers;

import com.stark.duckmodbus.models.HistoricModel;
import com.stark.duckmodbus.services.HistoricService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HistoricMenuController {

    private final ObservableList<HistoricModel> historicList = FXCollections.observableArrayList();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    @FXML
    private TableView<HistoricModel> TableHistoric;
    @FXML
    private TableColumn<HistoricModel, String> colDate;
    @FXML
    private TableColumn<HistoricModel, String> colSend;
    @FXML
    private TableColumn<HistoricModel, String> colRes;
    @FXML
    private TableColumn<HistoricModel, Integer> colLenght;

    @FXML
    public void initialize() {
        // Configura as colunas para usar as propriedades do HistoricModel
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colSend.setCellValueFactory(new PropertyValueFactory<>("request"));
        colRes.setCellValueFactory(new PropertyValueFactory<>("response"));
        colLenght.setCellValueFactory(new PropertyValueFactory<>("data"));

        // Define a lista como itens da tabela
        TableHistoric.setItems(historicList);

        // Carrega histórico imediatamente
        loadHistoric();

        // Atualiza a cada 5 segundos
        scheduler.scheduleAtFixedRate(() -> {

            List<HistoricModel> listFromDB = HistoricService.getHistoric();
            Platform.runLater(() -> {
                historicList.clear();
                historicList.addAll(listFromDB);
            });
        }, 5, 5, TimeUnit.SECONDS);
    }

    private void loadHistoric() {
        try {
            List<HistoricModel> listFromDB = HistoricService.getHistoric();
            historicList.clear();
            historicList.addAll(listFromDB);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // metodo para encerrar o scheduler ao fechar a janela
    public void stopUpdater() {
        scheduler.shutdownNow();
    }
}
