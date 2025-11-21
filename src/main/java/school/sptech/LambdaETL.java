package school.sptech;

import java.io.File;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.regions.Region;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

public class LambdaETL {

    private final S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();
    public void processarArquivo(String bucket, String key, String bucketSaida) {

        String destinatarioFinal = "OUTROS";
        String pastaMes = String.format("%02d", LocalDate.now().getMonthValue());
        int numeroSemanaMes = 0;

        int dia = LocalDate.now().getDayOfMonth();

        if (dia <= 7) {
            numeroSemanaMes = 1;
        }
        else if (dia <= 14) {
            numeroSemanaMes = 2;
        }
        else if (dia <= 21) {
            numeroSemanaMes = 3;
        }
        else {
            numeroSemanaMes = 4;
        }

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

            if (key.startsWith("1")) {
                destinatarioFinal = "1";
            }
            else if (key.startsWith("2")) {
                destinatarioFinal = "2";
            }
            else if (key.startsWith("3")) {
                destinatarioFinal = "3";
            }
            else if (key.startsWith("4")) {
                destinatarioFinal = "4";
            }
            else if (key.startsWith("5")) {
                destinatarioFinal = "5";
            }
            else if (key.startsWith("6")) {
                destinatarioFinal = "6";
            }

            Conexao conexao = new Conexao();
            ModeloDAO modeloDAO = new ModeloDAO();

            ModeloInfo info = modeloDAO.buscarPorLote(conexao.getConexao(), Integer.parseInt(destinatarioFinal));
            conexao.fecharConexao();

            if (info == null) {
                throw new RuntimeException("Nenhum modelo encontrado para o lote " + destinatarioFinal);
            }

// agora você tem:
            String nomeModelo = info.getNomeModelo();   // exemplo: "Navix2000"
            int fkModelo = info.getFkModelo();


            String saida = "/tmp/tratado_" + System.currentTimeMillis() + ".csv";
            MainETL.LeituraCSV leitura = new MainETL.LeituraCSV(fkModelo);
            leitura.processar(arquivoLocal.getAbsolutePath(), saida);

            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucketSaida)
                            .key("2025/"+nomeModelo+"/IDLote/"+destinatarioFinal+"/Mes/"+pastaMes+"/"+"Semana"+numeroSemanaMes+"/"+arquivoLocal.getName())
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
            String bucketSaida = System.getenv("NOME_BUCKET_SAIDA");

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

            processarArquivo(bucket, key, bucketSaida);
            return "Processamento finalizado com sucesso!";
        } catch (Exception e) {
            e.printStackTrace();
            return "Erro no handleRequest: " + e.getMessage();
        }
    }

}
