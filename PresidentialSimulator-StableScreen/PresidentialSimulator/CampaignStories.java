import java.util.*;
/** Fictional campaign scenarios. Debate schedules and costs are authored game rules. */
public final class CampaignStories {
    private CampaignStories(){}
    private static final CampaignEvent.Side P=CampaignEvent.Side.PLAYER,O=CampaignEvent.Side.OPPONENT;
    private static CampaignEvent.Effect cash(int n){return EventCatalog.cash(P,n);}
    private static CampaignEvent.Effect support(CampaignEvent.Side side,int basisPoints){return new CampaignEvent.Effect(side,CampaignEvent.Kind.SUPPORT,basisPoints,null,2);}
    private static CampaignEvent.Effect fund(int amount){return new CampaignEvent.Effect(P,CampaignEvent.Kind.FUNDRAISING,amount,null,2);}
    private static CampaignEvent.Option option(String label,CampaignEvent.Effect... effects){return new CampaignEvent.Option(label,List.of(effects));}
    private static CampaignEvent story(String id,String title,String description,CampaignEvent.Option a,CampaignEvent.Option b){return new CampaignEvent(id,title,description,CampaignEvent.Category.OPPORTUNITY,List.of(),List.of(a,b));}
    public static List<CampaignEvent> added(){return List.of(
        story("debate_economy","Debate: household costs","The moderator asks how you would address rising household costs without promising prices you cannot control.",
            option("Use a researched cost-of-living answer",cash(-100),support(P,65),support(O,25)),option("Focus on a family's experience",support(P,25),fund(35),support(O,25))),
        story("debate_record","Debate: your record","Your opponent challenges whether your promises are backed by results. The moderator gives you the floor.",
            option("Cite documented results and limits",cash(-75),support(P,60),support(O,20)),option("Acknowledge gaps and explain priorities",support(P,25),support(O,15))),
        story("debate_security","Debate: foreign affairs","The moderator presents a confrontation between two fictional states and asks how you would avoid escalation.",
            option("Explain a prepared diplomatic sequence",cash(-100),support(P,60),support(O,20)),option("Give a short statement of principles",support(P,20),fund(30),support(O,20))),
        story("live_interview","Live interview follow-up","A presenter asks for a concrete answer after your campaign's general statement. The broadcast is live.",
            option("Give the prepared explanation",cash(-50),support(P,40)),option("State what you do not yet know",support(P,15))),
        story("fact_check","Debate source request","A newsroom requests the documents behind your debate answer before publishing its follow-up.",
            option("Release sources with explanatory notes",cash(-50),support(P,35)),option("Send the existing campaign summary",support(P,-15))),
        story("ballot_help","Voter-information hotline","County volunteers report confusion about polling locations. They want verified information rather than campaign speculation.",
            option("Staff a hotline using official information",cash(-90),support(P,30)),option("Share links to election offices",support(P,10))),
        story("volunteer_retention","Volunteer shifts unfilled","Local organizers warn that long shifts are exhausting volunteers before the final push.",
            option("Pay for transport and shorter shifts",cash(-80),support(P,35)),option("Reduce the schedule to available staff",fund(-20))),
        story("accessible_venue","Venue accessibility complaint","A town-hall venue's accessible entrance is unavailable. Organizers ask whether to relocate.",
            option("Move to an accessible venue",cash(-100),support(P,30)),option("Switch to a remote question session",support(P,10),fund(-15))),
        story("ad_reservation","Advertising reservation","A broadcaster offers a short booking window. Buying now means less cash for field operations.",
            option("Buy the available airtime",cash(-140),support(P,45)),option("Keep the money for local organizing")),
        story("donor_conditions","Donor asks for privileged access","A donor asks for a private policy briefing unavailable to other supporters. Your team can set boundaries.",
            option("Offer the same public briefing to everyone",support(P,20),fund(-20)),option("Decline the meeting and move on",support(P,10))),
        story("endorsement_meeting","Local officials offer a meeting","A group of fictional local officials wants a substantive discussion before deciding whether to support your campaign.",
            option("Send a prepared policy team",cash(-80),support(P,40)),option("Keep the existing campaign schedule")),
        story("press_correction","Campaign handout contains an error","Staff find an incorrect statistic in a distributed handout. Reporters have asked for clarification.",
            option("Correct the handout and explain the error",cash(-40),support(P,20)),option("Remove it pending a full review",support(P,-10))),
        story("route_change","Airport disruption changes the route","Cancelled flights threaten two appearances. Staff present a costly reroute and a remote alternative.",
            option("Reroute the team",cash(-110),support(P,25)),option("Hold remote appearances",support(P,5),fund(-10))),
        story("rural_radio","Regional radio invitation","A regional program offers a long-form interview focused on local services and agricultural communities.",
            option("Prepare with local organizers",cash(-60),support(P,30)),option("Send a written response",support(P,5))),
        story("student_forum","Student civic forum","A nonpartisan student forum offers equal time to both campaigns. Preparation competes with fundraising.",
            option("Attend with a prepared Q&A",cash(-50),support(P,25),support(O,15)),option("Send written answers",support(P,10),support(O,15))),
        story("staff_security","Suspicious campaign login","Staff detect a suspicious account login. A security review will interrupt normal operations.",
            option("Reset access and commission a review",cash(-85),fund(-10)),option("Restrict affected accounts for now",fund(-35))),
        story("opponent_ad","Opponent releases a comparison ad","The opposing campaign buys airtime contrasting your records. Your team can answer substantively or save resources.",
            option("Publish a sourced response",cash(-70),support(P,25),support(O,20)),option("Stay on the existing message",support(O,35))),
        story("closing_address","Closing-address opportunity","A civic broadcaster offers both candidates time for a closing statement. There is no guarantee that a speech changes minds.",
            option("Prepare a concise record-and-priorities address",cash(-60),support(P,30),support(O,20)),option("Use the standard campaign statement",support(P,10),support(O,20)))
    );}
    public static List<CampaignEvent> all(){var result=new ArrayList<>(EventCatalog.all());result.addAll(added());return List.copyOf(result);}
    public static CampaignEvent byId(String id){return added().stream().filter(e->e.id().equals(id)).findFirst().orElseThrow();}
    public static String debateAt(int turn){return switch(turn){case 4->"debate_economy";case 8->"debate_record";case 12->"debate_security";default->null;};}
}
