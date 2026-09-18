import java.util.*;
/** Fictional ambient coverage; these flavor stories do not secretly change world metrics. */
public final class HeadlineCatalog {
 private HeadlineCatalog(){}
 private static final List<String> SERIOUS=List.of(
 "Local newspapers launch a shared guide to evaluating political claims",
 "Community colleges expand evening classes for working adults",
 "Transit planners seek public comment on bus-route redesigns",
 "Veterans groups organize a nationwide benefits-information drive",
 "Public libraries host workshops on spotting online misinformation",
 "Engineers call for more predictable bridge-maintenance funding",
 "Rural hospitals discuss sharing specialist appointment services",
 "Housing advocates and builders debate ways to speed up permits",
 "Small-business owners ask candidates for clearer tax guidance",
 "Election workers recruit volunteers for accessibility training",
 "Researchers present new approaches to conserving urban water",
 "School districts compare proposals for summer learning programs",
 "Coastal communities update emergency evacuation information",
 "Manufacturers and unions open talks on apprenticeship programs",
 "City councils consider longer public-comment hours",
 "Agricultural cooperatives test new soil-monitoring equipment",
 "Disability advocates review access at public meeting venues",
 "Food banks recruit delivery volunteers ahead of a busy season",
 "University researchers publish a guide to reading economic indicators",
 "Mayors exchange ideas for converting vacant commercial buildings",
 "Public health teams review emergency communication plans",
 "Independent bookstores organize forums with local journalists",
 "Consumer groups call for clearer explanations of service fees",
 "Neighborhood associations seek more shade along walking routes"
 );
 private static final List<String> LIGHT=List.of(
 "Campaign bus GPS insists the road to victory includes a boat ramp",
 "Town-hall microphone briefly gives the floor to a nearby leaf blower",
 "Local dog wins bipartisan support after stealing a ribbon-cutting photo",
 "Intern asked to make a short memo discovers the ninth appendix",
 "County fair butter sculpture receives unusually detailed policy questions",
 "Campaign office printer declares its own independence",
 "Candidate’s coffee order becomes longest item on the morning briefing",
 "Town mascot declines endorsement, citing contractual mascot neutrality",
 "Pothole named after committee still awaiting committee review",
 "Debate watch party spends twenty minutes debating pizza toppings",
 "Local cat occupies mayor’s chair; observers describe leadership as aloof",
 "Rally playlist accidentally repeats the walk-on song six times",
 "Volunteer discovers every campaign has the same box of tangled cables",
 "Bake sale cookie labeled bipartisan sells out before opening remarks",
 "Staff calendar schedules meeting to discuss excessive meetings",
 "State fair pie judge calls for a recount; requests another slice",
 "Campaign sign survives windstorm, loses argument with lawn mower",
 "Town hall audience applauds after someone finally fixes the thermostat",
 "Local marching band announces strict no-filibuster policy",
 "Newsroom coffee machine receives more attention than morning briefing",
 "Parade float gets stuck under banner reading Moving Forward",
 "Volunteer’s handwritten directions simply say turn at the big cow",
 "Library debate club tables motion on where to put the table",
 "County clerk confirms office goldfish is not a registered lobbyist"
 );
 public static List<String> forTurn(long seed,long turn){var serious=new ArrayList<>(SERIOUS);var light=new ArrayList<>(LIGHT);Collections.shuffle(serious,new Random(seed^0x53E710L));Collections.shuffle(light,new Random(seed^0x11A47L));int a=Math.floorMod(turn*2,serious.size());int b=Math.floorMod(turn*2,light.size());return List.of("Across the country: "+serious.get(a),"Across the country: "+serious.get((a+1)%serious.size()),"On a lighter note: "+light.get(b),"On a lighter note: "+light.get((b+1)%light.size()));}
}
