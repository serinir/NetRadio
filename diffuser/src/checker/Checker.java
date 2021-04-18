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
        public String getNb() {
            return nb;
        }
    }

    public static Data is_MESS(String mess){
        Matcher mess_match = Pattern.compile("MESS\\s(\\d{1,4})\\s(\\w{1,140})").matcher(mess);
        if (mess_match.matches())
            return new MessData(mess_match.group(1),mess_match.group(2));
        return null;
    }
    public static Data is_LAST(String mess){
        Matcher last_match = Pattern.compile("LAST\\s\\(d+)").matcher(mess);
        if (last_match.matches())
            return new LastData(last_match.group(1));
        return null;
    }

}
