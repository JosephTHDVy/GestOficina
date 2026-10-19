package br.unicentro.oficina.service;

import br.unicentro.oficina.exception.ServiceException;
import br.unicentro.oficina.model.Atendimento;
import br.unicentro.oficina.model.Servico;
import br.unicentro.oficina.util.Formatos;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Gera o resumo gerencial da oficina e exporta atendimentos para CSV. */
public class RelatorioService {

    public record LinhaRanking(String cliente, int atendimentos, BigDecimal total) { }

    public record Resumo(int totalAtendimentos, int emAndamento, int finalizados,
                         BigDecimal faturamento, BigDecimal ticketMedio,
                         List<LinhaRanking> ranking, List<Atendimento> atendimentos) { }

    private final AtendimentoService atendimentoService;

    public RelatorioService() {
        this(new AtendimentoService());
    }

    public RelatorioService(AtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    /** Datas nulas significam "sem limite". O período é inclusivo e usa a data de entrada. */
    public Resumo gerar(LocalDate inicio, LocalDate fim) throws ServiceException {
        if (inicio != null && fim != null && fim.isBefore(inicio)) {
            throw new ServiceException("A data final não pode ser anterior à data inicial.");
        }

        List<Atendimento> periodo = atendimentoService.listarTodos().stream()
                .filter(a -> inicio == null || !a.getDataEntrada().toLocalDate().isBefore(inicio))
                .filter(a -> fim == null || !a.getDataEntrada().toLocalDate().isAfter(fim))
                .toList();

        int emAndamento = (int) periodo.stream().filter(Atendimento::isEmAndamento).count();
        BigDecimal faturamento = periodo.stream()
                .map(Atendimento::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ticketMedio = periodo.isEmpty()
                ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(periodo.size()), 2, RoundingMode.HALF_UP);

        Map<Integer, List<Atendimento>> porCliente = periodo.stream()
                .collect(Collectors.groupingBy(a -> a.getCliente().getId()));

        List<LinhaRanking> ranking = porCliente.values().stream()
                .map(lista -> new LinhaRanking(
                        lista.get(0).getCliente().getNome(),
                        lista.size(),
                        lista.stream().map(Atendimento::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(LinhaRanking::total).reversed())
                .toList();

        return new Resumo(periodo.size(), emAndamento, periodo.size() - emAndamento,
                faturamento, ticketMedio, ranking, periodo);
    }

    /** CSV com separador ";" e BOM UTF-8 para abrir corretamente no Excel. */
    public void exportarCsv(List<Atendimento> lista, File destino) throws ServiceException {
        if (lista == null || lista.isEmpty()) {
            throw new ServiceException("Não há atendimentos para exportar. Gere o resumo primeiro.");
        }
        if (destino == null) {
            throw new ServiceException("Arquivo de destino não informado.");
        }

        try (Writer w = new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8)) {
            w.write('\uFEFF');
            w.write("Placa;Veículo;Cliente;CPF;Entrada;Saída;Situação;Serviços;Total\r\n");

            for (Atendimento a : lista) {
                String servicos = a.getServicos().stream()
                        .map(RelatorioService::descreverServico)
                        .collect(Collectors.joining(" | "));

                w.write(String.join(";",
                        campo(a.getVeiculoPlaca()),
                        campo(a.getVeiculoModelo()),
                        campo(a.getCliente().getNome()),
                        campo(a.getCliente().getCpf()),
                        campo(a.getDataEntrada().format(Formatos.DATA_HORA)),
                        campo(a.getDataSaida() == null ? "" : a.getDataSaida().format(Formatos.DATA_HORA)),
                        campo(a.isEmAndamento() ? "Em andamento" : "Finalizado"),
                        campo(servicos),
                        campo(decimal(a.getValorTotal()))));
                w.write("\r\n");
            }
        } catch (IOException e) {
            throw new ServiceException("Não foi possível gravar o arquivo: " + e.getMessage(), e);
        }
    }

    private static String descreverServico(Servico s) {
        return s.getDescricao() + " (R$ " + decimal(s.getValor()) + ")";
    }

    private static String decimal(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP).toPlainString().replace('.', ',');
    }

    /** Aspas duplas + escape; neutraliza fórmulas (=, +, -, @) ao abrir no Excel. */
    private static String campo(String texto) {
        String v = texto == null ? "" : texto;
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        return "\"" + v.replace("\"", "\"\"") + "\"";
    }
}
