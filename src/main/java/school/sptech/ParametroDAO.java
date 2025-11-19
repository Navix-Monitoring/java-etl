package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParametroDAO {
    public static double cpuMin, cpuNeutro, cpuAtencao, cpuCritico;
    public static double ramMin, ramNeutro, ramAtencao, ramCritico;
    public static double discoMin, discoNeutro, discoAtencao, discoCritico;
    public static double tempCpuMin, tempCpuNeutro, tempCpuAtencao, tempCpuCritico;

    int idModelo;

    public void carregarParametrosDoBanco(Connection conn, int fkModelo) {
        String sql = "SELECT h.tipo, ph.parametroMinimo, ph.parametroNeutro, ph.parametroAtencao, ph.parametroCritico, ph.unidadeMedida " +
                "FROM parametroHardware ph JOIN hardware h ON ph.fkHardware = h.id WHERE ph.fkModelo = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fkModelo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo").toUpperCase();
                    double min = rs.getDouble("parametroMinimo");
                    double neutro = rs.getDouble("parametroNeutro");
                    double atencao = rs.getDouble("parametroAtencao");
                    double critico = rs.getDouble("parametroCritico");
                    String unidade = rs.getString("unidadeMedida").toUpperCase();

                    switch (tipo) {
                        case "CPU" -> {
                            if (unidade.equals("USO")) {
                                cpuMin = min;
                                cpuNeutro = neutro;
                                cpuAtencao = atencao;
                                cpuCritico = critico;
                            } else {
                                tempCpuMin = min;
                                tempCpuNeutro = neutro;
                                tempCpuAtencao = atencao;
                                tempCpuCritico = critico;
                            }
                        }
                        case "RAM" -> {
                            ramMin = min;
                            ramNeutro = neutro;
                            ramAtencao = atencao;
                            ramCritico = critico;
                        }
                        case "DISCO" -> {
                            discoMin = min;
                            discoNeutro = neutro;
                            discoAtencao = atencao;
                            discoCritico = critico;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro parametros: " + e.getMessage());
        }
    }
}