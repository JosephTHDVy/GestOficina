package br.unicentro.oficina.service;

import br.unicentro.oficina.exception.DAOException;
import br.unicentro.oficina.exception.ServiceException;
import br.unicentro.oficina.model.Atendimento;
import br.unicentro.oficina.model.Servico;
import br.unicentro.oficina.model.dao.AtendimentoDAO;
import br.unicentro.oficina.model.dao.AtendimentoDAOImpl;
import br.unicentro.oficina.util.Validador;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

public class AtendimentoService {

    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999999.99");

    private final AtendimentoDAO atendimentoDAO;
    private final FotoService fotoService;

    public AtendimentoService() {
        this(new AtendimentoDAOImpl(), new FotoService());
    }

    public AtendimentoService(AtendimentoDAO atendimentoDAO, FotoService fotoService) {
        this.atendimentoDAO = atendimentoDAO;
        this.fotoService = fotoService;
    }

    /**
     * Insere (id nulo) ou atualiza (id preenchido) o atendimento com seus serviços.
     *
     * @param novaFoto imagem escolhida pelo usuário; null mantém a foto atual.
     */
    public void salvar(Atendimento a, File novaFoto) throws ServiceException {
        validar(a);

        String caminhoOriginal = a.getCaminhoFoto();
        String fotoNova = null;
        try {
            if (novaFoto != null) {
                fotoNova = fotoService.salvar(novaFoto);
                a.setCaminhoFoto(fotoNova);
            }
            if (a.getId() == null) {
                atendimentoDAO.inserir(a);
            } else {
                atendimentoDAO.atualizar(a);
            }
        } catch (DAOException | ServiceException e) {
            // Desfaz a cópia da foto e restaura o objeto.
            fotoService.excluir(fotoNova);
            a.setCaminhoFoto(caminhoOriginal);
            if (e instanceof ServiceException se) {
                throw se;
            }
            throw new ServiceException(e.getMessage(), e);
        }

        // Gravou com sucesso: a foto antiga não é mais necessária.
        if (fotoNova != null && caminhoOriginal != null && !caminhoOriginal.equals(fotoNova)) {
            fotoService.excluir(caminhoOriginal);
        }
    }

    public void excluir(Atendimento a) throws ServiceException {
        if (a == null || a.getId() == null) {
            throw new ServiceException("Selecione um atendimento para excluir.");
        }
        try {
            atendimentoDAO.excluir(a.getId());
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
        fotoService.excluir(a.getCaminhoFoto());
    }

    /** Registra a saída do veículo com a data/hora atual. */
    public void finalizar(int id) throws ServiceException {
        try {
            Atendimento a = atendimentoDAO.buscarPorId(id);
            if (a == null) {
                throw new ServiceException("Atendimento não encontrado.");
            }
            if (!a.isEmAndamento()) {
                throw new ServiceException("Este atendimento já foi finalizado.");
            }
            LocalDateTime agora = LocalDateTime.now();
            a.setDataSaida(agora.isBefore(a.getDataEntrada()) ? a.getDataEntrada() : agora);
            atendimentoDAO.atualizar(a);
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public Atendimento buscarPorId(int id) throws ServiceException {
        try {
            return atendimentoDAO.buscarPorId(id);
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public List<Atendimento> listarTodos() throws ServiceException {
        try {
            return atendimentoDAO.listarTodos();
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public List<Atendimento> listarEmAndamento() throws ServiceException {
        try {
            return atendimentoDAO.listarEmAndamento();
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    public List<Atendimento> listarPorCliente(int clienteId) throws ServiceException {
        try {
            return atendimentoDAO.listarPorCliente(clienteId);
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /** Se o texto estiver vazio, devolve todos os atendimentos. */
    public List<Atendimento> buscarPorPlaca(String placa) throws ServiceException {
        if (Validador.vazio(placa)) {
            return listarTodos();
        }
        try {
            return atendimentoDAO.buscarPorPlaca(placa.trim());
        } catch (DAOException e) {
            throw new ServiceException(e.getMessage(), e);
        }
    }

    /** Foto do atendimento como arquivo (null se não houver). Usada pelo Controller. */
    public File obterFoto(Atendimento a) {
        return a == null ? null : fotoService.obter(a.getCaminhoFoto());
    }

    // ------------------------------------------------------------- validação

    private void validar(Atendimento a) throws ServiceException {
        if (a == null) {
            throw new ServiceException("Atendimento inválido.");
        }
        if (a.getCliente() == null || a.getCliente().getId() == null) {
            throw new ServiceException("Selecione o cliente.");
        }
        String modelo = Validador.limpar(a.getVeiculoModelo());
        if (modelo == null) {
            throw new ServiceException("Informe o modelo do veículo.");
        }
        if (modelo.length() > 80) {
            throw new ServiceException("O modelo deve ter no máximo 80 caracteres.");
        }
        if (!Validador.placaValida(a.getVeiculoPlaca())) {
            throw new ServiceException("Placa inválida. Use o formato ABC-1234 ou ABC1D23.");
        }
        if (a.getDataEntrada() == null) {
            throw new ServiceException("Informe a data e hora de entrada.");
        }
        if (a.getDataSaida() != null && a.getDataSaida().isBefore(a.getDataEntrada())) {
            throw new ServiceException("A data/hora de saída não pode ser anterior à de entrada.");
        }

        for (Servico s : a.getServicos()) {
            String descricao = Validador.limpar(s.getDescricao());
            if (descricao == null) {
                throw new ServiceException("Todo serviço precisa de uma descrição.");
            }
            if (descricao.length() > 200) {
                throw new ServiceException("A descrição do serviço deve ter no máximo 200 caracteres.");
            }
            if (s.getValor() == null || s.getValor().signum() < 0) {
                throw new ServiceException("O valor do serviço \"" + descricao + "\" é inválido.");
            }
            BigDecimal valor = s.getValor().setScale(2, RoundingMode.HALF_UP);
            if (valor.compareTo(VALOR_MAXIMO) > 0) {
                throw new ServiceException("O valor do serviço \"" + descricao + "\" é muito alto.");
            }
            s.setDescricao(descricao);
            s.setValor(valor);
            s.setAtendimentoId(a.getId());
        }

        // Normalização
        a.setVeiculoModelo(modelo);
        a.setVeiculoPlaca(Validador.formatarPlaca(a.getVeiculoPlaca()));
        a.setObservacoes(Validador.limpar(a.getObservacoes()));
    }
}
