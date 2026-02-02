package br.com.sankhya.ce.utilitariosJava;

import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UtilsJava {

    public static String getReplaceFileInfo(String line) {
        String regex = "__start_fileinformation__.*__end_fileinformation__";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(line);
        return matcher.replaceAll("");
    }

    public static DynamicVO retornaVO(String instancia, String where) throws MGEModelException {

        JapeSession.SessionHandle sh = null;

        try {
            sh = JapeSession.open();
            JapeWrapper dao = JapeFactory.dao(instancia);
            return dao.findOne(where);

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(sh);
        }

        return null;
    }

    public static void inserirErroLOG(String erro, BigDecimal codImportador) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper logDAO = JapeFactory.dao("AD_IMPLOG");
            DynamicVO save = logDAO.create()
                    .set("CODIMP", codImportador)
                    .set("ERRO", erro)
                    .set("DHERRO", getDhAtual())
                    .save();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static BigDecimal converterValorMonetario(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        valor = valor.replace("\"", "")
                .replace(".", "")
                .replace(",", ".");

        try {
            return new BigDecimal(valor);
        } catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal toBigDecimal(String valor) {
        // 1. Verifica se é null primeiro (EVITA O NullPointerException)
        if (valor == null) {
            return BigDecimal.ZERO;
        }

        // 2. Chama trim() e verifica se está vazio
        String valorTratado = valor.trim();
        if (valorTratado.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 3. Converte
        try {
            return new BigDecimal(valorTratado);
        } catch (NumberFormatException e) {
            // Logar o erro ou lançar uma exceção mais específica se o formato for inválido
            System.err.println("Erro de formato BigDecimal para o valor: " + valorTratado);
            return BigDecimal.ZERO; // Ou trate como erro de linha
        }
    }

    public static Timestamp stringToTimeStamp(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatter.parse(str);
            return new Timestamp(date.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static Timestamp stringToTimeStampHora(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = formatter.parse(str);
            return new Timestamp(date.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static Time stringToTime(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        try {
            DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
            Date date = formatter.parse(str);
            return new Time(date.getTime());
        } catch (Exception e) {
            return null;
        }
    }


    public static Timestamp getDhAtual() {
        return new Timestamp(System.currentTimeMillis());
    }
}
