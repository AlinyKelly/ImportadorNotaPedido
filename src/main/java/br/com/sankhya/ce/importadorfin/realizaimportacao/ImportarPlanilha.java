package br.com.sankhya.ce.importadorfin.realizaimportacao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.ws.ServiceContext;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static br.com.sankhya.ce.utilitariosJava.UtilsJava.*;

public class ImportarPlanilha implements AcaoRotinaJava {
    private BigDecimal codImportador;

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {

        ServiceContext ctx = ServiceContext.getCurrent();

        JapeSession.SessionHandle hnd = null;
        JdbcWrapper jdbc = null;

        Registro[] linhas = contexto.getLinhas();

        // ultimaLinhaJson sera utilizo para exibir a linha em que ocorreu o errro.
        LinhaCSVFin ultimaLinhaCsv = null;

        if (linhas == null || linhas.length == 0) {
            throw new UnsupportedOperationException("Selecione um linha para realizar a importacao.");
        }

        try {
            for (Registro linha : linhas) {
                codImportador = (BigDecimal) linha.getCampo("ID_IMPFIN");

                byte[] data = (byte[]) linha.getCampo("ARQUIVO");

                File file = new File(ctx.getTempFolder(), "IMPORTADORFIN" + System.currentTimeMillis());
                FileUtils.writeByteArrayToFile(file, data);
                validarCSV(file);

                hnd = JapeSession.open();

                try (
                        Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                        CSVParser parser = CSVFormat.DEFAULT
                                .withDelimiter(';') // ou ','
                                .withQuote('"')
                                .withIgnoreSurroundingSpaces()
                                .withFirstRecordAsHeader()
                                .withIgnoreEmptyLines()
                                .parse(reader)
                ) {
                    for (CSVRecord record : parser) {
                        LinhaCSVFin json = trataLinha(record);
                        ultimaLinhaCsv = json;

                        if (json == null) {
                            continue;
                        }

                        // Comecar a buscar os valores do CSV para inserir na tabela
                        BigDecimal id_impfinlan = toBigDecimal(json.getIdImpfinlan());
                        BigDecimal nufin = toBigDecimal(json.getNufin());
                        BigDecimal codparc = toBigDecimal(json.getCodparc());
                        String cnpjcpfparc = json.getCnpjcpfparc();
                        String recdesp = json.getRecdesp();
                        String provisao = json.getProvisao();
                        BigDecimal codemp = toBigDecimal(json.getCodemp());
                        BigDecimal numnota = toBigDecimal(json.getNumnota());
                        BigDecimal nunota = toBigDecimal(json.getNunota());
                        Timestamp dtneg = stringToTimeStamp(json.getDtneg());
                        BigDecimal vlrdesdob = converterValorMonetario(json.getVlrdesdob());
                        Timestamp dtvenc = stringToTimeStamp(json.getDtvenc());
                        Timestamp dtvencinic = stringToTimeStamp(json.getDtvencinic());
                        String historico = json.getHistorico();
                        BigDecimal codbco = toBigDecimal(json.getCodbco());
                        BigDecimal codctabcoint = toBigDecimal(json.getCodctabcoint());
                        BigDecimal codtiptit = toBigDecimal(json.getCodtiptit());
                        BigDecimal codtipoper = toBigDecimal(json.getCodtipoper());
                        Timestamp dhtipoper = stringToTimeStampHora(json.getDhtipoper());
                        BigDecimal codnat = toBigDecimal(json.getCodnat());
                        BigDecimal codcencus = toBigDecimal(json.getCodcencus());
                        BigDecimal codproj = toBigDecimal(json.getCodproj());
                        Timestamp dhmov = stringToTimeStamp(json.getDhmov());
                        BigDecimal numcontrato = toBigDecimal(json.getNumcontrato());
                        String desdobramento = json.getDesdobramento();
                        BigDecimal codvend = toBigDecimal(json.getCodvend());
                        String nossonum = json.getNossonum();
                        BigDecimal vlrirf = converterValorMonetario(json.getVlrirf());
                        BigDecimal vlriss = converterValorMonetario(json.getVlriss());
                        String issretido = json.getIssretido();
                        BigDecimal vlrdesc = converterValorMonetario(json.getVlrdesc());
                        BigDecimal vlrvendor = converterValorMonetario(json.getVlrvendor());
                        String codigobarra = json.getCodigobarra();
                        String linhadigitavel = json.getLinhadigitavel();
                        String tipjuro = json.getTipjuro();
                        String tipmulta = json.getTipmulta();
                        BigDecimal vlrjuro = converterValorMonetario(json.getVlrjuro());
                        BigDecimal vlrmulta = converterValorMonetario(json.getVlrmulta());
                        BigDecimal codmoeda = toBigDecimal(json.getCodmoeda());
                        String origem = json.getOrigem();
                        String rateado = json.getRateado();
                        Timestamp dtentsai = stringToTimeStamp(json.getDtentsai());

                        try {
                            JapeWrapper lanDAO = JapeFactory.dao("IsceImportadorFinanceiroLan");
                            DynamicVO save = lanDAO.create()
                                    .set("ID_IMPFIN", codImportador)
                                    .set("NUFIN", nufin)
                                    .set("CODPARC", codparc)
                                    .set("CNPJCPFPARC", cnpjcpfparc)
                                    .set("RECDESP", recdesp)
                                    .set("PROVISAO", provisao)
                                    .set("CODEMP", codemp)
                                    .set("NUMNOTA", numnota)
                                    .set("NUNOTA", nunota)
                                    .set("DTNEG", dtneg)
                                    .set("VLRDESDOB", vlrdesdob)
                                    .set("DTVENC", dtvenc)
                                    .set("DTVENCINIC", dtvencinic)
                                    .set("HISTORICO", historico.toCharArray())
                                    .set("CODBCO", codbco)
                                    .set("CODCTABCOINT", codctabcoint)
                                    .set("CODTIPTIT", codtiptit)
                                    .set("CODTIPOPER", codtipoper)
                                    .set("DHTIPOPER", dhtipoper)
                                    .set("CODNAT", codnat)
                                    .set("CODCENCUS", codcencus)
                                    .set("CODPROJ", codproj)
                                    .set("DHMOV", dhmov)
                                    .set("NUMCONTRATO", numcontrato)
                                    .set("DESDOBRAMENTO", desdobramento)
                                    .set("CODVEND", codvend)
                                    .set("NOSSONUM", nossonum)
                                    .set("VLRIRF", vlrirf)
                                    .set("VLRISS", vlriss)
                                    .set("ISSRETIDO", issretido)
                                    .set("VLRDESC", vlrdesc)
                                    .set("VLRVENDOR", vlrvendor)
                                    .set("CODIGOBARRA", codigobarra)
                                    .set("LINHADIGITAVEL", linhadigitavel)
                                    .set("TIPJURO", tipjuro)
                                    .set("TIPMULTA", tipmulta)
                                    .set("VLRJURO", vlrjuro)
                                    .set("VLRMULTA", vlrmulta)
                                    .set("CODMOEDA", codmoeda)
                                    .set("ORIGEM", origem)
                                    .set("RATEADO", rateado)
                                    .set("DTENTSAI", dtentsai)
                                    .save();

//                            BigDecimal idImpfinlan = save.asBigDecimal("ID_IMPFINLAN");

                            contexto.setMensagemRetorno("Importacao Finalizada! ");

                        } catch (Exception e) {
                            inserirErroLOG("ID Importacao = " + id_impfinlan + "ERRO:" + e.getMessage() + "\nInconsistencia na linha:  \n" + record.getRecordNumber() + "\n" + ultimaLinhaCsv, codImportador);
                        }

                    }
                }
            }
        } catch (Exception e) {
            inserirErroLOG("ERRO:" + e.getMessage() + "\nInconsistencia na linha : \n" + ultimaLinhaCsv, codImportador);
        } finally {
            JapeSession.close(hnd);
        }

    }

