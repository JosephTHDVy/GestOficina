package br.unicentro.oficina.controller;

import br.unicentro.oficina.exception.ServiceException;
import br.unicentro.oficina.model.Cliente;
import br.unicentro.oficina.service.ClienteService;
import br.unicentro.oficina.util.Alertas;
import br.unicentro.oficina.util.Formatos;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class ClienteController {

    @FXML private TextField txtBusca;
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEmail;
    @FXML private Label lblTotalGasto;
    @FXML private Button btnExcluir;
    @FXML private TableView<Cliente> tabela;
    @FXML private TableColumn<Cliente, String> colNome;
    @FXML private TableColumn<Cliente, String> colCpf;
    @FXML private TableColumn<Cliente, String> colTelefone;
    @FXML private TableColumn<Cliente, String> colEmail;

    private final ClienteService service = new ClienteService();
    private final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private Cliente atual; // null = cadastrando um novo

    @FXML
    private void initialize() {
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCpf.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCpf()));
        colTelefone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefone()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        tabela.setItems(clientes);

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                editar(novo);
            }
        });

        novo();
        Platform.runLater(this::buscar);
    }

    /** Chamado pelo MainController ao selecionar a aba. */
    public void recarregar() {
        buscar();
    }

    @FXML
    private void buscar() {
        try {
            clientes.setAll(service.buscarPorNome(txtBusca.getText()));
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }

    @FXML
    private void limparBusca() {
        txtBusca.clear();
        buscar();
    }

    @FXML
    private void novo() {
        atual = null;
        txtNome.clear();
        txtCpf.clear();
        txtTelefone.clear();
        txtEmail.clear();
        lblTotalGasto.setText("");
        btnExcluir.setDisable(true);
        tabela.getSelectionModel().clearSelection();
        txtNome.requestFocus();
    }

    @FXML
    private void salvar() {
        Cliente c = new Cliente(
                atual == null ? null : atual.getId(),
                txtNome.getText(),
                txtCpf.getText(),
                txtTelefone.getText(),
                txtEmail.getText());
        try {
            service.salvar(c);
            Alertas.info("Cliente salvo com sucesso.");
            buscar();
            selecionar(c.getId());
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }

    @FXML
    private void excluir() {
        if (atual == null) {
            Alertas.erro("Selecione um cliente para excluir.");
            return;
        }
        if (!Alertas.confirmar("Excluir o cliente " + atual.getNome() + "?")) {
            return;
        }
        try {
            service.excluir(atual);
            buscar();
            novo();
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }

    private void editar(Cliente c) {
        atual = c;
        txtNome.setText(c.getNome());
        txtCpf.setText(c.getCpf());
        txtTelefone.setText(c.getTelefone());
        txtEmail.setText(c.getEmail());
        btnExcluir.setDisable(false);
        try {
            lblTotalGasto.setText("Total gasto na oficina: " + Formatos.moeda(service.totalGasto(c.getId())));
        } catch (ServiceException e) {
            lblTotalGasto.setText("");
        }
    }

    private void selecionar(Integer id) {
        for (Cliente c : clientes) {
            if (c.getId().equals(id)) {
                tabela.getSelectionModel().select(c);
                tabela.scrollTo(c);
                return;
            }
        }
    }
}
