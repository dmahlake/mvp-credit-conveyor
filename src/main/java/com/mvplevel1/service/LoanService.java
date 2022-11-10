package com.mvplevel1.service;

import com.mvplevel1.customEnum.EmploymentStatus;
import com.mvplevel1.customEnum.Gender;
import com.mvplevel1.customEnum.MaritalStatus;
import com.mvplevel1.dto.*;
import com.mvplevel1.exception.QualifyLoanException;
import org.apache.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LoanService {

    LoanService loanService = new LoanService();
    CreditDTO response = new CreditDTO();
    BigDecimal monthlyPayment;
    BigDecimal totalPayment;
    BigDecimal interestPayment;
    BigDecimal remainingDebt;
    BigDecimal rate = new BigDecimal(10);

    private static final Logger LOGGER = Logger.getLogger(LoanService.class);

    private final List<LoanOfferDTO> loanOffers = new ArrayList<>();
    private String emailPattern = "^(.+)@(.+)$";
    private String nameCharacters = "^[A-Z][a-z]{2,}(?: [A-Z][a-z]*)*$";

    public List<LoanOfferDTO> offers(LoanApplicationRequestDTO requestDTO )
    {
        if (requestDTO.getFirstName().matches(nameCharacters) && requestDTO.getLastName().matches(nameCharacters))
        {
                if (requestDTO.getTerm() >= 6) {
                    if (requestDTO.getEmail().matches(emailPattern)) {
                        if ((Integer.parseInt(requestDTO.getPassportNumber()) == 6)
                                && (Integer.parseInt(requestDTO.getPassportSeries()) == 4))
                        {

                            if ((Period.between(requestDTO.getBirthdate() , LocalDate.now()).getYears() >= 18))
                            {
                                loanOffers.add(new LoanOfferDTO(loanService.generateRandomApplicationId(), requestDTO.getAmount(), totalPayment, requestDTO.getTerm(), monthlyPayment, rate, false, false));
                                LOGGER.info(requestDTO.getAmount() +" , 2 years, 10%, no services");
                                loanOffers.add(new LoanOfferDTO(loanService.generateRandomApplicationId(), requestDTO.getAmount(), totalPayment, requestDTO.getTerm(), monthlyPayment, rate,true, false));
                                LOGGER.info(requestDTO.getAmount() + " , 2 years, 8%, “bank salary client” service");
                                loanOffers.add(new LoanOfferDTO(loanService.generateRandomApplicationId(), requestDTO.getAmount(), totalPayment, requestDTO.getTerm(), monthlyPayment, rate,false, true));
                                LOGGER.info(requestDTO.getAmount() + " , 2 years, 6%, “insurance” service (insurance price put into loan’s body)");
                                loanOffers.add(new LoanOfferDTO(loanService.generateRandomApplicationId(), requestDTO.getAmount(), totalPayment, requestDTO.getTerm(), monthlyPayment, rate,true, true));
                                LOGGER.info(requestDTO.getAmount() +" , 2 years, 4%, “insurance” and “salary client” services (insurance price put into loan’s body)");
                            }
                            else {
                                LOGGER.error("age");
                            }
                        }
                        else {
                            LOGGER.error("passport");
                        }


                    }
                    else {

                        LOGGER.error("email");

                    }
                }
                else {
                    LOGGER.error("term");
                }
        }
        else {
            LOGGER.error("Please enter a valid name");
        }

        return loanOffers;
    }


    public CreditDTO offerLoanCalculation(ScoringDataDTO requestDto)
    {
        response.setRate(rate);
        rate = response.getRate().divide(new BigDecimal(100), RoundingMode.HALF_UP);


        if(requestDto.getEmployment().getEmploymentStatus().toString().matches(EmploymentStatus.UNEMPLOYED.name())){
             LOGGER.error("Sorry you don't qualify for a loan");
            throw new QualifyLoanException("Sorry you don't qualify for loan");

         }
         if(requestDto.getEmployment().getEmploymentStatus().toString().matches(EmploymentStatus.SELFEMPLOYED.name()))
         {
           rate =  rate.add(new BigDecimal(1));
         }
         if(requestDto.getEmployment().getEmploymentStatus().toString().matches(EmploymentStatus.BUSINESSOWNER.name())){

             rate = rate.add(new BigDecimal(3));
         }
         if(requestDto.getMaritalStatus().toString().matches(MaritalStatus.MARRIED.name())){

             rate = rate.subtract(new BigDecimal(3));
         }
         if(requestDto.getMaritalStatus().toString().matches(MaritalStatus.DIVORCED.name()))
         {
             rate = rate.subtract(new BigDecimal(1));
         }
         if(requestDto.getDependentAmount() > 1)
         {
             rate = rate.add(new BigDecimal(1));
         }
         if (Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() < 18
                 && Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() > 60)
         {
             LOGGER.error("Sorry you don't qualify for loan");
             throw new QualifyLoanException("Sorry you don't qualify for loan");
         }
         if (requestDto.getGender().toString().matches(Gender.FEMALE.name())
                 || Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() == 35
                 && Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() > 60)
         {
             rate = rate.subtract(new BigDecimal(3));
         }
         if (requestDto.getGender().toString().matches(Gender.MALE.name())
                 && Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() == 35
                 && Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() > 55)
         {
             rate = rate.subtract(new BigDecimal(3));
         }
        if (requestDto.getEmployment().getWorkExperienceTotal() < 12
                && requestDto.getEmployment().getWorkExperienceCurrent() < 3)
         {
             LOGGER.error("Sorry you do not qualify for a loan");
             throw new QualifyLoanException("Sorry you don't qualify for loan");
         }

        totalPayment = requestDto.getAmount().multiply(rate);

        monthlyPayment = rate.divide(new BigDecimal(requestDto.getTerm()),RoundingMode.HALF_UP).multiply(requestDto.getAmount());

        interestPayment = requestDto.getAmount().multiply(rate.divide(new BigDecimal(12), RoundingMode.HALF_UP));

        remainingDebt = loanService.remainingBalance(requestDto.getAmount(), rate, requestDto.getTerm());

        return new CreditDTO(requestDto.getAmount(), requestDto.getTerm(), monthlyPayment, rate,
                totalPayment, true, true, (List<PaymentScheduleElement>) new PaymentScheduleElement(loanService.monthlySchedule(), totalPayment,
                interestPayment, totalPayment, remainingDebt));
    }

    @Scheduled(cron = "@monthly")
    private LocalDate monthlySchedule()
    {
        return LocalDate.now();
    }

    private BigDecimal remainingBalance(BigDecimal principalAmount, BigDecimal rate, Integer term)
    {
        for (int i = 0; i < term; i++) {
            principalAmount = monthlyPayment.subtract(principalAmount.multiply(rate));
        }
        return principalAmount;
    }

    private Long generateRandomApplicationId()
    {
        return new Random().nextLong();
    }


}
