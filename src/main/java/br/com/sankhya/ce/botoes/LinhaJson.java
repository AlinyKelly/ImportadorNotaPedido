package br.com.sankhya.ce.botoes;

public class LinhaJson {
    private final String sequencia;
    private final String codproduto;
    private final String codunidade;
    private final String percdesconto;
    private final String qtdnegociada;
    private final String vlrtotal;
    private final String vlrunitario;
    private final String dataalteracaoItem;
    private final String codempresa;
    private final String codparceiro;
    private final String codtipooperacao;
    private final String datahoraoperacao;
    private final String codtiponegociacao;
    private final String dataalteracaoCab;
    private final String datanegociacao;
    private final String nronota;
    private final String tipomovimento;
    private final String vlrdescontototal;
    private final String vlrnota;
    private final String serienota;
    private final String observacao;
    private final String centroresultados;
    private final String natureza;
    private final String projeto;
    private final String contrato;

    public LinhaJson(String sequencia, String codproduto, String codunidade, String percdesconto, String qtdnegociada, String vlrtotal, String vlrunitario, String dataalteracaoItem, String codempresa, String codparceiro, String codtipooperacao, String datahoraoperacao, String codtiponegociacao, String dataalteracaoCab, String datanegociacao, String nronota, String tipomovimento, String vlrdescontototal, String vlrnota, String serienota, String observacao, String centroresultados, String natureza, String projeto, String contrato) {
        this.sequencia = sequencia;
        this.codproduto = codproduto;
        this.codunidade = codunidade;
        this.percdesconto = percdesconto;
        this.qtdnegociada = qtdnegociada;
        this.vlrtotal = vlrtotal;
        this.vlrunitario = vlrunitario;
        this.dataalteracaoItem = dataalteracaoItem;
        this.codempresa = codempresa;
        this.codparceiro = codparceiro;
        this.codtipooperacao = codtipooperacao;
        this.datahoraoperacao = datahoraoperacao;
        this.codtiponegociacao = codtiponegociacao;
        this.dataalteracaoCab = dataalteracaoCab;
        this.datanegociacao = datanegociacao;
        this.nronota = nronota;
        this.tipomovimento = tipomovimento;
        this.vlrdescontototal = vlrdescontototal;
        this.vlrnota = vlrnota;
        this.serienota = serienota;
        this.observacao = observacao;
        this.centroresultados = centroresultados;
        this.natureza = natureza;
        this.projeto = projeto;
        this.contrato = contrato;
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

    public String getDataalteracaoItem() {
        return dataalteracaoItem;
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

    public String getDatahoraoperacao() {
        return datahoraoperacao;
    }

    public String getCodtiponegociacao() {
        return codtiponegociacao;
    }

    public String getDataalteracaoCab() {
        return dataalteracaoCab;
    }

    public String getDatanegociacao() {
        return datanegociacao;
    }

    public String getNronota() {
        return nronota;
    }

    public String getTipomovimento() {
        return tipomovimento;
    }

    public String getVlrdescontototal() {
        return vlrdescontototal;
    }

    public String getVlrnota() {
        return vlrnota;
    }

    public String getSerienota() {
        return serienota;
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

    @Override
    public String toString() {
        return "LinhaJson{" +
                "sequencia='" + sequencia + '\'' +
                ", codproduto='" + codproduto + '\'' +
                ", nronota='" + nronota + '\'' +
                '}';
    }
}
