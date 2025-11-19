package com.sptech.school;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.List;

public class LambdaHandler implements RequestHandler<Object, String> {

    @Override
    public String handleRequest(Object input, Context context) {
        try {
            LeituraCSV leitura = new LeituraCSV();

            List<LoteResumo> resumos = leitura.processar();

            String descricao = LeituraCSV.gerarDescricaoJira(resumos);

            ConexaoJira jira = new ConexaoJira();
            String resposta = jira.criarIssue(descricao);

            return "Processamento concluído. Jira respondeu: " + resposta;

        } catch (Exception e) {
            return "Erro no processamento: " + e.getMessage();
        }
    }
}