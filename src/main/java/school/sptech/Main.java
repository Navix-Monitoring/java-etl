package school.sptech;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.*;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Stream;

public class Main {
    //Criando o cliente s3
    static final S3Client s3 = S3Client.builder()
            .region(Region.US_EAST_1)
            .build();
    //Variáveis constantes de horário
    static final ZoneId horarioSP = ZoneId.of("America/Sao_Paulo");
    static final LocalDate dataAtual = LocalDate.now(horarioSP);
    static final int ano = dataAtual.getYear();
    static final int mes = dataAtual.getMonthValue();
    static final int dia = dataAtual.getDayOfMonth();
    static final int semana = (dia <= 7) ? 1 : (dia <= 14) ? 2 : (dia <= 21
    ) ? 3 : 4;

    static private final ParametroDAO dao = new ParametroDAO();
    static private final Conexao conexao = new Conexao();
    private static final double bytesParaMB = 1024.0 * 1024.0;

    //Recebe os processos, escreve os no csv e manda para o bucket
    public static void gerarConsolidado(List<Processo> processos,String saidaArquivo, int idModelo){
        dao.carregarParametrosDoBanco(conexao.getConexao(),idModelo);

        for(Processo p: processos){

            Escrever e = new Escrever(
                    p.getTimestamp(),
                    p.getPid(),
                    p.getNome(),
                    p.getCpu(),
                    p.getRam(),
                    p.getTempoVida(),
                    p.getBytesLidos(),
                    p.getBytesEscritos()
            );
            e.escrever(saidaArquivo);
        }

    }

    //Recebe os processos, os junta, escreve o csv e envia para o bucket
    public static void gerarSumarizacao(List<Processo> processos, String saidaArquivo){
        Map<String, List<Processo>> agrupar = new HashMap<>();

        for(Processo p : processos){
            agrupar.computeIfAbsent(p.getNome(), k -> new ArrayList<>()).add(p);
        }
        for (Map.Entry<String, List<Processo>> entry : agrupar.entrySet()) {

            String nome = entry.getKey();

            List<Processo> lista = entry.getValue();
            List<Double> valoresRam = lista.stream()
                    .map(Processo::getRam)
                    .toList();

            Double mediaCpu = lista.stream().mapToDouble(Processo::getCpu).average().orElse(0);

            String cpuStatus = mediaCpu <= dao.cpuMin? "BAIXO": mediaCpu<= dao.cpuNeutro ? "NEUTRO" :
                    mediaCpu<= dao.cpuAtencao ? "ATENÇÃO" : "CRÍTICO";



            Double mediaRam = lista.stream().mapToDouble(Processo::getRam).average().orElse(0);
            String statusRAM = mediaRam <= dao.ramMin ? "BAIXO" :
                    mediaRam <= dao.ramNeutro ? "NEUTRO" :
                            mediaRam <= dao.ramAtencao ? "ATENÇÃO" : "CRÍTICO";


            Double mediaBytesLidos = lista.stream().mapToDouble(Processo::getBytesLidos).average().orElse(0);
            Double mediaBytesEscritos = lista.stream().mapToDouble(Processo::getBytesEscritos).average().orElse(0);

            Double bytesTotais = mediaBytesLidos + mediaBytesEscritos;
            Double disco = bytesTotais/ bytesParaMB;

            String statusDISCO = disco <= dao.discoMin ? "BAIXO" :
                    disco <= dao.discoNeutro ? "NEUTRO" :
                            disco <= dao.discoAtencao ? "ATENÇÃO" : "CRÍTICO";

            Double desvioPadraoRam = calcularDesvioPadrao(valoresRam);
            Double mediaTempoVida = lista.stream().mapToDouble(Processo::getTempoVida).average().orElse(0);

            mediaCpu = Math.round(mediaCpu * 100.0) / 100.0;
            mediaRam = Math.round(mediaRam * 100.0) / 100.0;
            mediaBytesLidos = Math.round(mediaBytesLidos * 100.0) / 100.0;
            mediaBytesEscritos = Math.round(mediaBytesEscritos * 100.0) / 100.0;

            Escrever e2 = new Escrever(
                    nome,
                    mediaCpu,
                    mediaRam,
                    desvioPadraoRam,
                    mediaBytesLidos,
                    mediaBytesEscritos,
                    mediaTempoVida,
                    cpuStatus,
                    statusRAM,
                    statusDISCO);
            e2.escrever(saidaArquivo);
        }
    }

