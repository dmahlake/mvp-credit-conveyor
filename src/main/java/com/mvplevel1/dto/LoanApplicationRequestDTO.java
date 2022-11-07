package com.mvplevel1.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanApplicationRequestDTO {

    private BigDecimal amount;
    private Integer term;
    private String firstName;
    private String lastName;
    private String middleName;
    private String email;
    private LocalDate birthdate;
    private String passportSeries;
    private String passportNumber;
}
