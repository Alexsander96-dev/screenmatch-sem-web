package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.DadosEpisodio;
import br.com.alura.screenmatch.model.DadosSerie;
import br.com.alura.screenmatch.model.DadosTemporada;
import br.com.alura.screenmatch.model.Episodio;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;

import javax.print.attribute.standard.Media;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {


        private Scanner leitura = new Scanner(System.in);

        private ConsumoApi consumo = new ConsumoApi();
        private final String ENDERECO = "https://www.omdbapi.com/?t=";
        private final String API_KEY = "&apikey=9c0d6e0";
        private ConverteDados conversor = new ConverteDados();


        public void exibiMenu () {
            // Pede o nome da serie e faz a primeira consulta na API para obter os dados gerais.
            System.out.println("Digite o nome da série para a busca: ");
            var nomeSerie = leitura.nextLine();
            var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
            DadosSerie dados = conversor.obterDados(json, DadosSerie.class);

            // Exibe no console os dados principais da serie retornados pela API.
            System.out.println(dados);

            // Lista que vai armazenar os dados de todas as temporadas da serie.
            List<DadosTemporada> temporadas = new ArrayList<>();

            // Busca cada temporada individualmente na API e adiciona na lista de temporadas.
            for (int i = 1; i <= Integer.parseInt(dados.totalTemporadas()); i++) {
                json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }

            // Mostra no console os dados completos de cada temporada.
            temporadas.forEach(System.out::println);

            // Percorre temporada por temporada para imprimir apenas os titulos dos episodios.
            for (int i = 0; i < Integer.parseInt(dados.totalTemporadas()); i++) {
                List<DadosEpisodio> episodiosTemporada = temporadas.get(i).episodios();
                for (int j = 0; j < episodiosTemporada.size() ; j++) {
                    System.out.println(episodiosTemporada.get(j).titulo());
                }
            }

            // Outra forma de percorrer: usa forEach aninhado para imprimir cada episodio.
            temporadas.forEach(t -> t.episodios().forEach(System.out::println));


            // Junta episodios de todas as temporadas em uma unica lista de DadosEpisodio.
            List<DadosEpisodio> dadosEpisodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream())
                    .collect(Collectors.toList());

//            // Mostra no console o título da seção
//            System.out.println("\n Top 10 episodios");
//
//// Cria uma stream a partir da lista de episódios
//            dadosEpisodios.stream()
//
//                    // Filtra episódios que possuem avaliação válida
//                    // (alguns episódios da API vêm com "N/A")
//                    .filter(e -> !e.avaliacao().equalsIgnoreCase("N/A"))
//
//                    // Ordena os episódios pela avaliação
//                    // Comparator.comparing usa o método avaliacao do record
//                    // reversed() coloca da maior nota para a menor
//                    .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
//
//                    // Limita o resultado aos 5 primeiros episódios
//                    .limit(10)
//                    .map(e -> e.titulo().toUpperCase())
//
//                    // Imprime cada episódio no console
//                    .forEach(System.out::println);

            // Converte os dados brutos dos episodios em objetos Episodio (com numero da temporada).
            List<Episodio> episodios = temporadas.stream()
                    .flatMap(t -> t.episodios().stream()
                        .map(d -> new Episodio(t.numero(), d))
                        ).collect(Collectors.toList());

            // Pede um trecho do titulo para buscar um episodio especifico.
            System.out.println("Digite o trecho do titulo do episódio: ");
            var trechoTitulo = leitura.nextLine();

            // Procura o primeiro episodio cujo titulo contenha o texto digitado (ignora maiusculas/minusculas).
            Optional<Episodio> episodioBuscado = episodios.stream()
                    .filter(e -> e.getTitulo().toUpperCase().contains(trechoTitulo.toUpperCase()))
                    .findFirst();

            // Exibe o resultado da busca, mostrando a temporada quando encontrar o episodio.
            if ( episodioBuscado.isPresent()){
                System.out.println("Episódio encontrado! ");
                System.out.println("Temporada: " + episodioBuscado.get().getTemporada());
            }else {
                System.out.println("Episódio Nao encontrado!");
            }
//
//            episodios.forEach(System.out::println);
//
//            System.out.println("A partir de que ano você deseja ver os episódios? ");
//            var ano = leitura.nextInt();
//            leitura.nextLine();
//
//            LocalDate dataBusca = LocalDate.of(ano, 1, 1);
//
//            DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//
//
//            episodios.stream()
//                    .filter(e -> e.getDataLancamento() != null && e.getDataLancamento().isAfter(dataBusca))
//                    .forEach(e -> System.out.println(
//                            "Temporada: " + e.getTemporada() +
//                            " Episódio: " + e.getTitulo() +
//                            "Data lançamento: " + e.getDataLancamento().format(formatador)
//                    ));
//

            // Filtra episodios com nota valida e calcula a media de avaliacao por temporada.
            Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                    .filter(e -> e.getAvaliacao() > 0.0)
                    .collect(Collectors.groupingBy(Episodio::getTemporada,
                            Collectors.averagingDouble(Episodio::getAvaliacao)));

            // Mostra o mapa no formato: temporada -> media de avaliacao.
            System.out.println(avaliacoesPorTemporada);

            // Gera estatisticas gerais das notas: media, maior, menor e quantidade de episodios avaliados.
            DoubleSummaryStatistics est = episodios.stream()
                    .filter(e -> e.getAvaliacao() > 0.0)
                    .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

            // Exibe os indicadores resumidos das avaliacoes.
            System.out.println("Média: " + est.getAverage());
            System.out.println("Melhor episódio: " + est.getMax());
            System.out.println("Pior episódio: " + est.getMin());
            System.out.println("Quantidade: " + est.getCount());

        }


}

