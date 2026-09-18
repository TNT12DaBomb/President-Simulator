import java.util.List;
/** Shared protocol for staged, time-limited situations. The host owns time and persistence.
 * Implementations expose only the information the player may currently see.
 * Future interviews/crises can use this contract without depending on Swing.
 */
public interface PressureEvent {
    CampaignEvent event();
    int seconds();
    String missed();
    void respond(int index);
    void timeout();
    void leave();
    boolean complete();
    List<String> transcript();
}
