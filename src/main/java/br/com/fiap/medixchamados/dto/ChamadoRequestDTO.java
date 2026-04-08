package br.com.fiap.medixchamados.dto;

public record ChamadoRequestDTO(
        Integer idColaborador,
        String descricao,
        String prioridade
) {}