package br.com.sankhya.ce.importadorfin;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.JdbcUtils;
import com.sankhya.util.SessionFile;
import com.sankhya.util.UIDGenerator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class GerarPlanilhaFin implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        JdbcWrapper jdbc = null;
        NativeSql sql = null;
        ResultSet rset = null;
        JapeSession.SessionHandle hnd = null;

        List<String> colunas = new ArrayList<>();

        String chaveSessao = "download_TSCE_IMPFINLAN_" + UIDGenerator.getNextID();

        try {
            hnd = JapeSession.open();
            hnd.setFindersMaxRows(-1);
            EntityFacade entity = EntityFacadeFactory.getDWFFacade();
            jdbc = entity.getJdbcWrapper();
            jdbc.openSession();

            sql = new NativeSql(jdbc);

            sql.appendSql("SELECT NOMECAMPO FROM TDDCAM WHERE NOMETAB = 'TSCE_IMPFINLAN' AND NOMECAMPO NOT IN ('ID_IMPFIN', 'DTALTER', 'CODUSU', 'MENSAGEM', 'STATUSIMP', 'DHPROCESSAMENTO', 'DATA_IMPORTACAO') ORDER BY NUCAMPO");

            rset = sql.executeQuery();

            while (rset.next()) {
                colunas.add(rset.getString("NOMECAMPO"));

            }

            // =====================================================
            // CRIA O CONTEUDO DO CSV EM MEMORIA
            // =====================================================
            ByteArrayOutputStream csvOutput = new ByteArrayOutputStream();

            try (
                    OutputStreamWriter writer =
                            new OutputStreamWriter(csvOutput, StandardCharsets.UTF_8);

                    CSVPrinter csvPrinter = new CSVPrinter(
                            writer,
                            CSVFormat.DEFAULT
                                    .withDelimiter(';')
                                    .withHeader(colunas.toArray(new String[0]))
                    );
            ) {

                // Como eh um modelo vazio, nao adicionamos registros.
                // Apenas o cabecalho sera gravado.

                csvPrinter.flush();
            }

            // =====================================================
            // CRIA O ZIP EM MEMORIA
            // =====================================================
            ByteArrayOutputStream zipOutput = new ByteArrayOutputStream();

            try (ZipOutputStream zipOut = new ZipOutputStream(zipOutput)) {

                // Nome do arquivo dentro do ZIP
                ZipEntry zipEntry = new ZipEntry("TSCE_IMPFINLAN.csv");

                zipOut.putNextEntry(zipEntry);

                // Escreve o CSV dentro do ZIP
                zipOut.write(csvOutput.toByteArray());

                zipOut.closeEntry();
            }

            // =====================================================
            // DOWNLOAD DO ARQUIVO
            // =====================================================

            byte[] zipBytes = zipOutput.toByteArray();

            String nomeZip = "TSCE_IMPFINLAN.zip";
            SessionFile sessionFileZip = SessionFile.createSessionFile(nomeZip, "application/zip", zipBytes);
            ServiceContext.getCurrent().putHttpSessionAttribute(chaveSessao, sessionFileZip);

            contextoAcao.setMensagemRetorno(
                    "<a id=\"alink\" href=\"/mge/visualizadorArquivos.mge?chaveArquivo=" + chaveSessao +
                            "\" target=\"_blank\">Baixar Modelo de Planilha</a>"
            );

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JdbcUtils.closeResultSet(rset);
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
            JapeSession.close(hnd);

        }
    }
}
