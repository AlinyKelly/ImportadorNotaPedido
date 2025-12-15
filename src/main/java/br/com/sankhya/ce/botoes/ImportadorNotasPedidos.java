package br.com.sankhya.ce.botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.ws.ServiceContext;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static br.com.sankhya.ce.utilitariosJava.UtilsJava.*;

public class ImportadorNotasPedidos implements AcaoRotinaJava {
    private BigDecimal codImportador;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        System.out.println("Botao de acao Importador");

        JapeSession.SessionHandle hnd = null;

        Registro[] linhasSelecionadas = contextoAcao.getLinhas();

        LinhaJson ultimaLinhaJson = null;

        BigDecimal codlancnota = null;

        BigDecimal nroNotaControle = null;
        String serieNotaControle = null;

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

                        LinhaJson json = trataLinha(line);
                        ultimaLinhaJson = json;

                        //Item
                        BigDecimal sequencia = toBigDecimal(json.getSequencia());
                        BigDecimal codproduto = toBigDecimal(json.getCodproduto());
                        String codunidade = json.getCodunidade();
                        BigDecimal percdesconto = toBigDecimal(json.getPercdesconto());
                        BigDecimal qtdnegociada = converterValorMonetario(json.getQtdnegociada());
                        BigDecimal vlrtotal = converterValorMonetario(json.getVlrtotal());
                        BigDecimal vlrunitario = converterValorMonetario(json.getVlrunitario());
                        Timestamp dataalteracaoItem = stringToTimeStamp(json.getDataalteracaoItem());

                        //Cabecalho
                        BigDecimal codempresa = toBigDecimal(json.getCodempresa());
                        BigDecimal codparceiro = toBigDecimal(json.getCodparceiro());
                        BigDecimal codtipooperacao = toBigDecimal(json.getCodtipooperacao());
                        Timestamp datahoraoperacao = stringToTimeStamp(json.getDatahoraoperacao());
                        BigDecimal codtiponegociacao = toBigDecimal(json.getCodtiponegociacao());
                        Timestamp dataalteracaoCab = stringToTimeStamp(json.getDataalteracaoCab());
                        Timestamp datanegociacao = stringToTimeStamp(json.getDatanegociacao());
                        BigDecimal nronota = toBigDecimal(json.getNronota());
                        String tipomovimento = json.getTipomovimento();
                        BigDecimal vlrdescontototal = converterValorMonetario(json.getVlrdescontototal());
                        BigDecimal vlrnota = converterValorMonetario(json.getVlrnota());
                        String serienota = json.getSerienota();
                        String observacao = json.getObservacao();
                        BigDecimal centroresultados = toBigDecimal(json.getCentroresultados());
                        BigDecimal natureza = toBigDecimal(json.getNatureza());
                        BigDecimal projeto = toBigDecimal(json.getProjeto());
                        BigDecimal contrato = toBigDecimal(json.getContrato());

                        System.out.println("Sequencia " + sequencia);
                        System.out.println("Nronota " + nronota);

                        boolean novaNota = nroNotaControle == null || nronota.compareTo(nroNotaControle) != 0 || !serienota.equals(serieNotaControle);

                        try {
                            System.out.println("Inserir linha do cabecalho");

                            if (novaNota) {
                                Registro cabecalho = contextoAcao.novaLinha("AD_IMPCABITEDET");
                                cabecalho.setCampo("CODIMP", codImportador);
                                cabecalho.setCampo("CODEMP", codempresa);
                                cabecalho.setCampo("CODPARC", codparceiro);
                                cabecalho.setCampo("CODTIPOPER", codtipooperacao);
                                cabecalho.setCampo("DHTIPOPER", datahoraoperacao);
                                cabecalho.setCampo("CODTIPVENDA", codtiponegociacao);
                                cabecalho.setCampo("DTALTER", dataalteracaoCab);
                                cabecalho.setCampo("DTNEG", datanegociacao);
                                cabecalho.setCampo("NUMNOTA", nronota);
                                cabecalho.setCampo("TIPMOV", tipomovimento);
                                cabecalho.setCampo("SERIENOTA", serienota);
                                cabecalho.setCampo("VLRDESCTOT", vlrdescontototal);
                                cabecalho.setCampo("VLRNOTA", vlrnota);
                                cabecalho.setCampo("CODCENCUS", centroresultados);
                                cabecalho.setCampo("CODNAT", natureza);
                                cabecalho.setCampo("NUMCONTRATO", contrato);
                                cabecalho.setCampo("CODPROJ", projeto);
                                cabecalho.setCampo("OBSERVACAO", observacao);
                                cabecalho.save();

                                codlancnota = (BigDecimal) cabecalho.getCampo("CODLANCNOTA");

                                // Atualiza controles
                                nroNotaControle = nronota;
                                serieNotaControle = serienota;

                            }
                            // Inserir Itens
                            Registro itens = contextoAcao.novaLinha("AD_IMPITEDET");
                            itens.setCampo("CODLANCNOTA", codlancnota);
                            itens.setCampo("CODIMP", codImportador);
                            itens.setCampo("SEQUENCIA", sequencia);
                            itens.setCampo("CODPROD", codproduto);
                            itens.setCampo("CODVOL", codunidade);
                            itens.setCampo("PERCDESC", percdesconto);
                            itens.setCampo("QTDNEG", qtdnegociada);
                            itens.setCampo("VLRTOT", vlrtotal);
                            itens.setCampo("VLRUNIT", vlrunitario);
                            itens.setCampo("DHALTER", dataalteracaoItem);
                            itens.save();

                        } catch (Exception e) {
                            MGEModelException.throwMe(e);
                        }

                        line = br.readLine();
                    }

                }

                System.out.println("Count for = " + count);
            }

            System.out.println("For terminando");

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

        System.out.println("Botao finalizado");
    }

    private LinhaJson trataLinha(String linha) throws MGEModelException {
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

        return new LinhaJson(
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
                filtradas.get(24)
        );
    }

}
