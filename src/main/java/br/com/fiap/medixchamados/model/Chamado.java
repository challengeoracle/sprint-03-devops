package br.com.fiap.medixchamados.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chamados")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idChamado;

    @Column(name = "descricao")
    private String descricao;

    private String prioridade;
    private String status;
    private LocalDateTime dataAbertura = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "id_colaborador")
    @JsonBackReference
    private Colaborador colaborador;
}