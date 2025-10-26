package com.sptech.school;

public class Main {
    public static void main(String[] args) {
        Conexao conexao = new Conexao();

        String caminhoCsv = "/home/dan/sptech/2º Semestre/java-etl/ETLJava/src/capturas/captura_joao.csv";
        conexao.lerCsvEInserir(caminhoCsv);
        conexao.exibirAlerta();
        conexao.fecharConexao();

    }
}