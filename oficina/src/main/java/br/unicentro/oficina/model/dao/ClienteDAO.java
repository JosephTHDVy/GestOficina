package br.unicentro.oficina.model.dao;

import br.unicentro.oficina.exception.DAOException;
import br.unicentro.oficina.model.Cliente;

import java.util.List;

public interface ClienteDAO extends GenericDAO<Cliente, Integer> {

    /** Busca parcial, sem diferenciar maiúsculas/minúsculas. */
    List<Cliente> buscarPorNome(String nome) throws DAOException;

    Cliente buscarPorCpf(String cpf) throws DAOException;
}
