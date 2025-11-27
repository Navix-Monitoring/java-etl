package sptech.school;

// logica mediana

import java.util.Collections;
import java.util.List;

public class CalculadoraEstatistica {

    public Double calcularMediana(List<Double> valores) {
        if (valores == null || valores.isEmpty()) return 0.0;

        Collections.sort(valores);
        int meio = valores.size() / 2;

        if (valores.size() % 2 == 1) {
            return valores.get(meio);
        } else {
            return (valores.get(meio - 1) + valores.get(meio)) / 2.0;
        }
    }
}