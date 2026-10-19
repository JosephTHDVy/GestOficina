package br.unicentro.oficina.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;

import java.io.IOException;
import java.io.UncheckedIOException;

/** Carrega as telas de Clientes, Atendimentos e Resumo dentro das abas. */
public class MainController {

    private static final String VIEW = "/br/unicentro/oficina/view/";

    @FXML private Tab abaClientes;
    @FXML private Tab abaAtendimentos;
    @FXML private Tab abaResumo;

    private ClienteController clienteController;
    private AtendimentoController atendimentoController;
    private RelatorioController relatorioController;

    @FXML
    private void initialize() {
        try {
            FXMLLoader lc = new FXMLLoader(getClass().getResource(VIEW + "ClienteView.fxml"));
            abaClientes.setContent(lc.load());
            clienteController = lc.getController();

            FXMLLoader la = new FXMLLoader(getClass().getResource(VIEW + "AtendimentoView.fxml"));
            abaAtendimentos.setContent(la.load());
            atendimentoController = la.getController();

            FXMLLoader lr = new FXMLLoader(getClass().getResource(VIEW + "RelatorioView.fxml"));
            abaResumo.setContent(lr.load());
            relatorioController = lr.getController();
        } catch (IOException e) {
            throw new UncheckedIOException("Erro ao carregar as telas.", e);
        }

        // Ao voltar para uma aba, recarrega os dados.
        abaClientes.setOnSelectionChanged(e -> {
            if (abaClientes.isSelected()) {
                clienteController.recarregar();
            }
        });
        abaAtendimentos.setOnSelectionChanged(e -> {
            if (abaAtendimentos.isSelected()) {
                atendimentoController.recarregar();
            }
        });
        abaResumo.setOnSelectionChanged(e -> {
            if (abaResumo.isSelected()) {
                relatorioController.recarregar();
            }
        });
    }
}
