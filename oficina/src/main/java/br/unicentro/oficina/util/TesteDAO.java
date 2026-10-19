package br.unicentro.oficina.util;

import br.unicentro.oficina.model.Atendimento;
import br.unicentro.oficina.model.Cliente;
import br.unicentro.oficina.model.Servico;
import br.unicentro.oficina.model.dao.AtendimentoDAO;
import br.unicentro.oficina.model.dao.AtendimentoDAOImpl;
import br.unicentro.oficina.model.dao.ClienteDAO;
import br.unicentro.oficina.model.dao.ClienteDAOImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Teste manual do CRUD na camada DAO (execute o main). */
public class TesteDAO {

    public static void main(String[] args) {
        ClienteDAO clienteDAO = new ClienteDAOImpl();
        AtendimentoDAO atendimentoDAO = new AtendimentoDAOImpl();
        Cliente cliente = new Cliente(null, "João Teste", "000.000.000-00", "(42) 99999-0000", "joao@teste.com");
        Atendimento atendimento = new Atendimento();

        try {
            // CREATE
            clienteDAO.inserir(cliente);
            System.out.println("Cliente inserido, id = " + cliente.getId());

            atendimento.setCliente(cliente);
            atendimento.setVeiculoModelo("Fiat Uno 2010");
            atendimento.setVeiculoPlaca("ABC-1D23");
            atendimento.setDataEntrada(LocalDateTime.now().minusHours(3));
            atendimento.setObservacoes("Barulho na suspensão");
            atendimento.getServicos().add(new Servico(null, null, "Troca de amortecedores", new BigDecimal("450.00")));
            atendimento.getServicos().add(new Servico(null, null, "Alinhamento", new BigDecimal("80.00")));
            atendimentoDAO.inserir(atendimento);
            System.out.println("Atendimento inserido, id = " + atendimento.getId());

            // READ
            Atendimento lido = atendimentoDAO.buscarPorId(atendimento.getId());
            System.out.println("Lido: " + lido.getVeiculoModelo() + " | cliente: " + lido.getCliente().getNome()
                    + " | total: R$ " + lido.getValorTotal() + " | em andamento: " + lido.isEmAndamento());

            // UPDATE (fecha o atendimento e adiciona um serviço)
            lido.setDataSaida(LocalDateTime.now());
            lido.getServicos().add(new Servico(null, null, "Troca de óleo", new BigDecimal("120.00")));
            atendimentoDAO.atualizar(lido);
            Atendimento atualizado = atendimentoDAO.buscarPorId(lido.getId());
            System.out.println("Atualizado: total R$ " + atualizado.getValorTotal()
                    + " | em andamento: " + atualizado.isEmAndamento());

            // Teste da regra de integridade: excluir cliente com histórico deve falhar
            try {
                clienteDAO.excluir(cliente.getId());
                System.out.println("ERRO: o cliente não deveria ter sido excluído.");
            } catch (Exception e) {
                System.out.println("Bloqueio esperado: " + e.getMessage());
            }

        } catch (Exception e) {
            System.err.println("Falha no teste: " + e.getMessage());
        } finally {
            // DELETE (limpeza)
            try {
                if (atendimento.getId() != null) {
                    atendimentoDAO.excluir(atendimento.getId());
                }
                if (cliente.getId() != null) {
                    clienteDAO.excluir(cliente.getId());
                }
                System.out.println("Limpeza concluída.");
            } catch (Exception e) {
                System.err.println("Falha na limpeza: " + e.getMessage());
            }
        }
    }
}
