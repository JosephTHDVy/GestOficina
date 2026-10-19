package br.unicentro.oficina.model;

import java.math.BigDecimal;

/** Trabalho realizado em um atendimento (item de serviço). */
public class Servico {

    private Integer id;
    private Integer atendimentoId;
    private String descricao;
    private BigDecimal valor;

    public Servico() { }

    public Servico(Integer id, Integer atendimentoId, String descricao, BigDecimal valor) {
        this.id = id;
        this.atendimentoId = atendimentoId;
        this.descricao = descricao;
        this.valor = valor;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Integer getAtendimentoId() { return atendimentoId; }
    public void setAtendimentoId(Integer atendimentoId) { this.atendimentoId = atendimentoId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}
