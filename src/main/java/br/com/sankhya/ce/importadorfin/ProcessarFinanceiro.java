package br.com.sankhya.ce.importadorfin;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.FieldProxy;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.VOProperty;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.helper.ImportadorDadosHelper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;
import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ProcessarFinanceiro implements AcaoRotinaJava {
    private String ID_EXTERNO_ = "ID_EXTERNO_";

    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        ServiceContext ctx = new ServiceContext(null);
        ctx.setAutentication(AuthenticationInfo.getCurrent());
        ctx.makeCurrent();

        Registro[] linhas = contexto.getLinhas();

        JapeSession.SessionHandle hnd = null;
        JdbcWrapper jdbc = null;
        NativeSql sql = null;

        if (linhas == null || linhas.length == 0) {
            throw new UnsupportedOperationException("Selecione um linha para realizar o processamento.");
        }

        for (Registro linha : linhas) {
            BigDecimal idImpfin = (BigDecimal) linha.getCampo("ID_IMPFIN");

            try {
                hnd = JapeSession.open();

                JapeWrapper dao = JapeFactory.dao("IsceImportadorFinanceiroLan");

                Collection<DynamicVO> dynamicVOs = dao.find("ID_IMPFIN = ?", idImpfin.toString());

                for (DynamicVO modeloVO : dynamicVOs) {
                    String status = modeloVO.asString("STATUSIMP");

                    if (!"3".equals(status)) {
                        inserirRegistros(jdbc, sql, contexto, modeloVO);

                    }

                }

            } catch (Exception e) {
                inserirErroLOG("ERRO: " + e.getMessage(), idImpfin);
            } finally {
                JapeSession.close(hnd);
            }

        }

        contexto.setMensagemRetorno("Financeiro inserido com sucesso!");

    }

    private void inserirRegistros(JdbcWrapper jdbc, NativeSql sql, ContextoAcao contexto, DynamicVO modeloVO) throws MGEModelException {
        String insert = "INSERT INTO TGFFIN " + "(NUFIN, :NOME_COLUNA) VALUES((SELECT DISTINCT Snk_Get_Nufin AS NUFIN FROM TGFFIN WHERE ROWNUM = 1), :VALORES)";
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();

        EntityFacade entity = EntityFacadeFactory.getDWFFacade();
        jdbc = entity.getJdbcWrapper();
        sql = new NativeSql(jdbc);

        try {
            String queryInsert = getInsert(contexto, modeloVO, columns, values, insert);

            sql.appendSql(queryInsert);
            sql.executeUpdate();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            NativeSql.releaseResources(sql);
            JdbcWrapper.closeSession(jdbc);
        }
    }

    private void atualizarRegistros(JdbcWrapper jdbc, NativeSql sql, ContextoAcao contexto, DynamicVO modeloVO) {

    }

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

    private String getInsert(ContextoAcao contexto, DynamicVO modeloVO, StringBuilder columns, StringBuilder values, String insert) throws Exception {
        Iterator<?> iterator = modeloVO.iterator();
        while (iterator.hasNext()) {
            VOProperty property = (VOProperty) iterator.next();

            if (property.getValue() != null &&
                    !property.getValue().toString().isEmpty() &&
                    !property.getName().contains(this.ID_EXTERNO_) &&
                    !property.getName().startsWith("CPL_") &&
                    !property.getName().startsWith("TAB_") &&
                    !property.getName().startsWith("IsceImportador") &&
                    !property.getName().equals("NUFIN") &&
                    !property.getName().equals("MENSAGEM") &&
                    !property.getName().equals("ID_IMPFIN") &&
                    !property.getName().equals("ID_IMPFINLAN") &&
                    !property.getName().equals("STATUSIMP") &&
                    !property.getName().equals("DHPROCESSAMENTO") &&
                    !property.getName().equals("DATA_IMPORTACAO") &&
                    !property.getName().equals("DHTIPOPER") &&
                    !property.getName().equals("Usuario")
            ) {
                if (property.getName().startsWith("NTA_")) {
                    columns.append(property.getName().substring(4) + ",");
                } else {
                    columns.append(property.getName() + ",");
                }

                if (property.getName().equals("HISTORICO")) {
                    String historico = modeloVO.asString("HISTORICO");
                    values.append("'" + historico + "',");
                } else if (property.getValue() instanceof BigDecimal) {
                    values.append(((BigDecimal) property.getValue()).doubleValue() + ",");
                } else if (property.getValue() instanceof String) {
                    values.append("'" + (String) property.getValue() + "',");
                } else if (property.getValue() instanceof Timestamp) {
                    String sData = (new SimpleDateFormat("dd/MM/yyyy")).format((Timestamp) property.getValue());
                    values.append((ImportadorDadosHelper.isOracleDb() ? "TO_DATE('" + sData + "','DD/MM/YYYY')" : "CONVERT(DATE,'" + sData + "', 103)") + ",");
                }
            }

        }

        if (!columns.toString().isEmpty() && !values.toString().isEmpty()) {
            insert = insert.replace(":NOME_COLUNA", columns.replace(columns.length() - 1, columns.length(), "")).replace(":VALORES", values.replace(values.length() - 1, values.length(), ""));
            return insert;
        } else {
            return null;
        }

    }

}
