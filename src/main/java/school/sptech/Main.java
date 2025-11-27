package sptech.school;


// responsavel por varrer os 6 lotes no bucket Trusted
// processar os arquivos CSV do dia atual, tratar
// e salvar/atualizar o JSON no bucket de destino.


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.regions.Region;

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
        String pastaFinal = "dashMediana/2025/";
        String chaveDestinoFinal = "RelatorioGeral.json";

        String jsonGeral = buscarJsonGeral(bucketDestino, chaveDestinoFinal);

        int ano = 2025;
        int diaAtual = LocalDate.now().getDayOfMonth();
        int mesAtual = LocalDate.now().getMonthValue();

        int numeroSemana = diaAtual <= 7 ? 1 :
                diaAtual <= 15 ? 2 :
                        diaAtual <= 22 ? 3 : 4;

        System.out.println("Iniciando processamento dos 6 lotes...");

        for (int i = 1; i <= 6; i++) {
            try {
                String chaveArquivo = ano + "/SPTechMotors/NAV-M100/IDVeiculo/" + i + "/Mes/" + mesAtual + "/Semana" + numeroSemana + "/" +
                        i + "-" + diaAtual + "-" + mesAtual + "-" + ano + ".csv";

                InputStream s3InputStream;
                try {
                    s3InputStream = s3Client.getObject(GetObjectRequest.builder()
                            .bucket(bucketOrigem)
                            .key(chaveArquivo)
                            .build());
                } catch (NoSuchKeyException e) {
                    continue;
                }

                GerenciadorCsv.DadosLote dados = processadorCsv.processarStream(s3InputStream);

                if (dados.estaVazio()) continue;

                double medCpu = calculadora.calcularMediana(dados.listaCpu);
                double medRam = calculadora.calcularMediana(dados.listaRam);
                double medDisco = calculadora.calcularMediana(dados.listaDisco);
                double medProc = calculadora.calcularMediana(dados.listaProcessos);
                double medTemp = calculadora.calcularMediana(dados.listaTemperatura);

                String jsonDia = gerenciadorJson.criarJsonDoDia(
                        dados.diaSemanaPortugues, medCpu, medRam, medDisco, medProc, medTemp
                );

                jsonGeral = gerenciadorJson.adicionarLoteAoGeral(jsonGeral, i, jsonDia);

                log.append("Lote ").append(i).append(" processado; ");

            } catch (Exception e) {
                System.out.println("Erro ao processar Lote " + i + ": " + e.getMessage());
            }
        }

        try {
            s3Client.putObject(PutObjectRequest.builder()
                            .bucket(bucketDestino)
                            .key(pastaFinal+chaveDestinoFinal)
                            .contentType("application/json")
                            .build(),
                    RequestBody.fromString(jsonGeral, StandardCharsets.UTF_8));

            return "Sucesso! Lotes atualizados: " + log.toString();
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