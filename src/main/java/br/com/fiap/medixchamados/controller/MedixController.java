package br.com.fiap.medixchamados.controller;

import br.com.fiap.medixchamados.dto.ChamadoRequestDTO;
import br.com.fiap.medixchamados.dto.ChamadoUpdateDTO;
import br.com.fiap.medixchamados.dto.ColaboradorRequestDTO;
import br.com.fiap.medixchamados.model.Chamado;
import br.com.fiap.medixchamados.model.Colaborador;
import br.com.fiap.medixchamados.service.MedixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MedixController {

    @Autowired
    private MedixService service;

    // --- COLABORADORES ---
    @GetMapping("/colaboradores")
    public ResponseEntity<List<Colaborador>> getColaboradores() {
        return ResponseEntity.ok(service.listarColaboradores());
    }

    @PostMapping("/colaboradores")
    @ResponseStatus(HttpStatus.CREATED)
    public Colaborador postColaborador(@RequestBody ColaboradorRequestDTO dto) {
        return service.criarColaborador(dto);
    }

    // --- CHAMADOS ---
    @GetMapping("/chamados")
    public ResponseEntity<List<Chamado>> getChamados() {
        return ResponseEntity.ok(service.listarChamados());
    }

    @PostMapping("/chamados")
    @ResponseStatus(HttpStatus.CREATED)
    public Chamado postChamado(@RequestBody ChamadoRequestDTO dto) {
        return service.abrirChamado(dto);
    }

    @PutMapping("/chamados/{id}")
    public ResponseEntity<Chamado> putChamado(@PathVariable Integer id, @RequestBody ChamadoUpdateDTO dto) {
        try {
            return ResponseEntity.ok(service.atualizarStatusChamado(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/chamados/{id}")
    public ResponseEntity<Void> deleteChamado(@PathVariable Integer id) {
        try {
            service.deletarChamado(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}