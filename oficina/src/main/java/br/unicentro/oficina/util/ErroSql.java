package br.unicentro.oficina.util;

import br.unicentro.oficina.exception.DAOException;

import java.sql.SQLException;

/** Converte SQLException do PostgreSQL em DAOException com mensagem amigável. */
public final class ErroSql {

    private ErroSql() { }

    public static DAOException traduzir(String operacao, SQLException e) {
        String estado = e.getSQLState();
        if ("23505".equals(estado)) {
            return new DAOException("Já existe um registro com esses dados (valor duplicado, ex.: CPF).", e);
        }
        if ("23503".equals(estado)) {
            return new DAOException("Operação não permitida: há registros vinculados a este item "
                    + "ou a referência informada não existe.", e);
        }
        if ("23514".equals(estado)) {
            return new DAOException("Dados inválidos: verifique as datas e os valores informados.", e);
        }
        if ("23502".equals(estado)) {
            return new DAOException("Existem campos obrigatórios sem preenchimento.", e);
        }
        return new DAOException("Erro ao " + operacao + ": " + e.getMessage(), e);
    }
}
