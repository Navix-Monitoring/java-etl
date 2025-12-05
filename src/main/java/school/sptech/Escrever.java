package school.sptech;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Escrever {
    private String nome, timestamp;
    private String StatusCPU, StatusRAM, StatusDISCO, gargalo;
    private Double cpu,ram,disco, valor, limite;
    private Integer pid;
    private Double bytesLidos,bytesEscritos,tempoVida,desvioPadraoRam;


    private static final DecimalFormat df;
    private static final double bytesParaMB = 1024.0 * 1024.0;

    static {
        // Garante que o separador decimal seja sempre '.' para o CSV funcionar corretamente
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        df = new DecimalFormat("#.##", symbols);
    }
    public Escrever(String nome, Double cpu, Double ram,Double desvioPadraoRam, Double bytesLidos, Double bytesEscritos, Double tempoVida, String StatusCPU, String StatusRAM,String StatusDISCO) {
        this.nome = nome;
        this.cpu = cpu;
        this.ram = ram;
        this.desvioPadraoRam = desvioPadraoRam;
        this.tempoVida = tempoVida;
        this.StatusCPU = StatusCPU;
        this.StatusRAM = StatusRAM;
        this.StatusDISCO = StatusDISCO;
        if(bytesLidos!= null && bytesEscritos != null){
            Double bytesTotais = bytesLidos + bytesEscritos;
            Double megabytesTotais = bytesTotais/ bytesParaMB;
            this.disco = megabytesTotais;
        }
    }

    public Escrever( String timestamp, Integer pid,String nome, Double cpu, Double ram, Double tempoVida, Double bytesLidos, Double bytesEscritos) {
        this.pid = pid;
        this.ram = ram;
        this.cpu = cpu;
        this.timestamp = timestamp;
        this.nome = nome;
        this.tempoVida = tempoVida;
        if (bytesLidos != null && bytesEscritos != null) {
            Double bytesTotais = bytesLidos + bytesEscritos;
            Double megabytesTotais = bytesTotais/ bytesParaMB;
            this.disco = megabytesTotais;
        }
    }

    public Escrever(String timestamp, Integer pid, String nome, String gargalo, Double limite, Double valor) {
        this.nome = nome;
        this.timestamp = timestamp;
        this.gargalo = gargalo;
        this.limite = limite;
        this.valor = valor;
        this.pid = pid;
    }

    private String formatarTempoDeVida() {
        Double segundosTotais = this.tempoVida;
        if (segundosTotais == null || segundosTotais <= 0) {
            return "0s";
        }

        long segundos = Math.round(segundosTotais);
        long horas = segundos / 3600;
        long minutos = (segundos % 3600) / 60;
        long secs = segundos % 60;

        StringBuilder sb = new StringBuilder();
        if (horas > 0) {
            sb.append(horas).append("h ");
        }
        if (minutos > 0) {
            sb.append(minutos).append("min ");
        }
        sb.append(secs).append("s");

        return sb.toString().trim();
    }

    private List<String> gerarColunas() {
        List<String> colunas = new ArrayList<>();
        if (timestamp != null) colunas.add("Timestamp");
        if (pid != null) colunas.add("Pid");
        if (nome != null) colunas.add("Nome");
        if (gargalo != null) colunas.add("Gargalo");
        if (valor != null) colunas.add("Valor");
        if (limite != null) colunas.add("Limite");
        if (cpu != null) colunas.add("Cpu");
        if (ram != null) colunas.add("Ram");
        if (desvioPadraoRam != null) colunas.add("DesvioPadraoRam");
        if (disco != null) colunas.add("Disco(MB)");
        if (bytesLidos != null) colunas.add("BytesLidos");
        if (bytesEscritos != null) colunas.add("BytesEscritos");
        if (tempoVida != null) colunas.add("TempoVida");
        if (StatusCPU != null) colunas.add("StatusCPU");
        if (StatusRAM != null) colunas.add("StatusRAM");
        if (StatusDISCO != null) colunas.add("StatusDISCO");
        colunas.add("TempoVidaFormatado");
        return colunas;
    }

    private List<String> gerarValores() {
        List<String> valoresParaEscrever = new ArrayList<>();
        if (timestamp != null) valoresParaEscrever.add(timestamp);
        if (pid != null) valoresParaEscrever.add(String.valueOf(pid));
        if (nome != null) valoresParaEscrever.add(nome);
        if (gargalo != null) valoresParaEscrever.add(gargalo);
        if (valor != null) valoresParaEscrever.add(valor);
        if (limite != null) valoresParaEscrever.add(limite);
        if (cpu != null) valoresParaEscrever.add(String.valueOf(cpu));
        if (ram != null) valoresParaEscrever.add(String.valueOf(ram));
        if (desvioPadraoRam != null) valoresParaEscrever.add(String.valueOf(desvioPadraoRam));
        if (disco != null) valoresParaEscrever.add(df.format(disco));
        if (bytesLidos != null) valoresParaEscrever.add(String.valueOf(bytesLidos));
        if (bytesEscritos != null) valoresParaEscrever.add(String.valueOf(bytesEscritos));
        if (tempoVida != null) valoresParaEscrever.add(String.valueOf(tempoVida));
        if (StatusCPU != null) valoresParaEscrever.add(String.valueOf(StatusCPU));
        if (StatusRAM != null) valoresParaEscrever.add(String.valueOf(StatusRAM));
        if (StatusDISCO != null) valoresParaEscrever.add(String.valueOf(StatusDISCO));

        valoresParaEscrever.add(formatarTempoDeVida());
        return valoresParaEscrever;
    }

    public void escrever(String caminhoArquivo){

        // 1. Verificar se o arquivo existe. Se não existir, escrever o cabeçalho primeiro.
        boolean arquivoExiste = Files.exists(Paths.get("/tmp/"+caminhoArquivo));

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("/tmp/"+caminhoArquivo, true))) {

            if (!arquivoExiste) {
                System.out.println("Gerando o header do arquivo: "+ caminhoArquivo);
                List<String> colunas = gerarColunas();
                bw.write(String.join(",", colunas));
                bw.newLine();
                System.out.println("Header gerado!");
            }

            List<String> valores = gerarValores();
            bw.write(String.join(",", valores));
            bw.newLine();


        }catch(IOException e){
            System.out.println("Erro ao escrever o arquivo "+caminhoArquivo+": " + e.getMessage());
        }
    }
}
