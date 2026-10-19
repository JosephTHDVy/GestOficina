package br.unicentro.oficina.model.dao;

import br.unicentro.oficina.exception.DAOException;
import br.unicentro.oficina.model.Atendimento;
import br.unicentro.oficina.model.Cliente;
import br.unicentro.oficina.model.Servico;
import br.unicentro.oficina.util.ConnectionFactory;
import br.unicentro.oficina.util.ErroSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AtendimentoDAOImpl implements AtendimentoDAO {

    private static final String INSERT =
            "INSERT INTO atendimento (cliente_id, veiculo_modelo, veiculo_placa, caminho_foto, "
          + "data_entrada, data_saida, observacoes) VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE =
            "UPDATE atendimento SET cliente_id = ?, veiculo_modelo = ?, veiculo_placa = ?, "
          + "caminho_foto = ?, data_entrada = ?, data_saida = ?, observacoes = ? WHERE id = ?";

    private static final String DELETE =
            "DELETE FROM atendimento WHERE id = ?";

    private static final String SELECT =
            "SELECT a.id, a.cliente_id, a.veiculo_modelo, a.veiculo_placa, a.caminho_foto, "
          + "a.data_entrada, a.data_saida, a.observacoes, "
          + "c.nome, c.cpf, c.telefone, c.email "
          + "FROM atendimento a JOIN cliente c ON c.id = a.cliente_id ";

    private static final String INSERT_SERVICO =
            "INSERT INTO servico (atendimento_id, descricao, valor) VALUES (?, ?, ?)";
    private static final String DELETE_SERVICOS =
            "DELETE FROM servico WHERE atendimento_id = ?";
    private static final String SELECT_SERVICOS =
            "SELECT id, atendimento_id, descricao, valor FROM servico WHERE atendimento_id = ? ORDER BY id";

    // ---------------------------------------------------------------- CREATE

    @Override
    public void inserir(Atendimento a) throws DAOException {
        Connection conn = ConnectionFactory.getConnection();
        try {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(INSERT, new String[]{"id"})) {
                preencher(ps, a);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        a.setId(rs.getInt(1));
                    }
                }
            }
            inserirServicos(conn, a);
            conn.commit();

        } catch (SQLException e) {
            desfazer(conn);
            throw ErroSql.traduzir("inserir atendimento", e);
        } finally {
            fechar(conn);
        }
    }

    // ---------------------------------------------------------------- UPDATE

    @Override
    public void atualizar(Atendimento a) throws DAOException {
        Connection conn = ConnectionFactory.getConnection();
        try {
            conn.setAutoCommit(false);

            int linhas;
            try (PreparedStatement ps = conn.prepareStatement(UPDATE)) {
                preencher(ps, a);
                ps.setInt(8, a.getId());
                linhas = ps.executeUpdate();
            }
            if (linhas == 0) {
                throw new DAOException("Atendimento não encontrado para atualização.");
            }

            // Estratégia simples: remove os serviços antigos e grava a lista atual.
            try (PreparedStatement ps = conn.prepareStatement(DELETE_SERVICOS)) {
                ps.setInt(1, a.getId());
                ps.executeUpdate();
            }
            inserirServicos(conn, a);
            conn.commit();

        } catch (SQLException e) {
            desfazer(conn);
            throw ErroSql.traduzir("atualizar atendimento", e);
        } catch (DAOException e) {
            desfazer(conn);
            throw e;
        } finally {
            fechar(conn);
        }
    }

    // ---------------------------------------------------------------- DELETE

    @Override
    public void excluir(Integer id) throws DAOException {
        // Os serviços são removidos pelo ON DELETE CASCADE do banco.
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Atendimento não encontrado para exclusão.");
            }
        } catch (SQLException e) {
            throw ErroSql.traduzir("excluir atendimento", e);
        }
    }

    // ----------------------------------------------------------------- READ

    @Override
    public Atendimento buscarPorId(Integer id) throws DAOException {
        List<Atendimento> lista = consultar(SELECT + "WHERE a.id = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Atendimento> listarTodos() throws DAOException {
        return consultar(SELECT + "ORDER BY a.data_entrada DESC");
    }

    @Override
    public List<Atendimento> listarPorCliente(int clienteId) throws DAOException {
        return consultar(SELECT + "WHERE a.cliente_id = ? ORDER BY a.data_entrada DESC", clienteId);
    }

    @Override
    public List<Atendimento> buscarPorPlaca(String placa) throws DAOException {
        return consultar(SELECT + "WHERE a.veiculo_placa ILIKE ? ORDER BY a.data_entrada DESC",
                "%" + placa + "%");
    }

    @Override
    public List<Atendimento> listarEmAndamento() throws DAOException {
        return consultar(SELECT + "WHERE a.data_saida IS NULL ORDER BY a.data_entrada DESC");
    }

    // ------------------------------------------------------------- AUXILIARES

    /** Executa um SELECT parametrizado, mapeia os atendimentos e carrega seus serviços. */
    private List<Atendimento> consultar(String sql, Object... params) throws DAOException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            List<Atendimento> lista = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
            for (Atendimento a : lista) {
                a.setServicos(carregarServicos(conn, a.getId()));
            }
            return lista;

        } catch (SQLException e) {
            throw ErroSql.traduzir("consultar atendimentos", e);
        }
    }

    private Atendimento mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente(
                rs.getInt("cliente_id"),
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("telefone"),
                rs.getString("email"));

        Atendimento a = new Atendimento();
        a.setId(rs.getInt("id"));
        a.setCliente(c);
        a.setVeiculoModelo(rs.getString("veiculo_modelo"));
        a.setVeiculoPlaca(rs.getString("veiculo_placa"));
        a.setCaminhoFoto(rs.getString("caminho_foto"));
        a.setDataEntrada(rs.getTimestamp("data_entrada").toLocalDateTime());

        Timestamp saida = rs.getTimestamp("data_saida");
        a.setDataSaida(saida == null ? null : saida.toLocalDateTime());

        a.setObservacoes(rs.getString("observacoes"));
        return a;
    }

    private void preencher(PreparedStatement ps, Atendimento a) throws SQLException {
        ps.setInt(1, a.getCliente().getId());
        ps.setString(2, a.getVeiculoModelo());
        ps.setString(3, a.getVeiculoPlaca());
        ps.setString(4, a.getCaminhoFoto());
        ps.setTimestamp(5, Timestamp.valueOf(a.getDataEntrada()));
        ps.setTimestamp(6, a.getDataSaida() == null ? null : Timestamp.valueOf(a.getDataSaida()));
        ps.setString(7, a.getObservacoes());
    }

    private List<Servico> carregarServicos(Connection conn, int atendimentoId) throws SQLException {
        List<Servico> servicos = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(SELECT_SERVICOS)) {
            ps.setInt(1, atendimentoId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    servicos.add(new Servico(
                            rs.getInt("id"),
                            rs.getInt("atendimento_id"),
                            rs.getString("descricao"),
                            rs.getBigDecimal("valor")));
                }
            }
        }
        return servicos;
    }

    private void inserirServicos(Connection conn, Atendimento a) throws SQLException {
        if (a.getServicos().isEmpty()) {
            return;
        }
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SERVICO)) {
            for (Servico s : a.getServicos()) {
                ps.setInt(1, a.getId());
                ps.setString(2, s.getDescricao());
                ps.setBigDecimal(3, s.getValor());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void desfazer(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignorada) {
            // Se o rollback falhar, a conexão será fechada e o banco descarta a transação.
        }
    }

    private void fechar(Connection conn) {
        try {
            conn.setAutoCommit(true);
            conn.close();
        } catch (SQLException ignorada) {
            // Nada a fazer.
        }
    }
}