    public static void detectarGargalos(List<Processo> processos, String saidaArquivo, int idModelo) {
        dao.carregarParametrosDoBanco(conexao.getConexao(),idModelo);

        for (Processo p : processos) {
            // Gargalo de CPU
            if (p.getCpu() != null && p.getCpu() > dao.cpuCritico) {
                Escrever cpu = new Escrever(p.getTimestamp(),p.getPid(),p.getNome(),"CPU",dao.cpuCritico,p.getCpu());
                cpu.escrever(saidaArquivo);
            }

            // Gargalo de RAM
            if (p.getRam() != null && p.getRam() > dao.ramCritico) {
                Escrever ram = new Escrever(p.getTimestamp(),p.getPid(),p.getNome(),"RAM",dao.ramCritico,p.getRam());
                ram.escrever(saidaArquivo);
            }

            // Gargalo de Disco
            Double bytesTotais = p.getBytesLidos() + p.getBytesEscritos();
            Double megabytesTotais = bytesTotais/ bytesParaMB;

            if (p.getBytesLidos() != null && megabytesTotais > dao.discoCritico) {
                Escrever disco = new Escrever(p.getTimestamp(),p.getPid(),p.getNome(),"DISCO",dao.discoCritico,megabytesTotais);
                disco.escrever(saidaArquivo);
            }
        }
    }


    //Recebe o csv e adiciona a classe processo
    public static List<Processo> verificarArquivos(String bucket, String key) throws IOException {
        List<Processo> processos = new ArrayList<>();

        //Mapeando os nomes de processos e armazenando em uma variável
        Map<String, String> mapeamentoProcessos = Mapeamento.criarMapeamento();

        try (var csv = s3.getObject(
                GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build())) {
            try (Scanner sc = new java.util.Scanner(csv)) {
                System.out.println("Lendo CSV do S3: " + key);
                while (sc.hasNextLine()) {
                    String linha = sc.nextLine().trim();

                    if (linha.isEmpty() || linha.toLowerCase().startsWith("timestamp")) continue;

                    //Separando a linha por campos separados por vírgula
                    String[] campos = linha.split(",");

                    //Renomeando processos
                    String nomeOriginal = campos[2];
                    String novoNome = mapeamentoProcessos.getOrDefault(nomeOriginal, "Generic_System_Process");
                    campos[2] = novoNome;
                    String nome = campos[2];

                    // Criando objeto Processo
                    Processo processo = new Processo(
                            campos[0],                         // timestamp
                            Integer.valueOf(campos[1]),        // pid
                            campos[2],                         // nome
                            Double.valueOf(campos[3]),         // cpu
                            Double.valueOf(campos[5]),         // ram
                            Double.valueOf(campos[6]),         // tempoVida
                            Double.valueOf(campos[7]),         // bytesLidos
                            Double.valueOf(campos[8])          // bytesEscritos
                    );

                    processos.add(processo);
                }
            } catch (Exception e) {
                System.out.println("Erro lendo arquivo do S3: " + e.getMessage());
                e.printStackTrace();
            }

        }
        return processos;
    }


    public static Double calcularDesvioPadrao(List<Double> valores) {
        if (valores == null || valores.isEmpty()) {
            return 0.0;
        }

        double media = valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double somaDiferencasQuadrado = 0.0;

        for (Double valor : valores) {
            somaDiferencasQuadrado += Math.pow(valor - media, 2);
        }

        if (valores.size() <= 1) {
            return 0.0;
        }

        double variancia = somaDiferencasQuadrado / valores.size();

        return Math.sqrt(variancia);
    }

}