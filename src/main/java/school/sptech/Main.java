package school.sptech;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        ParametroDAO dao = new ParametroDAO();
        Conexao conexao = new Conexao();

        dao.carregarParametrosDoBanco(conexao.getConexao(), 1);
        dao.mostrarParametros();
    }
}