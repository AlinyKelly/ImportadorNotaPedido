package br.com.sankhya.ce.botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.ws.ServiceContext;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static br.com.sankhya.ce.utilitariosJava.UtilsJava.*;

public class ImportadorNotasPedidos implements AcaoRotinaJava {
    private BigDecimal codImportador;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        System.out.println("Botao de acao Importador Portal Iniciado");

        //Controle em memória dos IDs já validados no banco
        Set<String> idsImportadorJaVerificados = new HashSet<>();
        Map<String, String> idsImportadorExistentesBanco = new HashMap<>();

        JapeSession.SessionHandle hnd = null;

        Registro[] linhasSelecionadas = contextoAcao.getLinhas();

        LinhaJson ultimaLinhaJson = null;

        BigDecimal codlancnota = null;

        String idImportadorControle = null;

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

                        String idImportadorPlanilha = json.getIdImportador();
                        BigDecimal sequencia = toBigDecimal(json.getSequencia());

                        //Verificar se o IDIMPORTADOR existe na tabela AD_IMPCABITEDET, se exitir não insere o lançamento
                        String buscaIdImp = null;

                        if (!idsImportadorJaVerificados.contains(idImportadorPlanilha)) {
                            buscaIdImp = NativeSql.getString("IMP.CODIMP", "AD_IMPCABITEDET IMP", "IMP.IDIMPORTADOR = ?", new Object[]{idImportadorPlanilha});

                            if (buscaIdImp != null) {
                                idsImportadorExistentesBanco.put(idImportadorPlanilha, buscaIdImp);
                            }

                            idsImportadorJaVerificados.add(idImportadorPlanilha);
                        }

                        if (idsImportadorExistentesBanco.containsKey(idImportadorPlanilha)) {

                            String mensagemErro =
                                    "ID Importador: " + idImportadorPlanilha +
                                            " já foi importado anteriormente. Código Importação: " +
                                            idsImportadorExistentesBanco.get(idImportadorPlanilha) + " Sequência do Item: " + sequencia;

                            inserirErroLOG(mensagemErro, codImportador);

                            line = br.readLine();
                            continue;
                        }

                        //Item
                        BigDecimal codproduto = toBigDecimal(json.getCodproduto());
                        String codunidade = json.getCodunidade();
                        BigDecimal percdesconto = toBigDecimal(json.getPercdesconto());
                        BigDecimal qtdnegociada = converterValorMonetario(json.getQtdnegociada());
                        BigDecimal vlrtotal = converterValorMonetario(json.getVlrtotal());
                        BigDecimal vlrunitario = converterValorMonetario(json.getVlrunitario());
                        BigDecimal codLocalOrigem = toBigDecimal(json.getCodLocalOrig());
                        String usoProd = NativeSql.getString("PRO.USOPROD", "TGFPRO PRO", "PRO.CODPROD = ?", new Object[]{codproduto});

                        //Cabecalho
                        BigDecimal codempresa = toBigDecimal(json.getCodempresa());
                        BigDecimal codparceiro = toBigDecimal(json.getCodparceiro());
                        BigDecimal codtipooperacao = toBigDecimal(json.getCodtipooperacao());
                        BigDecimal codtiponegociacao = toBigDecimal(json.getCodtiponegociacao());
                        Timestamp dataalteracaoCab = stringToTimeStamp(json.getDataalteracaoCab());
                        Timestamp datanegociacao = stringToTimeStamp(json.getDatanegociacao());
                        BigDecimal nronota = toBigDecimal(json.getNronota());
                        String tipomovimento = NativeSql.getString("TOP1.TIPMOV", "TGFTOP TOP1", "TOP1.CODTIPOPER = ?", new Object[]{codtipooperacao});
                        String atualEstoque = NativeSql.getString("TOP2.ATUALEST", "TGFTOP TOP2", "TOP2.CODTIPOPER = ?", new Object[]{codtipooperacao});
                        BigDecimal vlrdescontototal = converterValorMonetario(json.getVlrdescontototal());
                        BigDecimal vlrnota = converterValorMonetario(json.getVlrnota());
                        String serienota = json.getSerienota();
                        String observacao = json.getObservacao();
                        BigDecimal centroresultados = toBigDecimal(json.getCentroresultados());
                        BigDecimal natureza = toBigDecimal(json.getNatureza());
                        BigDecimal projeto = toBigDecimal(json.getProjeto());
                        BigDecimal contrato = toBigDecimal(json.getContrato());
                        BigDecimal vendedor = toBigDecimal(json.getVendedor());

                        BigDecimal atualizaEstoque = BigDecimal.ZERO;

                        if ("E".equals(atualEstoque)) {
                            atualizaEstoque = BigDecimal.ONE;
                        } else if ("B".equals(atualEstoque)) {
                            atualizaEstoque = new BigDecimal(-1);
                        }

                        boolean novaNota = !idImportadorPlanilha.equals(idImportadorControle);

                        try {

                            if (novaNota) {
                                Registro cabecalho = contextoAcao.novaLinha("AD_IMPCABITEDET");
                                cabecalho.setCampo("IDIMPORTADOR", idImportadorPlanilha);
                                cabecalho.setCampo("CODIMP", codImportador);
                                cabecalho.setCampo("CODEMP", codempresa);
                                cabecalho.setCampo("CODPARC", codparceiro);
                                cabecalho.setCampo("CODTIPOPER", codtipooperacao);
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
                                cabecalho.setCampo("CODVEND", vendedor);
                                cabecalho.save();

                                codlancnota = (BigDecimal) cabecalho.getCampo("CODLANCNOTA");

                                // Atualiza controles
                                idImportadorControle = idImportadorPlanilha;

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
                            itens.setCampo("ATUALESTOQUE", atualizaEstoque);
                            itens.setCampo("CODLOCALORIG", codLocalOrigem);
                            itens.setCampo("USOPROD", usoProd);
                            itens.save();

                        } catch (Exception e) {
                            inserirErroLOG("ID Importação = " + idImportadorPlanilha + "ERRO:" + e.getMessage() + "\nErro na linha:  \n" + ultimaLinhaJson, codImportador);
                        }

                        line = br.readLine();
                    }

                }

            }

            System.out.println("Botao de acao Importador Portal Finalizado");

            contextoAcao.setMensagemRetorno("Importação Finalizada! ");

        } catch (Exception e) {

            inserirErroLOG("ERRO:" + e.getMessage() + "\nErro na linha:  \n" + ultimaLinhaJson, codImportador);

        } finally {
            JapeSession.close(hnd);
        }

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
