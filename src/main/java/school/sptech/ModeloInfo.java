package school.sptech;

public class ModeloInfo {
    private int fkModelo;
    private int fkLote;
    private String nomeModelo;
    private String empresaNome;


    public int getFkModelo() {
        return fkModelo;
    }

    public void setFkModelo(int fkModelo) {
        this.fkModelo = fkModelo;
    }

    public int getFkLote() {
        return fkLote;
    }

    public void setFkLote(int fkLote) {
        this.fkLote = fkLote;
    }

    public String getNomeModelo() {
        return nomeModelo;
    }

    public void setNomeModelo(String nomeModelo) {
        this.nomeModelo = nomeModelo;
    }

    public String getEmpresaNome() {
        return empresaNome;
    }

    public void setEmpresaNome(String empresaNome) {
        this.empresaNome = empresaNome;
    }
}
