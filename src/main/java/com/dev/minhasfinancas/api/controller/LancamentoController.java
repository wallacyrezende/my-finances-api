package com.dev.minhasfinancas.api.controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dev.minhasfinancas.api.dto.AtualizaStatusDTO;
import com.dev.minhasfinancas.api.dto.LancamentoDTO;
import com.dev.minhasfinancas.exception.RegraNegocioException;
import com.dev.minhasfinancas.model.entity.Lancamento;
import com.dev.minhasfinancas.model.entity.Usuario;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;
import com.dev.minhasfinancas.model.enums.TipoLancamentoEnum;
import com.dev.minhasfinancas.service.LancamentoService;
import com.dev.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/lancamentos")
@RequiredArgsConstructor
public class LancamentoController {
	
	private final LancamentoService service;
	private final UsuarioService usuarioService;
	
	@GetMapping
	public ResponseEntity buscar (
			@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "ano", required = false) Integer ano,
			@RequestParam("usuario") Long idUsuario
			) {
		Lancamento lancamentoFiltro = new Lancamento();
		lancamentoFiltro.setDescricao(descricao);
		lancamentoFiltro.setMes(mes);
		lancamentoFiltro.setAno(ano);
		
		Optional<Usuario> usuario = usuarioService.obterPorId(idUsuario);
		if(!usuario.isPresent()) {
			return ResponseEntity.badRequest().body("Não foi possível realizar a consulta.");
		} else {
			lancamentoFiltro.setUsuario(usuario.get());
		}
		
		List<Lancamento> lancamentos = service.buscar(lancamentoFiltro);
		return ResponseEntity.ok(lancamentos);
	}
	
	@GetMapping("{id}")
	public ResponseEntity obterLancamento( @PathVariable("id")  Long id ) {
		return service.obterPorId(id)
				.map( lancamento -> new ResponseEntity( converter(lancamento), HttpStatus.OK) )
				.orElseGet( () -> new ResponseEntity(HttpStatus.NOT_FOUND) );
	}

	@GetMapping("/ultimos-lancamentos/{idUsuario}")
	public ResponseEntity ultimosLancamentos( @PathVariable("idUsuario") @NonNull Long idUsuario ) {
		List<LancamentoDTO> lancamentos = new LinkedList<>();
		service.ultimosLancamentos(idUsuario).forEach(lancamento -> {
			lancamentos.add(converter(lancamento.get()));
		});
		return ResponseEntity.ok(lancamentos);
	}
	
	@PostMapping
	public ResponseEntity salvar( @RequestBody LancamentoDTO dto ) {
		try {
			Lancamento lancamento = converter(dto);
			lancamento = service.salvar(lancamento);
			return new ResponseEntity(lancamento, HttpStatus.CREATED);
		} catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PutMapping("{id}")
	public ResponseEntity atualizar( @PathVariable("id") Long id, @RequestBody LancamentoDTO dto) {
		return service.obterPorId(id).map( entity -> {
			
			try {
				Lancamento lancamento = converter(dto);
				lancamento.setId(entity.getId());
				service.atualizar(lancamento);
				
				return ResponseEntity.ok(lancamento);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
			
		}).orElseGet( () -> 
				new ResponseEntity("Lançamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST) );
	}
	
	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus( @PathVariable("id") Long id, @RequestBody AtualizaStatusDTO dto ) {
		return service.obterPorId(id).map( entity -> {
			StatusLancamentoEnum statusSelecionado = StatusLancamentoEnum.valueOf(dto.getStatus());
			
			if(statusSelecionado == null) {
				return ResponseEntity.badRequest().body("Não foi possível atualizar o status do lançamentos, informe um status válido");
			}
			
			try {
				entity.setStatus(statusSelecionado);
				service.atualizar(entity);
				return ResponseEntity.ok(entity);
			} catch (RegraNegocioException e) {
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet( () -> 
		new ResponseEntity("Lançamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST) );
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity deletar( @PathVariable("id") Long id) {
		return service.obterPorId(id).map( entity -> {
			service.deletar(entity);
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}).orElseGet( () -> 
				new ResponseEntity("Lançamento não encontrado na base de dados.", HttpStatus.BAD_REQUEST) );
	}
	
	private LancamentoDTO converter(Lancamento lancamento) {
		return LancamentoDTO.builder()
					.id(lancamento.getId())
					.descricao(lancamento.getDescricao())
					.valor(lancamento.getValor())
					.mes(lancamento.getMes())
					.ano(lancamento.getAno())
					.status(lancamento.getStatus().name())
					.tipo(lancamento.getTipo().name())
					.usuario(lancamento.getUsuario().getId())
					.build();
					
	}

	private List<LancamentoDTO> convertList(List<Lancamento> lancamentos) {
		List<LancamentoDTO> lancamentosDTOs = new LinkedList<>();
		lancamentos.forEach( lancamento -> {
			lancamentosDTOs.add(LancamentoDTO.builder()
					.id(lancamento.getId())
					.descricao(lancamento.getDescricao())
					.valor(lancamento.getValor())
					.mes(lancamento.getMes())
					.ano(lancamento.getAno())
					.status(lancamento.getStatus().name())
					.tipo(lancamento.getTipo().name())
					.usuario(lancamento.getUsuario().getId())
					.build());
		});
		return lancamentosDTOs;
	}
	
	private Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		
		Usuario usuario = usuarioService
			.obterPorId(dto.getUsuario())
			.orElseThrow( () -> new RegraNegocioException("Usuário não encontrado para o Id informado."));
		
		lancamento.setUsuario(usuario);
		
		if(dto.getTipo() != null)
			lancamento.setTipo(TipoLancamentoEnum.valueOf(dto.getTipo()));
		if(dto.getStatus() != null)
			lancamento.setStatus(StatusLancamentoEnum.valueOf(dto.getStatus()));
		
		return lancamento;
	}
}
