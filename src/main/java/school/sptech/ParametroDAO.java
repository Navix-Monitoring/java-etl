package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class ParametroDAO {
    public List<ParametroHardware> buscarParametrosPorModelo(Connection conn, int fkModelo) {

        List<ParametroHardware> listaParametros = new ArrayList<>();

        String sql = "SELECT " +
                "h.tipo, ph.unidadeMedida, ph.parametroMinimo, ph.parametroNeutro, " +
                "ph.parametroAtencao, ph.parametroCritico " +
                "FROM parametroHardware ph " +
                "JOIN hardware h ON ph.fkHardware = h.id " + // Usando h.id como referência
                "WHERE ph.fkModelo = ?"; // Usando '?' para PreparedStatement

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, fkModelo);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    // Extração dos dados
                    String tipoHardware = rs.getString("tipo");
                    String unidadeMedida = rs.getString("unidadeMedida");

                    // Pegando os 4 parâmetros pra cada um dos tipos de hardware
                    double minimo = rs.getDouble("parametroMinimo");
                    double neutro = rs.getDouble("parametroNeutro");
                    double atencao = rs.getDouble("parametroAtencao");
                    double critico = rs.getDouble("parametroCritico");

                    // Criando o objeto para cada um e colocando na lista
                    ParametroHardware parametro = new ParametroHardware(
                            tipoHardware, unidadeMedida, minimo, neutro, atencao, critico
                    );
                    listaParametros.add(parametro);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar parâmetros do modelo " + fkModelo + ": " + e.getMessage());
            e.printStackTrace();
        }

        return listaParametros;
    }
}