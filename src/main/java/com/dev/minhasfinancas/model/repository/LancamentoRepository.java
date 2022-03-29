package com.dev.minhasfinancas.model.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dev.minhasfinancas.model.entity.Lancamento;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;
import com.dev.minhasfinancas.model.enums.TipoLancamentoEnum;

public interface LancamentoRepository extends JpaRepository<Lancamento, Long>{
	
	@Query( value = 
			"select sum(l.valor) from Lancamento l join l.usuario u "
		  + "where u.id = :idUsuario and l.tipo = :tipo and l.status = :status group by u" )
	BigDecimal obterSaldoPorTipoLancamentoEUsuarioEStatus(
			@Param("idUsuario") Long idUsuario, 
			@Param("tipo") TipoLancamentoEnum tipo,
			@Param("status") StatusLancamentoEnum status);

	@Query( value =
			"select l from Lancamento l join l.usuario u "
					+ "where u.id = :idUsuario order by l.id desc" )
	List<Optional<Lancamento>> lastReleases(@Param("idUsuario") Long idUsuario);
}
