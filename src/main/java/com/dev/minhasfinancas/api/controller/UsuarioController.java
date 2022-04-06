package com.dev.minhasfinancas.api.controller;

import java.math.BigDecimal;
import java.util.Optional;

import com.dev.minhasfinancas.api.dto.UserAuthenticated;
import com.dev.minhasfinancas.model.enums.TipoLancamentoEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
		Optional<Usuario> usuario = service.getById(id);
		
		if(!usuario.isPresent())
			return new ResponseEntity(HttpStatus.NOT_FOUND);
		
		BigDecimal saldo = lancamentoService.obterSaldoPorUsuario(id);
		return ResponseEntity.ok(saldo);
	}

	@GetMapping("{userId}/extract")
	public ResponseEntity getExtract(@PathVariable("userId") Long userId, @RequestParam("releaseType") TipoLancamentoEnum releaseType) {
		Optional<Usuario> user = service.getById(userId);

		if(!user.isPresent())
			return new ResponseEntity(HttpStatus.NOT_FOUND);

		BigDecimal extract = lancamentoService.getExtractByReleaseType(userId, releaseType);
		return ResponseEntity.ok(extract);
	}

}
