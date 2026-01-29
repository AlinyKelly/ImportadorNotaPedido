package br.com.sankhya.ce.botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.ws.ServiceContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.csv.*;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static br.com.sankhya.ce.utilitariosJava.UtilsJava.converterValorMonetario;
import static br.com.sankhya.ce.utilitariosJava.UtilsJava.getDhAtual;
import static br.com.sankhya.ce.utilitariosJava.UtilsJava.inserirErroLOG;
import static br.com.sankhya.ce.utilitariosJava.UtilsJava.stringToTimeStamp;
import static br.com.sankhya.ce.utilitariosJava.UtilsJava.toBigDecimal;

public class ImportadorDocumentos implements AcaoRotinaJava {
    private BigDecimal codImportador;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        System.out.println("Botao de acao Importador Portal Iniciado");

        //Controle em memória dos IDs já validados no banco
        Set<String> idsImportadorJaVerificados = new HashSet<>();
        Map<String, String> idsImportadorExistentesBanco = new HashMap<>();

        JapeSession.SessionHandle hnd = null;

        Registro[] linhasSelecionadas = contextoAcao.getLinhas();

        // ultimaLinhaJson será utilizo para exibir a linha em que ocorreu o errro.
        LinhaCsv ultimaLinhaCsv = null;

        BigDecimal codlancnota = null;

        String idImportadorControle = null;

        try {

            for (Registro linha : linhasSelecionadas) {

                codImportador = (BigDecimal) linha.getCampo("CODIMP");

                byte[] data = (byte[]) linha.getCampo("ARQUIVO");

                ServiceContext ctx = ServiceContext.getCurrent();
                File file = new File(ctx.getTempFolder(), "IMPORTADOR" + System.currentTimeMillis());
                FileUtils.writeByteArrayToFile(file, data);
                validarCSV(file);

                hnd = JapeSession.open();

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

                    for (CSVRecord record : parser) {

                        LinhaCsv json = trataLinha(record);
                        ultimaLinhaCsv = json;

                        if (json == null) {
                            continue;
                        }

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

                            continue; // pula para o próximo CSVRecord
                        }


                        // Começar a buscar os valores do CSV para inserir na tabela
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
                        BigDecimal cidade = toBigDecimal(json.getCidade());

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
                                cabecalho.setCampo("DTALTER", getDhAtual());
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
                                cabecalho.setCampo("CODCID", cidade);
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
                            inserirErroLOG("ID Importação = " + idImportadorPlanilha + "ERRO:" + e.getMessage() + "\nInconsistência na linha:  \n" + record.getRecordNumber() + "\n" + ultimaLinhaCsv, codImportador);
                        }
                    }
                }
            }

            System.out.println("Botao de acao Importador Portal Finalizado");

            contextoAcao.setMensagemRetorno("Importação Finalizada! ");

        } catch (Exception e) {

            inserirErroLOG("ERRO:" + e.getMessage() + "\nInconsistência na linha : \n" + ultimaLinhaCsv, codImportador);

        } finally {
            JapeSession.close(hnd);
        }

    }

    private LinhaCsv trataLinha(CSVRecord record) throws MGEModelException {
        int TOTAL_COLUNAS = 25;

        if (record.size() < TOTAL_COLUNAS) {
            inserirErroLOG(
                    "Linha com colunas insuficientes (" + record.size() + " colunas). Conteúdo: " + record.toString(),
                    codImportador
            );
            return null;
        }

        List<String> filtradas = new ArrayList<>();

        for (int i = 0; i < TOTAL_COLUNAS; i++) {
            String valor = record.get(i);

            if (valor == null || valor.trim().isEmpty()) {
                filtradas.add(null);
            } else {
                filtradas.add(valor.trim());
            }
        }

        return new LinhaCsv(
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

    private void validarCSV(File file) throws Exception {
        int TOTAL_COLUNAS = 25;
        int linha = 1;

        try (
                Reader reader = new InputStreamReader(new FileInputStream(file), "UTF-8");
                CSVParser parser = CSVFormat.DEFAULT
                        .withDelimiter(';')
                        .withQuote('"')
                        .withFirstRecordAsHeader()
                        .withIgnoreEmptyLines()
                        .parse(reader)
        ) {

            for (CSVRecord record : parser) {
                linha++;

                if (record.size() < TOTAL_COLUNAS) {
                    inserirErroLOG(
                            "Erro no CSV na linha " + linha +
                                    ". Esperado " + TOTAL_COLUNAS +
                                    " colunas, encontrado " + record.size(),
                            codImportador
                    );
//                    throw new MGEModelException(
//                            "Erro no CSV na linha " + linha +
//                                    ". Esperado " + TOTAL_COLUNAS +
//                                    " colunas, encontrado " + record.size()
//                    );
                }
            }
        }
    }
}
