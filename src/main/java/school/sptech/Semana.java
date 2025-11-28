package school.sptech;

import java.util.ArrayList;
import java.util.List;

public class Semana {

    private int numeroSemana;
    private List<Dia> dias;

    public Semana(int numeroSemana) {
        this.numeroSemana = numeroSemana;
        this.dias = new ArrayList<>();
    }

    public int getNumeroSemana() { return numeroSemana; }
    public List<Dia> getDias() { return dias; }

    @Override
    public String toString() {
        return "Semana{" +
                "numeroSemana=" + numeroSemana +
                ", dias=" + dias +
                '}';
    }
}
