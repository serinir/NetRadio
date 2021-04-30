package checker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Checker {
    // inner class MessData to manage Mess queries data extraction
    public static class MessData implements Data{
        private String id;
        private String message;
        MessData(String id,String message){
            this.id=id;
            this.message=message;
        }
        public String getId() {
            return id;
        }
        public String getMessage() {
            return message;
        }
    }
    // inner class LastData to manage Last queries data extraction
    public static class LastData implements Data{
        private String nb;
        LastData(String nb){
            this.nb=nb;
        }
        public int getNb() {
            return Integer.valueOf(nb);
        }
    }
    public static Data check(String mess){
        Matcher match = Pattern.compile("MESS\\s(\\w{1,8})\\s(.{1,142})").matcher(mess);
        if (match.matches())
            return new MessData(match.group(1),match.group(2));
        match = Pattern.compile("LAST\\s(\\d+)").matcher(mess);
        if (match.matches())
            return new LastData(match.group(1));
        else return null;
    }

}
