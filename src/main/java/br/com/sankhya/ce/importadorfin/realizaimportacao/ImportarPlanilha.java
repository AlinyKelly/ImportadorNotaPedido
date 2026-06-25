package br.com.sankhya.ce.importadorfin.realizaimportacao;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.facades.ImportadorDadosSP;
import br.com.sankhya.modelcore.facades.ImportadorDadosSPBean;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.JdbcUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.ResultSet;
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
        NativeSql sql = null;
        ResultSet rset = null;

        Registro[] linhas = contexto.getLinhas();

        // ultimaLinhaJson sera utilizo para exibir a linha em que ocorreu o errro.
        LinhaCSVFin ultimaLinhaCsv = null;

        if (linhas == null || linhas.length == 0) {
            throw new UnsupportedOperationException("Selecione um linha para realizar a importacao.");
        }

        try {
            hnd = JapeSession.open();
            hnd.setFindersMaxRows(-1);
            EntityFacade entity = EntityFacadeFactory.getDWFFacade();
            jdbc = entity.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);

            Map<String, String> tiposCampos = new HashMap<>();

            sql.appendSql("SELECT NOMECAMPO, TIPCAMPO FROM TDDCAM WHERE NOMETAB = 'TSCEIMPFINLAN' AND NOMECAMPO NOT IN ('IDIMPFIN', 'DTALTER', 'CODUSU', 'MENSAGEM', 'STATUSIMP', 'DHPROCESSAMENTO', 'DATAIMPORTACAO') ORDER BY NUCAMPO");

            rset = sql.executeQuery();

            while (rset.next()) {
                // aqui podemos capturar o valor retornado na consulta
                String nomecampo = rset.getString("NOMECAMPO");
                String tipoCampo = rset.getString("TIPCAMPO");

                tiposCampos.put(nomecampo.toUpperCase(), tipoCampo);
            }

            for (Registro linha : linhas) {
                codImportador = (BigDecimal) linha.getCampo("IDIMPFIN");

                byte[] data = (byte[]) linha.getCampo("ARQUIVO");

                File file = new File(ctx.getTempFolder(), "IMPORTADORFIN" + System.currentTimeMillis());

                String conteudo = new String(data, java.nio.charset.StandardCharsets.UTF_8);

                conteudo = conteudo.replaceAll(
                        "__start_fileinformation__.*?__end_fileinformation__",
                        ""
                );

                FileUtils.writeStringToFile(
                        file,
                        conteudo,
                        String.valueOf(java.nio.charset.StandardCharsets.UTF_8)
                );

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
                    JapeWrapper lanDAO = JapeFactory.dao("IsceImportadorFinanceiroLan");

                    for (CSVRecord record : parser) {
                        Map<String, String> linhaCSV = record.toMap();

                        try {
                            FluidCreateVO save = lanDAO.create();
                            save.set("IDIMPFIN", codImportador);

                            Map<String, String> linhaCSVTratada = new HashMap<>();

                            for (Map.Entry<String, String> entry : linhaCSV.entrySet()) {

                                String coluna = entry.getKey()
                                        .replace("\uFEFF", "")
                                        .trim()
                                        .toUpperCase();

                                linhaCSVTratada.put(coluna, entry.getValue());
                            }

                            linhaCSV = linhaCSVTratada;

                            if (get(linhaCSV, "IDIMPFINLAN") == null) {
                                inserirErroLOG("Coluna obrigatoria IDIMPFINLAN nao encontrada", codImportador);
                                continue;
                            }

                            for (Map.Entry<String, String> coluna : linhaCSV.entrySet()) {
                                String nomeColuna = coluna.getKey();   // HEADER do CSV
                                String valor = coluna.getValue();      // valor da celula

                                if (!nomeColuna.equals("IDIMPFINLAN") && valor != null && !valor.trim().isEmpty()) {
                                    String tipoCampo = tiposCampos.get(nomeColuna);

                                    Object valorConvertido = converterValor(valor, tipoCampo);

                                    save.set(nomeColuna, valorConvertido);
                                }
                            }
                            save.set("STATUSIMP", "1");
                            save.save();

                            contexto.setMensagemRetorno("Importacao Finalizada! ");

                        } catch (Exception e) {
                            inserirErroLOG("ERRO: " + e.getMessage(), codImportador);
                        }

                    }
                }
            }
        } catch (Exception e) {
            inserirErroLOG("ERRO:" + e.getMessage() + "\nInconsistencia na linha : \n" + ultimaLinhaCsv, codImportador);
        } finally {
            JdbcUtils.closeResultSet(rset);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);
        }

    }

    //corrigir a criacao do log
    public static void inserirErroLOG(String erro, BigDecimal codImportador) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper logDAO = JapeFactory.dao("IsceImportadorFinanceiroLog");
            DynamicVO save = logDAO.create()
                    .set("IDIMPFIN", codImportador)
                    .set("ERRO", erro.toCharArray())
                    .set("DHERRO", new Timestamp(System.currentTimeMillis()))
                    .save();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    private String get(Map<String, String> map, String key) {
        String v = map.get(key);
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }

    private Object converterValor(String valor, String tipoCampo) {

        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        valor = valor.trim();

        switch (tipoCampo) {

            case "F": // Numero Decimal
            case "I": // Numero Inteiro
                return new BigDecimal(valor.replace(",", "."));


            case "D": // Data
            case "H": // Data e Hora
                return stringToTimeStamp(valor);


            case "C": // CLOB
                return valor.toCharArray();


            case "S": // Texto
            case "T": // Hora
            default:
                return valor;
        }
    }

}
