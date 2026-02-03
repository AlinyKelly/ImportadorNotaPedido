package br.com.sankhya.ce.botoes;

public class LinhaCsv {
    private final String idImportador;
    private final String sequencia;
    private final String codproduto;
    private final String codunidade;
    private final String percdesconto;
    private final String qtdnegociada;
    private final String vlrtotal;
    private final String vlrunitario;
    private final String codLocalOrig;
    private final String nronota;
    private final String serienota;
    private final String codempresa;
    private final String codparceiro;
    private final String codtipooperacao;
    private final String codtiponegociacao;
    private final String datanegociacao;
    private final String dataHoraFaturamento;
    private final String dataEntradasaida;
    private final String dataMovimento;
    private final String vlrdescontototal;
    private final String vlrnota;
    private final String observacao;
    private final String centroresultados;
    private final String natureza;
    private final String projeto;
    private final String contrato;
    private final String vendedor;
    private final String cidade;
    private final String nroNfse;

    public LinhaCsv(String idImportador, String sequencia, String codproduto, String codunidade, String percdesconto, String qtdnegociada, String vlrtotal, String vlrunitario, String codLocalOrig, String nronota, String serienota, String codempresa, String codparceiro, String codtipooperacao, String codtiponegociacao, String datanegociacao, String dataHoraFaturamento, String dataEntradasaida, String dataMovimento, String vlrdescontototal, String vlrnota, String observacao, String centroresultados, String natureza, String projeto, String contrato, String vendedor, String cidade, String nroNfse) {
        this.idImportador = idImportador;
        this.sequencia = sequencia;
        this.codproduto = codproduto;
        this.codunidade = codunidade;
        this.percdesconto = percdesconto;
        this.qtdnegociada = qtdnegociada;
        this.vlrtotal = vlrtotal;
        this.vlrunitario = vlrunitario;
        this.codLocalOrig = codLocalOrig;
        this.nronota = nronota;
        this.serienota = serienota;
        this.codempresa = codempresa;
        this.codparceiro = codparceiro;
        this.codtipooperacao = codtipooperacao;
        this.codtiponegociacao = codtiponegociacao;
        this.datanegociacao = datanegociacao;
        this.dataHoraFaturamento = dataHoraFaturamento;
        this.dataEntradasaida = dataEntradasaida;
        this.dataMovimento = dataMovimento;
        this.vlrdescontototal = vlrdescontototal;
        this.vlrnota = vlrnota;
        this.observacao = observacao;
        this.centroresultados = centroresultados;
        this.natureza = natureza;
        this.projeto = projeto;
        this.contrato = contrato;
        this.vendedor = vendedor;
        this.cidade = cidade;
        this.nroNfse = nroNfse;
    }

    public String getIdImportador() {
        return idImportador;
    }

    public String getSequencia() {
        return sequencia;
    }

    public String getCodproduto() {
        return codproduto;
    }

    public String getCodunidade() {
        return codunidade;
    }

    public String getPercdesconto() {
        return percdesconto;
    }

    public String getQtdnegociada() {
        return qtdnegociada;
    }

    public String getVlrtotal() {
        return vlrtotal;
    }

    public String getVlrunitario() {
        return vlrunitario;
    }

    public String getCodLocalOrig() {
        return codLocalOrig;
    }

    public String getNronota() {
        return nronota;
    }

    public String getSerienota() {
        return serienota;
    }

    public String getCodempresa() {
        return codempresa;
    }

    public String getCodparceiro() {
        return codparceiro;
    }

    public String getCodtipooperacao() {
        return codtipooperacao;
    }

    public String getCodtiponegociacao() {
        return codtiponegociacao;
    }

    public String getDatanegociacao() {
        return datanegociacao;
    }

    public String getDataHoraFaturamento() {
        return dataHoraFaturamento;
    }

    public String getDataEntradasaida() {
        return dataEntradasaida;
    }

    public String getDataMovimento() {
        return dataMovimento;
    }

    public String getVlrdescontototal() {
        return vlrdescontototal;
    }

    public String getVlrnota() {
        return vlrnota;
    }

    public String getObservacao() {
        return observacao;
    }

    public String getCentroresultados() {
        return centroresultados;
    }

    public String getNatureza() {
        return natureza;
    }

    public String getProjeto() {
        return projeto;
    }

    public String getContrato() {
        return contrato;
    }

    public String getVendedor() {
        return vendedor;
    }

    public String getCidade() {
        return cidade;
    }

    public String getNroNfse() { return nroNfse; }

    @Override
    public String toString() {
        return "LinhaJson{" +
                "sequencia='" + sequencia + '\'' +
                ", codproduto='" + codproduto + '\'' +
                ", nronota='" + nronota + '\'' +
                '}';
    }
}
