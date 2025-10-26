package com.sptech.school;

public class Alerta {
    private int id, prioridade;
    private String descricao, nivel, status, data_alerta;

    public Alerta(int prioridade, String descricao, String nivel, String status, String data_alerta) {
        this.prioridade = prioridade;
        this.descricao = descricao;
        this.nivel = nivel;
        this.status = status;
        this.data_alerta = data_alerta;
    }

    public Alerta(int id, int prioridade, String descricao, String nivel, String status, String data_alerta) {
        this.id = id;
        this.prioridade = prioridade;
        this.descricao = descricao;
        this.nivel = nivel;
        this.status = status;
        this.data_alerta = data_alerta;
    }



    public int getId() {
        return id;
    }

    public int getPrioridade() {
        return prioridade;
    }

    public String getDescricao() {
        return descricao;
    }

    public String getNivel() {
        return nivel;
    }

    public String getStatus() {
        return status;
    }

    public String getData_alerta() {
        return data_alerta;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPrioridade(int prioridade) {
        this.prioridade = prioridade;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setData_alerta(String data_alerta) {
        this.data_alerta = data_alerta;
    }
}
