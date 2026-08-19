package br.com.le.addon.qualitymanagement.services;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

/**
 * Avalia diariamente a DATAPRAZO de cada etapa da RNC (Causa Raiz, Acoes
 * Imediatas, Abrangencia, Acoes Corretivas, Implementacao) e sincroniza
 * STATUS: Atrasada quando o prazo venceu, Em Andamento quando nao venceu.
 * Etapas com STATUS = Concluida (C) nunca sao alteradas por esta rotina.
 * Em Causa Raiz, Acoes Corretivas e Implementacao, DATAPRAZO e calculado
 * automaticamente pelo trigger de banco a partir de TGQRNC.DATAREGISTRO (7,
 * 15 e 30 dias respectivamente), caso nao seja preenchido manualmente.
 */
public final class AtualizaStatusEtapasRncService {

    private static final String[] TABELAS = {
        "TGQCAUSARAIZ",
        "TGQACOESIMEDIATAS",
        "TGQABRANGENCIA",
        "TGQACOESCORRETIVAS",
        "TGQIMPLEMENTACAO"
    };

    private AtualizaStatusEtapasRncService() {
    }

    public static void atualizarStatusEtapas() throws Exception {
        JdbcWrapper jdbc = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            for (String tabela : TABELAS) {
                atualizarTabela(jdbc, tabela);
            }
        } finally {
            if (jdbc != null) {
                jdbc.closeSession();
            }
        }
    }

    private static void atualizarTabela(JdbcWrapper jdbc, String tabela) throws Exception {
        NativeSql sql = new NativeSql(jdbc);
        try {
            sql.appendSql(" UPDATE " + tabela + " ");
            sql.appendSql(" SET STATUS = CASE ");
            sql.appendSql("     WHEN TRUNC(DATAPRAZO) < TRUNC(SYSDATE) THEN 'A' ");
            sql.appendSql("     ELSE 'E' ");
            sql.appendSql(" END ");
            sql.appendSql(" WHERE DATAPRAZO IS NOT NULL ");
            sql.appendSql(" AND (STATUS IS NULL OR STATUS <> 'C') ");
            sql.executeUpdate();
            System.out.println("[AtualizaStatusEtapasRnc] " + tabela + " sincronizado.");
        } finally {
            NativeSql.releaseResources(sql);
        }
    }
}
