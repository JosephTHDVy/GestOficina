package br.unicentro.oficina.model.dao;

import br.unicentro.oficina.exception.DAOException;
import br.unicentro.oficina.model.Cliente;
import br.unicentro.oficina.util.ConnectionFactory;
import br.unicentro.oficina.util.ErroSql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAOImpl implements ClienteDAO {

    private static final String INSERT =
            "INSERT INTO cliente (nome, cpf, telefone, email) VALUES (?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE cliente SET nome = ?, cpf = ?, telefone = ?, email = ? WHERE id = ?";
    private static final String DELETE =
            "DELETE FROM cliente WHERE id = ?";
    private static final String SELECT =
            "SELECT id, nome, cpf, telefone, email FROM cliente ";

    @Override
    public void inserir(Cliente c) throws DAOException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT, new String[]{"id"})) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getCpf());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getEmail());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw ErroSql.traduzir("inserir cliente", e);
        }
    }

    @Override
    public void atualizar(Cliente c) throws DAOException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE)) {

            ps.setString(1, c.getNome());
            ps.setString(2, c.getCpf());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getEmail());
            ps.setInt(5, c.getId());

            if (ps.executeUpdate() == 0) {
                throw new DAOException("Cliente não encontrado para atualização.");
            }
        } catch (SQLException e) {
            throw ErroSql.traduzir("atualizar cliente", e);
        }
    }

    @Override
    public void excluir(Integer id) throws DAOException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE)) {

            ps.setInt(1, id);
            if (ps.executeUpdate() == 0) {
                throw new DAOException("Cliente não encontrado para exclusão.");
            }
        } catch (SQLException e) {
            throw ErroSql.traduzir("excluir cliente", e);
        }
    }

    @Override
    public Cliente buscarPorId(Integer id) throws DAOException {
        List<Cliente> lista = consultar(SELECT + "WHERE id = ?", id);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public Cliente buscarPorCpf(String cpf) throws DAOException {
        List<Cliente> lista = consultar(SELECT + "WHERE cpf = ?", cpf);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    public List<Cliente> buscarPorNome(String nome) throws DAOException {
        return consultar(SELECT + "WHERE nome ILIKE ? ORDER BY nome", "%" + nome + "%");
    }

    @Override
    public List<Cliente> listarTodos() throws DAOException {
        return consultar(SELECT + "ORDER BY nome");
    }

    /** Executa um SELECT parametrizado e mapeia o resultado. */
    private List<Cliente> consultar(String sql, Object... params) throws DAOException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Cliente> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(new Cliente(
                            rs.getInt("id"),
                            rs.getString("nome"),
                            rs.getString("cpf"),
                            rs.getString("telefone"),
                            rs.getString("email")));
                }
                return lista;
            }
        } catch (SQLException e) {
            throw ErroSql.traduzir("consultar clientes", e);
        }
    }
}
