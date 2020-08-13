package com.dev.minhasfinancas.service;

import java.util.List;

import com.dev.minhasfinancas.model.entity.Lancamento;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;

public interface LancamentoService {
	
	Lancamento salvar(Lancamento lancamento);
	
	Lancamento atualizar(Lancamento lancamento);
	
	void deletar(Lancamento lancamento);
	
	List<Lancamento> buscar(Lancamento lancamentoFiltro);
	
	void atualizarStatus(Lancamento lancamento, StatusLancamentoEnum status);
	
	void validar(Lancamento lancamento);
}
