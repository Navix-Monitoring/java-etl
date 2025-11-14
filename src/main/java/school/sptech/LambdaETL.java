package school.sptech;

import java.io.File;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.regions.Region;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class LambdaETL {

    private final S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();
    public void processarArquivo(String bucket, String key) {
        try {
            File arquivoLocal = new File("/tmp/" + new File(key).getName());

            if (arquivoLocal.exists()) {
                arquivoLocal.delete();
            }


            s3.getObject(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build(),
                    Paths.get(arquivoLocal.getAbsolutePath()));

            String saida = "/tmp/tratado_" + System.currentTimeMillis() + ".csv";
            MainETL.LeituraCSV leitura = new MainETL.LeituraCSV();
            leitura.processar(arquivoLocal.getAbsolutePath(), saida);

            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucket)
                            .key("tratados/" + arquivoLocal.getName())
                            .build(),
                    Paths.get(saida));

            System.out.println("Arquivo processado e salvo com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erro no processamento: " + e.getMessage());
        }
    }
    public String handleRequest(Map<String, Object> event) {
        try {
            String bucket = null;
            String key = null;

            if (event != null && event.containsKey("Records")) {
                var records = (List<Map<String, Object>>) event.get("Records");
                if (!records.isEmpty()) {
                    var s3 = (Map<String, Object>) records.get(0).get("s3");
                    bucket = ((Map<String, Object>) s3.get("bucket")).get("name").toString();
                    key = ((Map<String, Object>) s3.get("object")).get("key").toString();
                }
            }

            System.out.println("Bucket: " + bucket);
            System.out.println("Arquivo: " + key);

            processarArquivo(bucket, key);
            return "Processamento finalizado com sucesso!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro no handleRequest: " + e.getMessage();
        }
    }

}
