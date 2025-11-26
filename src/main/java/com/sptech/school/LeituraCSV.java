package com.sptech.school;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.time.Instant;
import java.time.ZoneId;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.regions.Region;

public class LeituraCSV {

    private final S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();

    public final ObjectMapper mapper = new ObjectMapper();

    public List<LoteResumo> processar() throws IOException {
        List<LoteResumo> resumos = new ArrayList<>();

        String nomeBucket = "bucket-trusted-navix";
        String bucketSaida = "bucket-client-navix";

        int ano = 2025;

        ZoneId horarioSP = ZoneId.of("America/Sao_Paulo");

        int diaAtual = LocalDate.now(horarioSP).getDayOfMonth();
        int mesAtual = LocalDate.now(horarioSP).getMonthValue();

        int numeroSemana =
                diaAtual <= 7 ? 1 :
                        diaAtual <= 15 ? 2 :
                                diaAtual <= 22 ? 3 : 4;

        int lote = 1;

            for (int i = 1; i <= 6; i++) {

                String keyEntrada =
                        ano +"/Tech Solutions LTDA/NAV-M100/IDVeiculo/"+ i+"/Mes/"+ mesAtual + "/Semana" + numeroSemana + "/" +
                                i + "-" + diaAtual + "-" + mesAtual + "-" + ano + ".csv";

                String localEntrada = "/tmp/input-" + i + ".csv";

                boolean arquivoExiste = true;
                try {
                    s3.getObject(
                            GetObjectRequest.builder()
                                    .bucket(nomeBucket)
                                    .key(keyEntrada)
                                    .build(),
                            Paths.get(localEntrada)
                    );
                } catch (Exception e) {
                    arquivoExiste = false;
                }

                int qtdCpuBaixo = 0, qtdCpuNeutro = 0, qtdCpuAlerta = 0, qtdCpuCritico = 0;
                int qtdRamBaixo = 0, qtdRamNeutro = 0, qtdRamAlerta = 0, qtdRamCritico = 0;
                int qtdDiscoBaixo = 0, qtdDiscoNeutro = 0, qtdDiscoAlerta = 0, qtdDiscoCritico = 0;
                int qtdTempBaixo = 0, qtdTempNeutro = 0, qtdTempAlerta = 0, qtdTempCritico = 0;

                if (!arquivoExiste) {

                    LoteResumo r = new LoteResumo();
                    r.lote = lote;
                    resumos.add(r);

                    lote++;
                    continue;
                }

                try (Scanner sc = new Scanner(new File(localEntrada))) {

                    while (sc.hasNextLine()) {

                        String linha = sc.nextLine().trim();
                        if (linha.isEmpty() || linha.toUpperCase().startsWith("TIMESTAMP"))
                            continue;

                        String[] campos = linha.split(",");
                        if (campos.length < 13) continue;

                        // CPU
                        String sCpu = campos[9].toUpperCase();
                        if (sCpu.equals("BAIXO")) qtdCpuBaixo++;
                        else if (sCpu.equals("NEUTRO")) qtdCpuNeutro++;
                        else if (sCpu.equals("ATENÇÃO")) qtdCpuAlerta++;
                        else if (sCpu.equals("CRÍTICO")) qtdCpuCritico++;

                        // RAM
                        String sRam = campos[10].toUpperCase();
                        if (sRam.equals("BAIXO")) qtdRamBaixo++;
                        else if (sRam.equals("NEUTRO")) qtdRamNeutro++;
                        else if (sRam.equals("ATENÇÃO")) qtdRamAlerta++;
                        else if (sRam.equals("CRÍTICO")) qtdRamCritico++;

                        // DISCO
                        String sDisco = campos[11].toUpperCase();
                        if (sDisco.equals("BAIXO")) qtdDiscoBaixo++;
                        else if (sDisco.equals("NEUTRO")) qtdDiscoNeutro++;
                        else if (sDisco.equals("ATENÇÃO")) qtdDiscoAlerta++;
                        else if (sDisco.equals("CRÍTICO")) qtdDiscoCritico++;

                        // TEMP
                        String sTemp = campos[12].toUpperCase();
                        if (sTemp.equals("BAIXO")) qtdTempBaixo++;
                        else if (sTemp.equals("NEUTRO")) qtdTempNeutro++;
                        else if (sTemp.equals("ATENÇÃO")) qtdTempAlerta++;
                        else if (sTemp.equals("CRÍTICO")) qtdTempCritico++;
                    }

                    int totalCritico = qtdCpuCritico + qtdRamCritico + qtdDiscoCritico + qtdTempCritico;
                    int totalAlerta  = qtdCpuAlerta + qtdRamAlerta + qtdDiscoAlerta + qtdTempAlerta;
                    int totalNeutro  = qtdCpuNeutro + qtdRamNeutro + qtdDiscoNeutro + qtdTempNeutro;
                    int totalBaixo   = qtdCpuBaixo + qtdRamBaixo + qtdDiscoBaixo + qtdTempBaixo;
                    int totalAvisos  = totalBaixo + totalNeutro + totalAlerta + totalCritico;

                    //Enviar o relatório pro Jira
                    LoteResumo r = new LoteResumo();
                    r.lote = lote;
                    r.cpuCritico = qtdCpuCritico;
                    r.ramCritico = qtdRamCritico;
                    r.discoCritico = qtdDiscoCritico;
                    r.tempCritico = qtdTempCritico;
                    r.totalBaixo = totalBaixo;
                    r.totalNeutro = totalNeutro;
                    r.totalAlerta = totalAlerta;
                    r.totalCritico = totalCritico;
                    r.totalAvisos = totalAvisos;
                    resumos.add(r);

                }
                lote++;
                File fIn = new File(localEntrada);
                if (fIn.exists()) fIn.delete();
            }




        String localJson = "/tmp/relatorio.json";

        String json = mapper.writeValueAsString(resumos);

        Files.writeString(Paths.get(localJson),json);

        String keySaida =
                "dashAlertas/"+ano+"/" + mesAtual + "/Semana" + numeroSemana +
                        "/Relatorio-Final-" + diaAtual + "-" + mesAtual + "-" + ano + ".json";

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucketSaida)
                        .key(keySaida)
                        .build(),
                Paths.get(localJson)

        );

        File fJson = new File(localJson);
        if(fJson.exists()) fJson.delete();
        return resumos;
    }

    public static String gerarDescricaoJira(List<LoteResumo> resumos) {
        StringBuilder sb = new StringBuilder();

        ZoneId zoneSp = ZoneId.of("America/Sao_Paulo");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(zoneSp);

        String dataHoraSp = dtf.format(Instant.now());

        sb.append("Relatório gerado em: ").append(dataHoraSp).append("\n\n");

        for (LoteResumo r : resumos) {
            sb.append("Lote ").append(r.lote).append(":\n")
                    .append("  - CPU Crítico: ").append(r.cpuCritico).append("\n")
                    .append("  - RAM Crítico: ").append(r.ramCritico).append("\n")
                    .append("  - DISCO Crítico: ").append(r.discoCritico).append("\n")
                    .append("  - TEMP Crítico: ").append(r.tempCritico).append("\n")
                    .append("  - Total Baixo: ").append(r.totalBaixo).append("\n")
                    .append("  - Total Neutro: ").append(r.totalNeutro).append("\n")
                    .append("  - Total Alerta: ").append(r.totalAlerta).append("\n")
                    .append("  - Total Crítico: ").append(r.totalCritico).append("\n")
                    .append("  - Total Avisos: ").append(r.totalAvisos).append("\n\n");
        }

        return sb.toString();
    }

}
