package school.sptech;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GerenciadorJson {

    public String criarJsonDoDia(String diaSemana, double cpu, double ram, double disco, double procs, double temp) {
        return String.format(
                "{\"diaSemana\": \"%s\", \"cpu\": %.1f, \"ram\": %.1f, \"disco\": %.1f, \"processos\": %.1f, \"temperatura\": %.1f}",
                diaSemana, cpu, ram, disco, procs, temp
        );
    }

    public String adicionarLoteAoGeral(String jsonGeral, int idLote, String novoDiaJson) {
        if (jsonGeral == null || jsonGeral.trim().isEmpty()) {
            jsonGeral = "[]";
        }

        String diaSemana = extrairDiaDoJson(novoDiaJson);

        String buscaLote = "\"lote\": " + idLote;
        int posicaoLote = jsonGeral.indexOf(buscaLote);

        if (posicaoLote != -1) {
            // Acha onde começa a lista de dias do lote
            int inicioListaDias = jsonGeral.indexOf("\"dias\": [", posicaoLote);

            // Acha onde termina a lista de dias
            int fimListaDias = encontrarFechamentoColchete(jsonGeral, inicioListaDias + 8);

            String conteudoDias = jsonGeral.substring(inicioListaDias, fimListaDias + 1);

            if (conteudoDias.contains("\"diaSemana\": \"" + diaSemana + "\"")) {
                String regex = "\\{\\s*\"diaSemana\":\\s*\"" + diaSemana + "\"[^}]*\\}";

                String conteudoDiasAtualizado = conteudoDias.replaceAll(regex, novoDiaJson);

                return jsonGeral.substring(0, inicioListaDias) +
                        conteudoDiasAtualizado +
                        jsonGeral.substring(fimListaDias + 1);

            } else {
                StringBuilder sb = new StringBuilder(jsonGeral);
                int ondeInserir = inicioListaDias + "\"dias\": [".length();

                char proximoCaractere = sb.charAt(ondeInserir);

                if (proximoCaractere == ']') {
                    sb.insert(ondeInserir, novoDiaJson);
                } else {
                    sb.insert(ondeInserir, novoDiaJson + ",");
                }
                return sb.toString();
            }

        }
        else {
            String novoLoteCompleto = String.format("{\"lote\": %d, \"dias\": [%s]}", idLote, novoDiaJson);

            if (jsonGeral.trim().equals("[]")) {
                return "[" + novoLoteCompleto + "]";
            } else {
                StringBuilder sb = new StringBuilder(jsonGeral);
                int ultimoColchete = sb.lastIndexOf("]");
                sb.insert(ultimoColchete, "," + novoLoteCompleto);
                return sb.toString();
            }
        }
    }


    private String extrairDiaDoJson(String json) {
        // Usa Regex para pegar o valor de "diaSemana"
        Pattern pattern = Pattern.compile("\"diaSemana\":\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private int encontrarFechamentoColchete(String texto, int inicio) {
        int contador = 1;
        for (int i = inicio + 1; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == '[') contador++;
            if (c == ']') contador--;

            if (contador == 0) return i;
        }
        return -1; 
    }
}