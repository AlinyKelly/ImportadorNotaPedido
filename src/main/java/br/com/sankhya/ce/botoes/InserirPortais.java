package br.com.sankhya.ce.botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.comercial.CentralFinanceiro;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;

import static br.com.sankhya.ce.utilitariosJava.UtilsJava.*;

public class InserirPortais implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhasSelecionadas = contextoAcao.getLinhas();
        BigDecimal codImportador = null;
        String idImportador = "";
        try {
            for (Registro linha : linhasSelecionadas) {
                BigDecimal codlancnota = (BigDecimal) linha.getCampo("CODLANCNOTA");
                codImportador = (BigDecimal) linha.getCampo("CODIMP");
                BigDecimal codempresa = (BigDecimal) linha.getCampo("CODEMP");
                BigDecimal codparceiro = (BigDecimal) linha.getCampo("CODPARC");
                BigDecimal codtipooperacao = (BigDecimal) linha.getCampo("CODTIPOPER");
                BigDecimal codtiponegociacao = (BigDecimal) linha.getCampo("CODTIPVENDA");
                Timestamp dataalteracaoCab = (Timestamp) linha.getCampo("DTALTER");
                Timestamp datanegociacao = (Timestamp) linha.getCampo("DTNEG");
                Timestamp dataHoraFaturamento = (Timestamp) linha.getCampo("DTFATUR");
                Timestamp dataEntradasaida = (Timestamp) linha.getCampo("DTENTSAI");
                Timestamp dataMovimento = (Timestamp) linha.getCampo("DTMOV");
                Time horaMovimento = (Time) linha.getCampo("HRMOV");
                BigDecimal nronota = (BigDecimal) linha.getCampo("NUMNOTA");
                String tipomovimento = (String) linha.getCampo("TIPMOV");
                BigDecimal vlrdescontototal = (BigDecimal) linha.getCampo("VLRDESCTOT");
                BigDecimal vlrnota = (BigDecimal) linha.getCampo("VLRNOTA");
                String serienota = (String) linha.getCampo("SERIENOTA");
                String observacao = (String) linha.getCampo("OBSERVACAO");
                BigDecimal centroresultados = (BigDecimal) linha.getCampo("CODCENCUS");
                BigDecimal natureza = (BigDecimal) linha.getCampo("CODNAT");
                BigDecimal projeto = (BigDecimal) linha.getCampo("CODPROJ");
                BigDecimal contrato = (BigDecimal) linha.getCampo("NUMCONTRATO");
                BigDecimal nroUnico = (BigDecimal) linha.getCampo("NUNOTA");
                BigDecimal cidade = (BigDecimal) linha.getCampo("CODCID");
                idImportador = (String) linha.getCampo("IDIMPORTADOR");

                String retemISS = NativeSql.getString("PAR.RETEMISS", "TGFPAR PAR", "PAR.CODPARC = ?", new Object[]{codparceiro});

                String issRetido = "S".equals(retemISS) ? "S" : "N";

                if (nroUnico == null) {
                    EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
                    DynamicVO cabecalhoNota = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("CabecalhoNota");
                    cabecalhoNota.setProperty("NUMNOTA", nronota);
                    cabecalhoNota.setProperty("CODEMP", codempresa);
                    cabecalhoNota.setProperty("CODPARC", codparceiro);
                    cabecalhoNota.setProperty("CODTIPOPER", codtipooperacao);
                    cabecalhoNota.setProperty("CODTIPVENDA", codtiponegociacao);
                    cabecalhoNota.setProperty("DTALTER", dataalteracaoCab);
                    cabecalhoNota.setProperty("DTNEG", datanegociacao);
                    cabecalhoNota.setProperty("DTFATUR", dataHoraFaturamento);
                    cabecalhoNota.setProperty("DTENTSAI", dataEntradasaida);
                    cabecalhoNota.setProperty("DTMOV", dataMovimento);
                    cabecalhoNota.setProperty("HRMOV", horaMovimento);
                    cabecalhoNota.setProperty("TIPMOV", tipomovimento);
                    cabecalhoNota.setProperty("VLRDESCTOT", vlrdescontototal);
                    cabecalhoNota.setProperty("VLRNOTA", vlrnota);
                    cabecalhoNota.setProperty("SERIENOTA", serienota);
                    cabecalhoNota.setProperty("OBSERVACAO", observacao);
                    cabecalhoNota.setProperty("CODCENCUS", centroresultados);
                    cabecalhoNota.setProperty("CODNAT", natureza);
                    cabecalhoNota.setProperty("CODPROJ", projeto);
                    cabecalhoNota.setProperty("NUMCONTRATO", contrato);
                    cabecalhoNota.setProperty("CODCID", cidade);
                    cabecalhoNota.setProperty("AD_CODLANCNOTA", codlancnota);
                    cabecalhoNota.setProperty("AD_CODIMP", codImportador);
                    cabecalhoNota.setProperty("ISSRETIDO", issRetido);
                    dwfFacade.createEntity("CabecalhoNota", (EntityVO) cabecalhoNota);

                    BigDecimal nunota = cabecalhoNota.asBigDecimal("NUNOTA");

                    EntityFacade facade = EntityFacadeFactory.getDWFFacade();
                    Collection<DynamicVO> itens = facade.findByDynamicFinderAsVO(new FinderWrapper("AD_IMPITEDET", "this.CODLANCNOTA = ? AND this.CODIMP = ?", new Object[]{codlancnota, codImportador}));
                    for (DynamicVO itensVO : itens) {
                        BigDecimal codItem = itensVO.asBigDecimal("CODITEM");
                        BigDecimal codlancnotaItem = itensVO.asBigDecimal("CODLANCNOTA");
                        BigDecimal codimportadorItem = itensVO.asBigDecimal("CODIMP");
                        BigDecimal codproduto = itensVO.asBigDecimal("CODPROD");
                        String codunidade = itensVO.asString("CODVOL");
                        BigDecimal percdesconto = itensVO.asBigDecimalOrZero("PERCDESC");
                        BigDecimal qtdnegociada = itensVO.asBigDecimal("QTDNEG");
                        BigDecimal vlrtotal = itensVO.asBigDecimal("VLRTOT");
                        BigDecimal vlrunitario = itensVO.asBigDecimal("VLRUNIT");
                        BigDecimal atualEstoque = itensVO.asBigDecimal("ATUALESTOQUE");
                        BigDecimal codlocalorig = itensVO.asBigDecimal("CODLOCALORIG");
                        String usoprod = itensVO.asString("USOPROD");

                        EntityFacade dwfFacadeIte = EntityFacadeFactory.getDWFFacade();
                        DynamicVO itemNota = (DynamicVO) dwfFacadeIte.getDefaultValueObjectInstance("ItemNota");
                        itemNota.setProperty("NUNOTA", nunota);
                        itemNota.setProperty("CODPROD", codproduto);
                        itemNota.setProperty("CODVOL", codunidade);
                        itemNota.setProperty("PERCDESC", percdesconto);
                        itemNota.setProperty("QTDNEG", qtdnegociada);
                        itemNota.setProperty("VLRTOT", vlrtotal);
                        itemNota.setProperty("VLRUNIT", vlrunitario);
                        itemNota.setProperty("ATUALESTOQUE", atualEstoque);
                        itemNota.setProperty("CODLOCALORIG", codlocalorig);
                        itemNota.setProperty("USOPROD", usoprod);
                        itemNota.setProperty("AD_CODLANCNOTA", codlancnotaItem);
                        itemNota.setProperty("AD_CODIMP", codimportadorItem);
                        itemNota.setProperty("AD_CODITEM", codItem);
                        dwfFacadeIte.createEntity("ItemNota", (EntityVO) itemNota);

                    }

                    // Refazer Impostos
                    JapeSession.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);

                    ImpostosHelpper impostosHelper = new ImpostosHelpper();
                    impostosHelper.setForcarRecalculo(true);
                    impostosHelper.carregarNota(nunota);
                    impostosHelper.calcularImpostos(nunota);// IPI, ICMS e etc..
                    impostosHelper.totalizarNota(nunota);
                    impostosHelper.salvarNota();

                    // Refazer Financeiro
                    BigDecimal atualizaFinanceiro = NativeSql.getBigDecimal("ATUALFIN", "TGFTOP", "CODTIPOPER = ? ", new Object[]{codtipooperacao});
                    if (!(atualizaFinanceiro.compareTo(BigDecimal.ZERO) == 0)) {
                        CentralFinanceiro financeiro = new CentralFinanceiro();
                        financeiro.inicializaNota(nunota);
                        financeiro.refazerFinanceiro();
                    }
                }
            }

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println("Log de erro Importador: " + sw.toString());

            inserirErroLOG("Erro na criação da Nota. Verifique o ID Importação = " + idImportador + " ERRO: " + e.getMessage(), codImportador);
        }
    }
}
