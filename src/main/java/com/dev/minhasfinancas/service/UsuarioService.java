package com.dev.minhasfinancas.service;

import java.util.Optional;

import com.dev.minhasfinancas.api.dto.UserAuthenticated;
import com.dev.minhasfinancas.model.entity.Usuario;

public interface UsuarioService {

	UserAuthenticated autenticar(String email, String senha);
	
	Usuario salvarUsuario(Usuario usuario);

	void validarEmail(String email);
	
	Optional<Usuario> obterPorId(Long id);
}
