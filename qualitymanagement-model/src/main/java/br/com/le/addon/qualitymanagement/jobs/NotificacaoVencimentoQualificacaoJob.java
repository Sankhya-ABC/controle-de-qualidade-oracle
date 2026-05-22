package br.com.le.addon.qualitymanagement.jobs;

import br.com.le.addon.qualitymanagement.services.NotificacaoVencimentoQualificacaoService;
import br.com.sankhya.studio.annotations.Job;
import br.com.sankhya.studio.annotations.enums.EJBTransactionType;
import br.com.sankhya.studio.stereotypes.IJob;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Job agendado: consulta VW_VENC_QUALIF_FORN e enfileira e-mails quando ENVIAR_NOTIFICACAO = 'S'.
 * Frequencia: a cada 10 segundos (&10000 ms).
 */
@Job(
    serviceName = "NotificacaoVencimentoQualifJobSP",
    frequency = "&10000",
    transactionType = EJBTransactionType.Supports
)
public class NotificacaoVencimentoQualificacaoJob extends IJob {

    private static final Logger LOG = Logger.getLogger(NotificacaoVencimentoQualificacaoJob.class.getName());

    @Override
    public void onSchedule() {
        try {
            NotificacaoVencimentoQualificacaoService.processarNotificacoesPendentes();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Erro ao processar notificacoes de vencimento de qualificacao.", e);
        }
    }
}
