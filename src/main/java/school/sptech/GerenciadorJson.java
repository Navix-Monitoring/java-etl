package school.sptech;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class GerenciadorJson {

    private final ObjectMapper mapper = new ObjectMapper();

    public String criarJsonDoDia(String diaSemana, double cpu, double ram, double disco, double procs, double temp) {
        try {
            RegistroDia registro = new RegistroDia(diaSemana, cpu, ram, disco, procs, temp);
            return mapper.writeValueAsString(registro);
        } catch (Exception e) {
            e.printStackTrace();
            return "{}";
        }
    }

    public String adicionarLoteAoGeral(String jsonGeral, int idLote, String novoDiaJson) {
        try {
            List<Lote> listaLotes;

            // converter o json para uma Lista de obj Java
            if (jsonGeral == null || jsonGeral.trim().isEmpty() || jsonGeral.equals("[]")) {
                listaLotes = new ArrayList<>();
            } else {
                listaLotes = mapper.readValue(jsonGeral, new TypeReference<List<Lote>>() {});
            }

            // converte o JSON do dia novo para Objeto
            RegistroDia novoDiaObj = mapper.readValue(novoDiaJson, RegistroDia.class);

            Lote loteEncontrado = null;

            for (Lote lote : listaLotes) {
                if (lote.getIdLote() == idLote) {
                    loteEncontrado = lote;
                    break;
                }
            }

            if (loteEncontrado != null) {
                boolean diaAtualizado = false;

                for (int i = 0; i < loteEncontrado.getDias().size(); i++) {
                    RegistroDia diaExistente = loteEncontrado.getDias().get(i);

                    if (diaExistente.getDiaSemana().equals(novoDiaObj.getDiaSemana())) {
                        loteEncontrado.getDias().set(i, novoDiaObj);
                        diaAtualizado = true;
                        break;
                    }
                }

                if (!diaAtualizado) {
                    loteEncontrado.getDias().add(novoDiaObj);
                }

            } else {
                Lote novoLote = new Lote();
                novoLote.setIdLote(idLote);
                novoLote.addDia(novoDiaObj);
                listaLotes.add(novoLote);
            }

            // convertendo tudo de volta para String JSON
            return mapper.writeValueAsString(listaLotes);

        } catch (Exception e) {
            e.printStackTrace();
            return jsonGeral;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Lote {
        @JsonProperty("lote")
        private int idLote;

        @JsonProperty("dias")
        private List<RegistroDia> dias = new ArrayList<>();

        public void addDia(RegistroDia dia) {
            this.dias.add(dia);
        }

        public int getIdLote() { return idLote; }
        public void setIdLote(int idLote) { this.idLote = idLote; }
        public List<RegistroDia> getDias() { return dias; }
        public void setDias(List<RegistroDia> dias) { this.dias = dias; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RegistroDia {
        @JsonProperty("diaSemana")
        private String diaSemana;
        @JsonProperty("cpu")
        private double cpu;
        @JsonProperty("ram")
        private double ram;
        @JsonProperty("disco")
        private double disco;
        @JsonProperty("processos")
        private double processos;
        @JsonProperty("temperatura")
        private double temperatura;

        public RegistroDia() {}

        public RegistroDia(String diaSemana, double cpu, double ram, double disco, double processos, double temperatura) {
            this.diaSemana = diaSemana;
            this.cpu = cpu;
            this.ram = ram;
            this.disco = disco;
            this.processos = processos;
            this.temperatura = temperatura;
        }

        public String getDiaSemana() { return diaSemana; }
        public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
        public double getCpu() { return cpu; }
        public void setCpu(double cpu) { this.cpu = cpu; }
        public double getRam() { return ram; }
        public void setRam(double ram) { this.ram = ram; }
        public double getDisco() { return disco; }
        public void setDisco(double disco) { this.disco = disco; }
        public double getProcessos() { return processos; }
        public void setProcessos(double processos) { this.processos = processos; }
        public double getTemperatura() { return temperatura; }
        public void setTemperatura(double temperatura) { this.temperatura = temperatura; }
    }
}