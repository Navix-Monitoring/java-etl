package school.sptech;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class ConexaoJira {
    public static String criarIssue(int qtdAlertaRam,int qtdAlertaCpu,int qtdAlertaDisco, int totalAlertas, int qtdAlertaTemp) {
        try {
            // Váriaveis para usar com a API do JIRA
            String jiraUrl = "URLdoSiteJira";
            String usuarioEmail = "EmailAdminJira";
            String apiToken = "TokenDoJira";
            String projetoKey = "ChaveDoProjeto3Digitos";


            // Autenticação Basic
            String auth = usuarioEmail + ":" + apiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            // JSON para criar a issue
            String json = "{"
                    + "\"fields\": {"
                    + "\"project\": {\"key\": \"" + projetoKey + "\"},"
                    + "\"summary\": \"Relatório de Alertas \","
                    + "\"description\": {"
                    + "    \"type\": \"doc\","
                    + "    \"version\": 1,"
                    + "    \"content\": [{"
                    + "        \"type\": \"paragraph\","
                    + "        \"content\": [{"
                    + "            \"type\": \"text\","
                    + "            \"text\": \"Alertas da ETL feitas em um lote:\\nRAM Crítica " + qtdAlertaRam
                    + "\\nCPU Crítica: " + qtdAlertaCpu
                    + "\\nDisco Crítica : " + qtdAlertaDisco
                    + "\\nTemperatura Crítica : " +qtdAlertaTemp
                    + "\\nTotal de Críticos: " + totalAlertas + "\""
                    + "        }]"
                    + "    }]"
                    + "},"
                    + "\"issuetype\": {\"name\": \"Task\"}"
                    + "}"
                    + "}";


            // Criar conexão HTTP
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(jiraUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Basic " + encodedAuth)
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            // Enviar requisição
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Retornar a resposta
            return "Status: " + response.statusCode() + "\nResposta: " + response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "Erro ao criar issue: " + e.getMessage();
        }
    }


}
