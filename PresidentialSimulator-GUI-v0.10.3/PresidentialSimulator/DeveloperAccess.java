import java.util.Arrays;
/** Convenience playtest lock, not a security boundary in this source-distributed game. */
public final class DeveloperAccess {
 private DeveloperAccess(){}
 public static boolean accepts(char[] password){try{return Arrays.equals(password,new char[]{'d','e','v','t','o','o','l','s'});}finally{Arrays.fill(password,'\0');}}
}
