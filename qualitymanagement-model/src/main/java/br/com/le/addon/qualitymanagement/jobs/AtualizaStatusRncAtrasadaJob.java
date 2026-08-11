package br.com.le.addon.qualitymanagement.jobs;

import br.com.le.addon.qualitymanagement.services.AtualizaStatusRncAtrasadaService;
import br.com.le.addon.qualitymanagement.utils.JapeSessionJobUtil;
import br.com.sankhya.studio.annotations.Job;
import br.com.sankhya.studio.annotations.enums.EJBTransactionType;
import br.com.sankhya.studio.stereotypes.IJob;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Job agendado: percorre os registros de RNC (TGQRNC / ConsultaRNC) e marca como
 * Atrasada (STATUS = 'A') todo registro cuja DATAPREVENCERRAR ja tenha passado.
 * Frequencia: a cada 24 horas
 */
@Job(
    serviceName = "AtualizaStatusRncAtrasadaJobSP",
    frequency = "&86400000",
    transactionType = EJBTransactionType.NotSupported
)
public class AtualizaStatusRncAtrasadaJob extends IJob {

    private static final Logger LOG = Logger.getLogger(AtualizaStatusRncAtrasadaJob.class.getName());

    @Override
    public void onSchedule() {
        try {
            LOG.info("Job atualizacao status RNC atrasada - iniciado");
            JapeSessionJobUtil.executarComSessao(
                AtualizaStatusRncAtrasadaService::atualizarRncsAtrasadas
            );
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Erro ao atualizar status de RNCs atrasadas.", e);
        }
    }
}
