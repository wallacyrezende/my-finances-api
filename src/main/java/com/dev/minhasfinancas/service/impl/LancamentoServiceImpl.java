package com.dev.minhasfinancas.service.impl;

import com.dev.minhasfinancas.api.dto.PaginatedResponseDTO;
import com.dev.minhasfinancas.api.dto.ReleasesDTO;
import com.dev.minhasfinancas.exception.RegraNegocioException;
import com.dev.minhasfinancas.model.entity.Release;
import com.dev.minhasfinancas.model.entity.Usuario;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;
import com.dev.minhasfinancas.model.enums.TipoLancamentoEnum;
import com.dev.minhasfinancas.model.repository.LancamentoRepository;
import com.dev.minhasfinancas.service.LancamentoService;
import com.dev.minhasfinancas.service.UsuarioService;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class LancamentoServiceImpl implements LancamentoService {

	private final LancamentoRepository repository;
	private final @NonNull UsuarioService usuarioService;

	@Override
	@Transactional
	public Release salvar(Release lancamento) {
		validar(lancamento);
		lancamento.setStatus(StatusLancamentoEnum.PENDENTE);
		lancamento.setDataCadastro(LocalDate.now());
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public Release atualizar(Release lancamento) {
		Objects.requireNonNull(lancamento.getId());
		validar(lancamento);
		return repository.save(lancamento);
	}

	@Override
	@Transactional
	public void deletar(Release lancamento) {
		Objects.requireNonNull(lancamento.getId());
		repository.delete(lancamento);
	}

	@Override
	@Transactional(readOnly = true)
	public List<Release> buscar(Release lancamentoFiltro) {
		Example example = Example.of(lancamentoFiltro,
				ExampleMatcher.matching()
					.withIgnoreCase()
					.withStringMatcher(StringMatcher.CONTAINING));
		return repository.findAll(example);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ReleasesDTO> lastReleases(Long userId) {
		List<ReleasesDTO> releases = new LinkedList<>();
		repository.lastReleases(userId, LocalDate.now().minusDays(30), LocalDate.now()).forEach( release -> {
			releases.add(convert(release.get()));
		});
		return releases;
	}

	@Override
	@Transactional
	public BigDecimal getExtractByReleaseType(Long userId, TipoLancamentoEnum releaseType) {
		BigDecimal extract = repository.getBalanceByReleaseTypeUserAndStatus(userId, releaseType, StatusLancamentoEnum.EFETIVADO, LocalDate.now().minusDays(30), LocalDate.now());
		return (extract == null) ? BigDecimal.ZERO : extract;
	}

	@Override
	@Transactional
	public PaginatedResponseDTO<ReleasesDTO> getReleasesPaginated(Long userId, Integer page, Integer size) {
		Page<ReleasesDTO> pageReleases = Page.empty();
		Optional<Usuario> user = usuarioService.getById(userId);
		if (user.isPresent()) {
			Pageable pageable = PageRequest.of(page, size, Sort.by("ano","mes","id").descending());
			pageReleases = repository.findAll(user.get().getId(), pageable);
		}
		return new PaginatedResponseDTO<ReleasesDTO>(pageReleases.getContent(), pageReleases.getTotalElements());
	}

	@Override
	public void atualizarStatus(Release lancamento, StatusLancamentoEnum status) {
		lancamento.setStatus(status);
		atualizar(lancamento);
	}
	
	@Override
	public void validar(Release lancamento) {
		if(lancamento.getDescricao() == null || lancamento.getDescricao().trim().equals("")) {
			throw new RegraNegocioException("Informe uma Descrição válida.");
		}
		
		if(lancamento.getMes() == null || lancamento.getMes() < 1 || lancamento.getMes() > 12) {
			throw new RegraNegocioException("Informe um Mês válido.");
		}
		
		if(lancamento.getAno() == null || lancamento.getAno().toString().length() != 4) {
			throw new RegraNegocioException("Informe um Ano válido.");
		}
		
		if(lancamento.getUsuario() == null || lancamento.getUsuario().getId() == null) {
			throw new RegraNegocioException("Informe um Usuário.");
		}
		
		if(lancamento.getValor() == null || lancamento.getValor().compareTo(BigDecimal.ZERO) < 1) {
			throw new RegraNegocioException("Informe um Valor válido.");
		}
		
		if(lancamento.getTipo() == null) {
			throw new RegraNegocioException("Informe um Tipo de lançamento.");
		}
	}
	
	@Override
	public Optional<Release> obterPorId(Long id) {
		return repository.findById(id);
	}
	
	@Override
	@Transactional(readOnly = true)
	public BigDecimal obterSaldoPorUsuario(Long id) {
		BigDecimal receitas = repository.getBalanceByReleaseTypeUserAndStatus(id, TipoLancamentoEnum.RECEITA, StatusLancamentoEnum.EFETIVADO, LocalDate.now().minusDays(30), LocalDate.now());
		BigDecimal despesas = repository.getBalanceByReleaseTypeUserAndStatus(id, TipoLancamentoEnum.DESPESA, StatusLancamentoEnum.EFETIVADO, LocalDate.now().minusDays(30), LocalDate.now());
		
		if(receitas == null)
			receitas = BigDecimal.ZERO;
		
		if(despesas == null)
			despesas = BigDecimal.ZERO;
		
		return receitas.subtract(despesas);
	}

	private ReleasesDTO convert(Release lancamento) {
		return ReleasesDTO.builder()
				.id(lancamento.getId())
				.description(lancamento.getDescricao())
				.value(lancamento.getValor())
				.mouth(lancamento.getMes())
				.year(lancamento.getAno())
				.status(lancamento.getStatus())
				.type(lancamento.getTipo())
				.userId(lancamento.getUsuario().getId())
				.build();

	}

}
