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
import com.dev.minhasfinancas.api.dto.ReleasesDTO;
import com.dev.minhasfinancas.exception.RegraNegocioException;
import com.dev.minhasfinancas.model.entity.Lancamento;
import com.dev.minhasfinancas.model.entity.Usuario;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;
import com.dev.minhasfinancas.model.enums.TipoLancamentoEnum;
import com.dev.minhasfinancas.service.LancamentoService;
import com.dev.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/releases")
@RequiredArgsConstructor
public class ReleasesController {
	
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
		
		Optional<Usuario> usuario = usuarioService.getById(idUsuario);
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

	@GetMapping("/last-releases/{userId}")
	public ResponseEntity ultimosLancamentos( @PathVariable("userId") @NonNull Long userId ) {
		List<ReleasesDTO> releases = new LinkedList<>();
		service.lastReleases(userId).forEach(release -> {
			releases.add(converter(release.get()));
		});
		return ResponseEntity.ok(releases);
	}
	
	@PostMapping("/create-release")
	public ResponseEntity create(@RequestBody ReleasesDTO dto ) {
		try {
			Lancamento lancamento = converter(dto);
			lancamento = service.salvar(lancamento);
			return new ResponseEntity(lancamento, HttpStatus.CREATED);
		} catch (RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PutMapping("{id}")
	public ResponseEntity update(@PathVariable("id") Long id, @RequestBody ReleasesDTO dto) {
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
	
	@PutMapping("{id}/update-status")
	public ResponseEntity updateStatus(@PathVariable("id") Long id, @RequestParam("status") StatusLancamentoEnum status) {
		return service.obterPorId(id).map( entity -> {
			StatusLancamentoEnum selectedStatus = StatusLancamentoEnum.valueOf(status.name());
			
			if(selectedStatus == null) {
				return ResponseEntity.badRequest().body("Não foi possível atualizar o status do lançamentos, informe um status válido");
			}
			
			try {
				entity.setStatus(selectedStatus);
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
	
	private ReleasesDTO converter(Lancamento lancamento) {
		return ReleasesDTO.builder()
					.id(lancamento.getId())
					.description(lancamento.getDescricao())
					.value(lancamento.getValor())
					.mouth(lancamento.getMes())
					.year(lancamento.getAno())
					.status(lancamento.getStatus().name())
					.type(lancamento.getTipo().name())
					.userId(lancamento.getUsuario().getId())
					.build();
					
	}

	private List<ReleasesDTO> convertList(List<Lancamento> lancamentos) {
		List<ReleasesDTO> lancamentosDTOs = new LinkedList<>();
		lancamentos.forEach( lancamento -> {
			lancamentosDTOs.add(ReleasesDTO.builder()
					.id(lancamento.getId())
					.description(lancamento.getDescricao())
					.value(lancamento.getValor())
					.mouth(lancamento.getMes())
					.year(lancamento.getAno())
					.status(lancamento.getStatus().name())
					.type(lancamento.getTipo().name())
					.userId(lancamento.getUsuario().getId())
					.build());
		});
		return lancamentosDTOs;
	}
	
	private Lancamento converter(ReleasesDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescription());
		lancamento.setAno(dto.getYear());
		lancamento.setMes(dto.getMouth());
		lancamento.setValor(dto.getValue());
		
		Usuario usuario = usuarioService
			.getById(dto.getUserId())
			.orElseThrow( () -> new RegraNegocioException("Usuário não encontrado para o Id informado."));
		
		lancamento.setUsuario(usuario);
		
		if(dto.getType() != null)
			lancamento.setTipo(TipoLancamentoEnum.valueOf(dto.getType()));
		if(dto.getStatus() != null)
			lancamento.setStatus(StatusLancamentoEnum.valueOf(dto.getStatus()));
		
		return lancamento;
	}
}
