package com.dev.minhasfinancas.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dev.minhasfinancas.model.entity.Lancamento;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{

}
