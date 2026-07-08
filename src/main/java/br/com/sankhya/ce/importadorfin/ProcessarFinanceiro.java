package br.com.sankhya.ce.importadorfin;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.VOProperty;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
import br.com.sankhya.jape.wrapper.fluid.FluidUpdateVO;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.helper.ImportadorDadosHelper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Iterator;

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
            BigDecimal idImpfin = (BigDecimal) linha.getCampo("IDIMPFIN");

            try {
                hnd = JapeSession.open();

                JapeWrapper dao = JapeFactory.dao("IsceImportadorFinanceiroLan");

                Collection<DynamicVO> dynamicVOs = dao.find("IDIMPFIN = ?", idImpfin.toString());

                for (DynamicVO modeloVO : dynamicVOs) {
                    BigDecimal idLancamento = modeloVO.asBigDecimal("IDIMPFINLAN");
                    String status = modeloVO.asString("STATUSIMP");
                    BigDecimal nufin = modeloVO.asBigDecimal("NUFIN");

                    System.out.println("Lancamento = " + idLancamento);

                    if (!"3".equals(status)) {

                        if (nufin != null) {
                            try {
                                updateRegistros(modeloVO, idImpfin, idLancamento, nufin);
                            } catch (Exception e) {
                                inserirErroStatus("Erro: " + e.getMessage(), idImpfin, idLancamento);
                            }
                        } else {
                            try {
                                insertRegistros(modeloVO, idImpfin, idLancamento);
                            } catch (Exception e) {
                                inserirErroStatus("Erro: " + e.getMessage(), idImpfin, idLancamento);
                            }
                        }

                    }

                }

            } catch (Exception e) {
                inserirErroLOG("ERRO: " + e.getMessage(), idImpfin);
            } finally {
                JapeSession.close(hnd);
            }

        }

        contexto.setMensagemRetorno("Proceso concluido!");

    }

    private void updateRegistros(DynamicVO modeloVO, BigDecimal codImportador, BigDecimal idLancamento, BigDecimal nufin) throws Exception {
        JapeWrapper lanDAO = JapeFactory.dao("Financeiro");
        FluidUpdateVO updateFin = lanDAO.prepareToUpdateByPK(nufin);

        try {
            Iterator<?> iterator = modeloVO.iterator();
            while (iterator.hasNext()) {
                VOProperty property = (VOProperty) iterator.next();

                if (property.getValue() != null &&
                        !property.getValue().toString().isEmpty() &&
                        !property.getName().contains(this.ID_EXTERNO_) &&
                        !property.getName().startsWith("CPL_") &&
                        !property.getName().startsWith("TAB_") &&
                        !property.getName().startsWith("IsceImportador") &&
                        !property.getName().equals("MENSAGEM") &&
                        !property.getName().equals("IDIMPFIN") &&
                        !property.getName().equals("IDIMPFINLAN") &&
                        !property.getName().equals("STATUSIMP") &&
                        !property.getName().equals("DHPROCESSAMENTO") &&
                        !property.getName().equals("DATAIMPORTACAO") &&
                        !property.getName().equals("DHTIPOPER") &&
                        !property.getName().equals("Usuario")
                ) {
                    if (property.getName().startsWith("NTA_")) {
                        String coluna = property.getName().substring(4);
                        updateFin.set(coluna, property.getValue());
                    } else if (property.getName().equals("HISTORICO")) {
                        String historico = modeloVO.asString("HISTORICO");
                        updateFin.set(property.getName(), historico);
                    } else {
                        updateFin.set(property.getName(), property.getValue());
                    }
                }

                updateFin.update();

                inserirStatusUpdate(codImportador, idLancamento, "Financeiro atualizado.");

            }
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        }

    }

    private void insertRegistros(DynamicVO modeloVO, BigDecimal idImpFin, BigDecimal idImpFinLan) throws Exception {
        System.out.println("Linha: " + modeloVO.asBigDecimal("IDIMPFINLAN"));

        BigDecimal nufin = null;
        try {
            JapeWrapper insertFinDAO = JapeFactory.dao("Financeiro");
            FluidCreateVO save = insertFinDAO.create();

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
                        !property.getName().equals("IDIMPFIN") &&
                        !property.getName().equals("IDIMPFINLAN") &&
                        !property.getName().equals("STATUSIMP") &&
                        !property.getName().equals("DHPROCESSAMENTO") &&
                        !property.getName().equals("DATAIMPORTACAO") &&
                        !property.getName().equals("DHTIPOPER") &&
                        !property.getName().equals("Usuario")
                ) {
                    if (property.getName().startsWith("NTA_")) {
                        String coluna = property.getName().substring(4);
                        save.set(coluna, property.getValue());
                    } else if (property.getName().equals("HISTORICO")) {
                        String historico = modeloVO.asString("HISTORICO");
                        save.set(property.getName(), historico);
                    } else {
                        save.set(property.getName(), property.getValue());
                    }
                }

            }

            nufin = save.save().asBigDecimal("NUFIN");

            inserirStatusConcluido(idImpFin, idImpFinLan, nufin, "Financeiro criado.");

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        }

    }

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

    public static void inserirErroStatus(String erro, BigDecimal codImportador, BigDecimal codlancamento) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao("IsceImportadorFinanceiroLan")
                    .prepareToUpdateByPK(codlancamento, codImportador)
                    .set("STATUSIMP", "2")
                    .set("MENSAGEM", erro.toCharArray())
                    .set("DHPROCESSAMENTO", new Timestamp(System.currentTimeMillis()))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void inserirStatusConcluido(BigDecimal codImportador, BigDecimal codlancamento, BigDecimal nufinCriado, String msg) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao("IsceImportadorFinanceiroLan")
                    .prepareToUpdateByPK(codlancamento, codImportador)
                    .set("NUFIN", nufinCriado)
                    .set("STATUSIMP", "3")
                    .set("MENSAGEM", msg.toCharArray())
                    .set("DHPROCESSAMENTO", new Timestamp(System.currentTimeMillis()))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public static void inserirStatusUpdate(BigDecimal codImportador, BigDecimal codlancamento, String msg) throws MGEModelException {
        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeFactory.dao("IsceImportadorFinanceiroLan")
                    .prepareToUpdateByPK(codlancamento, codImportador)
                    .set("STATUSIMP", "3")
                    .set("MENSAGEM", msg.toCharArray())
                    .set("DHPROCESSAMENTO", new Timestamp(System.currentTimeMillis()))
                    .update();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

}
