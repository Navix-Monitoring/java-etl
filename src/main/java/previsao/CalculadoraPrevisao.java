package previsao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalculadoraPrevisao {

    // Calcula a Mediana (agrupa as milhares de linhas do dia em 1 número)
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

    // Calcula o Slope (Inclinação/Taxa de crescimento)
    public Double calcularSlope(List<Double> historico) {
        int n = historico.size();
        if (n < 2) return 0.0;

        double somaX = 0, somaY = 0, somaXY = 0, somaX2 = 0;
        // X = Tempo (0, 1, 2...), Y = Valor
        for (int i = 0; i < n; i++) {
            double y = historico.get(i);
            somaX += i;
            somaY += y;
            somaXY += (i * y);
            somaX2 += (i * i);
        }
        return (n * somaXY - somaX * somaY) / (n * somaX2 - somaX * somaX);
    }

    // Gera a lista: Histórico + 7 Dias Futuros
    public List<Double> gerarProjecao(List<Double> historico, int diasFuturos) {
        List<Double> projecao = new ArrayList<>(historico);
        int n = historico.size();

        if (n < 2) return projecao;

        Double slope = calcularSlope(historico);

        // Calcula Intercepto
        double somaX = 0, somaY = 0;
        for (int i = 0; i < n; i++) {
            somaX += i;
            somaY += historico.get(i);
        }
        double intercept = (somaY - slope * somaX) / n;

        // Preenche o futuro
        for (int i = 0; i < diasFuturos; i++) {
            int xFuturo = n + i;
            double yPrevisto = (slope * xFuturo) + intercept;

            // Trava valores entre 0 e 100
            if (yPrevisto > 100.0) yPrevisto = 100.0;
            if (yPrevisto < 0.0) yPrevisto = 0.0;

            projecao.add(Math.round(yPrevisto * 100.0) / 100.0);
        }
        return projecao;
    }

    // Calcula RUL (Dias restantes até 90%)
    public int calcularRUL(List<Double> historico, double limite) {
        if (historico.isEmpty()) return 999;

        double slope = calcularSlope(historico);
        double ultimoValor = historico.get(historico.size() - 1);

        if (ultimoValor >= limite) return 0; // Já quebrou
        if (slope <= 0) return 999; // Tendência de queda ou estável

        return (int) ((limite - ultimoValor) / slope);
    }
}