package school.sptech;

import java.io.File;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.regions.Region;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

public class LambdaETL  {

    private final S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();

    public void processarArquivo(String bucket, String key, String bucketSaida) {
        ZoneId horarioSP = ZoneId.of("America/Sao_Paulo");

        String destinatarioFinal = "OUTROS";
        String ano = String.format("%02d",LocalDate.now().getYear());
        String mes = String.format("%02d", LocalDate.now().getMonthValue());
        int dia = LocalDate.now(horarioSP).getDayOfMonth();
        int numeroSemanaMes = 0;
        if (dia <= 7) {
            numeroSemanaMes = 1;
        }
        else if (dia <= 15) {
            numeroSemanaMes = 2;
        }
        else if (dia <= 22) {
            numeroSemanaMes = 3;
        }
        else {
            numeroSemanaMes = 4;
        }
        try{
            File arquivoLocal = new File("/tmp/" + new File(key).getName());
            s3.getObject(GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build(),
                    Paths.get(arquivoLocal.getAbsolutePath()));

            destinatarioFinal = key.split("/")[0];
            Conexao conexao = new Conexao();
            ModeloDAO modeloDAO = new ModeloDAO();
            ModeloInfo info = modeloDAO.buscarPorLote(conexao.getConexao(), Integer.parseInt(destinatarioFinal));
            conexao.fecharConexao();

            if (info == null) {
                throw new RuntimeException("Nenhum modelo encontrado para o lote " + destinatarioFinal);
            }

            String nomeModelo = info.getNomeModelo();
            int fkModelo = info.getFkModelo();

            String saida = "/tmp/tratado_" + System.currentTimeMillis() + ".csv";
            Main.LeituraCSV leitura = new Main.LeituraCSV(fkModelo);

            leitura.processar(arquivoLocal.getAbsolutePath(), saida);

            s3.putObject(PutObjectRequest.builder()
                            .bucket(bucketSaida)
                            .key("dashProcessos/ano/"+ano+/"+nomeModelo+"/IDLote/"+destinatarioFinal+"/Mes/"+mes+"/"+"Semana"+numeroSemanaMes+"/Dia/"+dia+"/"+arquivoLocal.getName())
                            .build(),
                    Paths.get(saida));
            System.out.println("Arquivo processado e salvo com sucesso!");
        }catch (Exception e) {
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
