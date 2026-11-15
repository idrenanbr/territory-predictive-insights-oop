package dominio;

public abstract class ComponenteTerritorial {

    public enum Status {
        OPERACIONAL("Operacional"),
        DEGRADADO("Degradado"),
        INDISPONIVEL("Indisponível");

        private final String descricao;

        Status(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }

    private final String id;
    private final String nome;
    private final String regiao;
    private Status status;

    protected ComponenteTerritorial(String id, String nome, String regiao, Status statusInicial) {
        this.id = id;
        this.nome = nome;
        this.regiao = regiao;
        this.status = statusInicial;
    }

    public abstract Status verificarStatus();

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getRegiao() {
        return regiao;
    }

    public Status getStatus() {
        return status;
    }

    protected void atualizarStatus(Status status) {
        this.status = status;
    }
}
