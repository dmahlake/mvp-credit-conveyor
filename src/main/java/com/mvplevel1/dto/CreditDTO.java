package com.mvplevel1.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreditDTO {

    private BigDecimal amount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal rate;
    private BigDecimal psk;
    private Boolean isInsuranceEnabled;
    private Boolean isSalaryClient;
    private List<PaymentScheduleElement> paymentSchedule;

    public CreditDTO(BigDecimal rate, BigDecimal psk,  BigDecimal monthlyPayment)
    {
        this.rate = rate;
        this.psk = psk;
        //this.paymentSchedule = paymentSchedule;
        this.monthlyPayment = monthlyPayment;

    }
}
