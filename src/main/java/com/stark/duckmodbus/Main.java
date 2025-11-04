package com.stark.duckmodbus;

import com.stark.duckmodbus.db.Database;
import com.stark.duckmodbus.services.SerialService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception{
        Database.createTable();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            Database.closeConnection();
            SerialService.getInstance().disconnect();
            System.out.println("Fechando aplicação");
        }));
        Parent root = FXMLLoader.load(getClass().getResource("/Main.fxml"));
        Scene scene = new Scene(root,800,600);
        stage.setTitle("Duck Modbus");
        stage.setScene(scene);
        stage.show();
    }
    public static void main(String[] args){
        launch();
    }
}
