package com.dev.minhasfinancas.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dev.minhasfinancas.api.dto.UsuarioDTO;
import com.dev.minhasfinancas.exception.ErroAutenticacao;
import com.dev.minhasfinancas.exception.RegraNegocioException;
import com.dev.minhasfinancas.model.entity.Usuario;
import com.dev.minhasfinancas.service.UsuarioService;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
	
	private UsuarioService service;
	
	public UsuarioController(UsuarioService service) {
		this.service = service;
	}
	
	@PostMapping("/autenticar")
	public ResponseEntity autenticar( @RequestBody UsuarioDTO dto ) {
		
		try {
			Usuario usuarioAutenticado = service.autenticar(dto.getEmail(), dto.getSenha());
			return ResponseEntity.ok(usuarioAutenticado);
		} catch(ErroAutenticacao e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PostMapping
	public ResponseEntity salvar( @RequestBody UsuarioDTO dto ) {
		
		Usuario usuario = Usuario.builder()
						         .nome(dto.getNome())
						         .email(dto.getEmail())
						         .senha(dto.getSenha())
						         .build();
		try {
			Usuario usuarioSalvo = service.salvarUsuario(usuario);
			return new ResponseEntity(usuarioSalvo, HttpStatus.CREATED);
		} catch(RegraNegocioException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

}
