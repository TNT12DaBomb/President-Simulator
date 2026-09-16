/** Units and bounds are explicit. Scenario parameters are fictional, not empirical estimates. */
public enum WorldMetric {
    PARTY_APPROVAL("Own-party job approval", "%", 0,100,82),
    INDEPENDENT_APPROVAL("Independent job approval", "%",0,100,48),
    OPPOSITION_APPROVAL("Opposition job approval", "%",0,100,20),
    POPULARITY("Personal favorability", "%",0,100,54),
    TRUST("Public trust", "/100",0,100,55),
    UNITY("Party unity", "/100",0,100,70),
    DONORS("Donor support", "/100",0,100,55),
    REPUTATION("Career reputation", "/100",0,100,50),
    CAPITAL("Political capital", "/100",0,100,60),
    ENERGY("Personal energy", "/100",0,100,80),
    PREPAREDNESS("Emergency preparedness", "/100",0,100,45),
    RESILIENCE("Infrastructure resilience", "/100",0,100,45),
    TENSION("International tension", "/100",0,100,25),
    SCANDAL("Unresolved ethics exposure", "/100",0,100,0),
    CONFIDENCE("Consumer confidence", "/100",0,100,55),
    GROWTH("Real GDP growth (annualized model rate)", "%",-12,12,2.0),
    UNEMPLOYMENT("Unemployment", "%",1,25,4.5),
    INFLATION("Inflation (year-over-year model rate)", "%",-3,20,2.5),
    RATE("Independent central-bank policy rate", "%",0,20,3.5),
    DEFICIT("Annualized deficit / GDP", "%",-10,25,4),
    DEBT("Debt / GDP", "%",0,300,95);
    public final String label,unit; public final double min,max,initial;
    WorldMetric(String label,String unit,double min,double max,double initial){this.label=label;this.unit=unit;this.min=min;this.max=max;this.initial=initial;}
}
