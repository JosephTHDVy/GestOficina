package br.unicentro.oficina.util;

import br.unicentro.oficina.model.Atendimento;
import br.unicentro.oficina.model.Cliente;
import br.unicentro.oficina.model.Servico;
import br.unicentro.oficina.service.AtendimentoService;
import br.unicentro.oficina.service.ClienteService;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDateTime;

/** Teste manual da camada Service (execute o main). */
public class TesteService {

    @FunctionalInterface
    interface Acao {
        void executar() throws Exception;
    }

    /** Executa uma ação que DEVE falhar e imprime a mensagem recebida. */
    private static void esperarErro(String descricao, Acao acao) {
        try {
            acao.executar();
            System.out.println("ERRO: \"" + descricao + "\" deveria ter falhado.");
        } catch (Exception e) {
            System.out.println("OK (" + descricao + "): " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        ClienteService clienteService = new ClienteService();
        AtendimentoService atendimentoService = new AtendimentoService();

        Cliente cliente = new Cliente(null, "  Maria Teste ", "52998224725", "(42) 98888-1111", "MARIA@Teste.com");
        Atendimento atendimento = new Atendimento();

        File fotoTemp = File.createTempFile("foto", ".png");
        Files.write(fotoTemp.toPath(), new byte[]{1, 2, 3});

        try {
            // 1) Validações
            esperarErro("CPF inválido", () ->
                    clienteService.salvar(new Cliente(null, "Fulano", "111.111.111-11", null, null)));

            // 2) Cliente válido (CPF só com dígitos deve ser formatado)
            clienteService.salvar(cliente);
            System.out.println("Cliente salvo: " + cliente.getNome() + " | " + cliente.getCpf()
                    + " | " + cliente.getEmail());

            esperarErro("CPF duplicado", () ->
                    clienteService.salvar(new Cliente(null, "Outra Pessoa", "529.982.247-25", null, null)));

            // 3) Atendimento com foto e serviços
            atendimento.setCliente(cliente);
            atendimento.setVeiculoModelo("VW Gol 2015");
            atendimento.setVeiculoPlaca("abc1d23");
            atendimento.setDataEntrada(LocalDateTime.now().minusHours(2));
            atendimento.getServicos().add(new Servico(null, null, "Troca de pastilhas", new BigDecimal("250")));
            atendimento.getServicos().add(new Servico(null, null, "Revisão elétrica", new BigDecimal("180.5")));
            atendimentoService.salvar(atendimento, fotoTemp);

            File foto = atendimentoService.obterFoto(atendimento);
            System.out.println("Atendimento salvo: placa " + atendimento.getVeiculoPlaca()
                    + " | total R$ " + atendimento.getValorTotal()
                    + " | foto copiada: " + (foto != null && foto.exists()));

            esperarErro("saída antes da entrada", () -> {
                Atendimento ruim = atendimentoService.buscarPorId(atendimento.getId());
                ruim.setDataSaida(ruim.getDataEntrada().minusHours(1));
                atendimentoService.salvar(ruim, null);
            });

            // 4) Finalizar
            atendimentoService.finalizar(atendimento.getId());
            System.out.println("Finalizado: saída = "
                    + atendimentoService.buscarPorId(atendimento.getId()).getDataSaida());
            esperarErro("finalizar duas vezes", () -> atendimentoService.finalizar(atendimento.getId()));

            // 5) Total gasto e integridade
            System.out.println("Total gasto do cliente: R$ " + clienteService.totalGasto(cliente.getId()));
            esperarErro("excluir cliente com histórico", () -> clienteService.excluir(cliente));

        } catch (Exception e) {
            System.err.println("Falha no teste: " + e.getMessage());
        } finally {
            // 6) Limpeza (também valida a exclusão da foto)
            try {
                File foto = atendimentoService.obterFoto(atendimento);
                if (atendimento.getId() != null) {
                    atendimentoService.excluir(atendimento);
                    System.out.println("Foto removida junto com o atendimento: "
                            + (foto != null && !foto.exists()));
                }
                if (cliente.getId() != null) {
                    clienteService.excluir(cliente);
                }
                System.out.println("Limpeza concluída.");
            } catch (Exception e) {
                System.err.println("Falha na limpeza: " + e.getMessage());
            }
            fotoTemp.delete();
        }
    }
}
