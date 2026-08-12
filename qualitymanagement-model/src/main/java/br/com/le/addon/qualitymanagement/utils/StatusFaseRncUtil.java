package br.com.le.addon.qualitymanagement.utils;

import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.vo.DynamicVO;

/**
 * Logica compartilhada pelos listeners de fase da RNC (Causa Raiz, Acoes
 * Imediatas, Abrangencia, Acoes Corretivas, Implementacao): ao incluir um
 * novo registro, define STATUS = Em Andamento.
 */
public final class StatusFaseRncUtil {

    private static final String STATUS_EM_ANDAMENTO = "E";

    private StatusFaseRncUtil() {
    }

    public static void marcarEmAndamento(PersistenceEvent event) throws Exception {
        ((DynamicVO) event.getVo()).setProperty("STATUS", STATUS_EM_ANDAMENTO);
    }
}
