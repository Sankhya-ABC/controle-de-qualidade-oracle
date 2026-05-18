package br.com.le.addon.qualitymanagement.utils;

public class AuthQuestionarioUtil {

    private AuthQuestionarioUtil() {

    }

    public static String montarAuth(String emailBase64, String codQualifBase64, String codQuestBase64, String loginBase64, String senhaBase64)
    {
        return limpar(emailBase64)
            + "?"
            + limpar(codQualifBase64)
            + "?"
            + limpar(codQuestBase64)
            + "?"
            + limpar(loginBase64)
            + "?"
            + limpar(senhaBase64)
            + "?";

    }

    public static String limpar(String valor) {
       if (valor == null) {
              return "";
       }

       return valor.replaceAll("\r", "")
            .replaceAll("\t", "")
            .replaceAll("\n", "")
            .replaceAll(" ", "");
    }

}
