package br.unicentro.oficina.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Uma visita de um cliente à oficina, com veículo, foto, serviços e datas. */
public class Atendimento {

    private Integer id;
    private Cliente cliente;
    private String veiculoModelo;
    private String veiculoPlaca;
    private String caminhoFoto;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataSaida;
    private String observacoes;
    private List<Servico> servicos = new ArrayList<>();

    public Atendimento() { }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public String getVeiculoModelo() { return veiculoModelo; }
    public void setVeiculoModelo(String veiculoModelo) { this.veiculoModelo = veiculoModelo; }

    public String getVeiculoPlaca() { return veiculoPlaca; }
    public void setVeiculoPlaca(String veiculoPlaca) { this.veiculoPlaca = veiculoPlaca; }

    public String getCaminhoFoto() { return caminhoFoto; }
    public void setCaminhoFoto(String caminhoFoto) { this.caminhoFoto = caminhoFoto; }

    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDateTime dataEntrada) { this.dataEntrada = dataEntrada; }

    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public List<Servico> getServicos() { return servicos; }
    public void setServicos(List<Servico> servicos) { this.servicos = servicos; }

    /** Total gasto = soma dos valores dos serviços realizados. */
    public BigDecimal getValorTotal() {
        return servicos.stream()
                .map(Servico::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Atendimento está em andamento enquanto não há data de saída. */
    public boolean isEmAndamento() {
        return dataSaida == null;
    }
}
