package br.com.le.addon.qualitymanagement.utils;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.MGECoreParameter;

import java.io.File;
import java.nio.file.Files;
import java.sql.ResultSet;

/**
 * Carrega anexo da instancia Questionarios (TSIANX + repositorio de arquivos),
 * seguindo o padrao de AbrirAnexo (FREPBASEFOLDER + Sistema/Anexos/{instancia}/CHAVEARQUIVO).
 */
public final class AnexoQuestionarioUtil {

    private static final String INSTANCIA_QUESTIONARIOS = "Questionarios";
    private static final String PARAM_BASE_FOLDER = "FREPBASEFOLDER";
    private static final String MIME_XLSX =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private AnexoQuestionarioUtil() {
    }

    public static AnexoEmail carregarAnexoQuestionario(String idQuest, String nomeArquivoEsperado) throws Exception {
        validarCampo(idQuest, "ID do questionario");
        validarCampo(nomeArquivoEsperado, "Nome do arquivo do questionario");

        String pkRegistro = idQuest.trim() + "_" + INSTANCIA_QUESTIONARIOS;
        MetadadoAnexo metadado = buscarMetadadoAnexo(pkRegistro, nomeArquivoEsperado);

        if (metadado == null) {
            throw new Exception(
                "Anexo nao encontrado na TSIANX para o questionario IDQUEST="
                    + idQuest
                    + ", PKREGISTRO="
                    + pkRegistro
                    + ", NOMEARQUIVO esperado='"
                    + nomeArquivoEsperado
                    + "'. Verifique o anexo na tela Questionarios e o parametro NOMEARQUIVOQUEST.");
        }

        byte[] conteudo = lerArquivoRepositorio(metadado.chaveArquivo);

        if (conteudo == null || conteudo.length == 0) {
            throw new Exception(
                "Conteudo do anexo vazio ou inacessivel. IDQUEST="
                    + idQuest
                    + ", CHAVEARQUIVO="
                    + metadado.chaveArquivo
                    + ", NOMEARQUIVO="
                    + metadado.nomeArquivo);
        }

        return new AnexoEmail(metadado.nomeArquivo, resolverMimeType(metadado.nomeArquivo), conteudo);
    }

    private static MetadadoAnexo buscarMetadadoAnexo(String pkRegistro, String nomeArquivoEsperado) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;

        try {
            EntityFacade dwf = EntityFacadeFactory.getDWFFacade();
            jdbc = dwf.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);
            sql.appendSql(" SELECT NUATTACH, CHAVEARQUIVO, NOMEARQUIVO ");
            sql.appendSql(" FROM TSIANX ");
            sql.appendSql(" WHERE NOMEINSTANCIA = :NOMEINSTANCIA ");
            sql.appendSql(" AND PKREGISTRO = :PKREGISTRO ");
            sql.appendSql(" AND UPPER(TRIM(NOMEARQUIVO)) = UPPER(TRIM(:NOMEARQUIVO)) ");

            sql.setNamedParameter("NOMEINSTANCIA", INSTANCIA_QUESTIONARIOS);
            sql.setNamedParameter("PKREGISTRO", pkRegistro);
            sql.setNamedParameter("NOMEARQUIVO", nomeArquivoEsperado.trim());

            rset = sql.executeQuery();

            if (!rset.next()) {
                return null;
            }

            MetadadoAnexo metadado = new MetadadoAnexo();
            metadado.chaveArquivo = rset.getString("CHAVEARQUIVO");
            metadado.nomeArquivo = rset.getString("NOMEARQUIVO");
            return metadado;
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
    }

    private static byte[] lerArquivoRepositorio(String chaveArquivo) throws Exception {
        validarCampo(chaveArquivo, "CHAVEARQUIVO");

        String caminhoBase = MGECoreParameter.getParameterAsString(PARAM_BASE_FOLDER);
        if (caminhoBase == null || caminhoBase.trim().isEmpty()) {
            throw new Exception("Parametro " + PARAM_BASE_FOLDER + " nao configurado no Sankhya.");
        }

        String caminhoRelativo = "/Sistema//Anexos//" + INSTANCIA_QUESTIONARIOS + "//" + chaveArquivo.trim();
        File arquivoRepositorio = new File(caminhoBase + caminhoRelativo);

        if (!arquivoRepositorio.exists() || !arquivoRepositorio.isFile()) {
            throw new Exception(
                "Arquivo fisico do anexo nao encontrado: "
                    + arquivoRepositorio.getAbsolutePath()
                    + ". Verifique FREPBASEFOLDER e o repositorio de anexos.");
        }

        return Files.readAllBytes(arquivoRepositorio.toPath());
    }

    private static String resolverMimeType(String nomeArquivo) {
        if (nomeArquivo == null) {
            return "application/octet-stream";
        }
        String nome = nomeArquivo.toLowerCase();
        if (nome.endsWith(".xlsx")) {
            return MIME_XLSX;
        }
        if (nome.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        }
        if (nome.endsWith(".pdf")) {
            return "application/pdf";
        }
        return "application/octet-stream";
    }

    private static void validarCampo(String valor, String nomeCampo) throws Exception {
        if (valor == null || valor.trim().isEmpty()) {
            throw new Exception(nomeCampo + " nao informado.");
        }
    }

    private static final class MetadadoAnexo {
        private String chaveArquivo;
        private String nomeArquivo;
    }
}
