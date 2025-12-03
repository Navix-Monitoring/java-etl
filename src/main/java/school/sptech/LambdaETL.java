package school.sptech;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class LambdaETL {

    final S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();

    final ZoneId horarioSP = ZoneId.of("America/Sao_Paulo");
    final LocalDate dataAtual = LocalDate.now(horarioSP);
    final int ano = dataAtual.getYear();
    final int mes = dataAtual.getMonthValue();
    final int dia = dataAtual.getDayOfMonth();
    final int semana = (dia <= 7) ? 1 : (dia <= 15) ? 2 : (dia <= 22) ? 3 : 4;


    public String handlerRequest() throws IOException {
        String bucketTrusted = System.getenv("NOME_BUCKET_ENTRADA");
        String bucketClient = System.getenv("NOME_BUCKET_SAIDA");
        String prefixoModelos = "dashProcessos/ano" + ano + "/";

        System.out.println("Iniciando o tratamento de dados");

        //listar pastas de modelo
        List<String> pastasModelo = listarPastas(bucketTrusted, prefixoModelos);

        for(String modelo: pastasModelo){
            //Criando um prefixo dinâmico baseado no modelo
            String prefixoLotes = String.format(
                    "dashProcessos/ano/%d/%s/",
                    ano, modelo
            );
            //listar pastas de lotes
            List<String> pastasLotes = listarPastas(bucketTrusted,prefixoLotes);

            //For para percorrer os lotes
            for (String lote : pastasLotes){
                List<Processo> todosProcessos = new ArrayList<>();
                //Gerando o caminno para encontrar os processos
                String prefixoCsvs =
                        "dashProcessos/ano/"+ano+"/"+modelo+"/IDLote/"+lote+"/Mes/"+mes+"/Semana"+semana+"/Dia/"+dia+"/";

                ListObjectsV2Request requisicao = ListObjectsV2Request.builder()
                        .bucket(bucketTrusted)
                        .prefix(prefixoCsvs)
                        .build();
                var resposta = s3.listObjectsV2(requisicao);

                //for para ler e salvar cada conteudo de dentro da pasta
                for (var obj : resposta.contents()) {

                    System.out.println("Arquivo encontrado: " + obj.key());

                    //baixando o csv na memória
                    var s3Object = s3.getObject(
                            GetObjectRequest.builder()
                                    .bucket(bucketTrusted)
                                    .key(obj.key())
                                    .build()
                    );

                    List<Processo> processosDoArquivo =
                            Main.verificarArquivos(bucketTrusted, obj.key());

                    todosProcessos.addAll(processosDoArquivo);

                }

                //Salvando a data atual em uma variavel
                String data = ano+"-"+mes+"-"+dia+"-";

                //gerando os csv's
                Main.gerarSumarizacao(todosProcessos,bucketClient,"listaProcessos_"+data+".csv",modelo,lote);
                //Mandando o arquivo para o bucket
                s3.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketClient)
                                .key("dashProcessos/medianaProcessos/Modelo/"+modelo+
                                        "/IDLote/"+lote+"/Ano/"+ano+"/Mes/"+mes+"/Semana/"
                                        +semana+"/Dia/"+dia+"/"+"listaProcessos_"+data+".csv")
                                .build(),
                        Paths.get("/tmp/listaProcessos_"+data+".csv")
                );
                //Limpando para que não ocupe todos os 512MB de tamanho da pasta tmp
                LambdaETL.limparTmp();

                Main.gerarMediana(todosProcessos,bucketClient, "mediaProcessos_"+data+".csv",modelo,lote);
                //Mandando o arquivo para o bucket
                s3.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketClient)
                                .key("dashProcessos/medianaProcessos/Modelo/"+modelo+
                                        "/IDLote/"+lote+"/Ano/"+ano+"/Mes/"+mes+"/Semana/"
                                        +semana+"/Dia/"+dia+"/"+"mediaProcessos_"+data+".csv")
                                .build(),
                        Paths.get("/tmp/mediaProcessos_"+data+".csv")
                );
                //Limpando para que não ocupe todos os 512MB de tamanho da pasta tmp
                LambdaETL.limparTmp();

            }
        }
        return "Processamento de arquivos finalizado!";
    }

    public List<String> listarPastas(String bucket, String prefixo){

        ListObjectsV2Request requisicao = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefixo)
                .delimiter("/")
                .build();

        var resposta = s3.listObjectsV2(requisicao);

        return resposta.commonPrefixes().stream()
                .map(cp -> cp.prefix()
                        .replace(prefixo, "")
                        .replace("/", ""))
                .toList();

    }

    public static void limparTmp() {
        File tmp = new File("/tmp/");

        if (tmp.exists() && tmp.isDirectory()) {
            for (File file : tmp.listFiles()) {
                deletarRecursivamente(file);
            }
        }
    }

    private static void deletarRecursivamente(File file) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                deletarRecursivamente(sub);
            }
        }
        file.delete();
    }

}


