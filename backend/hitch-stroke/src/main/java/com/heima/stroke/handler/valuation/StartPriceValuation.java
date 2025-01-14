package com.heima.stroke.handler.valuation;

public class StartPriceValuation implements Valuation {
    private Valuation valuation;

    public StartPriceValuation(Valuation valuation) {
        this.valuation = valuation;
    }

    @Override
    public float calculation(float km) {
        float cost = 0.0f;
        if(km > 3.00) cost = (float) ((km - 3.0)*2.3);
        if (valuation == null) { return cost;}
        return cost + valuation.calculation(km);
    }
}
