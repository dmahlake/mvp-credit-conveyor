package com.mvplevel1.service;

import com.mvplevel1.customEnum.EmploymentStatus;
import com.mvplevel1.customEnum.Gender;
import com.mvplevel1.customEnum.MaritalStatus;
import com.mvplevel1.dto.*;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

public class OfferService {

    private static final Logger LOGGER = Logger.getLogger(OfferService.class);

    private final List<LoanOfferDTO> loanOffers = new ArrayList<>();
    private String emailPattern = "^(.+)@(.+)$";
    private String regex1 = "^[A-Z][a-z]{2,}(?: [A-Z][a-z]*)*$";

    public List<LoanOfferDTO> offers(LoanApplicationRequestDTO requestDTO )
    {


        if (requestDTO.getFirstName().matches(regex1) && requestDTO.getLastName().matches(regex1))
        {
                if (requestDTO.getTerm() >= 6) {
                    if (requestDTO.getEmail().matches(emailPattern)) {
                        if ((Integer.parseInt(requestDTO.getPassportNumber()) == 6) && (Integer.parseInt(requestDTO.getPassportSeries()) == 4))
                        {

                            if ((Period.between(requestDTO.getBirthdate() , LocalDate.now()).getYears() >= 18))
                            {
                                loanOffers.add(new LoanOfferDTO(false, false));
                                LOGGER.info(requestDTO.getAmount() +" , 2 years, 10%, no services");
                                loanOffers.add(new LoanOfferDTO(true, false));
                                LOGGER.info(requestDTO.getAmount() + " , 2 years, 8%, “bank salary client” service");
                                loanOffers.add(new LoanOfferDTO(false, true));
                                LOGGER.info(requestDTO.getAmount() + " , 2 years, 6%, “insurance” service (insurance price put into loan’s body)");
                               loanOffers.add(new LoanOfferDTO(true, true));
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

        BigDecimal monthlyPayment;
        BigDecimal totalCost;

        CreditDTO response = new CreditDTO();
        BigDecimal rate = new BigDecimal(10);
        response.setRate(rate);
        rate = response.getRate();


        if(requestDto.getEmployment().getEmploymentStatus().toString().matches(EmploymentStatus.UNEMPLOYED.name())){
             LOGGER.error("Sorry you don't qualify for a loan");
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
         if (Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() < 18 && Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() > 60)
         {
             LOGGER.error("Sorry you dont qualify for loan");
         }
         if (requestDto.getGender().toString().matches(Gender.FEMALE.name()) || Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() == 35 && Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() > 60)
         {
             rate = rate.subtract(new BigDecimal(3));
         }
         if (requestDto.getGender().toString().matches(Gender.MALE.name()) && Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() == 35 && Period.between(requestDto.getBirthdate() , LocalDate.now()).getYears() > 55)
         {
             rate = rate.subtract(new BigDecimal(3));
         }
        if (requestDto.getEmployment().getWorkExperienceTotal() < 12 && requestDto.getEmployment().getWorkExperienceCurrent() < 3)
         {
             LOGGER.error("Sorry you do not qualify for a loan");
         }
        totalCost = requestDto.getAmount().multiply(rate);
        monthlyPayment = totalCost.divide(new BigDecimal(requestDto.getTerm()),  RoundingMode.HALF_UP);

        LOGGER.info("Results " + new CreditDTO(rate, totalCost, monthlyPayment));
        return new CreditDTO(rate, totalCost, monthlyPayment);
    }

}
