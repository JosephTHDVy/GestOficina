package br.unicentro.oficina.controller;

import br.unicentro.oficina.exception.ServiceException;
import br.unicentro.oficina.model.Atendimento;
import br.unicentro.oficina.model.Cliente;
import br.unicentro.oficina.model.Servico;
import br.unicentro.oficina.service.AtendimentoService;
import br.unicentro.oficina.service.ClienteService;
import br.unicentro.oficina.util.Alertas;
import br.unicentro.oficina.util.Formatos;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class AtendimentoController {

    // Filtro
    @FXML private TextField txtBuscaPlaca;
    @FXML private CheckBox chkEmAndamento;

    // Lista
    @FXML private TableView<Atendimento> tabela;
    @FXML private TableColumn<Atendimento, String> colPlaca;
    @FXML private TableColumn<Atendimento, String> colModelo;
    @FXML private TableColumn<Atendimento, String> colCliente;
    @FXML private TableColumn<Atendimento, String> colEntrada;
    @FXML private TableColumn<Atendimento, String> colSaida;
    @FXML private TableColumn<Atendimento, String> colTotal;
    @FXML private TableColumn<Atendimento, String> colStatus;

    // Formulário
    @FXML private ComboBox<Cliente> cmbCliente;
    @FXML private TextField txtModelo;
    @FXML private TextField txtPlaca;
    @FXML private DatePicker dtEntrada;
    @FXML private TextField txtEntradaHora;
    @FXML private DatePicker dtSaida;
    @FXML private TextField txtSaidaHora;
    @FXML private ImageView imgFoto;
    @FXML private TextArea txtObservacoes;
    @FXML private Button btnFinalizar;
    @FXML private Button btnExcluir;

    // Serviços do atendimento
    @FXML private TableView<Servico> tabelaServicos;
    @FXML private TableColumn<Servico, String> colServicoDescricao;
    @FXML private TableColumn<Servico, String> colServicoValor;
    @FXML private TextField txtServicoDescricao;
    @FXML private TextField txtServicoValor;
    @FXML private Label lblTotal;

    private final AtendimentoService atendimentoService = new AtendimentoService();
    private final ClienteService clienteService = new ClienteService();

    private final ObservableList<Atendimento> atendimentos = FXCollections.observableArrayList();
    private final ObservableList<Servico> servicosForm = FXCollections.observableArrayList();

    private Atendimento atual;      // null = cadastrando um novo
    private File fotoSelecionada;   // foto escolhida e ainda não salva

    @FXML
    private void initialize() {
        configurarTabelas();
        configurarComboCliente();

        servicosForm.addListener((ListChangeListener<Servico>) c -> atualizarTotal());
        tabela.getSelectionModel().selectedItemProperty().addListener((obs, antigo, novo) -> {
            if (novo != null) {
                editar(novo);
            }
        });

        novo();
        Platform.runLater(this::recarregar);
    }

    /** Chamado pelo MainController ao selecionar a aba. */
    public void recarregar() {
        try {
            Cliente selecionado = cmbCliente.getValue();
            List<Cliente> clientes = clienteService.listarTodos();
            cmbCliente.setItems(FXCollections.observableArrayList(clientes));
            if (selecionado != null && clientes.contains(selecionado)) {
                cmbCliente.setValue(selecionado);
            }
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
        buscar();
    }

    // ------------------------------------------------------------------ busca

    @FXML
    private void buscar() {
        try {
            List<Atendimento> lista = atendimentoService.buscarPorPlaca(txtBuscaPlaca.getText());
            if (chkEmAndamento.isSelected()) {
                lista = lista.stream().filter(Atendimento::isEmAndamento).toList();
            }
            atendimentos.setAll(lista);
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }

    @FXML
    private void limparBusca() {
        txtBuscaPlaca.clear();
        chkEmAndamento.setSelected(false);
        buscar();
    }

    // ------------------------------------------------------------------- CRUD

    @FXML
    private void novo() {
        atual = null;
        fotoSelecionada = null;
        cmbCliente.setValue(null);
        txtModelo.clear();
        txtPlaca.clear();
        dtEntrada.setValue(LocalDate.now());
        txtEntradaHora.setText(LocalTime.now().format(Formatos.HORA_EXIBICAO));
        dtSaida.setValue(null);
        txtSaidaHora.clear();
        txtObservacoes.clear();
        imgFoto.setImage(null);
        servicosForm.clear();
        limparCamposServico();
        btnExcluir.setDisable(true);
        btnFinalizar.setDisable(true);
        tabela.getSelectionModel().clearSelection();
    }

    @FXML
    private void salvar() {
        try {
            Atendimento a = new Atendimento();
            a.setId(atual == null ? null : atual.getId());
            a.setCaminhoFoto(atual == null ? null : atual.getCaminhoFoto());
            a.setCliente(cmbCliente.getValue());
            a.setVeiculoModelo(txtModelo.getText());
            a.setVeiculoPlaca(txtPlaca.getText());
            a.setDataEntrada(lerDataHora(dtEntrada, txtEntradaHora, "entrada"));
            a.setDataSaida(lerDataHora(dtSaida, txtSaidaHora, "saída"));
            a.setObservacoes(txtObservacoes.getText());
            a.setServicos(new ArrayList<>(servicosForm));

            atendimentoService.salvar(a, fotoSelecionada);

            Alertas.info("Atendimento salvo com sucesso.");
            buscar();
            selecionar(a.getId());
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }

    @FXML
    private void excluir() {
        if (atual == null) {
            Alertas.erro("Selecione um atendimento para excluir.");
            return;
        }
        if (!Alertas.confirmar("Excluir o atendimento do veículo " + atual.getVeiculoPlaca()
                + "? Os serviços e a foto também serão removidos.")) {
            return;
        }
        try {
            atendimentoService.excluir(atual);
            buscar();
            novo();
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }

    /** Registra a saída com a data/hora atual. Alterações não salvas no formulário são descartadas. */
    @FXML
    private void finalizar() {
        if (atual == null || !atual.isEmAndamento()) {
            Alertas.erro("Selecione um atendimento em andamento.");
            return;
        }
        if (!Alertas.confirmar("Registrar a saída do veículo " + atual.getVeiculoPlaca() + " agora?")) {
            return;
        }
        try {
            atendimentoService.finalizar(atual.getId());
            int id = atual.getId();
            buscar();
            selecionar(id);
        } catch (ServiceException e) {
            Alertas.erro(e.getMessage());
        }
    }

    // ------------------------------------------------------------------- foto

    @FXML
    private void escolherFoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Escolher foto do veículo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File arquivo = chooser.showOpenDialog(imgFoto.getScene().getWindow());
        if (arquivo != null) {
            fotoSelecionada = arquivo;
            mostrarFoto(arquivo);
        }
    }

    private void mostrarFoto(File arquivo) {
        imgFoto.setImage(arquivo == null ? null : new Image(arquivo.toURI().toString(), 260, 160, true, true));
    }

    // --------------------------------------------------------------- serviços

    @FXML
    private void adicionarServico() {
        String descricao = txtServicoDescricao.getText() == null ? "" : txtServicoDescricao.getText().trim();
        if (descricao.isEmpty()) {
            Alertas.erro("Informe a descrição do serviço.");
            return;
        }
        BigDecimal valor = Formatos.lerValor(txtServicoValor.getText());
        if (valor == null || valor.signum() < 0) {
            Alertas.erro("Informe um valor válido (ex.: 250,00).");
            return;
        }
        servicosForm.add(new Servico(null, null, descricao, valor));
        limparCamposServico();
        txtServicoDescricao.requestFocus();
    }

    @FXML
    private void removerServico() {
        Servico selecionado = tabelaServicos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alertas.erro("Selecione um serviço na lista para remover.");
            return;
        }
        servicosForm.remove(selecionado);
    }

    private void limparCamposServico() {
        txtServicoDescricao.clear();
        txtServicoValor.clear();
    }

    private void atualizarTotal() {
        BigDecimal total = servicosForm.stream()
                .map(Servico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lblTotal.setText("Total gasto: " + Formatos.moeda(total));
    }

    // ---------------------------------------------------------------- edição

    private void editar(Atendimento a) {
        atual = a;
        fotoSelecionada = null;
        cmbCliente.setValue(a.getCliente());
        txtModelo.setText(a.getVeiculoModelo());
        txtPlaca.setText(a.getVeiculoPlaca());
        dtEntrada.setValue(a.getDataEntrada().toLocalDate());
        txtEntradaHora.setText(a.getDataEntrada().format(Formatos.HORA_EXIBICAO));
        if (a.getDataSaida() != null) {
            dtSaida.setValue(a.getDataSaida().toLocalDate());
            txtSaidaHora.setText(a.getDataSaida().format(Formatos.HORA_EXIBICAO));
        } else {
            dtSaida.setValue(null);
            txtSaidaHora.clear();
        }
        txtObservacoes.setText(a.getObservacoes());
        mostrarFoto(atendimentoService.obterFoto(a));
        servicosForm.setAll(a.getServicos());
        limparCamposServico();
        btnExcluir.setDisable(false);
        btnFinalizar.setDisable(!a.isEmAndamento());
    }

    private void selecionar(Integer id) {
        for (Atendimento a : atendimentos) {
            if (a.getId().equals(id)) {
                tabela.getSelectionModel().select(a);
                tabela.scrollTo(a);
                return;
            }
        }
    }

    /** Combina data + hora digitada. Ambos vazios = null (ex.: saída ainda não registrada). */
    private LocalDateTime lerDataHora(DatePicker data, TextField hora, String nome) throws ServiceException {
        LocalDate d = data.getValue();
        String h = hora.getText() == null ? "" : hora.getText().trim();
        if (d == null && h.isEmpty()) {
            return null;
        }
        if (d == null) {
            throw new ServiceException("Informe a data de " + nome + ".");
        }
        if (h.isEmpty()) {
            throw new ServiceException("Informe a hora de " + nome + " (HH:mm).");
        }
        try {
            return d.atTime(LocalTime.parse(h, Formatos.HORA_ENTRADA));
        } catch (DateTimeParseException e) {
            throw new ServiceException("Hora de " + nome + " inválida. Use o formato HH:mm.");
        }
    }

    // ----------------------------------------------------------- configuração

    private void configurarTabelas() {
        colPlaca.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVeiculoPlaca()));
        colModelo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVeiculoModelo()));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente().getNome()));
        colEntrada.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDataEntrada().format(Formatos.DATA_HORA)));
        colSaida.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataSaida() == null ? "—" : c.getValue().getDataSaida().format(Formatos.DATA_HORA)));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(Formatos.moeda(c.getValue().getValorTotal())));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().isEmAndamento() ? "Em andamento" : "Finalizado"));
        tabela.setItems(atendimentos);

        colServicoDescricao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescricao()));
        colServicoValor.setCellValueFactory(c -> new SimpleStringProperty(Formatos.moeda(c.getValue().getValor())));
        tabelaServicos.setItems(servicosForm);
    }

    private void configurarComboCliente() {
        cmbCliente.setConverter(new StringConverter<>() {
            @Override
            public String toString(Cliente c) {
                return c == null ? "" : c.getNome() + " (" + c.getCpf() + ")";
            }

            @Override
            public Cliente fromString(String texto) {
                return null;
            }
        });
        cmbCliente.setPromptText("Selecione o cliente");
    }
}
