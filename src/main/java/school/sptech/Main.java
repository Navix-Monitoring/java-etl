package school.sptech;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;

public class Main implements RequestHandler<S3Event, String> {

    private final S3Client s3Client = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();

    private final GerenciadorCsv processadorCsv = new GerenciadorCsv();
    private final CalculadoraEstatistica calculadora = new CalculadoraEstatistica();
    private final GerenciadorJson gerenciadorJson = new GerenciadorJson();

    @Override
    public String handleRequest(S3Event s3Event, Context context) {
        StringBuilder log = new StringBuilder();

        String bucketOrigem = "bucket-trusted-navix";
        String bucketDestino = "bucket-client-navix";
        String caminhoCompleto = "dashMediana/2025/RelatorioGeral.json";

        // Busca o JSON atual (para fazer o append/update dos dados)
        String jsonGeral = buscarJsonGeral(bucketDestino, caminhoCompleto);

        System.out.println("Iniciando processamento semanal dos 6 lotes...");

        // Data final é hoje
        LocalDate dataHoje = LocalDate.now();

        // Loop pelos Lotes (Veículos/Máquinas)
        for (int i = 1; i <= 6; i++) {
            System.out.println("Processando Lote " + i);

            // Loop para pegar os últimos 7 dias (hoje + 6 dias para trás)
            // j = 0 é hoje, j = 1 é ontem, etc.
            for (int j = 0; j < 7; j++) {

                // Calcula a data específica da iteração
                LocalDate dataAlvo = dataHoje.minusDays(j);

                int ano = dataAlvo.getYear();
                int mes = dataAlvo.getMonthValue();
                int dia = dataAlvo.getDayOfMonth();

                // Calcula a semana baseada no dia do mês da DATA ALVO
                int numeroSemana = dia <= 7 ? 1 :
                        dia <= 14 ? 2 :
                                dia <= 21 ? 3 : 4;

                try {
                    // Monta o caminho dinâmico baseado na data do loop
                    String chaveArquivo = ano + "/SPTechMotors/NAV-M100/IDVeiculo/" + i + "/Mes/" + mes + "/Semana" + numeroSemana + "/" +
                            i + "-" + dia + "-" + mes + "-" + ano + ".csv";

                    InputStream s3InputStream;
                    try {
                        s3InputStream = s3Client.getObject(GetObjectRequest.builder()
                                .bucket(bucketOrigem)
                                .key(chaveArquivo)
                                .build());
                    } catch (NoSuchKeyException e) {
                        // Se o arquivo desse dia específico não existir, apenas pula para o próximo dia
                        continue;
                    }

                    GerenciadorCsv.DadosLote dados = processadorCsv.processarStream(s3InputStream);

                    if (dados.estaVazio()) continue;

                    double medCpu = calculadora.calcularMediana(dados.listaCpu);
                    double medRam = calculadora.calcularMediana(dados.listaRam);
                    double medDisco = calculadora.calcularMediana(dados.listaDisco);
                    double medProc = calculadora.calcularMediana(dados.listaProcessos);
                    double medTemp = calculadora.calcularMediana(dados.listaTemperatura);

                    // Cria o JSON apenas deste dia
                    String jsonDia = gerenciadorJson.criarJsonDoDia(
                            dados.diaSemanaPortugues, medCpu, medRam, medDisco, medProc, medTemp
                    );

                    // Adiciona/Atualiza este dia no JSON Geral
                    jsonGeral = gerenciadorJson.adicionarLoteAoGeral(jsonGeral, i, jsonDia);

                    log.append("Lote ").append(i).append(" (Data: ").append(dataAlvo).append(") OK; ");

                } catch (Exception e) {
                    System.out.println("Erro ao processar Lote " + i + " na data " + dataAlvo + ": " + e.getMessage());
                }
            }
        }

        try {
            // Salva o JSON consolidado da semana inteira
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketDestino)
                            .key(caminhoCompleto)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(jsonGeral, StandardCharsets.UTF_8));

            return "Sucesso! Processamento semanal concluído. Logs: " + log.toString();
        } catch (Exception e) {
            return "Erro ao salvar JSON final: " + e.getMessage();
        }
    }

    private String buscarJsonGeral(String bucket, String key) {
        try {
            InputStream is = s3Client.getObject(GetObjectRequest.builder().bucket(bucket).key(key).build());
            Scanner sc = new Scanner(is);
            String content = sc.useDelimiter("\\A").next();
            sc.close();
            if (content == null || content.trim().isEmpty()) return "[]";
            return content;
        } catch (NoSuchKeyException e) {
            return "[]";
        } catch (Exception e) {
            return "[]";
        }
    }
}