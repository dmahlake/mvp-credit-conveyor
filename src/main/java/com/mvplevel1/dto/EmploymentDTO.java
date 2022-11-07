package com.mvplevel1.dto;

import com.mvplevel1.customEnum.EmploymentStatus;
import com.mvplevel1.customEnum.Position;
import lombok.*;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentDTO {

    private EmploymentStatus employmentStatus;
    private String employerINN;
    private BigDecimal salary;
    private Position position;
    private Integer workExperienceTotal;
    private Integer workExperienceCurrent;
}
