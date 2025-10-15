package com.sptech.school;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Conexao {
    private Connection conexao;

    public Conexao() {
        try {
            String url = "jdbc:mysql://localhost:3306/navix";
            String user = "aluno";
            String pass = "sptech";

            Connection conexao = DriverManager.getConnection(url, user, pass);
            Statement stmt = conexao.createStatement();
            this.conexao = conexao;

        } catch (Exception e) {
            System.out.println("Não foi possível conectar ao banco" + e);
        }
    }

    public List<Empresa> exibir() {
        List<Empresa> empresas = new ArrayList<>();

        try{
            String sqlSelect = "SELECT * FROM empresa";
            ResultSet rs = conexao.createStatement().executeQuery(sqlSelect);
            System.out.println("Registros selecionados: ");
            while (rs.next()){
                Empresa empresa = new Empresa(
                        rs.getInt("id"),
                        rs.getInt("fkEndereco"),
                        rs.getLong("cnpj"),
                        rs.getString("razaoSocial"),
                        rs.getString("codigo_ativacao")
                );

                System.out.println(
                        "id | razaoSocial | cnpj | codigo_ativacao | fkEndereco\n"
                        + empresa.getId() + " | " + empresa.getRazaoSocial() + " | " + empresa.getCnpj() + " | " + empresa.getFkEndereco());
                empresas.add(empresa);
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Erro ao listar empresas!" + e);
        }
        return empresas;
    }

    public void fecharConexao() {
        try {
            if(conexao != null && !conexao.isClosed()){
                conexao.close();
                System.out.println("Conexão fechada com sucesso!");
            }
        } catch (SQLException e) {
            System.out.println("Erro ao fechar Conexão!" + e);
        }
    }
}