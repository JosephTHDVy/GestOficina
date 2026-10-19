package br.unicentro.oficina.util;

import java.sql.Connection;

/** Teste manual da conexão com o banco (execute o main). */
public class TesteConexao {

    public static void main(String[] args) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            System.out.println("Conexão OK: " + conn.getMetaData().getURL());
        } catch (Exception e) {
            System.err.println("Falha: " + e.getMessage());
        }
    }
}
