package school.sptech;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import software.amazon.awssdk.regions.Region;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class RelatorioSemanalLambda implements RequestHandler<Map<String, Object>, String> {

    private final ObjectMapper mapper = new ObjectMapper();
    private final String bucket = "bucket-client-navix";

    private final S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();


    @Override
    public String handleRequest(Map<String, Object> input, Context context) {

        try {
            //Ontem
            LocalDate hoje = LocalDate.now();
            LocalDate ontem = VerificadorData.diaAnterior(hoje);

            //Ultimos 7 dias
            List<LocalDate> ultimos7 = VerificadorData.retroceder(ontem, 7);
            Semana semanaAtual = new Semana(1);

            for (LocalDate data : ultimos7) {

                String key = VerificadorData.caminhoS3(data);
                context.getLogger().log("Lendo S3: " + key + "\n");

                InputStream json = baixarDoS3(bucket, key);
                if (json == null) {
                    context.getLogger().log("Arquivo não existe: " + key + "\n");
                    continue;
                }

                List<DiasSemana> lotes =
                        mapper.readValue(json, new TypeReference<>() {});

                Dia dia = new Dia(data.getDayOfMonth());
                dia.getLotes().addAll(lotes);
                semanaAtual.getDias().add(dia);
            }

            String jsonUltimos7 = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(semanaAtual);

            uploadParaS3(bucket,
                    "dashAlertas/Ultimos7Dias/Relatorio-Ultimos7Dias.json",
                    jsonUltimos7
            );



            //Sete dias anteriores a esse
            LocalDate ultimoDia = ultimos7.get(6);
            LocalDate anterior = VerificadorData.diaAnterior(ultimoDia);

            List<LocalDate> ultimos7Antes = VerificadorData.retroceder(anterior, 7);
            Semana semanaAnterior = new Semana(2);

            for (LocalDate data : ultimos7Antes) {

                String key = VerificadorData.caminhoS3(data);
                context.getLogger().log("Lendo S3: " + key + "\n");

                InputStream json = baixarDoS3(bucket, key);
                if (json == null) continue;

                List<DiasSemana> lotes =
                        mapper.readValue(json, new TypeReference<>() {});

                Dia dia = new Dia(data.getDayOfMonth());
                dia.getLotes().addAll(lotes);
                semanaAnterior.getDias().add(dia);
            }

            String jsonUltimos7Antes = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(semanaAnterior);

            uploadParaS3(bucket,
                    "dashAlertas/Ultimos7Dias/Relatorio-Ultimos7Dias-Anteriores.json",
                    jsonUltimos7Antes
            );

            return "Relatórios gerados com sucesso";

        } catch (Exception e) {
            context.getLogger().log("ERRO: " + e.getMessage());
            return "Erro no relatório";
        }
    }


    //Get do S3
    private InputStream baixarDoS3(String bucket, String key) {
        try {
            return s3.getObject(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
        } catch (Exception e) {
            return null;
        }
    }


    //Put do S3

    private void uploadParaS3(String bucket, String key, String conteudoJson) {

        s3.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .contentType("application/json")
                        .build(),
                RequestBody.fromString(conteudoJson)
        );
    }
}
