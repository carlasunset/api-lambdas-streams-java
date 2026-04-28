package com.carla.apilambdasstreans.service;

public interface ConverteDadosInterface {

    <T> T obterDados(String json, Class<T> classe);
}
