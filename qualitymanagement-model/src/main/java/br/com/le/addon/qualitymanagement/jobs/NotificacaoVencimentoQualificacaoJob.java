package br.com.le.addon.qualitymanagement.jobs;

import br.com.le.addon.qualitymanagement.services.NotificacaoVencimentoQualificacaoService;
import static br.com.le.addon.qualitymanagement.services.NotificacaoVencimentoQualificacaoService.VW_VENC_QUALIF_FORN;
import br.com.le.addon.qualitymanagement.utils.JapeSessionJobUtil;
import br.com.sankhya.studio.annotations.Job;
import br.com.sankhya.studio.annotations.enums.EJBTransactionType;
import br.com.sankhya.studio.stereotypes.IJob;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Job agendado: consulta VW_VENC_QUALIF_FORN e enfileira e-mails quando ENVIAR_NOTIFICACAO = 'S'.
 * Frequencia: a cada 24 horas
 */
@Job(
    serviceName = "NotificacaoVencimentoQualifJobSP",
    frequency = "&86400000",
    transactionType = EJBTransactionType.NotSupported
)
public class NotificacaoVencimentoQualificacaoJob extends IJob {

    private static final Logger LOG = Logger.getLogger(NotificacaoVencimentoQualificacaoJob.class.getName());

    @Override
    public void onSchedule() {
        try {
            LOG.info("Job vencimento qualificacao - view " + VW_VENC_QUALIF_FORN);
            JapeSessionJobUtil.executarComSessao(
                NotificacaoVencimentoQualificacaoService::processarNotificacoesPendentes
            );
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Erro ao processar notificacoes de vencimento (view "
                + VW_VENC_QUALIF_FORN + ").", e);
        }
    }
}
