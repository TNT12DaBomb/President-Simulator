import java.util.*;
import static java.util.Objects.requireNonNull;
/** Content definitions. Prerequisites and branch flags are evaluated without drawing randomness. */
public record WorldEvent(String id,String title,String description,int weight,int earliestMonth,
                         String requiresFlag,WorldMetric belowMetric,double below,List<Choice> choices) {
    public record Choice(String label,int capitalCost,WorldEffect immediate,int delay,double successChance,
                         WorldEffect success,WorldEffect failure,String consequence) {
        public Choice {requireNonNull(label);requireNonNull(immediate);requireNonNull(success);requireNonNull(failure);requireNonNull(consequence);if(capitalCost<0||delay<1||successChance<0||successChance>1)throw new IllegalArgumentException();}
        public String preview(){return "Capital cost "+capitalCost+". Now: "+immediate.description()+". In "+delay+" months: "+Math.round(successChance*100)+"% chance ["+success.description()+"]; otherwise ["+failure.description()+"]. "+consequence;}
    }
    public WorldEvent {choices=List.copyOf(choices);if(choices.size()!=2||weight<1)throw new IllegalArgumentException();}
    public boolean eligible(int month,Set<String> flags,Map<WorldMetric,Double> metrics){return month>=earliestMonth&&(requiresFlag==null||flags.contains(requiresFlag))&&(belowMetric==null||metrics.get(belowMetric)<below);}
    public PresidencyEvent display(){return new PresidencyEvent(id,title,description,weight,earliestMonth,false,choices.stream().map(c->new PresidencyEvent.Choice(c.label(),0,new PresidencyEvent.Effect(0,c.preview()),0,null)).toList());}
}
