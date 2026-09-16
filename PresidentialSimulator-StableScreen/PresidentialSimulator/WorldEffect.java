import java.util.*;
/** Shared typed changes, with immutable deltas and persistent story flags. */
public record WorldEffect(Map<WorldMetric,Double> changes, Set<String> addFlags, Set<String> removeFlags) {
    public WorldEffect { var copy=new EnumMap<WorldMetric,Double>(WorldMetric.class); copy.putAll(changes); for(double d:copy.values())if(!Double.isFinite(d))throw new IllegalArgumentException("Nonfinite effect"); changes=Collections.unmodifiableMap(copy);addFlags=Set.copyOf(addFlags);removeFlags=Set.copyOf(removeFlags); }
    public static WorldEffect of(Object... pairs){var map=new EnumMap<WorldMetric,Double>(WorldMetric.class);if(pairs.length%2!=0)throw new IllegalArgumentException();for(int i=0;i<pairs.length;i+=2)map.put((WorldMetric)pairs[i],((Number)pairs[i+1]).doubleValue());return new WorldEffect(map,Set.of(),Set.of());}
    public WorldEffect flag(String add){return new WorldEffect(changes,Set.of(add),Set.of());}
    public WorldEffect clear(String flag){return new WorldEffect(changes,addFlags,Set.of(flag));}
    public String description(){var parts=new ArrayList<String>();changes.forEach((m,d)->parts.add(m.label+String.format(java.util.Locale.ROOT," %+.1f",d)+(m.unit.equals("%")?" pp":"")));addFlags.stream().sorted().forEach(f->parts.add("opens path: "+f));removeFlags.stream().sorted().forEach(f->parts.add("closes path: "+f));return parts.isEmpty()?"No immediate metric change":String.join("; ",parts);}
}
