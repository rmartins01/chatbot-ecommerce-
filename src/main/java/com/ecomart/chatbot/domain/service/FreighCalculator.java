package com.ecomart.chatbot.domain.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.ecomart.chatbot.domain.FreightCalculatorData;

@Service
public class FreighCalculator {

    public BigDecimal calc(FreightCalculatorData data) {
        //freight calculation logic here ...

        return new BigDecimal("3.45").multiply(new BigDecimal(data.quantityProducts()));
    }

}
