package school.sptech;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;

public class Conexao {
    private Connection conexao;

    public Conexao() {
        try {
            String url = "jdbc:mysql://localhost:3306/navix";
            String user = "root";
            String pass = "GuilhermeLeon020605";

            Connection conexao = DriverManager.getConnection(url, user, pass);
            this.conexao = conexao;
            System.out.println("Conexão estabelecida com sucesso!");

        } catch (Exception e) {
            System.out.println("Não foi possível conectar ao banco: " + e.getMessage());
        }
    }

    public void fecharConexao() {
        try {
            if(conexao != null && !conexao.isClosed()){
                conexao.close();
                System.out.println("Conexão fechada com sucesso!");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar Conexão: " + e.getMessage());
        }
    }

    public Connection getConexao(){
        return conexao;
    }
}