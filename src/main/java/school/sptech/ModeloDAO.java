package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ModeloDAO {
    public ModeloInfo buscarPorLote(Connection conn, int fkLote) {
        String sql = """
            SELECT 
                veiculo.fkLote,
                modelo.id AS idModelo,
                modelo.nome AS nomeModelo
            FROM veiculo
            JOIN modelo ON modelo.id = veiculo.fkModelo
            WHERE veiculo.fkLote = ?;
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fkLote);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ModeloInfo info = new ModeloInfo();
                    info.setFkLote(rs.getInt("fkLote"));
                    info.setFkModelo(rs.getInt("idModelo"));
                    info.setNomeModelo(rs.getString("nomeModelo"));
                    return info;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar modelo por lote: " + e.getMessage());
        }
        return null;
    }
}
