package com.sptech.school;

public class Empresa {
    private int id, fkEndereco;
    private long cnpj;
    private String razaoSocial, codigo_ativacao;

    public Empresa(int id, int fkEndereco, long cnpj, String razaoSocial, String codigo_ativacao) {
        this.id = id;
        this.fkEndereco = fkEndereco;
        this.cnpj = cnpj;
        this.razaoSocial = razaoSocial;
        this.codigo_ativacao = codigo_ativacao;
    }

    public int getId() {
        return id;
    }

    public int getFkEndereco() {
        return fkEndereco;
    }

    public long getCnpj() {
        return cnpj;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCodigo_ativacao() {
        return codigo_ativacao;
    }
}
