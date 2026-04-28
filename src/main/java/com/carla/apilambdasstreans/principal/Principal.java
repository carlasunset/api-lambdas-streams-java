package com.carla.apilambdasstreans.principal;

import com.carla.apilambdasstreans.model.DadosEpisodio;
import com.carla.apilambdasstreans.model.DadosSerie;
import com.carla.apilambdasstreans.model.DadosTemporada;
import com.carla.apilambdasstreans.model.Episodio;
import com.carla.apilambdasstreans.service.ConsumoAPI;
import com.carla.apilambdasstreans.service.ConverteDados;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private final Scanner LEITURA = new Scanner(System.in);

    private ConsumoAPI consumoAPI = new ConsumoAPI();

    private ConverteDados converteDados = new ConverteDados();

    private final String ENDERECO = "https://www.omdbapi.com/?t=";

    private final String API_KEY = "&apikey=548cbf7a";

    public void menu(){
        System.out.println("Digite o nome da série para busca");
        String nomeSerie = LEITURA.nextLine();

        String json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);

        DadosSerie dadosSerie = converteDados.obterDados(json, DadosSerie.class);
        System.out.println(dadosSerie);

        List<DadosTemporada> temporadas = new ArrayList<>();

        for (int i = 1; i <= dadosSerie.totalTemporadas(); i++) {
            json = consumoAPI.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + "&Season=" + i + API_KEY);
            DadosTemporada dadosTemporada = converteDados.obterDados(json, DadosTemporada.class);
            temporadas.add(dadosTemporada);
        }
        //temporadas.forEach(x -> x.episodios().forEach(e -> System.out.println(e.titulo())));

        List<DadosEpisodio> dadosEpisodios = temporadas.stream().flatMap(t -> t.episodios().stream()).collect(Collectors.toList());

        List<Episodio> episodios = temporadas.stream()
                .flatMap(t -> t.episodios().stream()
                        .map( e -> new Episodio(t.temporada(), e))).collect(Collectors.toList());

        episodios.forEach(System.out::println);

        System.out.print("A partir de que ano voce deseja ver os episódios?: ");
        int ano = LEITURA.nextInt();
        LEITURA.nextLine();

        LocalDate dataBusca = LocalDate.of(ano, 1, 1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        episodios.stream()
                .filter(ep -> ep.getDataLancamento() != null && ep.getDataLancamento().isAfter(dataBusca))
                .forEach(ep -> System.out.println("Temporada: " + ep.getTemporada() +
                                " Episódio: " + ep.getTitulo() +
                                " Data de lançamento: " + ep.getDataLancamento().format(formatter)));

        System.out.println("\nTop 5 episódios:");
        dadosEpisodios.stream()
                .filter(x -> !x.avaliacao().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(DadosEpisodio::avaliacao).reversed())
                .limit(5)
                .forEach(System.out::println);

        //encontrando a primeira ocorrência de uma busca a uma coleção
        System.out.print("\nDigite o nome do episódio: ");
        String trechoTitulo = LEITURA.nextLine();

        Optional<Episodio> episodioBuscado = episodios.stream()
                .filter(x -> x.getTitulo().toLowerCase().contains(trechoTitulo.toLowerCase()))
                .findFirst();

        if(episodioBuscado.isPresent()){
            System.out.println("Episódio encontrado! " + episodioBuscado.get().getTitulo());
            System.out.println("Temporada: " + episodioBuscado.get().getTemporada() + " Episódio: " + episodioBuscado.get().getNumeroEpisodio());
        } else {
            System.out.println("Episódio não encontrado!");
        }

        //criando um Map com dados por temporada
        Map<Integer, Double> avaliacoesPorTemporada = episodios.stream()
                .filter(ep -> ep.getAvaliacao() > 0.0)
                .collect(Collectors.groupingBy(Episodio::getTemporada,
                        Collectors.averagingDouble(Episodio::getAvaliacao)));

        System.out.println("\nMédia de avaliações por temporada: " + avaliacoesPorTemporada);

        //criando estatisticas dos episódios
        DoubleSummaryStatistics est = episodios.stream()
                .filter(ep -> ep.getAvaliacao() > 0.0)
                .collect(Collectors.summarizingDouble(Episodio::getAvaliacao));

        System.out.println("\nEstatísticas:");
        System.out.println("Média de avaliação dos episódios: " + est.getAverage());
        System.out.println("Melhor episódio: " + est.getMax());
        System.out.println("Pior episódio: " + est.getMin());
        System.out.println("Total de episódios: " + est.getCount());
    }
}
