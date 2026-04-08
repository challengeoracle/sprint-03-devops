package br.com.fiap.medixchamados.repository;

import br.com.fiap.medixchamados.model.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Integer> {
}