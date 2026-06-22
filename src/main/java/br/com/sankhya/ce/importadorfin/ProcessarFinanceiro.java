package br.com.sankhya.ce.importadorfin;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

import java.math.BigDecimal;
import java.sql.CallableStatement;

public class ProcessarFinanceiro implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contexto) throws Exception {
        ServiceContext ctx = new ServiceContext(null);
        ctx.setAutentication(AuthenticationInfo.getCurrent());
        ctx.makeCurrent();

        Registro[] linhas = contexto.getLinhas();

        JapeSession.SessionHandle hnd = null;
        JdbcWrapper jdbc = null;

        if (linhas == null || linhas.length == 0) {
            throw new UnsupportedOperationException("Selecione um linha para realizar o processamento.");
        }

        for (Registro linha : linhas) {
            int qtdLinha = linhas.length;

            BigDecimal idImpfin = (BigDecimal) linha.getCampo("ID_IMPFIN");

            try {
                hnd = JapeSession.open();
                EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

                jdbc = dwfFacade.getJdbcWrapper();
                jdbc.openSession();

                CallableStatement cstmt = jdbc.getConnection().prepareCall("{call STP_SCE_PROCESSARFIN(?)}");
//                CallableStatement cstmt = jdbc.getConnection().prepareCall("{call STP_SCE_PROCESSAR_LANC(?,?,?,?,?,?)}");
                cstmt.setQueryTimeout(60);

                cstmt.setBigDecimal(1, idImpfin);
//                cstmt.setBigDecimal(1, contexto.getUsuarioLogado());
//                cstmt.setString(2, ctx.getHttpSessionId());
//                cstmt.setInt(3, qtdLinha);
//                cstmt.setString(4, "Processado.");

                cstmt.execute();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JdbcWrapper.closeSession(jdbc);
                JapeSession.close(hnd);
            }
        }

    }
}
