package br.com.le.addon.qualitymanagement.services;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import java.math.BigDecimal;
import java.sql.ResultSet;

public class QuestionarioGestaoMudanca {
    public static void criaQuestionariosGestao(BigDecimal fase, String idGestao) throws Exception {
        System.out.println("criaQuestionariosGestao");
        BigDecimal origem = new BigDecimal(0);
        BigDecimal idQuest = new BigDecimal(0);
        BigDecimal idAvaliacao = new BigDecimal(0);
        JdbcWrapper jdbc = null;
        EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
        jdbc = dwf.getJdbcWrapper();
        jdbc.openSession();
        try {
            if (fase.compareTo(new BigDecimal(2)) == 0 ||
                fase.compareTo(new BigDecimal(4)) == 0 ||
                fase.compareTo(new BigDecimal(5)) == 0) {
                NativeSql sqlQuestionario = new NativeSql(jdbc);
                sqlQuestionario.appendSql(" SELECT IDQUEST, ORIGEM ");
                sqlQuestionario.appendSql(" FROM TGQQUESTQUALIF Q ");
                sqlQuestionario.appendSql(" WHERE ISNULL(INATIVO,'N') = 'N' ");
                sqlQuestionario.appendSql(" AND ORIGEM <> 1 ");
                sqlQuestionario
                    .appendSql(" AND NOT EXISTS (SELECT * FROM TGQQUESTAVALIACAO A WHERE A.IDQUEST =  Q.IDQUEST AND IDGESTAO =" +
                        idGestao + ")");
                sqlQuestionario.appendSql(" ORDER BY ORIGEM ");
                ResultSet rsetQuestionario = sqlQuestionario.executeQuery();
                while (rsetQuestionario.next()) {
                    origem = rsetQuestionario.getBigDecimal("ORIGEM");
                    idQuest = rsetQuestionario.getBigDecimal("IDQUEST");
                    System.out.println("insert questionario");
                    StringBuilder query = new StringBuilder();
                    query.append(" INSERT INTO TGQQUESTAVALIACAO (IDAVALIACAO, IDGESTAO, IDQUEST, ORIGEM ) ");
                    query.append(" VALUES ( NEXT VALUE FOR SEQ_TGQQUESTAVALIACAO, " +
                        Integer.valueOf(idGestao) + ", " + idQuest + ", " +
                        origem + " )");
                    NativeSql sql = new NativeSql(jdbc);
                    sql.appendSql(query.toString());
                    sql.executeUpdate();
                    NativeSql sqlAvaliacao = new NativeSql(jdbc);
                    sqlAvaliacao.appendSql(" SELECT IDAVALIACAO ");
                    sqlAvaliacao.appendSql(" FROM TGQQUESTAVALIACAO ");
                    sqlAvaliacao.appendSql(" WHERE IDQUEST = " + idQuest);
                    sqlAvaliacao.appendSql(" AND IDGESTAO = " +
                        Integer.valueOf(idGestao));
                    sqlAvaliacao.appendSql(" AND ORIGEM = " + origem);
                    ResultSet rsetAvaliacao = sqlAvaliacao.executeQuery();
                    rsetAvaliacao.next();
                    idAvaliacao = rsetAvaliacao.getBigDecimal("IDAVALIACAO");
                    System.out.println("idAvaliacao: " + idAvaliacao);
                    System.out.println("idQuest: " + idQuest);
                    NativeSql sqlPerguntas = new NativeSql(jdbc);
                    sqlPerguntas.appendSql(" SELECT IDPERG ");
                    sqlPerguntas.appendSql(" FROM TGQPERGQUEST ");
                    sqlPerguntas.appendSql(" WHERE IDQUEST = " + idQuest);
                    ResultSet rsetPerguntas = sqlPerguntas.executeQuery();
                    while (rsetPerguntas.next()) {
                        System.out.println("Cria perguntas " + rsetPerguntas.getBigDecimal("IDPERG"));
                        BigDecimal idPergunta = rsetPerguntas.getBigDecimal("IDPERG");
                        StringBuilder queryPerguntas = new StringBuilder();
                        queryPerguntas
                            .append(" INSERT INTO TGQPERGUNTASAVALIACAO (IDAVALIACAO, IDPERGUNTA ) ");
                        queryPerguntas.append(" VALUES ( " + idAvaliacao + ", " +
                            idPergunta + " )");
                        System.out.println(queryPerguntas.toString());
                        NativeSql sqlPerguta = new NativeSql(jdbc);
                        sqlPerguta.appendSql(queryPerguntas.toString());
                        sqlPerguta.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