    private LinhaCSVFin trataLinha(CSVRecord record) throws MGEModelException {
        int TOTAL_COLUNAS = 43;

        if (record.size() < TOTAL_COLUNAS) {
            inserirErroLOG(
                    "Linha com colunas insuficientes (" + record.size() + " colunas). Conteudo: " + record.toString(),
                    codImportador
            );
            return null;
        }

        List<String> filtradas = new ArrayList<>();

        for (int i = 0; i < TOTAL_COLUNAS; i++) {
            String valor = record.get(i);

            if (valor == null || valor.trim().isEmpty()) {
                filtradas.add(null);
            } else {
                filtradas.add(valor.trim());
            }
        }

        return new LinhaCSVFin(
                filtradas.get(0),
                filtradas.get(1),
                filtradas.get(2),
                filtradas.get(3),
                filtradas.get(4),
                filtradas.get(5),
                filtradas.get(6),
                filtradas.get(7),
                filtradas.get(8),
                filtradas.get(9),
                filtradas.get(10),
                filtradas.get(11),
                filtradas.get(12),
                filtradas.get(13),
                filtradas.get(14),
                filtradas.get(15),
                filtradas.get(16),
                filtradas.get(17),
                filtradas.get(18),
                filtradas.get(19),
                filtradas.get(20),
                filtradas.get(21),
                filtradas.get(22),
                filtradas.get(23),
                filtradas.get(24),
                filtradas.get(25),
                filtradas.get(26),
                filtradas.get(27),
                filtradas.get(28),
                filtradas.get(29),
                filtradas.get(30),
                filtradas.get(31),
                filtradas.get(32),
                filtradas.get(33),
                filtradas.get(34),
                filtradas.get(35),
                filtradas.get(36),
                filtradas.get(37),
                filtradas.get(38),
                filtradas.get(39),
                filtradas.get(40),
                filtradas.get(41),
                filtradas.get(42)
        );
    }

    private void validarCSV(File file) throws Exception {
        int TOTAL_COLUNAS = 43;
        int linha = 1;

        try (
                Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                CSVParser parser = CSVFormat.DEFAULT
                        .withDelimiter(';')
                        .withQuote('"')
                        .withFirstRecordAsHeader()
                        .withIgnoreEmptyLines()
                        .parse(reader)
        ) {

            for (CSVRecord record : parser) {
                linha++;

                if (record.size() < TOTAL_COLUNAS) {
                    inserirErroLOG(
                            "Erro no CSV na linha " + linha +
                                    ". Esperado " + TOTAL_COLUNAS +
                                    " colunas, encontrado " + record.size(),
                            codImportador
                    );
                }
            }
        }
    }

    //corrigir a criacao do log
    public static void inserirErroLOG(String erro, BigDecimal codImportador) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper logDAO = JapeFactory.dao("IsceImportadorFinanceiroLog");
            DynamicVO save = logDAO.create()
                    .set("ID_IMPFIN", codImportador)
                    .set("ERRO", erro.toCharArray())
                    .set("DHERRO", new Timestamp(System.currentTimeMillis()))
                    .save();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

}
