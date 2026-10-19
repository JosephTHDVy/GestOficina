package br.unicentro.oficina.util;

import br.unicentro.oficina.exception.DAOException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Fábrica de conexões JDBC com o PostgreSQL. */
public final class ConnectionFactory {

    private static final String ARQUIVO_CONFIG = "/db.properties";
    private static final Properties config = carregarConfig();

    private ConnectionFactory() { }

    private static Properties carregarConfig() {
        Properties props = new Properties();
        try (InputStream in = ConnectionFactory.class.getResourceAsStream(ARQUIVO_CONFIG)) {
            if (in == null) {
                throw new IllegalStateException("Arquivo db.properties não encontrado em resources.");
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Erro ao ler db.properties.", e);
        }
        return props;
    }

    public static Connection getConnection() throws DAOException {
        try {
            return DriverManager.getConnection(
                    config.getProperty("db.url"),
                    config.getProperty("db.user"),
                    config.getProperty("db.password"));
        } catch (SQLException e) {
            throw new DAOException("Não foi possível conectar ao banco de dados: " + e.getMessage(), e);
        }
    }
}
