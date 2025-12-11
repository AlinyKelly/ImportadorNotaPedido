package br.com.sankhya.ce.botoes;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.jape.wrapper.fluid.FluidCreateVO;
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

public class ImportadorNotasPedidos implements AcaoRotinaJava {
    private BigDecimal codImportador;

    @Override
    public void doAction(ContextoAcao contextoAcao) throws Exception {
        System.out.println("Botao de acao Importador");

        JapeSession.SessionHandle hnd = null;

        Registro[] linhasSelecionadas = contextoAcao.getLinhas();

        LinhaJson ultimaLinhaJson = null;

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


                        try {

                            JapeWrapper cabecalhoDAO = JapeFactory.dao("AD_IMPCABITEDET");
                            FluidCreateVO saveCab = cabecalhoDAO.create();
                            saveCab.set("CODIMP", codImportador);
                            saveCab.set("CODEMP", codempresa);
                            saveCab.set("CODPARC", codparceiro);
                            saveCab.set("CODTIPOPER", codtipooperacao);
                            saveCab.set("DHTIPOPER", datahoraoperacao);
                            saveCab.set("CODTIPVENDA", codtiponegociacao);
                            saveCab.set("DTALTER", dataalteracaoCab);
                            saveCab.set("DTNEG", datanegociacao);
                            saveCab.set("NUMNOTA", nronota);
                            saveCab.set("TIPMOV", tipomovimento);
                            saveCab.set("SERIENOTA", serienota);
                            saveCab.set("VLRDESCTOT", vlrdescontototal);
                            saveCab.set("VLRNOTA", vlrnota);
                            saveCab.set("CODCENCUS", centroresultados);
                            saveCab.set("CODNAT", natureza);
                            saveCab.set("NUMCONTRATO", contrato);
                            saveCab.set("CODPROJ", projeto);
                            saveCab.set("OBSERVACAO", observacao);
                            DynamicVO save = saveCab.save();

                            BigDecimal codlancnota = save.asBigDecimal("CODLANCNOTA");

                            try {
                                JapeWrapper itemDAO = JapeFactory.dao("AD_IMPITEDET");
                                FluidCreateVO saveItem = itemDAO.create();
                                saveItem.set("CODLANCNOTA", codlancnota);
                                saveItem.set("CODIMP", codImportador);
                                saveItem.set("SEQUENCIA", sequencia);
                                saveItem.set("CODPROD", codproduto);
                                saveItem.set("CODVOL", codunidade);
                                saveItem.set("PERCDESC", percdesconto);
                                saveItem.set("QTDNEG", qtdnegociada);
                                saveItem.set("VLRTOT", vlrtotal);
                                saveItem.set("VLRUNIT", vlrunitario);
                                saveItem.set("DHALTER", dataalteracaoItem);
                                saveItem.save();

                            } catch (Exception e) {
                                MGEModelException.throwMe(e);
                            }

                        } catch (Exception e) {
                            MGEModelException.throwMe(e);
                        }

                    }

                    line = br.readLine();
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

    private void inserirErroLOG(String erro, BigDecimal codImportador) throws MGEModelException {

        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            JapeWrapper logDAO = JapeFactory.dao("AD_IMPLOG");
            DynamicVO save = logDAO.create()
                    .set("CODIMP", codImportador)
                    .set("ERRO", erro)
                    .set("DHERRO", getDhAtual())
                    .save();
        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }
    }

    public DynamicVO retornaVO(String instancia, String where) throws MGEModelException {

        JapeSession.SessionHandle sh = null;

        try {
            sh = JapeSession.open();
            JapeWrapper dao = JapeFactory.dao(instancia);
            return dao.findOne(where);

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(sh);
        }

        return null;
    }

    private String getReplaceFileInfo(String line) {
        String regex = "__start_fileinformation__.*__end_fileinformation__";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(line);
        return matcher.replaceAll("");
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

    public BigDecimal converterValorMonetario(String valor) {
        if (valor == null || valor.trim().isEmpty()) {
            return null;
        }

        valor = valor.replace("\"", "")
                .replace(".", "")
                .replace(",", ".");

        try {
            return new BigDecimal(valor);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal toBigDecimal(String valor) {
        // 1. Verifica se é null primeiro (EVITA O NullPointerException)
        if (valor == null) {
            return BigDecimal.ZERO;
        }

        // 2. Chama trim() e verifica se está vazio
        String valorTratado = valor.trim();
        if (valorTratado.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 3. Converte
        try {
            return new BigDecimal(valorTratado);
        } catch (NumberFormatException e) {
            // Logar o erro ou lançar uma exceção mais específica se o formato for inválido
            System.err.println("Erro de formato BigDecimal para o valor: " + valorTratado);
            return BigDecimal.ZERO; // Ou trate como erro de linha
        }
    }

    public Timestamp stringToTimeStamp(String str) {
        if (str == null || str.trim().isEmpty()) {
            return null;
        }

        try {
            DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            Date date = formatter.parse(str);
            return new Timestamp(date.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public Timestamp getDhAtual() {
        return new Timestamp(System.currentTimeMillis());
    }
}
