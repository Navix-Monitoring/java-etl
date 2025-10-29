package school.sptech;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ParametroDAO {

    public static double cpuMin, cpuNeutro, cpuAtencao, cpuCritico;
    public static double ramMin, ramNeutro, ramAtencao, ramCritico;
    public static double discoMin, discoNeutro, discoAtencao, discoCritico;



    // --- Buscando os parâmetros do banco e armazenando nas variáveis ---
    public void carregarParametrosDoBanco(Connection conn, int fkModelo) {
        String sql = "SELECT h.tipo, ph.parametroMinimo, ph.parametroNeutro, ph.parametroAtencao, ph.parametroCritico " +
                "FROM parametroHardware ph " +
                "JOIN hardware h ON ph.fkHardware = h.id " +
                "WHERE ph.fkModelo = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, fkModelo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String tipo = rs.getString("tipo").toUpperCase();
                    double min = rs.getDouble("parametroMinimo");
                    double neutro = rs.getDouble("parametroNeutro");
                    double atencao = rs.getDouble("parametroAtencao");
                    double critico = rs.getDouble("parametroCritico");

                    switch (tipo) {
                        case "CPU" -> { cpuMin = min; cpuNeutro = neutro; cpuAtencao = atencao; cpuCritico = critico; }
                        case "RAM" -> { ramMin = min; ramNeutro = neutro; ramAtencao = atencao; ramCritico = critico; }
                        case "DISCO" -> { discoMin = min; discoNeutro = neutro; discoAtencao = atencao; discoCritico = critico; }
                    }
                }
            }

            System.out.println("Parâmetros do banco carregados com sucesso!");

        } catch (SQLException e) {
            System.out.println("Erro ao carregar parâmetros do modelo " + fkModelo + ": " + e.getMessage());
        }
    }

    public static void mostrarParametros() {
        System.out.println("=== Parâmetros carregados ===");
        System.out.printf("CPU    → Min: %.2f, Neutro: %.2f, Atenção: %.2f, Crítico: %.2f%n",
                cpuMin, cpuNeutro, cpuAtencao, cpuCritico);
        System.out.printf("RAM    → Min: %.2f, Neutro: %.2f, Atenção: %.2f, Crítico: %.2f%n",
                ramMin, ramNeutro, ramAtencao, ramCritico);
        System.out.printf("DISCO  → Min: %.2f, Neutro: %.2f, Atenção: %.2f, Crítico: %.2f%n",
                discoMin, discoNeutro, discoAtencao, discoCritico);
    }



}
