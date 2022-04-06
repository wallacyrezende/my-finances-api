package com.dev.minhasfinancas.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.dev.minhasfinancas.model.entity.Lancamento;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;
import com.dev.minhasfinancas.model.enums.TipoLancamentoEnum;

public interface LancamentoService {
	
	Lancamento salvar(Lancamento lancamento);
	
	Lancamento atualizar(Lancamento lancamento);
	
	void deletar(Lancamento lancamento);
	
	List<Lancamento> buscar(Lancamento lancamentoFiltro);
	
	void atualizarStatus(Lancamento lancamento, StatusLancamentoEnum status);
	
	void validar(Lancamento lancamento);
	
	Optional<Lancamento> obterPorId(Long id);
	
	BigDecimal obterSaldoPorUsuario(Long id);

	List<Optional<Lancamento>> lastReleases(Long idUsuario);

	BigDecimal getExtractByReleaseType(Long userId, TipoLancamentoEnum releaseType);
}
