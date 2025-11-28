package previsao;

import java.util.List;

public class SerieDados {
    public String label;
    public List<Double> data;

    // Construtor vazio para o Jackson
    public SerieDados() {}

    public SerieDados(String label, List<Double> data) {
        this.label = label;
        this.data = data;
    }
}