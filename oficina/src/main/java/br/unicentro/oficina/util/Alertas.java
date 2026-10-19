package br.unicentro.oficina.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public final class Alertas {

    private Alertas() { }

    public static void erro(String mensagem) {
        exibir(Alert.AlertType.ERROR, "Erro", mensagem);
    }

    public static void info(String mensagem) {
        exibir(Alert.AlertType.INFORMATION, "Informação", mensagem);
    }

    public static boolean confirmar(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, mensagem, ButtonType.YES, ButtonType.NO);
        alerta.setTitle("Confirmação");
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(b -> b == ButtonType.YES).isPresent();
    }

    private static void exibir(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo, mensagem, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
