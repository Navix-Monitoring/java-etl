package previsao;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GerenciadorCsv {

    // Classe interna para segurar os dados brutos do dia
    public static class DadosDia {
        public List<Double> cpu = new ArrayList<>();
        public List<Double> ram = new ArrayList<>();
        public List<Double> disco = new ArrayList<>();

        public boolean estaVazio() {
            return cpu.isEmpty();
        }
    }

    public DadosDia processarStream(InputStream inputStream) {
        DadosDia dados = new DadosDia();

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();

                // Ignora cabeçalho ou linhas vazias
                if (linha.trim().isEmpty() || linha.toUpperCase().startsWith("TIMESTAMP")) {
                    continue;
                }

                String[] colunas = linha.split(",");
                if (colunas.length < 5) continue; // Garante tamanho mínimo

                try {
                    // Índices baseados na sua ETL 1:
                    // Col 2: CPU, Col 3: RAM, Col 4: Disco
                    dados.cpu.add(Double.parseDouble(colunas[2]));
                    dados.ram.add(Double.parseDouble(colunas[3]));
                    dados.disco.add(Double.parseDouble(colunas[4]));
                } catch (NumberFormatException e) {
                    // Ignora erro de parse
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao ler CSV: " + e.getMessage());
        }
        return dados;
    }
}