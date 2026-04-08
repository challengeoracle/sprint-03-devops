package br.com.fiap.medixchamados.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "colaboradores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idColaborador;

    private String nome;
    private String cargo;
    private String setor;

    @OneToMany(mappedBy = "colaborador", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Chamado> chamados;
}