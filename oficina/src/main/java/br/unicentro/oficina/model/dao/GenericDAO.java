package br.unicentro.oficina.model.dao;

import br.unicentro.oficina.exception.DAOException;

import java.util.List;

public interface GenericDAO<T, ID> {

    void inserir(T obj) throws DAOException;

    void atualizar(T obj) throws DAOException;

    void excluir(ID id) throws DAOException;

    T buscarPorId(ID id) throws DAOException;

    List<T> listarTodos() throws DAOException;
}
