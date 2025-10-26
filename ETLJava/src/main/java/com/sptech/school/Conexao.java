package com.sptech.school;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.*;

public class Conexao {
    private Connection conexao;

    // conexão com o banco de dados
    public Conexao() {
        String caminhoCsv = "dados.csv";
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

    // leitura e escrita do csv
    public List<Alerta> lerCsv(String caminhoCsv) {
        List<Alerta> alertas = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCsv))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                // ignorar cabeçalho
                if (linha.startsWith("timestamp") || linha.trim().isEmpty()) continue;

                String[] campos = linha.split(",");

                try {
                    // passando campos para a classe Aleta
                    int quantidadeProcessos = Integer.parseInt(campos[6].trim());
                    int prioridade = determinarPrioridade(quantidadeProcessos);
                    String descricao = campos[2].trim();
                    String nivel = determinarNivel(Double.parseDouble(campos[3].trim()));
                    String status = "pendente";
                    String data_alerta = campos[0].trim();

                    Alerta alerta = new Alerta(prioridade, descricao, nivel, status, data_alerta);
                    alertas.add(alerta);
                } catch (Exception e) {
                    System.out.println("Erro ao processar linha: " + linha + ". Ignorando a linha.");
                }
            }
        } catch (IOException e) {
            System.out.println("Erro ao ler o arquivo CSV: " + e.getMessage());
        }
        return alertas;
    }

    public void inserirAlertaNoBanco(List<Alerta> alertas) {
        String sqlInsert = "INSERT INTO alerta (descricao, nivel, data_alerta, status, prioridade) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conexao.prepareStatement(sqlInsert)) {
            for (Alerta alerta : alertas) {
                ps.setString(1, alerta.getDescricao());
                ps.setString(2, alerta.getNivel());
                ps.setString(3, alerta.getData_alerta());
                ps.setString(4, alerta.getStatus());
                ps.setInt(5, alerta.getPrioridade());
                ps.addBatch();
            }
            ps.executeBatch();
            System.out.println("Alertas inseridos com sucesso!");
        } catch (SQLException e) {
            System.out.println("Erro ao inserir alertas no banco: " + e.getMessage());
        }
    }

    public void lerCsvEInserir(String caminhoCsv) {
        List<Alerta> alertas = lerCsv(caminhoCsv);
        inserirAlertaNoBanco(alertas);
    }


    // nível com base no uso de CPU
    private String determinarNivel(double cpu) {
        if (cpu < 30) {
            return "baixo";
        } else if (cpu >= 30 && cpu < 60) {
            return "medio";
        } else {
            return "alto";
        }
    }

    // prioridade com base na quantidade de processos
    private int determinarPrioridade(int quantidadeProcessos) {
        if (quantidadeProcessos <= 200) {
            return 5;
        } else if (quantidadeProcessos <= 400) {
            return 4;
        } else if (quantidadeProcessos <= 600) {
            return 3;
        } else if (quantidadeProcessos <= 800) {
            return 2;
        } else {
            return 1;
        }
    }

    // exibir tabelas do banco de dados
    public List<Alerta> exibirAlerta() {
        List<Alerta> alertas = new ArrayList<>();

        try {
            String sqlSelect = "SELECT * FROM alerta";
            ResultSet rs = conexao.createStatement().executeQuery(sqlSelect);
            System.out.println("Registros selecionados: ");

            while (rs.next()) {
                Alerta alerta = new Alerta(
                        rs.getInt("id"),
                        rs.getInt("prioridade"),
                        rs.getString("descricao"),
                        rs.getString("nivel"),
                        rs.getString("status"),
                        rs.getString("data_alerta")
                );

                System.out.println(
                        "id | prioridade | descricao | nivel | status | data_alerta\n" +
                                alerta.getId() + " | " + alerta.getPrioridade() + " | " + alerta.getDescricao() + " | " + alerta.getNivel() + " | " + alerta.getStatus() + " | " + alerta.getData_alerta()
                );
                alertas.add(alerta);
            }
            rs.close();
        } catch (Exception e) {
            System.out.println("Erro ao listar alertas!" + e);
        }
        return alertas;
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
}