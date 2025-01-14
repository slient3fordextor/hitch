package com.heima.stroke.handler.valuation;

public class FuelCostValuation implements Valuation {

    private Valuation valuation;

    public FuelCostValuation(Valuation valuation) {
        this.valuation = valuation;
    }

    @Override
    public float calculation(float km) {
        if(valuation == null) return 1.0f;
        return valuation.calculation(km) + 1.0f;
    }
}
