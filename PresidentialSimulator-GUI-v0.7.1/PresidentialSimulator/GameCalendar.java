import java.time.LocalDate;
import java.time.DayOfWeek;
/** Calendar helpers, separate from the game's coarse monthly scheduling. */
public final class GameCalendar {
    private GameCalendar() { }
    public static LocalDate federalElectionDay(int year) {
        LocalDate date = LocalDate.of(year, 11, 2);
        while (date.getDayOfWeek() != DayOfWeek.TUESDAY) date = date.plusDays(1);
        return date;
    }
    public static LocalDate inauguration(int year) { return LocalDate.of(year, 1, 20); }
    public static LocalDate congressConvenes(int year) { return LocalDate.of(year, 1, 3); }
}
