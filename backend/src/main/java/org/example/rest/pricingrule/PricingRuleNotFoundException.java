package org.example.rest.pricingrule;

public class PricingRuleNotFoundException extends RuntimeException {
    public PricingRuleNotFoundException(Long id) {
        super("Pricing rule not found with id: " + id);
    }
}