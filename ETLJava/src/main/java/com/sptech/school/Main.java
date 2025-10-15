package com.sptech.school;

public class Main {
    public static void main(String[] args) {
        Conexao conexao = new Conexao();

        conexao.exibir();
        conexao.fecharConexao();
    }
}