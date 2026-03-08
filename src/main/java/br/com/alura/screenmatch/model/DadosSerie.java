package br.com.alura.screenmatch.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// jsonAlias e tipo um apelido que vou dar para o dado que quero buscar no json original,
//com isso ele vai procurar pelo nome e salvar na variavel.
@JsonIgnoreProperties(ignoreUnknown = true)
public record DadosSerie(@JsonAlias("Title") String titulo,
                         @JsonAlias("totalSeasons") String totalTemporadas,
                         @JsonAlias("imdbRating") String avaliacao) {
}
