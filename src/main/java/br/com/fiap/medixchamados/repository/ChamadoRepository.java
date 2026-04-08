package br.com.fiap.medixchamados.repository;

import br.com.fiap.medixchamados.model.Chamado;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Integer> {
}