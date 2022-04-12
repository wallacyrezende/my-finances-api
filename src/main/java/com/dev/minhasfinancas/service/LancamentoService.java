package com.dev.minhasfinancas.service;

import com.dev.minhasfinancas.api.dto.PaginatedResponseDTO;
import com.dev.minhasfinancas.api.dto.ReleasesDTO;
import com.dev.minhasfinancas.model.entity.Release;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;
import com.dev.minhasfinancas.model.enums.TipoLancamentoEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LancamentoService {
	
	Release salvar(Release lancamento);
	
	Release atualizar(Release lancamento);
	
	void deletar(Release lancamento);
	
	List<Release> buscar(Release lancamentoFiltro);
	
	void atualizarStatus(Release lancamento, StatusLancamentoEnum status);
	
	void validar(Release lancamento);
	
	Optional<Release> obterPorId(Long id);
	
	BigDecimal obterSaldoPorUsuario(Long id);

	List<ReleasesDTO> lastReleases(Long userId);

	BigDecimal getExtractByReleaseType(Long userId, TipoLancamentoEnum releaseType);

	PaginatedResponseDTO<ReleasesDTO> getReleasesPaginated(Long userId, Integer page, Integer size);
}
