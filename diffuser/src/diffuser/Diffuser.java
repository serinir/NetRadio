package diffuser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Vector;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import dotenvparser.ConfigEnv;
import exceptions.ServerNotRuningException;

class Message{
    static int message_number = 0;
    private Vector<byte[]> messages_queue;
    Message(){
        messages_queue = new Vector<byte[]>();
    } 
    static byte[] format(String mess,String id){
        byte[] message;
        message = ("DIFF "+ String.valueOf(message_number)+" "+id+ " "+mess).getBytes();
        message_number++;
        return message;
    }
    void add_message(String message,String id){
        this.messages_queue.add(format(message, id));
    }
    byte[] get_message(int i){
        if (i < 0) return this.messages_queue.get(this.messages_queue.size()-1-(i+1));
        return this.messages_queue.get(i);
    }
    public int length(){
        return this.messages_queue.size();
    }
}
/**
 * <H2>The Diffuser implementations </h2>
 * @implNote 
 * 
 */
public class Diffuser{
    private Map<String,String> env;
    // private char[] sid = new char[8];
    // private int single_port;
    private InetSocketAddress diff_ip; // Address de multi_diffusion
    private int diff_port;
    private boolean connected;
    private int frequencey;
    private DatagramSocket udp_broadcast_socket;
    private Message messages;
    ServerSocket chat_socket ;
    private boolean last_message_sent = false;

    public Diffuser(){
        //Load the env Variables
        try {
            this.env = ConfigEnv.load_variables("./.env.xml");
            
        } catch (Exception e) {
            this.env = null;
            logerr("path to variable not found : ./.env.xml");
        }
        // Declare the message attribute
        messages = new Message();
        // Setup the different variables
        this.diff_port = Integer.parseInt(this.env.get("DIFF_PORT"));
        this.frequencey = Integer.parseInt(this.env.get("DIFF_FREQ")); 
        this.diff_ip = new InetSocketAddress(this.env.get("DIFF_IP"),this.diff_port);
        log("variables loaded");
        // setup the socket UDP
        try {
            this.udp_broadcast_socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
           logerr("couldn't creat the udp socket");
        }
        //logging du setup
        log("Diffuser running and diffusing to @ : "+this.env.get("DIFF_IP")+":"+this.env.get("DIFF_PORT"));        
    }

    /**
     * La fonction run demare les fonctions du diffuseur
     * @apiNote la fonction devrais tourner dans un thread apart et doit broadcast le dernier message envoyer si il est nouveau
     * @see #Broadcast_message(String)
     * @see #logerr(String)
     */
    public void run(){
        try {
            this.connected = true;
            if(messages.length()>0 && !this.last_message_sent)
                Broadcast_message(this.messages.get_message(-1));
                this.last_message_sent= true;
        } catch (Exception e) {
            e.printStackTrace();
            logerr("message not sent");
        }
    }
    /**
     * @param formated_message Message to be broadcasted throug DIFF_PORT AND DIFF_IP
     * @throws {@link ServerNotRuningException }
     * @throws IOException
     */
    private void Broadcast_message(byte[] formated_message) throws Exception{
        if(!connected) throw new ServerNotRuningException();
        DatagramPacket diffused = new DatagramPacket(formated_message,formated_message.length,this.diff_ip);
        udp_broadcast_socket.send(diffused);
        log("message broadcasted");
    }
    /**
     * log the errors in the terminal
     * @param err 
     */
    public static void logerr(String err){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        System.out.println("[-] "+formatter.format(new Date())+" "+err);
    }
    /**
     * start the tcp server : bind on port SERV_PORT
     * @see #env
     */
    public void start_tcp_server()  {
        try {
            chat_socket = new ServerSocket(Integer.parseInt(this.env.get("SERV_PORT")));
        } catch (NumberFormatException e){
            logerr("SERV_PORT FORMAT ISSUE");
        } catch(IOException e) {
            logerr("cannot open a new Tcp socket");
        }
        log("TCP SERVER ON");
    }
    /**
     * listen and process requests
     * 
     */
    public void listen(){
        while(true){
            try {
                Socket client = this.chat_socket.accept();
                log("client " + String.valueOf(client.getPort()) +
                        client.getInetAddress().getHostAddress() +
                        " Connected");
                new Thread(()->{
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        String message = br.readLine();
                        pw.print("ACKM");
                        pw.flush();
                        if(is_well_formatted_mess(message)){
                            this.messages.add_message("aha", "ihi");
                            this.last_message_sent=false;
                        }

                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }).start();
                
            } catch (IOException e) {
                logerr("cannot accept more clients");

            }
            

        }
    }
    //
    private boolean is_well_formatted_mess(String mess){
        //TODO with REGEX
        return true;
    }
    //
    synchronized private void update_messages(String message){
        try {
            messages.add_message("haloa","0001");
        } catch (Exception e) {
            logerr("couldn't add message to the queue");
        }
    }
    /**
     * log the states in the terminals
     * @param _log
     */
    public static void log(String _log){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        System.out.println("[+] "+formatter.format(new Date())+" "+_log);
    }
    /**
     * @return {@link #frequencey}
     */
    public int getFrequencey() {
        return frequencey;
    }
}