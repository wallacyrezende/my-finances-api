package com.dev.minhasfinancas.service.impl;

import java.util.Optional;

import com.dev.minhasfinancas.api.dto.UserAuthenticated;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dev.minhasfinancas.exception.ErroAutenticacao;
import com.dev.minhasfinancas.exception.RegraNegocioException;
import com.dev.minhasfinancas.model.entity.Usuario;
import com.dev.minhasfinancas.model.repository.UsuarioRepository;
import com.dev.minhasfinancas.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {
	
	private UsuarioRepository repository;
	
	public UsuarioServiceImpl(UsuarioRepository repository) {
		super();
		this.repository = repository;
	}

	@Override
	public UserAuthenticated autenticar(String email, String password) {
		Optional<Usuario> user = repository.findByEmail(email);
		
		if(!user.isPresent()) {
			throw new ErroAutenticacao("Usuário não encontrado para o e-mail informado.");
		}
		
		if(!user.get().getSenha().equals(password)) {
			throw new ErroAutenticacao("Senha inválida.");
		}

		return UserAuthenticated.builder()
				.id(user.get().getId())
				.name(user.get().getNome())
				.email(user.get().getEmail())
				.build();
	}

	@Override
	@Transactional
	public Usuario salvarUsuario(Usuario usuario) {
		validarEmail(usuario.getEmail());
		return repository.save(usuario);
	}

	@Override
	public void validarEmail(String email) {
		boolean existe = repository.existsByEmail(email);
		if(existe) {
			throw new RegraNegocioException("Já existe um usuário cadastrado com este email.");
		}
	}

	@Override
	public Optional<Usuario> getById(Long id) {
		return repository.findById(id);
	}

}
