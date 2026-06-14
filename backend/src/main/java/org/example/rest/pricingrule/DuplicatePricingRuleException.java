package org.example.rest.pricingrule;

public class DuplicatePricingRuleException extends RuntimeException {
    public DuplicatePricingRuleException(String name, Long roomId) {
        super("A pricing rule named '" + name + "' already exists for room id: " + roomId);
    }
}