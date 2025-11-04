package com.stark.duckmodbus.services;

import com.stark.duckmodbus.db.Database;
import com.stark.duckmodbus.models.HistoricModel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class HistoricService {
    private static final Logger logger = Logger.getLogger(HistoricService.class.getName());

    // 🔹 Salvar histórico (cada operação abre e fecha a conexão)
    public static void saveHistoric(HistoricModel historic) {
        String sql = "INSERT INTO historic (request, response, date, data) VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             var stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, historic.getRequest());
            stmt.setString(2, historic.getResponse());
            stmt.setString(3, historic.getDate().toString());
            stmt.setInt(4, historic.getData());

            stmt.executeUpdate();
            logger.info("Histórico salvo com sucesso.");

        } catch (Exception e) {
            logger.warning("Erro ao salvar histórico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 🔹 Ler histórico do banco
    public static List<HistoricModel> getHistoric() {
        List<HistoricModel> list = new ArrayList<>();
        String sql = "SELECT request, response, date, data FROM historic ORDER BY id DESC";

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String request = rs.getString("request");
                String response = rs.getString("response");
                LocalDate date = LocalDate.parse(rs.getString("date"));
                int dataLength = rs.getInt("data");

                HistoricModel model = new HistoricModel(request, response, date, dataLength);
                list.add(model);
            }

        } catch (Exception e) {
            logger.warning("Erro ao carregar histórico: " + e.getMessage());
            e.printStackTrace();
        }

        return list;
    }
}
