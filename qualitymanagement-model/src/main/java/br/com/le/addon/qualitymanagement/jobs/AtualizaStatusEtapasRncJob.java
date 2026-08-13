package br.com.le.addon.qualitymanagement.jobs;

import br.com.le.addon.qualitymanagement.services.AtualizaStatusEtapasRncService;
import br.com.le.addon.qualitymanagement.utils.JapeSessionJobUtil;
import br.com.sankhya.studio.annotations.Job;
import br.com.sankhya.studio.annotations.enums.EJBTransactionType;
import br.com.sankhya.studio.stereotypes.IJob;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Job agendado: sincroniza diariamente o STATUS das etapas da RNC (Causa
 * Raiz, Acoes Imediatas, Abrangencia, Acoes Corretivas, Implementacao)
 * conforme a DATAPRAZO de cada etapa. Frequencia: a cada 24 horas.
 */
@Job(
    serviceName = "AtualizaStatusEtapasRncJobSP",
    frequency = "&86400000",
    transactionType = EJBTransactionType.NotSupported
)
public class AtualizaStatusEtapasRncJob extends IJob {

    private static final Logger LOG = Logger.getLogger(AtualizaStatusEtapasRncJob.class.getName());

    @Override
    public void onSchedule() {
        try {
            LOG.info("Job atualizacao status etapas RNC - iniciado");
            JapeSessionJobUtil.executarComSessao(
                AtualizaStatusEtapasRncService::atualizarStatusEtapas
            );
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Erro ao atualizar status das etapas da RNC.", e);
        }
    }
}
