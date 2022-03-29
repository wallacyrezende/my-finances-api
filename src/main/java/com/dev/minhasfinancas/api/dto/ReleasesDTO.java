package com.dev.minhasfinancas.api.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReleasesDTO {

	private Long id;
	private String description;
	private Integer mouth;
	private Integer year;
	private BigDecimal value;
	private Long userId;
	private String type;
	private String status;
}
