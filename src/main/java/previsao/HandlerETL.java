package previsao;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HandlerETL implements RequestHandler<Object, String> {

    private final S3Client s3Client = S3Client.builder().region(Region.US_EAST_1).build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final GerenciadorCsv leitorCsv = new GerenciadorCsv();
    private final CalculadoraPrevisao calculadora = new CalculadoraPrevisao();

    @Override
    public String handleRequest(Object input, Context context) {
        String bucketOrigem = "bucket-trusted-navix";
        String bucketDestino = "bucket-client-navix";
        String chaveJsonFinal = "dashboard_predicao.json"; // Arquivo que o front lê

        try {
            // 1. TENTA BAIXAR O JSON ATUAL (Para ter o histórico)
            FrotaDadosDTO dadosGerais;
            try {
                InputStream is = s3Client.getObject(GetObjectRequest.builder()
                        .bucket(bucketDestino).key(chaveJsonFinal).build());
                dadosGerais = mapper.readValue(is, FrotaDadosDTO.class);
            } catch (Exception e) {
                System.out.println("JSON não encontrado. Criando novo.");
                dadosGerais = new FrotaDadosDTO();
            }

            // Variáveis de Data (Hoje)
            LocalDate hoje = LocalDate.now();
            int ano = hoje.getYear();
            int mes = hoje.getMonthValue();
            int dia = hoje.getDayOfMonth();
            // Lógica de semana simples:
            int semana = (dia <= 7) ? 1 : (dia <= 15) ? 2 : (dia <= 22) ? 3 : 4;

            // 2. LOOP PELOS 6 LOTES
            for (int i = 1; i <= 6; i++) {
                String nomeLote = "A00" + i;

                // Monta o caminho do CSV na Trusted (baseado na sua ETL 1)
                // Ex: 2025/NAV-M100/IDLote/1/Mes/11/Semana4/1-27-11-2025.csv
                String chaveCsv = ano + "/NAV-M100/IDLote/" + i + "/Mes/" + mes + "/Semana" + semana + "/" +
                        i + "-" + dia + "-" + mes + "-" + ano + ".csv";

                try {
                    InputStream s3Is = s3Client.getObject(GetObjectRequest.builder()
                            .bucket(bucketOrigem).key(chaveCsv).build());

                    GerenciadorCsv.DadosDia dadosDia = leitorCsv.processarStream(s3Is);

                    if (!dadosDia.estaVazio()) {
                        // Calcula Medianas do dia
                        double medCpu = calculadora.calcularMediana(dadosDia.cpu);
                        double medRam = calculadora.calcularMediana(dadosDia.ram);
                        double medDisco = calculadora.calcularMediana(dadosDia.disco);

                        // Atualiza CPU, RAM e Disco no objeto principal
                        atualizarSerieComponente(dadosGerais.cpu.semana, nomeLote, medCpu);
                        atualizarSerieComponente(dadosGerais.ram.semana, nomeLote, medRam);
                        atualizarSerieComponente(dadosGerais.disco.semana, nomeLote, medDisco);
                    }
                } catch (Exception e) {
                    System.out.println("CSV não encontrado para " + nomeLote + " (pode não ter rodado hoje ainda).");
                }
            }

            // 3. RECALCULAR KPIS, SLOPE E RUL PARA TODOS OS COMPONENTES
            finalizarComponente(dadosGerais.cpu.semana);
            finalizarComponente(dadosGerais.ram.semana);
            finalizarComponente(dadosGerais.disco.semana);

            // 4. SALVAR JSON ATUALIZADO NO CLIENT BUCKET
            String jsonOutput = mapper.writeValueAsString(dadosGerais);

            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketDestino)
                            .key(chaveJsonFinal)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(jsonOutput, StandardCharsets.UTF_8));

            return "Sucesso! JSON atualizado no bucket client.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro fatal: " + e.getMessage();
        }
    }

    // Adiciona o dado de hoje e recalcula a previsão
    private void atualizarSerieComponente(ViewPeriodo view, String nomeLote, double valorHoje) {
        // Acha a série do lote ou cria
        SerieDados serie = view.series.stream()
                .filter(s -> s.label.equals(nomeLote))
                .findFirst()
                .orElse(null);

        if (serie == null) {
            serie = new SerieDados(nomeLote, new ArrayList<>());
            view.series.add(serie);
        }

        // Recupera apenas o histórico (remove previsão anterior)
        // Se a lista for maior que pontoCorte, corta o excesso (que era futuro)
        if (view.pontoCorte > 0 && serie.data.size() > view.pontoCorte + 1) {
            serie.data = new ArrayList<>(serie.data.subList(0, view.pontoCorte + 1));
        }

        // Adiciona o valor de hoje
        serie.data.add(valorHoje);

        // Limita histórico a 7 dias para não ficar gigante
        if (serie.data.size() > 7) {
            serie.data.remove(0);
        }

        // Gera nova projeção (7 dias)
        List<Double> comPrevisao = calculadora.gerarProjecao(serie.data, 7);
        serie.data = comPrevisao;

        // Atualiza labels genéricas se estiver vazio
        if (view.labels.isEmpty() || view.labels.size() != comPrevisao.size()) {
            view.labels = Arrays.asList("D-6", "D-5", "D-4", "D-3", "D-2", "Ontem", "Hoje", "F+1", "F+2", "F+3", "F+4", "F+5", "F+6", "F+7");
            view.pontoCorte = 6; // Índice do "Hoje"
        }
    }

    // Calcula RUL, Slope e KPIs para o JSON final
    private void finalizarComponente(ViewPeriodo view) {
        List<LoteAuxiliar> auxList = new ArrayList<>();

        for (SerieDados s : view.series) {
            // Isola histórico para calcular matemática
            int tamanhoReal = view.pontoCorte + 1;
            if (tamanhoReal > s.data.size()) tamanhoReal = s.data.size();

            List<Double> historico = s.data.subList(0, tamanhoReal);

            double slope = calculadora.calcularSlope(historico);
            int rul = calculadora.calcularRUL(historico, 90.0); // Limite 90%

            // Verifica se é crítico (Histórico ou Futuro > 90)
            boolean critico = s.data.stream().anyMatch(v -> v >= 90.0);

            auxList.add(new LoteAuxiliar(s.label, slope, rul, critico));
        }

        // Ordena por Slope (Pior primeiro)
        auxList.sort((a, b) -> Double.compare(b.slope, a.slope));

        // Preenche listas do JSON
        view.slopeData = auxList.stream().map(a -> a.slope).collect(Collectors.toList());
        view.slopeLabels = auxList.stream().map(a -> a.nome).collect(Collectors.toList());
        view.rul = auxList.stream().map(a -> a.rul).collect(Collectors.toList());
        view.rulLabels = auxList.stream().map(a -> a.nome).collect(Collectors.toList());

        // Preenche KPIs
        view.kpis.criticos = (int) auxList.stream().filter(a -> a.critico).count();
        view.kpis.seguros = 6 - view.kpis.criticos;
        if (!auxList.isEmpty()) {
            view.kpis.pior = "Lote " + auxList.get(0).nome;
        }
    }

    // Classe interna auxiliar para ordenação
    static class LoteAuxiliar {
        String nome; double slope; int rul; boolean critico;
        public LoteAuxiliar(String n, double s, int r, boolean c) {
            this.nome = n; this.slope = s; this.rul = r; this.critico = c;
        }
    }
}