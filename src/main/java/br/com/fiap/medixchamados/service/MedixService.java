package br.com.fiap.medixchamados.service;


import br.com.fiap.medixchamados.dto.ChamadoRequestDTO;
import br.com.fiap.medixchamados.dto.ChamadoUpdateDTO;
import br.com.fiap.medixchamados.dto.ColaboradorRequestDTO;
import br.com.fiap.medixchamados.model.Chamado;
import br.com.fiap.medixchamados.model.Colaborador;
import br.com.fiap.medixchamados.repository.ChamadoRepository;
import br.com.fiap.medixchamados.repository.ColaboradorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedixService {

    @Autowired
    private ColaboradorRepository colaboradorRepository;

    @Autowired
    private ChamadoRepository chamadoRepository;

    // --- MÉTODOS PARA COLABORADOR ---
    public List<Colaborador> listarColaboradores() {
        return colaboradorRepository.findAll();
    }

    @Transactional
    public Colaborador criarColaborador(ColaboradorRequestDTO dto) {
        Colaborador c = new Colaborador();
        c.setNome(dto.nome());
        c.setCargo(dto.cargo());
        c.setSetor(dto.setor());
        return colaboradorRepository.save(c);
    }

    // --- MÉTODOS PARA CHAMADOS (O CRUD PRINCIPAL) ---
    public List<Chamado> listarChamados() {
        return chamadoRepository.findAll();
    }

    @Transactional
    public Chamado abrirChamado(ChamadoRequestDTO dto) {
        Colaborador c = colaboradorRepository.findById(dto.idColaborador())
                .orElseThrow(() -> new RuntimeException("Colaborador não encontrado com ID: " + dto.idColaborador()));

        Chamado cham = new Chamado();
        cham.setDescricao(dto.descricao());
        cham.setPrioridade(dto.prioridade());
        cham.setStatus("ABERTO");
        cham.setColaborador(c);

        return chamadoRepository.save(cham);
    }

    @Transactional
    public Chamado atualizarStatusChamado(Integer id, ChamadoUpdateDTO dto) {
        Chamado cham = chamadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chamado não encontrado"));

        cham.setStatus(dto.status());
        cham.setPrioridade(dto.prioridade());

        return chamadoRepository.save(cham);
    }

    @Transactional
    public void deletarChamado(Integer id) {
        if (!chamadoRepository.existsById(id)) {
            throw new RuntimeException("Chamado não encontrado");
        }
        chamadoRepository.deleteById(id);
    }

    public Chamado buscarChamadoPorId(Integer id) {
        return chamadoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado inválido com o ID: " + id));
    }
}