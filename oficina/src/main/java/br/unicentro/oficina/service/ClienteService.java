package br.unicentro.oficina.service;

import br.unicentro.oficina.exception.DAOException;
import br.unicentro.oficina.exception.ServiceException;
import br.unicentro.oficina.model.Atendimento;
import br.unicentro.oficina.model.Cliente;
import br.unicentro.oficina.model.dao.AtendimentoDAO;
import br.unicentro.oficina.model.dao.AtendimentoDAOImpl;
import br.unicentro.oficina.model.dao.ClienteDAO;
import br.unicentro.oficina.model.dao.ClienteDAOImpl;
import br.unicentro.oficina.util.Validador;

import java.math.BigDecimal;
import java.util.List;

public class ClienteService {

    private final ClienteDAO clienteDAO;
    private final AtendimentoDAO atendimentoDAO;

    public ClienteService() {
        this(new ClienteDAOImpl(), new AtendimentoDAOImpl());
    }

    public ClienteService(ClienteDAO clienteDAO, AtendimentoDAO atendimentoDAO) {
        this.clienteDAO = clienteDAO;
        this.atendimentoDAO = atendimentoDAO;
    }

    /** Insere (id nulo) ou atualiza (id preenchido) o cliente, após validar e normalizar. */
    public void salvar(Cliente c) throws ServiceException {
        validar(c);
        try {
            Cliente existente = clienteDAO.buscarPorCpf(c.getCpf());
            if (existente != null && !existente.getId().equals(c.getId())) {
                throw new ServiceException("Já existe um cliente cadastrado com este CPF.");
            }
            if (c.getId() == null) {
                clienteDAO.inserir(c);
            } else {
                clienteDAO.atualizar(c);
            }
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public void excluir(Cliente c) throws ServiceException {
        if (c == null || c.getId() == null) {
            throw new ServiceException("Selecione um cliente para excluir.");
        }
        try {
            int qtd = atendimentoDAO.listarPorCliente(c.getId()).size();
            if (qtd > 0) {
                throw new ServiceException("Não é possível excluir " + c.getNome() + ": o cliente possui "
                        + qtd + " atendimento(s) registrado(s). Exclua os atendimentos primeiro.");
            }
            clienteDAO.excluir(c.getId());
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public Cliente buscarPorId(int id) throws ServiceException {
        try {
            return clienteDAO.buscarPorId(id);
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public List<Cliente> listarTodos() throws ServiceException {
        try {
            return clienteDAO.listarTodos();
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /** Se o texto estiver vazio, devolve todos os clientes. */
    public List<Cliente> buscarPorNome(String nome) throws ServiceException {
        if (Validador.vazio(nome)) {
            return listarTodos();
        }
        try {
            return clienteDAO.buscarPorNome(nome.trim());
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /** Total gasto pelo cliente: soma dos totais de todos os seus atendimentos. */
    public BigDecimal totalGasto(int clienteId) throws ServiceException {
        try {
            return atendimentoDAO.listarPorCliente(clienteId).stream()
                    .map(Atendimento::getValorTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------- validação

    private void validar(Cliente c) throws ServiceException {
        if (c == null) {
            throw new ServiceException("Cliente inválido.");
        }
        String nome = Validador.limpar(c.getNome());
        if (nome == null) {
            throw new ServiceException("Informe o nome do cliente.");
        }
        if (nome.length() > 100) {
            throw new ServiceException("O nome deve ter no máximo 100 caracteres.");
        }
        if (!Validador.cpfValido(c.getCpf())) {
            throw new ServiceException("CPF inválido.");
        }
        String telefone = Validador.limpar(c.getTelefone());
        if (telefone != null && telefone.length() > 20) {
            throw new ServiceException("O telefone deve ter no máximo 20 caracteres.");
        }
        String email = Validador.limpar(c.getEmail());
        if (email != null && (email.length() > 100 || !Validador.emailValido(email))) {
            throw new ServiceException("E-mail inválido.");
        }

        // Normalização
        c.setNome(nome);
        c.setCpf(Validador.formatarCpf(c.getCpf()));
        c.setTelefone(telefone);
        c.setEmail(email == null ? null : email.toLowerCase());
    }
}
