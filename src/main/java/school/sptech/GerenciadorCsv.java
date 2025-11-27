package school.sptech;

// leitura csv

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale; // Importe Locale
import java.util.Scanner;

public class GerenciadorCsv {

    private final Data utilData = new Data();

    public static class DadosLote {
        public List<Double> listaCpu = new ArrayList<>();
        public List<Double> listaRam = new ArrayList<>();
        public List<Double> listaDisco = new ArrayList<>();
        public List<Double> listaProcessos = new ArrayList<>();
        public List<Double> listaTemperatura = new ArrayList<>();

        public String diaSemanaPortugues = "Desconhecido";

        public boolean estaVazio() {
            return listaCpu.isEmpty();
        }
    }

    public DadosLote processarStream(InputStream inputStream) {
        DadosLote dados = new DadosLote();

        // Define o Locale para US para garantir que Double.parseDouble entenda o ponto como separador
        Locale.setDefault(Locale.US);

        try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {

            boolean identificouDia = false;

            while (scanner.hasNextLine()) {
                String linha = scanner.nextLine();

                // 1. CORREÇÃO NO FILTRO DE CABEÇALHO
                // Verifica se a linha começa exatamente com "TIMESTAMP,MAC,CPU..."
                if (linha.trim().isEmpty() || linha.toUpperCase().startsWith("TIMESTAMP,")) {
                    continue;
                }

                String[] colunas = linha.split(",");

                // 2. CORREÇÃO NO MÍNIMO DE COLUNAS
                // Precisamos de pelo menos 8 colunas para pegar a temperatura (índice 7)
                if (colunas.length < 8) {
                    System.out.println("Linha ignorada: Número insuficiente de colunas.");
                    continue;
                }

                try {
                    if (!identificouDia) {
                        // TIMESTAMP está no índice 0
                        String dataString = colunas[0];

                        LocalDateTime dataHora = utilData.converterStringParaData(dataString);

                        dados.diaSemanaPortugues = utilData.traduzirDiaSemana(dataHora.getDayOfWeek());

                        identificouDia = true;
                    }

                    // 3. CORREÇÃO NOS ÍNDICES DE ACESSO
                    // CPU está no índice 2
                    dados.listaCpu.add(Double.parseDouble(colunas[2]));
                    // RAM está no índice 3
                    dados.listaRam.add(Double.parseDouble(colunas[3]));
                    // DISCO está no índice 4
                    dados.listaDisco.add(Double.parseDouble(colunas[4]));
                    // PROCESSOS (PROC) está no índice 5
                    dados.listaProcessos.add(Double.parseDouble(colunas[5]));
                    // TEMPERATURA (TEMP) está no índice 7
                    dados.listaTemperatura.add(Double.parseDouble(colunas[7]));

                } catch (NumberFormatException e) {
                    System.out.println("Linha ignorada: Erro de formato numérico (verifique ponto/vírgula decimal).");
                } catch (Exception e) {
                    System.out.println("Linha ignorada: Outro erro. Detalhes: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.out.println("Erro ao processar CSV: " + e.getMessage());
        }

        return dados;
    }
}
