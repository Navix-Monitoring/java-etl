package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ModeloDAO {

    public ModeloInfo buscarPorLote(Connection conn, int fkLote) {

        String sql = """
            SELECT
                        v.fkLote,
                        m.id AS idModelo,
                        m.nome AS nomeModelo,
                        e.razaoSocial AS empresaNome
                    FROM veiculo v
                    JOIN modelo m ON m.id = v.fkModelo
                    JOIN lote l ON l.id = v.fkLote
                    JOIN empresa e ON e.id = l.fkEmpresa
                    WHERE v.fkLote = ?
                    LIMIT 1;
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fkLote);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ModeloInfo info = new ModeloInfo();
                    info.setFkLote(rs.getInt("fkLote"));
                    info.setFkModelo(rs.getInt("idModelo"));
                    info.setNomeModelo(rs.getString("nomeModelo"));
                    info.setEmpresaNome(rs.getString("empresaNome"));
                    return info;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar modelo por lote: " + e.getMessage());
        }

        return null;
    }
}
