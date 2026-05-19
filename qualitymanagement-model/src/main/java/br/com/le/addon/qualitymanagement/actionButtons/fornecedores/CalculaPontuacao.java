package br.com.le.addon.qualitymanagement.actionButtons.fornecedores;

import br.com.le.addon.qualitymanagement.services.CalculaPontuacaoQualificacao;
import br.com.le.addon.qualitymanagement.services.CalculaPontuacaoQualificacao.ResultadoPontuacao;
import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.studio.annotations.hooks.ActionButton;
import br.com.sankhya.studio.annotations.hooks.RefreshTypeEnum;
import br.com.sankhya.studio.annotations.hooks.TransactionType;

import java.math.BigDecimal;

@ActionButton(
    description = "Calcula Pontuacao",
    instanceName = "QualificacaoFornecedor",
    accessControlled = false,
    transactionType = TransactionType.AUTOMATIC,
    refreshType = RefreshTypeEnum.PARENT_ITEM
)
public class CalculaPontuacao implements AcaoRotinaJava {

    @Override
    public void doAction(ContextoAcao ctx) throws Exception {
        Registro[] linhas = ctx.getLinhas();

        if (linhas == null || linhas.length == 0) {
            ctx.setMensagemRetorno("Nenhum registro selecionado.");
            return;
        }

        StringBuilder mensagens = new StringBuilder();

        for (Registro linha : linhas) {
            BigDecimal idQualif = toBigDecimal(linha.getCampo("IDQUALIF"));

            if (idQualif == null) {
                throw new Exception("Campo obrigatorio nao informado: IDQUALIF");
            }

            ResultadoPontuacao resultado = CalculaPontuacaoQualificacao.calcularEAtualizar(idQualif);

            if (mensagens.length() > 0) {
                mensagens.append("\n");
            }
            mensagens
                .append("Qualificacao ")
                .append(idQualif)
                .append(": Pontuacao ")
                .append(resultado.getPontuacao())
                .append(" | IQF ")
                .append(resultado.getResultadoIqf())
                .append(" (")
                .append(resultado.getQuantidadeRespostas())
                .append(" respostas)");
        }

        ctx.setMensagemRetorno(mensagens.toString());
    }

    private BigDecimal toBigDecimal(Object valor) {
        if (valor == null) {
            return null;
        }
        if (valor instanceof BigDecimal) {
            return (BigDecimal) valor;
        }
        String texto = valor.toString().trim();
        if (texto.isEmpty()) {
            return null;
        }
        return new BigDecimal(texto);
    }
}
