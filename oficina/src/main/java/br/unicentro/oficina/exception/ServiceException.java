package br.unicentro.oficina.exception;

/** Exceção de regra de negócio; a mensagem é própria para exibir ao usuário. */
public class ServiceException extends Exception {

    public ServiceException(String mensagem) {
        super(mensagem);
    }

    public ServiceException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
