package br.com.alura.screenmatch.service;
// usa o <T> quando nao sabe que tipo de dados vou receber.
// cria uma classe para o json for transformado.

public interface IConverteDados {
    <T> T obterDados (String json, Class<T> classe);

}
