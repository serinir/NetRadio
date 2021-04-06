package exceptions;

public class ServerNotRuningException extends Exception{
    /**
     *
     */
    private static final long serialVersionUID = 665361720546907227L;

    public ServerNotRuningException(){
        super("server not connected");
    }
}
