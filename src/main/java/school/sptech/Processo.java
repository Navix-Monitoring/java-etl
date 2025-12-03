package school.sptech;

public class Processo {
    private String nome,timestamp;
    private Integer pid;
    private Double cpu, ram;
    private Double bytesLidos, bytesEscritos, tempoVida;

    public Processo(String timestamp, Integer pid, String nome, Double cpu, Double ram,Double tempoVida, Double bytesLidos, Double bytesEscritos) {
        this.timestamp = timestamp;
        this.pid = pid;
        this.nome = nome;
        this.cpu = cpu;
        this.ram = ram;
        this.tempoVida = tempoVida;
        this.bytesLidos = bytesLidos;
        this.bytesEscritos = bytesEscritos;
    }

    @Override
    public String toString() {
        return "Processo" + nome +
                ", pid " + pid +
                ", cpu " + cpu +
                ", ram " + ram +
                ", tempoVida " + tempoVida +
                ", bytesLidos " + bytesLidos +
                ", bytesEscritos " + bytesEscritos +
                '}';
    }

    public Double getBytesLidos() {
        return bytesLidos;
    }

    public Double getBytesEscritos() {
        return bytesEscritos;
    }

    public Double getTempoVida() {
        return tempoVida;
    }

    public void setTempoVida(Double tempoVida) {
        this.tempoVida = tempoVida;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public void setBytesLidos(Double bytesLidos) {
        this.bytesLidos = bytesLidos;
    }

    public void setBytesEscritos(Double bytesEscritos) {
        this.bytesEscritos = bytesEscritos;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public Double getRam() {
        return ram;
    }

    public void setRam(Double ram) {
        this.ram = ram;
    }


}