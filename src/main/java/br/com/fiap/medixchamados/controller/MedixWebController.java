package br.com.fiap.medixchamados.controller;

import br.com.fiap.medixchamados.dto.ChamadoRequestDTO;
import br.com.fiap.medixchamados.dto.ChamadoUpdateDTO;
import br.com.fiap.medixchamados.model.Chamado;
import br.com.fiap.medixchamados.service.MedixService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MedixWebController {

    @Autowired
    private MedixService service;

    // --- TELA INICIAL (LISTAR E CRIAR) ---
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("chamados", service.listarChamados());
        model.addAttribute("colaboradores", service.listarColaboradores());
        return "index";
    }

    @PostMapping("/web/chamados")
    public String novoChamado(ChamadoRequestDTO dto) {
        service.abrirChamado(dto);
        return "redirect:/"; // Atualiza a página
    }

    // --- TELA DE EDIÇÃO (ATUALIZAR) ---
    @GetMapping("/web/chamados/editar/{id}")
    public String exibirFormularioEdicao(@PathVariable("id") Integer id, Model model) {
        Chamado chamado = service.buscarChamadoPorId(id);
        model.addAttribute("chamado", chamado);
        return "editar-chamado"; // Chama o novo arquivo HTML que vamos criar
    }

    @PostMapping("/web/chamados/editar/{id}")
    public String atualizarChamado(@PathVariable("id") Integer id, ChamadoUpdateDTO dto) {
        service.atualizarStatusChamado(id, dto);
        return "redirect:/"; // Volta pra home após salvar
    }

    // --- DELETAR ---
    @PostMapping("/web/chamados/deletar/{id}")
    public String deletarChamado(@PathVariable("id") Integer id) {
        service.deletarChamado(id);
        return "redirect:/";
    }
}