package com.dev.minhasfinancas.model.repository;

import com.dev.minhasfinancas.api.dto.ReleasesDTO;
import com.dev.minhasfinancas.model.entity.Release;
import com.dev.minhasfinancas.model.enums.StatusLancamentoEnum;
import com.dev.minhasfinancas.model.enums.TipoLancamentoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LancamentoRepository extends JpaRepository<Release, Long> {

    String WHERE_RELEASE_TYPE_USER_AND_STATUS = " u.id = :idUsuario and l.tipo = :tipo and l.status = :status group by u ";
    String SELECT_COLUMNS_FIND_ALL = " select new com.dev.minhasfinancas.api.dto.ReleasesDTO(l.id, l.descricao, l.mes, l.ano, l.valor, l.usuario.id, l.tipo, l.status) ";
    String WHERE_FIND_ALL = " l.usuario.id = :userId ";

    @Query(value = "select sum(l.valor) from Release l join l.usuario u where " + WHERE_RELEASE_TYPE_USER_AND_STATUS)
    BigDecimal getBalanceByReleaseTypeUserAndStatus(
            @Param("idUsuario") Long idUsuario,
            @Param("tipo") TipoLancamentoEnum tipo,
            @Param("status") StatusLancamentoEnum status);

    @Query(value = "select l from Release l join l.usuario u where u.id = :idUsuario order by l.id desc")
    List<Optional<Release>> lastReleases(@Param("idUsuario") Long idUsuario);

    @Query(value = SELECT_COLUMNS_FIND_ALL + "from Release l where " + WHERE_FIND_ALL,
            countQuery = "select count(l.id) from Release l where " + WHERE_FIND_ALL)
    Page<ReleasesDTO> findAll(@Param("userId") Long userId, Pageable pageable);
}
