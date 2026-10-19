package br.unicentro.oficina.exception;

/** Exceção lançada pela camada de acesso a dados (DAO). */
public class DAOException extends Exception {

    public DAOException(String mensagem) {
        super(mensagem);
    }

    public DAOException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
