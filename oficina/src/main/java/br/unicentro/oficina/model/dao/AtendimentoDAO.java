package br.unicentro.oficina.model.dao;

import br.unicentro.oficina.exception.DAOException;
import br.unicentro.oficina.model.Atendimento;

import java.util.List;

public interface AtendimentoDAO extends GenericDAO<Atendimento, Integer> {

    List<Atendimento> listarPorCliente(int clienteId) throws DAOException;

    /** Busca parcial pela placa do veículo. */
    List<Atendimento> buscarPorPlaca(String placa) throws DAOException;

    /** Atendimentos sem data de saída (veículo ainda na oficina). */
    List<Atendimento> listarEmAndamento() throws DAOException;
}
