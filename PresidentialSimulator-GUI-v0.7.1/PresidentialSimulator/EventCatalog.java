import java.util.ArrayList;
import java.util.List;
import static java.util.List.of;

/** Fictional operational events, with no real candidates or political judgments. */
public final class EventCatalog {
    private static final CampaignEvent.Side P = CampaignEvent.Side.PLAYER;
    private static final CampaignEvent.Side O = CampaignEvent.Side.OPPONENT;
    private static final CampaignEvent.Side B = CampaignEvent.Side.BOTH;
    private static final State.Task H = State.Task.TOWN_HALL;
    private static final State.Task F = State.Task.FIELD_OFFICE;
    private static final State.Task R = State.Task.OUTREACH;
    private EventCatalog() { }
    static CampaignEvent.Effect cash(CampaignEvent.Side side, int amount) {
        return new CampaignEvent.Effect(side, CampaignEvent.Kind.FUNDS, amount, null, 0);
    }
    static CampaignEvent.Effect task(CampaignEvent.Side side, State.Task task, boolean complete) {
        return new CampaignEvent.Effect(side, complete ? CampaignEvent.Kind.COMPLETE_TASK : CampaignEvent.Kind.REMOVE_TASK, 0, task, 0);
    }
    private static CampaignEvent money(String id, String title, String text, CampaignEvent.Side side, int amount) {
        return new CampaignEvent(id, title, text, CampaignEvent.Category.FUNDING, of(cash(side, amount)), of());
    }
    private static CampaignEvent work(String id, String title, String text, CampaignEvent.Side side, State.Task task, boolean complete) {
        return new CampaignEvent(id, title, text, complete ? CampaignEvent.Category.VOLUNTEERS : CampaignEvent.Category.DISRUPTION,
            of(task(side, task, complete)), of());
    }
    private static CampaignEvent timed(String id, String title, String text, CampaignEvent.Side side, State.Task task, int amount) {
        return new CampaignEvent(id, title, text, CampaignEvent.Category.OPERATIONS,
            of(new CampaignEvent.Effect(side, task == null ? CampaignEvent.Kind.FUNDRAISING : CampaignEvent.Kind.ACTION_COST, amount, task, 2)), of());
    }
    private static CampaignEvent choice(String id, String title, String text, String accept, List<CampaignEvent.Effect> effects) {
        return new CampaignEvent(id, title, text, CampaignEvent.Category.OPPORTUNITY, of(),
            of(new CampaignEvent.Option(accept, effects), new CampaignEvent.Option("Decline; keep current resources", of())));
    }
    public static List<CampaignEvent> all() {
        List<CampaignEvent> events = new ArrayList<>();
        events.add(money("small_donations", "Small-donor weekend", "Your campaign receives an unexpected batch of contributions.", P, 180));
        events.add(money("bus_repair", "Campaign bus repair", "Your team's bus needs a repair before its next trip.", P, -120));
        events.add(money("rival_dinner", "Opponent fundraising dinner", "The opponent's fictional fundraising event brings in extra cash.", O, 180));
        events.add(money("rival_invoice", "Opponent printing invoice", "The opponent receives an unplanned printing bill.", O, -150));
        events.add(money("travel_surcharge", "Travel surcharge", "A supplier adds a travel charge to both campaigns' bookings.", B, -80));
        events.add(money("venue_refunds", "Venue refunds", "Both campaigns receive refunds on deposits.", B, 90));
        events.add(money("merchandise", "Merchandise shipment sold", "Your team sells surplus campaign merchandise.", P, 130));
        events.add(money("rival_refund", "Opponent equipment refund", "The opponent receives an equipment deposit back.", O, 130));
        events.add(work("local_hosts", "Volunteer hosts step in", "Local volunteers organize one of your outstanding town halls.", P, H, true));
        events.add(work("office_team", "Office team ready", "A volunteer team sets up one of your required field offices.", P, F, true));
        events.add(work("outreach_team", "Outreach team arrives", "A volunteer team completes one of your outreach objectives.", P, R, true));
        events.add(work("rival_hosts", "Opponent hosts step in", "Local hosts complete an outstanding town hall for the opponent.", O, H, true));
        events.add(work("rival_office", "Opponent office opens", "The opponent gains a completed field-office objective.", O, F, true));
        events.add(work("rival_outreach", "Opponent outreach team", "A volunteer team completes an opponent outreach objective.", O, R, true));
        events.add(work("townhall_followup", "Town hall follow-up required", "A completed town-hall objective is reopened because its follow-up session fell through.", P, H, false));
        events.add(work("office_closure", "Field office closes", "A lease problem reopens one of your completed field-office objectives.", P, F, false));
        events.add(work("outreach_rework", "Outreach follow-up lost", "A coordination failure means one outreach objective must be completed again.", P, R, false));
        events.add(work("rival_townhall", "Opponent follow-up cancelled", "The opponent must redo a town-hall objective after its follow-up fell through.", O, H, false));
        events.add(work("rival_closure", "Opponent office closes", "A lease problem reopens an opponent field-office objective.", O, F, false));
        events.add(work("rival_rework", "Opponent outreach rework", "The opponent must redo an outreach objective after a coordination failure.", O, R, false));
        events.add(timed("venue_discount", "Discounted venue bookings", "Your town halls cost less for the next two turns.", P, H, -40));
        events.add(timed("rival_rent", "Opponent rental discount", "Opponent field offices cost less for the next two turns.", O, F, -50));
        events.add(timed("transport_delay", "Outreach transport costs", "Both campaigns pay extra for outreach during the next two turns.", B, R, 40));
        events.add(timed("donation_tools", "Fundraising tools donated", "Your fundraising actions collect additional cash for two turns.", P, null, 100));
        events.add(timed("rival_payment_delay", "Opponent payment delays", "Opponent fundraising actions collect less cash for two turns.", O, null, -80));
        events.add(timed("office_supplies", "Field-office supply shortage", "Your field-office actions cost extra for two turns.", P, F, 50));
        events.add(timed("rival_venue_cost", "Opponent venue costs rise", "Opponent town halls cost extra for two turns.", O, H, 40));
        events.add(timed("shared_donation_tools", "Fundraising platform upgrade", "Both campaigns collect additional cash when fundraising for two turns.", B, null, 60));
        events.add(choice("venue_offer", "A venue opens up", "A host offers to complete a town-hall objective if you cover the booking.",
            "Pay $100; complete this town-hall objective", of(cash(P, -100), task(P, H, true))));
        events.add(choice("office_offer", "Ready-to-use office", "A local team can open an office immediately if you fund its setup.",
            "Pay $150; complete this field-office objective", of(cash(P, -150), task(P, F, true))));
        events.add(choice("outreach_offer", "Weekend outreach opening", "Volunteers can complete an outreach objective with supplies you provide.",
            "Pay $100; complete this outreach objective", of(cash(P, -100), task(P, R, true))));
        events.add(choice("training_offer", "Volunteer training offer", "Training now can lower your outreach costs over the next two turns.",
            "Pay $75; outreach costs $50 less for two turns", of(cash(P, -75), new CampaignEvent.Effect(P, CampaignEvent.Kind.ACTION_COST, -50, R, 2))));
        events.add(choice("fundraising_offer", "Fundraising workshop", "A workshop can improve the proceeds of your next two turns' fundraising actions.",
            "Pay $75; fundraising earns $100 more for two turns", of(cash(P, -75), new CampaignEvent.Effect(P, CampaignEvent.Kind.FUNDRAISING, 100, null, 2))));
        events.add(choice("shared_booking", "Shared booking refund", "A joint cancellation releases deposits only if both campaigns accept the refund.",
            "Accept: both campaigns receive $125", of(cash(P, 125), cash(O, 125))));
        events.add(new CampaignEvent("quiet_week", "A quiet news cycle", "Both teams continue their planned work. No additional effects.", CampaignEvent.Category.QUIET, of(), of()));
        events.add(new CampaignEvent("routine_week", "Routine campaign week", "Schedules hold and no unexpected costs arise. No additional effects.", CampaignEvent.Category.QUIET, of(), of()));
        return List.copyOf(events);
    }
}
