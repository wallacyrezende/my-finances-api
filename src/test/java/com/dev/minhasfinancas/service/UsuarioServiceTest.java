package com.dev.minhasfinancas.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import com.dev.minhasfinancas.api.dto.UserAuthenticated;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dev.minhasfinancas.exception.ErroAutenticacao;
import com.dev.minhasfinancas.exception.RegraNegocioException;
import com.dev.minhasfinancas.model.entity.Usuario;
import com.dev.minhasfinancas.model.repository.UsuarioRepository;
import com.dev.minhasfinancas.service.impl.UsuarioServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class UsuarioServiceTest {
	
	@SpyBean
	UsuarioServiceImpl service;
	
	@MockBean
	UsuarioRepository repository;

	@Test
	public void deveSalvarUmUsuario() {
		Mockito.doNothing().when(service).validarEmail(Mockito.anyString());
		Usuario usuario = Usuario.builder()
								 .id(1l)
								 .nome("nome")
								 .email("email@email.com")
								 .senha("senha")
								 .build();
		
		Assertions.assertDoesNotThrow(() -> {
			
			Mockito.when(repository.save(Mockito.any(Usuario.class))).thenReturn(usuario);
			Usuario usuarioSalvo = service.salvarUsuario(new Usuario());
			
			Assertions.assertEquals(usuarioSalvo != null, usuarioSalvo != null);
			Assertions.assertEquals(1l, usuarioSalvo.getId());
			Assertions.assertEquals("nome", usuarioSalvo.getNome());
			Assertions.assertEquals("email@email.com", usuarioSalvo.getEmail());
			Assertions.assertEquals("senha", usuarioSalvo.getSenha());
		});		
	}
	
	@Test
	public void naoDeveSalvarUmUsuarioComEmailJaCadastrado() {
		String email = "email@email.com";
		Usuario usuario = Usuario.builder().email(email).build();
		Mockito.doThrow(RegraNegocioException.class).when(service).validarEmail(email);
		
		
		
		Assertions.assertThrows(RegraNegocioException.class, () -> {
			service.salvarUsuario(usuario);
		});
		
		Mockito.verify( repository, Mockito.never() ).save(usuario);
	}
	
	@Test
	public void deveAutenticarUmUsuarioComSucesso() {
//		cenario
		String email = "email@email.com";
		String password = "senha";
		
		Usuario usuario = Usuario.builder().email(email).senha(password).id(1l).build();
		Mockito.when(repository.findByEmail(email)).thenReturn(Optional.of(usuario));
		

		Assertions.assertDoesNotThrow(() -> {
//			acao
			UserAuthenticated result = service.autenticar(email, password);
		
//			verificacao
			Assertions.assertNotNull(result);
		});
	}
	
	@Test
	public void deveLancarErroQuandoNaoEncontrarUsuarioCadastradoComOEmailInformado() {
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
		
		Exception exception = Assertions.assertThrows(ErroAutenticacao.class, () -> {
			service.autenticar("email@email.com", "senha");
		});
		
		assertEquals("Usuário não encontrado para o e-mail informado.", exception.getMessage());
		
//		verificacao junit 4
//		Throwblw exception = Assertions.assertCatchThrowable( () -> service.autenticar("email@email.com", "senha"));
//		Assertions.assertThat(exception).isInstanceOf(ErroAutenticacao.class).hasMessage("Usuário não encontrado para o e-mail informado.");
	}
	
	@Test
	public void deveLancarErroQuandoSenhaNaoCorresponder() {
		String senha = "senha";
		Usuario usuario = Usuario.builder()
								 .email("email@email.com")
								 .senha(senha)
								 .build();
		Mockito.when(repository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(usuario));
		
	    Exception exception = Assertions.assertThrows(ErroAutenticacao.class, () -> {
			service.autenticar("email@email.com", "123");
		});
	    
	 	assertEquals("Senha inválida.", exception.getMessage());
	}
	
	@Test
	public void deveValidarEmail() {
//		cenario
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(false);
		
//		ação
		Assertions.assertDoesNotThrow(() -> service.validarEmail("email@email.com"));
	}
	
	@Test
	public void deveLancarErroAoValidarEmailQuandoExistirEmailCadastrado() {
		Mockito.when(repository.existsByEmail(Mockito.anyString())).thenReturn(true);
		
		Assertions.assertThrows(RegraNegocioException.class, () -> service.validarEmail("email@email.com"));	
	}

}
