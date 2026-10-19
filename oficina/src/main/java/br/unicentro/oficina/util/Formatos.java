package br.unicentro.oficina.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Formatação e leitura de datas, horas e valores no padrão brasileiro. */
public final class Formatos {

    public static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    public static final DateTimeFormatter HORA_EXIBICAO = DateTimeFormatter.ofPattern("HH:mm");
    /** Aceita "9:30" e "09:30". */
    public static final DateTimeFormatter HORA_ENTRADA = DateTimeFormatter.ofPattern("H:mm");

    private static final Locale PT_BR = Locale.of("pt", "BR");

    private Formatos() { }

    public static String moeda(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor == null ? BigDecimal.ZERO : valor);
    }

    /**
     * Lê valores como "250", "250.50", "250,50" ou "1.250,50".
     * Retorna null se o texto estiver vazio ou for inválido.
     */
    public static BigDecimal lerValor(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.replace("R$", "").replace(" ", "").trim();
        if (t.isEmpty()) {
            return null;
        }
        if (t.contains(",")) {
            t = t.replace(".", "").replace(",", ".");
        } else if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
            t = t.replace(".", ""); // 1.500 = mil e quinhentos
        }
        try {
            return new BigDecimal(t);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
