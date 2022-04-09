package com.dev.minhasfinancas.service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Arrays;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Example;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.dev.minhasfinancas.exception.RegraNegocioException;
import com.dev.minhasfinancas.model.entity.Release;
import com.dev.minhasfinancas.model.entity.Usuario;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;
import com.dev.minhasfinancas.model.repository.LancamentoRepository;
import com.dev.minhasfinancas.model.repository.ReleaseRepositoryTest;
import com.dev.minhasfinancas.service.impl.LancamentoServiceImpl;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class ReleaseServiceTest {
	
	@SpyBean
	LancamentoServiceImpl service;
	
	@MockBean
	LancamentoRepository repository;
	
	@Test
	public void deveSalvarUmLancamento() {
		//cenario
		Release lancamentoASalvar = ReleaseRepositoryTest.criarLancamento();
		Mockito.doNothing().when(service).validar(lancamentoASalvar);
		
		Release lancamentoSalvo = ReleaseRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamentoEnum.PENDENTE);
		Mockito.when(repository.save(lancamentoASalvar)).thenReturn(lancamentoSalvo);
		
		//execucao
		Release lancamento = service.salvar(lancamentoASalvar);
		
		//verificacao
		Assertions.assertThat(lancamento.getId()).isEqualTo(lancamentoSalvo.getId());
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(StatusLancamentoEnum.PENDENTE);
	}
	
	@Test
	public void naoDeveSalvarUmLancamentoQuandoHouverErroDeValidacao() {
		Release lancamentoASalvar = ReleaseRepositoryTest.criarLancamento();
		Mockito.doThrow(RegraNegocioException.class).when(service).validar(lancamentoASalvar);
		
		Assertions.catchThrowableOfType( () -> service.salvar(lancamentoASalvar), RegraNegocioException.class);
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}
	
	@Test
	public void deveAtualizarUmLancamento() {
		//cenario
		Release lancamentoSalvo = ReleaseRepositoryTest.criarLancamento();
		lancamentoSalvo.setId(1l);
		lancamentoSalvo.setStatus(StatusLancamentoEnum.PENDENTE);
		
		Mockito.doNothing().when(service).validar(lancamentoSalvo);
		Mockito.when(repository.save(lancamentoSalvo)).thenReturn(lancamentoSalvo);
		
		//execucao
		service.atualizar(lancamentoSalvo);
		
		//verificacao
		Mockito.verify(repository, Mockito.times(1)).save(lancamentoSalvo);
	}
	
	@Test
	public void deveLancarErroAoTentarAtualizarUmLancamentoQueAindaNaoFoiSalvo() {
		//cenario
		Release lancamentoASalvar = ReleaseRepositoryTest.criarLancamento();
		
		//execucao
		Assertions.catchThrowableOfType( () -> service.atualizar(lancamentoASalvar), NullPointerException.class);
		
		//verificacao
		Mockito.verify(repository, Mockito.never()).save(lancamentoASalvar);
	}
	
	@Test
	public void deveDeletarUmLancamento() {
		Release lancamento = ReleaseRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		service.deletar(lancamento);
		
		Mockito.verify(repository).delete(lancamento);
	}
	
	@Test
	public void deveLancarErroAoTentarDeletarUmLancamentoQueAindaNaoFoiSalvo() {
		Release lancamento = ReleaseRepositoryTest.criarLancamento();
		
		Assertions.catchThrowableOfType(() -> service.deletar(lancamento), NullPointerException.class);
		
		Mockito.verify(repository, Mockito.never()).delete(lancamento);
	}
	
	@Test
	public void deveFiltrarLancamentos() {
		Release lancamento = ReleaseRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		
		List<Release> lista = Arrays.asList(lancamento);
		Mockito.when( repository.findAll(Mockito.any(Example.class))).thenReturn(lista);
		
		List<Release> resultado = service.buscar(lancamento);
		
		Assertions
			.assertThat(resultado)
			.isNotEmpty()
			.hasSize(1)
			.contains(lancamento);
	}
	
	@Test
	public void deveAtualizarOStatusDeUmLancamento() {
		Release lancamento = ReleaseRepositoryTest.criarLancamento();
		lancamento.setId(1l);
		lancamento.setStatus(StatusLancamentoEnum.PENDENTE);
		
		StatusLancamentoEnum novoStatus = StatusLancamentoEnum.EFETIVADO;
		Mockito.doReturn(lancamento).when(service).atualizar(lancamento);
		
		
		service.atualizarStatus(lancamento, novoStatus);
		
		Assertions.assertThat(lancamento.getStatus()).isEqualTo(novoStatus);
		Mockito.verify(service).atualizar(lancamento);
	}
	
	@Test
	public void deveObterUmLancamentoPorId() {
		Long id = 1l;
		Release lancamento = ReleaseRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.of(lancamento));
		
		Optional<Release> resultado = service.obterPorId(id);
		
		Assertions.assertThat(resultado.isPresent()).isTrue();
	}
	
	@Test
	public void deveRetornarVazioQuandoOLancamentoNaoExiste() {
		Long id = 1l;
		Release lancamento = ReleaseRepositoryTest.criarLancamento();
		lancamento.setId(id);
		
		Mockito.when(repository.findById(id)).thenReturn(Optional.empty());
		
		Optional<Release> resultado = service.obterPorId(id);
		
		Assertions.assertThat(resultado.isPresent()).isFalse();
	}
	
	@Test
	public void deveLancarErrosAoValidarUmLancamento() {
		Release release = new Release();
		
		Throwable erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		
		release.setDescricao("");
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe uma Descrição válida.");
		
		release.setDescricao("Salário");
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		release.setMes(0);
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		release.setMes(13);
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Mês válido.");
		
		release.setMes(1);
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		release.setAno(202);
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Ano válido.");
		
		release.setAno(2020);
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");
		
		release.setUsuario(new Usuario());
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Usuário.");
		
		release.getUsuario().setId(1l);
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		
		release.setValor(BigDecimal.ZERO);
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Valor válido.");
		
		release.setValor(BigDecimal.ONE);
		erro = Assertions.catchThrowable( () -> service.validar(release) );
		Assertions.assertThat(erro).isInstanceOf(RegraNegocioException.class).hasMessage("Informe um Tipo de lançamento.");
	}
}
