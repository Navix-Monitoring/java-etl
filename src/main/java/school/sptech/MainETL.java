package school.sptech;

import java.io.*;
import java.sql.*;
import java.util.Random;
import java.util.Scanner;

import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.regions.Region;

public class MainETL {

    public static class Conexao {
        private Connection conexao;

        public Conexao() {
            try {
                Ec2Client ec2 = Ec2Client.builder()
                        .region(Region.US_EAST_1)
                        .build();

                DescribeInstancesRequest request = DescribeInstancesRequest.builder()
                        .filters(Filter.builder()
                                .name("tag:Name")
                                .values("servidorNavix")
                                .build())
                        .build();

                DescribeInstancesResponse response = ec2.describeInstances(request);

                String ipPublico = null;
                for (var reservation : response.reservations()) {
                    for (Instance instance : reservation.instances()) {
                        ipPublico = instance.publicIpAddress();
                    }
                }

                if (ipPublico == null) throw new RuntimeException("Nenhum IP encontrado para 'servidorNavix'");

                String url = "jdbc:mysql://" + ipPublico + ":3306/navix";
                String user = "navix";
                String pass = "SPTech@2025";

                conexao = DriverManager.getConnection(url, user, pass);
                System.out.println("Conexão estabelecida com " + ipPublico);

            } catch (Exception e) {
                System.out.println("Erro conexão: " + e.getMessage());
                e.printStackTrace();
            }
        }

        public void fecharConexao() {
            try {
                if (conexao != null && !conexao.isClosed()) conexao.close();
            } catch (SQLException e) {
                System.out.println("Erro fechar: " + e.getMessage());
            }
        }

        public Connection getConexao() {
            return conexao;
        }
    }

    public static class ParametroDAO {
        public static double cpuMin, cpuNeutro, cpuAtencao, cpuCritico;
        public static double ramMin, ramNeutro, ramAtencao, ramCritico;
        public static double discoMin, discoNeutro, discoAtencao, discoCritico;
        public static double tempCpuMin, tempCpuNeutro, tempCpuAtencao, tempCpuCritico;

        public void carregarParametrosDoBanco(Connection conn, int fkModelo) {
            String sql = "SELECT h.tipo, ph.parametroMinimo, ph.parametroNeutro, ph.parametroAtencao, ph.parametroCritico, ph.unidadeMedida " +
                    "FROM parametroHardware ph JOIN hardware h ON ph.fkHardware = h.id WHERE ph.fkModelo = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, fkModelo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String tipo = rs.getString("tipo").toUpperCase();
                        double min = rs.getDouble("parametroMinimo");
                        double neutro = rs.getDouble("parametroNeutro");
                        double atencao = rs.getDouble("parametroAtencao");
                        double critico = rs.getDouble("parametroCritico");
                        String unidade = rs.getString("unidadeMedida").toUpperCase();

                        switch (tipo) {
                            case "CPU" -> {
                                if (unidade.equals("USO")) {
                                    cpuMin = min;
                                    cpuNeutro = neutro;
                                    cpuAtencao = atencao;
                                    cpuCritico = critico;
                                } else {
                                    tempCpuMin = min;
                                    tempCpuNeutro = neutro;
                                    tempCpuAtencao = atencao;
                                    tempCpuCritico = critico;
                                }
                            }
                            case "RAM" -> {
                                ramMin = min;
                                ramNeutro = neutro;
                                ramAtencao = atencao;
                                ramCritico = critico;
                            }
                            case "DISCO" -> {
                                discoMin = min;
                                discoNeutro = neutro;
                                discoAtencao = atencao;
                                discoCritico = critico;
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                System.out.println("Erro parametros: " + e.getMessage());
            }
        }
    }

    public static class LeituraCSV {
        private final ParametroDAO dao;
        private final Conexao conexao;

        public LeituraCSV() {
            dao = new ParametroDAO();
            conexao = new Conexao();
            dao.carregarParametrosDoBanco(conexao.getConexao(), 1);
        }

