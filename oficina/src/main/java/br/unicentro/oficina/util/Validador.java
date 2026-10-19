package br.unicentro.oficina.util;

import java.util.regex.Pattern;

/** Validações e formatações reutilizadas pelas Services. */
public final class Validador {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    // Placa antiga (ABC1234) e Mercosul (ABC1D23)
    private static final Pattern PLACA = Pattern.compile("^[A-Z]{3}[0-9][A-Z0-9][0-9]{2}$");

    private Validador() { }

    public static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    /** Retorna null se o texto for nulo ou em branco; senão, o texto sem espaços nas pontas. */
    public static String limpar(String s) {
        return vazio(s) ? null : s.trim();
    }

    public static String somenteDigitos(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
    }

    // ------------------------------------------------------------------- CPF

    public static boolean cpfValido(String cpf) {
        String d = somenteDigitos(cpf);
        if (d.length() != 11 || d.chars().distinct().count() == 1) {
            return false;
        }
        return digitoCpf(d, 9) == d.charAt(9) - '0'
            && digitoCpf(d, 10) == d.charAt(10) - '0';
    }

    private static int digitoCpf(String d, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (d.charAt(i) - '0') * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    /** Formata 11 dígitos como 000.000.000-00. */
    public static String formatarCpf(String cpf) {
        String d = somenteDigitos(cpf);
        return d.substring(0, 3) + "." + d.substring(3, 6) + "." + d.substring(6, 9) + "-" + d.substring(9);
    }

    // ----------------------------------------------------------------- PLACA

    private static String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.replaceAll("[\\s-]", "").toUpperCase();
    }

    public static boolean placaValida(String placa) {
        return PLACA.matcher(normalizarPlaca(placa)).matches();
    }

    /** Placa antiga vira ABC-1234; Mercosul permanece ABC1D23. */
    public static String formatarPlaca(String placa) {
        String p = normalizarPlaca(placa);
        boolean antiga = Character.isDigit(p.charAt(4));
        return antiga ? p.substring(0, 3) + "-" + p.substring(3) : p;
    }

    // ----------------------------------------------------------------- EMAIL

    public static boolean emailValido(String email) {
        return EMAIL.matcher(email).matches();
    }
}
