package br.com.sankhya.ce.botoes;

public class LinhaJson {
    private final String sequencia;
    private final String codproduto;
    private final String codunidade;
    private final String percdesconto;
    private final String qtdnegociada;
    private final String vlrtotal;
    private final String vlrunitario;
    private final String atualizaEstoque;
    private final String codLocalOrig;
    private final String usoProd;
    private final String nronota;
    private final String serienota;
    private final String tipomovimento;
    private final String codempresa;
    private final String codparceiro;
    private final String codtipooperacao;
    private final String codtiponegociacao;
    private final String dataalteracaoCab;
    private final String datanegociacao;
    private final String vlrdescontototal;
    private final String vlrnota;
    private final String observacao;
    private final String centroresultados;
    private final String natureza;
    private final String projeto;
    private final String contrato;

    public LinhaJson(String sequencia, String codproduto, String codunidade, String percdesconto, String qtdnegociada, String vlrtotal, String vlrunitario, String atualizaEstoque, String codLocalOrig, String usoProd, String nronota, String serienota, String tipomovimento, String codempresa, String codparceiro, String codtipooperacao, String codtiponegociacao, String dataalteracaoCab, String datanegociacao, String vlrdescontototal, String vlrnota, String observacao, String centroresultados, String natureza, String projeto, String contrato) {
        this.sequencia = sequencia;
        this.codproduto = codproduto;
        this.codunidade = codunidade;
        this.percdesconto = percdesconto;
        this.qtdnegociada = qtdnegociada;
        this.vlrtotal = vlrtotal;
        this.vlrunitario = vlrunitario;
        this.atualizaEstoque = atualizaEstoque;
        this.codLocalOrig = codLocalOrig;
        this.usoProd = usoProd;
        this.nronota = nronota;
        this.serienota = serienota;
        this.tipomovimento = tipomovimento;
        this.codempresa = codempresa;
        this.codparceiro = codparceiro;
        this.codtipooperacao = codtipooperacao;
        this.codtiponegociacao = codtiponegociacao;
        this.dataalteracaoCab = dataalteracaoCab;
        this.datanegociacao = datanegociacao;
        this.vlrdescontototal = vlrdescontototal;
        this.vlrnota = vlrnota;
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

    public String getAtualizaEstoque() {
        return atualizaEstoque;
    }

    public String getCodLocalOrig() {
        return codLocalOrig;
    }

    public String getUsoProd() {
        return usoProd;
    }

    public String getNronota() {
        return nronota;
    }

    public String getSerienota() {
        return serienota;
    }

    public String getTipomovimento() {
        return tipomovimento;
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

    public String getDataalteracaoCab() {
        return dataalteracaoCab;
    }

    public String getDatanegociacao() {
        return datanegociacao;
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

    @Override
    public String toString() {
        return "LinhaJson{" +
                "sequencia='" + sequencia + '\'' +
                ", codproduto='" + codproduto + '\'' +
                ", nronota='" + nronota + '\'' +
                '}';
    }
}
