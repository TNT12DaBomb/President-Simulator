import java.security.SecureRandom;
/** Fresh, unpredictable 64-bit seeds. No global uniqueness promise across offline installations. */
public final class GameSeeds {
 private static final SecureRandom RANDOM=new SecureRandom();
 private static Long previous;
 private GameSeeds(){}
 public static synchronized long fresh(){long seed;do{seed=RANDOM.nextLong();}while(previous!=null&&previous==seed);previous=seed;return seed;}
}
