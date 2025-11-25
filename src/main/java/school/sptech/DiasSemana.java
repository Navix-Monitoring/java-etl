package school.sptech;

public class DiasSemana {

    private int lote;
    private int cpuCritico;
    private int ramCritico;
    private int discoCritico;
    private int tempCritico;
    private int totalBaixo;
    private int totalNeutro;
    private int totalAlerta;
    private int totalCritico;
    private int totalAvisos;

    // Getters e Setters
    public int getLote() {
        return lote;
    }

    public void setLote(int lote) {
        this.lote = lote;
    }

    public int getCpuCritico() {
        return cpuCritico;
    }

    public void setCpuCritico(int cpuCritico) {
        this.cpuCritico = cpuCritico;
    }

    public int getRamCritico() {
        return ramCritico;
    }

    public void setRamCritico(int ramCritico) {
        this.ramCritico = ramCritico;
    }

    public int getDiscoCritico() {
        return discoCritico;
    }

    public void setDiscoCritico(int discoCritico) {
        this.discoCritico = discoCritico;
    }

    public int getTempCritico() {
        return tempCritico;
    }

    public void setTempCritico(int tempCritico) {
        this.tempCritico = tempCritico;
    }

    public int getTotalBaixo() {
        return totalBaixo;
    }

    public void setTotalBaixo(int totalBaixo) {
        this.totalBaixo = totalBaixo;
    }

    public int getTotalNeutro() {
        return totalNeutro;
    }

    public void setTotalNeutro(int totalNeutro) {
        this.totalNeutro = totalNeutro;
    }

    public int getTotalAlerta() {
        return totalAlerta;
    }

    public void setTotalAlerta(int totalAlerta) {
        this.totalAlerta = totalAlerta;
    }

    public int getTotalCritico() {
        return totalCritico;
    }

    public void setTotalCritico(int totalCritico) {
        this.totalCritico = totalCritico;
    }

    public int getTotalAvisos() {
        return totalAvisos;
    }

    public void setTotalAvisos(int totalAvisos) {
        this.totalAvisos = totalAvisos;
    }

    @Override
    public String toString() {
        return "DiasSemana{" +
                "lote=" + lote +
                ", cpuCritico=" + cpuCritico +
                ", ramCritico=" + ramCritico +
                ", discoCritico=" + discoCritico +
                ", tempCritico=" + tempCritico +
                ", totalBaixo=" + totalBaixo +
                ", totalNeutro=" + totalNeutro +
                ", totalAlerta=" + totalAlerta +
                ", totalCritico=" + totalCritico +
                ", totalAvisos=" + totalAvisos +
                '}';
    }
}
