package com.dev.minhasfinancas.api.controller;

import java.math.BigDecimal;
import java.util.Optional;

import com.dev.minhasfinancas.api.dto.UserAuthenticated;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.minhasfinancas.api.dto.UsuarioDTO;
import com.dev.minhasfinancas.exception.ErroAutenticacao;
import com.dev.minhasfinancas.exception.RegraNegocioException;
import com.dev.minhasfinancas.model.entity.Usuario;
import com.dev.minhasfinancas.service.LancamentoService;
import com.dev.minhasfinancas.service.UsuarioService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
	
	private final UsuarioService service;
	private final LancamentoService lancamentoService;
	
	@PostMapping("/autenticar")
	public ResponseEntity autenticar( @RequestBody UsuarioDTO dto ) {
		
		try {
			UserAuthenticated userAuthenticated = service.autenticar(dto.getEmail(), dto.getPassword());
			return ResponseEntity.ok(userAuthenticated);
		} catch(ErroAutenticacao e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PostMapping
	public ResponseEntity salvar( @RequestBody UsuarioDTO dto ) {
		
		Usuario usuario = Usuario.builder()
						         .nome(dto.getName())
						         .email(dto.getEmail())
						         .senha(dto.getPassword())
						         .build();
		try {
			Usuario usuarioSalvo = service.salvarUsuario(usuario);
			return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
		} catch(RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@GetMapping("{id}/saldo")
	public ResponseEntity obterSaldo( @PathVariable("id") Long id ) {
		Optional<Usuario> usuario = service.obterPorId(id);
		
		if(!usuario.isPresent())
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		
		BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
		return ResponseEntity.ok(saldo);
	}

}
