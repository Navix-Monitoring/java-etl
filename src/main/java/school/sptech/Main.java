package school.sptech;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        Conexao db = new Conexao();
        Connection conn = db.getConexao();

        if (conn != null) {
            ParametroDAO dao = new ParametroDAO();
            int modeloDesejado = 1; // Qual modelo ele irá buscar

            List<ParametroHardware> parametrosDoModelo = dao.buscarParametrosPorModelo(conn, modeloDesejado);

            System.out.println("\n--- Parâmetros do Modelo ID: " + modeloDesejado + " ---");

            // Itera e imprime cada objeto (CPU, RAM, DISCO)
            for (ParametroHardware p : parametrosDoModelo) {
                System.out.printf(
                        "Hardware: %s (%s) | Min: %.1f | Neutro: %.1f | Atenção: %.1f | Crítico: %.1f\n",
                        p.tipoHardware, p.unidadeMedida, p.minimo, p.neutro, p.atencao, p.critico
                );
            }

            db.fecharConexao();
        }
    }
}