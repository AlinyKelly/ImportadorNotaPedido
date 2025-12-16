package br.com.sankhya.ce.botoes;

public class LinhaJsonFin {
    private final String codEmp;
    private final String nunota;
    private final String numNota;
    private final String serieNota;
    private final String desdobramento;
    private final String vlrDesdobramento;
    private final String parceiro;
    private final String top;
    private final String dhTOP;
    private final String banco;
    private final String conta;
    private final String natureza;
    private final String centroResultado;
    private final String vendedor;
    private final String tipoTitulo;
    private final String historico;
    private final String recDesp;
    private final String provisao;
    private final String origem;
    private final String sequencia;
    private final String dtNegociacao;
    private final String dhMov;
    private final String dtVencInic;
    private final String dtVencimento;
    private final String dtEntSai;
    private final String dtAlteracao;

    public LinhaJsonFin(String codEmp, String nunota, String numNota, String serieNota, String desdobramento, String vlrDesdobramento, String parceiro, String top, String dhTOP, String banco, String conta, String natureza, String centroResultado, String vendedor, String tipoTitulo, String historico, String recDesp, String provisao, String origem, String sequencia, String dtNegociacao, String dhMov, String dtVencInic, String dtVencimento, String dtEntSai, String dtAlteracao) {
        this.codEmp = codEmp;
        this.nunota = nunota;
        this.numNota = numNota;
        this.serieNota = serieNota;
        this.desdobramento = desdobramento;
        this.vlrDesdobramento = vlrDesdobramento;
        this.parceiro = parceiro;
        this.top = top;
        this.dhTOP = dhTOP;
        this.banco = banco;
        this.conta = conta;
        this.natureza = natureza;
        this.centroResultado = centroResultado;
        this.vendedor = vendedor;
        this.tipoTitulo = tipoTitulo;
        this.historico = historico;
        this.recDesp = recDesp;
        this.provisao = provisao;
        this.origem = origem;
        this.sequencia = sequencia;
        this.dtNegociacao = dtNegociacao;
        this.dhMov = dhMov;
        this.dtVencInic = dtVencInic;
        this.dtVencimento = dtVencimento;
        this.dtEntSai = dtEntSai;
        this.dtAlteracao = dtAlteracao;
    }

    public String getCodEmp() {
        return codEmp;
    }

    public String getNunota() {
        return nunota;
    }

    public String getNumNota() {
        return numNota;
    }

    public String getSerieNota() {
        return serieNota;
    }

    public String getDesdobramento() {
        return desdobramento;
    }

    public String getVlrDesdobramento() {
        return vlrDesdobramento;
    }

    public String getParceiro() {
        return parceiro;
    }

    public String getTop() {
        return top;
    }

    public String getDhTOP() {
        return dhTOP;
    }

    public String getBanco() {
        return banco;
    }

    public String getConta() {
        return conta;
    }

    public String getNatureza() {
        return natureza;
    }

    public String getCentroResultado() {
        return centroResultado;
    }

    public String getVendedor() {
        return vendedor;
    }

    public String getTipoTitulo() {
        return tipoTitulo;
    }

    public String getHistorico() {
        return historico;
    }

    public String getRecDesp() {
        return recDesp;
    }

    public String getProvisao() {
        return provisao;
    }

    public String getOrigem() {
        return origem;
    }

    public String getSequencia() {
        return sequencia;
    }

    public String getDtNegociacao() {
        return dtNegociacao;
    }

    public String getDhMov() {
        return dhMov;
    }

    public String getDtVencInic() {
        return dtVencInic;
    }

    public String getDtVencimento() {
        return dtVencimento;
    }

    public String getDtEntSai() {
        return dtEntSai;
    }

    public String getDtAlteracao() {
        return dtAlteracao;
    }

    @Override
    public String toString() {
        return "LinhaJsonFin{" +
                "codEmp='" + codEmp + '\'' +
                ", numNota='" + numNota + '\'' +
                ", serieNota='" + serieNota + '\'' +
                ", desdobramento='" + desdobramento + '\'' +
                '}';
    }
}
