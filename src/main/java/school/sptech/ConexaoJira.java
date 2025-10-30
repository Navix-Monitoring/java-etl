package school.sptech;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

public class ConexaoJira {
    public static String criarIssue() {
        try {
            // Váriaveis para usar com a API do JIRA
            String jiraUrl = "URLJIRA";
            String usuarioEmail = "EMAILADMJIRA";
            String apiToken = "TOKENJIRA";
            String projetoKey = "CHAVEDOPROJETO";

            // Autenticação Basic
            String auth = usuarioEmail + ":" + apiToken;
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            // JSON para criar a issue
            String json = """
                {
                  "fields": {
                    "project": { "key": "%s" },
                    "summary": "Alerta da ETL: Falha na execução",
                    "description": {
                      "type": "doc",
                      "version": 1,
                      "content": [
                        {
                          "type": "paragraph",
                          "content": [
                            {
                              "type": "text",
                              "text": "A execução da ETL falhou ao processar o lote de dados das TESTE:00."
                            }
                          ]
                        }
                      ]
                    },
                    "issuetype" : {"name" : "Task" } 
                  }
                }
                """.formatted(projetoKey);

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
