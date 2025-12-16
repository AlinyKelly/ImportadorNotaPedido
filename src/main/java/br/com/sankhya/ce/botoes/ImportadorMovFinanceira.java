package br.com.sankhya.ce.botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.ws.ServiceContext;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static br.com.sankhya.ce.utilitariosJava.UtilsJava.*;
import static br.com.sankhya.ce.utilitariosJava.UtilsJava.converterValorMonetario;
import static br.com.sankhya.ce.utilitariosJava.UtilsJava.inserirErroLOG;
import static br.com.sankhya.ce.utilitariosJava.UtilsJava.stringToTimeStamp;
import static br.com.sankhya.ce.utilitariosJava.UtilsJava.toBigDecimal;

public class ImportadorMovFinanceira implements AcaoRotinaJava {
    private BigDecimal codImportador;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        System.out.println("Botao de acao Importador Mov. Financeira");

        JapeSession.SessionHandle hnd = null;

        Registro[] linhasSelecionadas = contextoAcao.getLinhas();

        LinhaJsonFin ultimaLinhaJson = null;

        BigDecimal codlancfin = null;

        try {

            for (Registro linha : linhasSelecionadas) {
                int count = 0;

                codImportador = (BigDecimal) linha.getCampo("CODIMP");

                byte[] data = (byte[]) linha.getCampo("ARQUIVO");

                ServiceContext ctx = ServiceContext.getCurrent();
                File file = new File(ctx.getTempFolder(), "IMPORTADOR" + System.currentTimeMillis());
                FileUtils.writeByteArrayToFile(file, data);

                hnd = JapeSession.open();

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line = br.readLine();

                    while (line != null) {
                        if (count == 0) {
                            count++;
                            line = br.readLine();
                            continue;
                        }

                        count++;

                        if (line.contains("__end_fileinformation__")) {
                            line = getReplaceFileInfo(line);
                        }

                        LinhaJsonFin json = trataLinha(line);
                        ultimaLinhaJson = json;

                        // Financeiro
                        BigDecimal codEmp = toBigDecimal(json.getCodEmp());
                        BigDecimal nunota = toBigDecimal(json.getNunota());
                        BigDecimal numNota = toBigDecimal(json.getNumNota());
                        String serieNota = json.getSerieNota();
                        String desdobramento = json.getDesdobramento();
                        BigDecimal vlrDesdobramento = converterValorMonetario(json.getVlrDesdobramento());
                        BigDecimal parceiro = toBigDecimal(json.getParceiro());
                        BigDecimal top = toBigDecimal(json.getTop());
                        Timestamp dhTOP = stringToTimeStamp(json.getDhTOP());
                        BigDecimal banco = toBigDecimal(json.getBanco());
                        BigDecimal conta = toBigDecimal(json.getConta());
                        BigDecimal natureza = toBigDecimal(json.getNatureza());
                        BigDecimal cr = toBigDecimal(json.getCentroResultado());
                        BigDecimal vendedor = toBigDecimal(json.getVendedor());
                        BigDecimal tipoTitulo = toBigDecimal(json.getTipoTitulo());
                        String historico = json.getHistorico();
                        BigDecimal recDesp = toBigDecimal(json.getRecDesp());
                        String provisao = json.getProvisao();
                        String origem = json.getOrigem();
                        BigDecimal sequencia = toBigDecimal(json.getSequencia());
                        Timestamp dtNegociacao = stringToTimeStamp(json.getDtNegociacao());
                        Timestamp dhMov = stringToTimeStamp(json.getDhMov());
                        Timestamp dtVencInic = stringToTimeStamp(json.getDtVencInic());
                        Timestamp dtVencimento = stringToTimeStamp(json.getDtVencimento());
                        Timestamp dtEntSai = stringToTimeStamp(json.getDtEntSai());
                        Timestamp dtAlteracao = stringToTimeStamp(json.getDtAlteracao());

                        try {
                            Registro financeiro = contextoAcao.novaLinha("AD_IMPFINDET");
                            financeiro.setCampo("CODIMP", codImportador);
                            financeiro.setCampo("CODEMP", codEmp);
                            financeiro.setCampo("NUMNOTA", numNota);
                            financeiro.setCampo("SERIENOTA", serieNota);
                            financeiro.setCampo("DTNEG", dtNegociacao);
                            financeiro.setCampo("DESDOBRAMENTO", desdobramento);
                            financeiro.setCampo("DHMOV", dhMov);
                            financeiro.setCampo("DTVENCINIC", dtVencInic);
                            financeiro.setCampo("DTVENC", dtVencimento);
                            financeiro.setCampo("CODPARC", parceiro);
                            financeiro.setCampo("CODTIPOPER", top);
                            financeiro.setCampo("DHTIPOPER", dhTOP);
                            financeiro.setCampo("CODBCO", banco);
                            financeiro.setCampo("CODCTABCOINT", conta);
                            financeiro.setCampo("CODNAT", natureza);
                            financeiro.setCampo("CODCENCUS", cr);
                            financeiro.setCampo("CODVEND", vendedor);
                            financeiro.setCampo("CODTIPTIT", tipoTitulo);
                            financeiro.setCampo("HISTORICO", historico);
                            financeiro.setCampo("RECDESP", recDesp);
                            financeiro.setCampo("PROVISAO", provisao);
                            financeiro.setCampo("ORIGEM", origem);
                            financeiro.setCampo("DTENTSAI", dtEntSai);
                            financeiro.setCampo("DTALTER", dtAlteracao);
                            financeiro.setCampo("SEQUENCIA", sequencia);
                            financeiro.setCampo("NUNOTA", nunota);
                            financeiro.setCampo("VLRDESDOB", vlrDesdobramento);
                            financeiro.save();

                        } catch (Exception e) {
                            MGEModelException.throwMe(e);
                        }

                        line = br.readLine();
                    }

                }

            }

            contextoAcao.setMensagemRetorno("Importação Finalizada! ");

        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            System.out.println("Log de erro Importador: " + sw.toString());

            inserirErroLOG("ERRO:" + e.getMessage() + "\nErro na linha  \n" + ultimaLinhaJson, codImportador);

        } finally {
            JapeSession.close(hnd);
        }

    }

    private LinhaJsonFin trataLinha(String linha) throws MGEModelException {
        // split preservando colunas vazias
        String[] cells;
        if (linha.contains(";")) {
            cells = linha.split(";(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        } else {
            cells = linha.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        }

        List<String> filtradas = new ArrayList<>();

        // Mantém a estrutura e converte "" → null
        for (String c : cells) {
            if (c == null || c.trim().isEmpty()) {
                filtradas.add(null);
            } else {
                filtradas.add(c.trim());
            }
        }

        if (filtradas.size() < 25) {
            inserirErroLOG(
                    "Linha com colunas insuficientes (" + filtradas.size() + " colunas): " + linha,
                    codImportador);
            return null;
        }

        return new LinhaJsonFin(
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
                filtradas.get(25)
        );
    }
}
