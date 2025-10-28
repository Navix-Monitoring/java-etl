package school.sptech;

// Classe para armazenar os parâmetros de um único tipo de hardware
class ParametroHardware {
    public String tipoHardware; // Renomeado de nomeHardware para tipoHardware
    public String unidadeMedida;
    public double minimo;
    public double neutro;
    public double atencao;
    public double critico;

    // Construtor
    public ParametroHardware(String tipoHardware, String unidadeMedida, double minimo, double neutro, double atencao, double critico) {
        this.tipoHardware = tipoHardware;
        this.unidadeMedida = unidadeMedida;
        this.minimo = minimo;
        this.neutro = neutro;
        this.atencao = atencao;
        this.critico = critico;
    }
}