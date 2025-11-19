package com.sptech.school;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class ConexaoJira {

    private final String jiraUrl = System.getenv("JIRA_URL");
    private final String usuarioEmail = System.getenv("JIRA_USER");
    private final String apiToken = System.getenv("JIRA_TOKEN");
    private final String projetoKey = System.getenv("JIRA_PROJECT_KEY");

    public String criarIssue(String descricao) {
        try {
            String auth = usuarioEmail + ":" + apiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
            LocalDate dataAtual = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
            DateTimeFormatter formatadorBrasil = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            String dataFormatada = dataAtual.format(formatadorBrasil);

            String payload = "{"
                    + "\"fields\": {"
                    + "\"project\": {\"key\": \"" + projetoKey + "\"},"
                    + "\"summary\": \"Relatório de Alertas do dia " + dataFormatada + "\","
                    + "\"description\": {"
                    + "  \"type\": \"doc\","
                    + "  \"version\": 1,"
                    + "  \"content\": [{"
                    + "    \"type\": \"paragraph\","
                    + "    \"content\": [{"
                    + "      \"type\": \"text\","
                    + "      \"text\": " + escapeJsonStringForTextNode(descricao)
                    + "    }]"
                    + "  }]"
                    + "},"
                    + "\"issuetype\": {\"name\": \"Task\"}"
                    + "}"
                    + "}";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jiraUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + encodedAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            return "Status: " + resp.statusCode() + " - " + resp.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao criar issue: " + e.getMessage();
        }
    }

    private static String escapeJsonStringForTextNode(String s) {
        if (s == null) return "\"\"";

        StringBuilder sb = new StringBuilder();
        sb.append('"');

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c <= 0x1F || c > 0x7E) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
