package school.sptech;

import java.util.ArrayList;
import java.util.List;

public class Dia {

    private int numeroDia; // 22, 23, 24...
    private List<DiasSemana> lotes;

    public Dia(int numeroDia) {
        this.numeroDia = numeroDia;
        this.lotes = new ArrayList<>();
    }

    public int getNumeroDia() {
        return numeroDia;
    }

    public List<DiasSemana> getLotes() {
        return lotes;
    }

    @Override
    public String toString() {
        return "Dia{" +
                "numeroDia=" + numeroDia +
                ", lotes=" + lotes +
                '}';
    }
}
