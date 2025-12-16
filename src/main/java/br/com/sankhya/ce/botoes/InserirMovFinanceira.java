package br.com.sankhya.ce.botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.Timestamp;

import static br.com.sankhya.ce.utilitariosJava.UtilsJava.*;

public class InserirMovFinanceira implements AcaoRotinaJava {
    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        Registro[] linhasSelecionadas = contextoAcao.getLinhas();
        BigDecimal codImportador = null;

        try {
            for (Registro linha : linhasSelecionadas) {
                BigDecimal codlancfin = (BigDecimal) linha.getCampo("CODLANFIN");
                codImportador = (BigDecimal) linha.getCampo("CODIMP");
                BigDecimal codEmp = (BigDecimal) linha.getCampo("CODEMP");
                BigDecimal nunota = (BigDecimal) linha.getCampo("NUNOTA");
                BigDecimal numNota = (BigDecimal) linha.getCampo("NUMNOTA");
                String serieNota = (String) linha.getCampo("SERIENOTA");
                String desdobramento = (String) linha.getCampo("DESDOBRAMENTO");
                String vlrDesdobramento = (String) linha.getCampo("VLRDESDOB");
                BigDecimal parceiro = (BigDecimal) linha.getCampo("CODPARC");
                BigDecimal top = (BigDecimal) linha.getCampo("CODTIPOPER");
                Timestamp dhTOP = (Timestamp) linha.getCampo("DHTIPOPER");
                BigDecimal banco = (BigDecimal) linha.getCampo("CODBCO");
                BigDecimal conta = (BigDecimal) linha.getCampo("CODCTABCOINT");
                BigDecimal natureza = (BigDecimal) linha.getCampo("CODNAT");
                BigDecimal cr = (BigDecimal) linha.getCampo("CODCENCUS");
                BigDecimal vendedor = (BigDecimal) linha.getCampo("CODVEND");
                BigDecimal tipoTitulo = (BigDecimal) linha.getCampo("CODTIPTIT");
                String historico = (String) linha.getCampo("HISTORICO");
                BigDecimal recDesp = (BigDecimal) linha.getCampo("RECDESP");
                String provisao = (String) linha.getCampo("PROVISAO");
                String origem = (String) linha.getCampo("ORTGEM");
                BigDecimal sequencia = (BigDecimal) linha.getCampo("SEQUENCIA");
                Timestamp dtNegociacao = (Timestamp) linha.getCampo("DTNEG");
                Timestamp dhMov = (Timestamp) linha.getCampo("DHMOV");
                Timestamp dtVencInic = (Timestamp) linha.getCampo("DTVENCINIC");
                Timestamp dtVencimento = (Timestamp) linha.getCampo("DTVENC");
                Timestamp dtEntSai = (Timestamp) linha.getCampo("DTENTSAI");
                Timestamp dtAlteracao = (Timestamp) linha.getCampo("DTALTER");

                EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
                DynamicVO financeiro = (DynamicVO) dwfFacade.getDefaultValueObjectInstance("Financeiro");
                financeiro.setProperty("CODEMP", codEmp);
                financeiro.setProperty("NUMNOTA", numNota);
                financeiro.setProperty("SERIENOTA", serieNota);
                financeiro.setProperty("DTNEG", dtNegociacao);
                financeiro.setProperty("DESDOBRAMENTO", desdobramento);
                financeiro.setProperty("DHMOV", dhMov);
                financeiro.setProperty("DTVENCINIC", dtVencInic);
                financeiro.setProperty("DTVENC", dtVencimento);
                financeiro.setProperty("CODPARC", parceiro);
                financeiro.setProperty("CODTIPOPER", top);
                financeiro.setProperty("DHTIPOPER", dhTOP);
                financeiro.setProperty("CODBCO", banco);
                financeiro.setProperty("CODCTABCOINT", conta);
                financeiro.setProperty("CODNAT", natureza);
                financeiro.setProperty("CODCENCUS", cr);
                financeiro.setProperty("CODVEND", vendedor);
                financeiro.setProperty("CODTIPTIT", tipoTitulo);
                financeiro.setProperty("HISTORICO", historico);
                financeiro.setProperty("RECDESP", recDesp);
                financeiro.setProperty("PROVISAO", provisao);
                financeiro.setProperty("ORIGEM", origem);
                financeiro.setProperty("DTENTSAI", dtEntSai);
                financeiro.setProperty("DTALTER", dtAlteracao);
                financeiro.setProperty("SEQUENCIA", sequencia);
                financeiro.setProperty("NUNOTA", nunota);
                financeiro.setProperty("VLRDESDOB", vlrDesdobramento);
                financeiro.setProperty("AD_CODLANFIN", codlancfin);
                financeiro.setProperty("AD_CODIMP", codImportador);
                dwfFacade.createEntity("Financeiro", (EntityVO) financeiro);

                BigDecimal nufin = financeiro.asBigDecimal("NUFIN");

                linha.setCampo("NUFIN", nufin);
            }
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println("Log de erro Importador: " + sw.toString());

            inserirErroLOG("Erro na criação do Financeiro. ERRO: " + e.getMessage(), codImportador);
        }


    }
}
