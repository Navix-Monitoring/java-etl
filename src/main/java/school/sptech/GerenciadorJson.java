package sptech.school;

// utiliza StringBuilder para inserir novos dias ou novos lotes no arquivo

public class GerenciadorJson {

    public String criarJsonDoDia(String diaSemana, double cpu, double ram, double disco, double procs, double temp) {
        return String.format(
                "{\"diaSemana\": \"%s\", \"cpu\": %.1f, \"ram\": %.1f, \"disco\": %.1f, \"processos\": %.1f, \"temperatura\": %.1f}",
                diaSemana, cpu, ram, disco, procs, temp
        ).replace(",", ".");
    }

    public String adicionarLoteAoGeral(String jsonGeral, int idLote, String novoDiaJson) {
        if (jsonGeral == null || jsonGeral.trim().isEmpty()) {
            jsonGeral = "[]";
        }

        StringBuilder sb = new StringBuilder(jsonGeral.trim());

        String buscaLote = "\"lote\": " + idLote;

        int posicaoLote = sb.indexOf(buscaLote);

        if (posicaoLote != -1) {
            int posicaoLista = sb.indexOf("\"dias\": [", posicaoLote);

            int ondeInserir = posicaoLista + "\"dias\": [".length();
            char proximoCaractere = sb.charAt(ondeInserir);

            if (proximoCaractere == ']') {
                sb.insert(ondeInserir, novoDiaJson);
            } else {
                sb.insert(ondeInserir, novoDiaJson + ",");
            }

        }
        else {
            String novoLoteCompleto = String.format("{\"lote\": %d, \"dias\": [%s]}", idLote, novoDiaJson);

            if (sb.toString().equals("[]")) {
                return "[" + novoLoteCompleto + "]";
            } else {
                int ultimoColchete = sb.lastIndexOf("]");

                sb.insert(ultimoColchete, "," + novoLoteCompleto);
            }
        }

        return sb.toString();
    }
}