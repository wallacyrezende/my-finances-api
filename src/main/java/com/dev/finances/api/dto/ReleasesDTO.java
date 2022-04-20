package com.dev.finances.api.dto;

import java.math.BigDecimal;
import java.util.Date;

import com.dev.finances.model.enums.ReleaseStatusEnum;
import com.dev.finances.model.enums.ReleaseTypeEnum;
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
	private ReleaseTypeEnum type;
	private ReleaseStatusEnum status;
	private Date releaseDate;
}
