package br.unicentro.oficina;

import br.unicentro.oficina.exception.DAOException;
import br.unicentro.oficina.util.Alertas;
import br.unicentro.oficina.util.ConnectionFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Falha cedo, com mensagem clara, se o banco estiver inacessível.
        try (Connection ignorada = ConnectionFactory.getConnection()) {
            // conexão ok
        } catch (DAOException | SQLException e) {
            Alertas.erro("Não foi possível conectar ao banco de dados.\n\n" + e.getMessage()
                    + "\n\nVerifique se o PostgreSQL está ativo e se o arquivo db.properties está correto.");
            Platform.exit();
            return;
        }

        Parent raiz = FXMLLoader.load(getClass().getResource("/br/unicentro/oficina/view/MainView.fxml"));
        stage.setTitle("Oficina Mecânica - Registro de Atendimentos");
        stage.setScene(new Scene(raiz, 1280, 720));
        stage.setMinWidth(1100);
        stage.setMinHeight(650);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