        public void processar(String caminhoEntrada, String caminhoSaida) {
            try (Scanner sc = new Scanner(new File(caminhoEntrada));
                 BufferedWriter bw = new BufferedWriter(new FileWriter(caminhoSaida))) {

                bw.write("TIMESTAMP,MAC,CPU,RAM,DISCO,PROC,BATERIA,TEMP,TEMPBATERIA,statusCPU,statusRAM,statusDISCO,statusTEMP\n");

                while (sc.hasNextLine()) {
                    String linha = sc.nextLine().trim();
                    if (linha.isEmpty() || linha.toUpperCase().startsWith("TIMESTAMP")) continue;

                    String[] campos = linha.split(",");
                    if (campos.length < 9) continue;

                    // ===== BATERIA — campo 6 =====
                    if (campos[6].trim().equalsIgnoreCase("N/A") || campos[6].trim().isEmpty()) {
                        campos[6] = "100";
                    }

                    // ===== TEMP_CPU — campo 7 =====
                    if (campos[7].trim().equalsIgnoreCase("N/A") || campos[7].trim().isEmpty()) {

                        double cpu = Double.parseDouble(campos[2].trim()); // CPU está em campos[2]

                        double TEMP_BASE_IDLE = 45.0;
                        double FATOR_LINEAR = 0.35;
                        double FATOR_QUADRATICO = 0.002;
                        double DESVIO_RUIDO = 1.5;

                        Random rand = new Random();
                        double ruido = rand.nextGaussian() * DESVIO_RUIDO;

                        double tempCpu = TEMP_BASE_IDLE
                                + (cpu * FATOR_LINEAR)
                                + (cpu * cpu * FATOR_QUADRATICO)
                                + ruido;

                        tempCpu = Math.round(tempCpu * 100.0) / 100.0;

                        campos[7] = String.valueOf(tempCpu);
                    }

                    // ===== TEMP_BATERIA — campo 8 =====
                    if (campos[8].trim().equalsIgnoreCase("N/A") || campos[8].trim().isEmpty()) {
                        campos[8] = "30"; // usa valor padrão leve
                    }

                    // ===== Parse dos valores =====
                    double cpu = Double.parseDouble(campos[2].trim());
                    double ram = Double.parseDouble(campos[3].trim());
                    double disco = Double.parseDouble(campos[4].trim());
                    double temp = Double.parseDouble(campos[7].trim()); // temp da CPU

                    // ===== Status faixas =====
                    String statusCPU = cpu <= dao.cpuMin ? "BAIXO" :
                            cpu <= dao.cpuNeutro ? "NEUTRO" :
                                    cpu <= dao.cpuAtencao ? "ATENÇÃO" : "CRÍTICO";

                    String statusRAM = ram <= dao.ramMin ? "BAIXO" :
                            ram <= dao.ramNeutro ? "NEUTRO" :
                                    ram <= dao.ramAtencao ? "ATENÇÃO" : "CRÍTICO";

                    String statusDISCO = disco <= dao.discoMin ? "BAIXO" :
                            disco <= dao.discoNeutro ? "NEUTRO" :
                                    disco <= dao.discoAtencao ? "ATENÇÃO" : "CRÍTICO";

                    String statusTEMP = temp <= dao.tempCpuMin ? "BAIXO" :
                            temp <= dao.tempCpuNeutro ? "NEUTRO" :
                                    temp <= dao.tempCpuAtencao ? "ATENÇÃO" : "CRÍTICO";

                    // ===== Escrita correta =====
                    bw.write(String.join(",",
                            campos[0], // timestamp
                            campos[1], // mac
                            campos[2], // cpu
                            campos[3], // ram
                            campos[4], // disco
                            campos[5], // processos
                            campos[6], // bateria
                            campos[7], // temp_cpu
                            campos[8], // temp_bateria
                            statusCPU, statusRAM, statusDISCO, statusTEMP
                    ));
                    bw.newLine();
                }

            } catch (Exception e) {
                System.out.println("Erro processar CSV: " + e.getMessage());
            } finally {
                conexao.fecharConexao();
            }
        }
    }
}
