package br.unicentro.oficina.controller;

import br.unicentro.oficina.exception.ServiceException;
import br.unicentro.oficina.service.RelatorioService;
import br.unicentro.oficina.service.RelatorioService.LinhaRanking;
import br.unicentro.oficina.service.RelatorioService.Resumo;
import br.unicentro.oficina.util.Alertas;
import br.unicentro.oficina.util.Formatos;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;

public class RelatorioController {

    @FXML private DatePicker dtInicio;
    @FXML private DatePicker dtFim;
    @FXML private Label lblFaturamento;
    @FXML private Label lblTotalAtendimentos;
    @FXML private Label lblEmAndamento;
    @FXML private Label lblFinalizados;
    @FXML private Label lblTicketMedio;
    @FXML private TableView<LinhaRanking> tabelaRanking;
    @FXML private TableColumn<LinhaRanking, String> colRankCliente;
    @FXML private TableColumn<LinhaRanking, String> colRankQtd;
    @FXML private TableColumn<LinhaRanking, String> colRankTotal;

    private final RelatorioService service = new RelatorioService();
    private final ObservableList<LinhaRanking> ranking = FXCollections.observableArrayList();
    private Resumo resumoAtual;

    @FXML
    private void initialize() {
        colRankCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cliente()));
        colRankQtd.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().atendimentos())));
        colRankTotal.setCellValueFactory(c -> new SimpleStringProperty(Formatos.moeda(c.getValue().total())));
        tabelaRanking.setItems(ranking);

        LocalDate hoje = LocalDate.now();
        dtInicio.setValue(hoje.withDayOfMonth(1));
        dtFim.setValue(hoje);
        Platform.runLater(this::gerar);
    }

    /** Chamado pelo MainController ao selecionar a aba. */
    public void recarregar() {
        gerar();
    }

    @FXML
    private void gerar() {
        try {
            resumoAtual = service.gerar(dtInicio.getValue(), dtFim.getValue());
            lblFaturamento.setText(Formatos.moeda(resumoAtual.faturamento()));
            lblTotalAtendimentos.setText(String.valueOf(resumoAtual.totalAtendimentos()));
            lblEmAndamento.setText(String.valueOf(resumoAtual.emAndamento()));
            lblFinalizados.setText(String.valueOf(resumoAtual.finalizados()));
            lblTicketMedio.setText(Formatos.moeda(resumoAtual.ticketMedio()));
            ranking.setAll(resumoAtual.ranking());
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }

    @FXML
    private void mesAtual() {
        LocalDate hoje = LocalDate.now();
        dtInicio.setValue(hoje.withDayOfMonth(1));
        dtFim.setValue(hoje);
        gerar();
    }

    @FXML
    private void todoPeriodo() {
        dtInicio.setValue(null);
        dtFim.setValue(null);
        gerar();
    }

    @FXML
    private void exportar() {
        if (resumoAtual == null) {
            Alertas.erro("Gere o resumo antes de exportar.");
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Exportar atendimentos");
        chooser.setInitialFileName("atendimentos.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo CSV", "*.csv"));
        File destino = chooser.showSaveDialog(tabelaRanking.getScene().getWindow());
        if (destino == null) {
            return;
        }
        try {
            service.exportarCsv(resumoAtual.atendimentos(), destino);
            Alertas.info("Arquivo exportado com sucesso:\n" + destino.getAbsolutePath());
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }
}
