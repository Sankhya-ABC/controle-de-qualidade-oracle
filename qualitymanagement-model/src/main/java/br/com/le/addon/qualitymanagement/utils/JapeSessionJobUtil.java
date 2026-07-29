package br.com.le.addon.qualitymanagement.utils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.core.JapeSession.TXBlock;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;

/**
 * Abre sessao JAPE e transacao para jobs/agendamentos (sem contexto de tela).
 */
public final class JapeSessionJobUtil {

    private JapeSessionJobUtil() {
    }

    public interface JobAction {
        void executar() throws Exception;
    }

    public static void executarComSessao(JobAction action) throws Exception {
        SessionHandle handle = null;
        try {
            handle = JapeSession.open();
            handle.setCanTimeout(false);
            configurarPropriedadesSessao();
            executarAcaoComTransacao(handle, action);
        } finally {
            JapeSession.close(handle);
        }
    }

    private static void executarAcaoComTransacao(SessionHandle handle, JobAction action) throws Exception {
        try {
            handle.execWithTX(new TXBlock() {
                @Override
                public void doWithTx() throws Exception {
                    action.executar();
                }
            });
        } catch (Exception e) {
            if (transacaoJaAtiva(e)) {
                action.executar();
                return;
            }
            throw e;
        }
    }

    private static boolean transacaoJaAtiva(Exception e) {
        String msg = e.getMessage();
        if (msg == null) {
            return false;
        }
        return msg.contains("transa") && msg.contains("andamento");
    }

    private static void configurarPropriedadesSessao() throws Exception {
        JapeSession.putProperty("usuario_logado", obterCodUsuarioLog());
    }

    private static BigDecimal obterCodUsuarioLog() throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT CODUSU FROM TGQCONFIG WHERE ROWNUM = 1 ");
            rset = sql.executeQuery();
            if (rset.next()) {
                BigDecimal codUsu = rset.getBigDecimal("CODUSU");
                if (codUsu != null) {
                    return codUsu;
                }
            }
        } catch (Exception ignored) {
            // fallback abaixo
        } finally {
            if (rset != null) {
                try {
                    rset.close();
                } catch (Exception ignored) {
                }
            }
            if (sql != null) {
                NativeSql.releaseResources(sql);
            }
            if (jdbc != null) {
                jdbc.closeSession();
            }
        }

        return BigDecimal.ZERO;
    }
}
