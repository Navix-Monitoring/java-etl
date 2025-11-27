package school.sptech;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Main {

    public static class LeituraCSV {
        private final ParametroDAO dao;
        private final Conexao conexao;

        public LeituraCSV(int fkModelo) {
            dao = new ParametroDAO();
            conexao = new Conexao();
            dao.carregarParametrosDoBanco(conexao.getConexao(), fkModelo);
        }
        public void processar(String caminhoEntrada, String caminhoSaida) {
            try (Scanner sc = new Scanner(new File(caminhoEntrada));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoSaida))) {

                bw.write("Timestamp,Pid,Nome,Cpu,Ram,TempoVida,BytesLidos,BytesEscritos\n");

                while(sc.hasNextLine()) {

                    String linha = sc.nextLine().trim();
                    if (linha.isEmpty() || linha.toUpperCase().startsWith("TIMESTAMP")) continue;
                    String[] campos = linha.split(",");
                    if (campos.length < 9) continue;

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                    LocalDateTime t;
                    try {
                        t = LocalDateTime.parse(campos[0], formatter);
                    } catch (Exception e) {
                        continue;
                    }
                    int pid = Integer.parseInt(campos[1]);
                    String nome = campos[2];
                    double cpu = Double.parseDouble(campos[3]);
                    double totalRam = Double.parseDouble(campos[4]);
                    double ram = Double.parseDouble(campos[5]);
                    double tempoVida = Double.parseDouble(campos[6]);
                    long bytesLidos = Long.parseLong(campos[7]);
                    long bytesEscritos = Long.parseLong(campos[8]);

                    bw.write(String.join(",",
                            t.toString(),
                            String.valueOf(pid),
                            nome,
                            String.valueOf(cpu),
                            String.valueOf(totalRam),
                            String.valueOf(ram),
                            String.valueOf(tempoVida),
                            String.valueOf(bytesLidos),
                            String.valueOf(bytesEscritos)
                    ));
                    bw.newLine();
                }
                }catch(Exception e){
                System.out.println("Erro ao processar CSV: "+ e.getMessage());
            }finally {
                conexao.fecharConexao();
            }
        }

    }

}
