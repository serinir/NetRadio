package diffuser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import checker.Checker;
import checker.Data;
import checker.Checker.MessData;

import java.text.BreakIterator;
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
        String mess_num = String.valueOf(message_number);
            while(mess_num.length()<4)
                mess_num = "0"+mess_num;
        message = ("DIFF "+ mess_num +" "+id+ " "+mess.substring(0, mess.length() > 140 ? 140 : mess.length() )+"\r\n").getBytes();
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

class Client{
    /**
     * utility class to manage client more easily
     * it takes the client socket as argument for the constructore
     * - all the Socket methods are accessible trhough the  {@link #use_sock()} method
     * - usernamer can be added through the {@link #register_username(String)} method
     * - {@link #client_id} is the id received frome the client it should be set only once
     */
    private Socket sock;
    private String username = null;
    private String client_id = null;
    Client(Socket cl){
        sock = cl;
    }
    public Socket use_sock(){
        return sock;
    }
    
    public void register_username(String username){
            this.username=username;
    }

    public void setClient_id(String client_id) {
        if(client_id != null)
            this.client_id = client_id;
    }
    public String getClient_id() {
        return client_id;
    }
    @Override
    public String toString() {
        return  this.username != null ? this.username : this.client_id != null ? this.client_id : String.valueOf(this.sock.getPort())  ;
    }
}
/**
 * <H2>The Diffuser implementations </h2>
 * @implNote 
 * 
 */
public class Diffuser{
    //CONSTANTE
    private String _ENDTOKEN = "ENDM\r\n";
    private String _ACKMTOKEN = "ACKM\r\n";
    private String _IMOK = "IMOK\r\n";
    private String _RUOK = "RUOK";
    private String _REOK = "REOK";
    private String _RENO = "RENO";


    private Map<String,String> env;
    private char[] sid = new char[8];
    private int single_port;
    private InetSocketAddress diff_ip; // Address de multi_diffusion
    private InetSocketAddress gestionnaire;
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
        this.single_port = Integer.parseInt(this.env.get("SERV_PORT"));

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
            if(messages.length()>0 && !this.last_message_sent){
                Broadcast_message(this.messages.get_message(-1));
                this.last_message_sent= true;
            }
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
     * start the tcp server : bind on port SERV_PORT
     * @see #env
     * soon to be deprecated
     */
    public void start_tcp_thread_server()  {
        try {
            chat_socket = new ServerSocket(Integer.parseInt(this.env.get("SERV_PORT")));
        } catch (NumberFormatException e){
            logerr("SERV_PORT FORMAT ISSUE");
        } catch(IOException e) {
            logerr("cannot open a new Tcp socket");
        }
        try {
            log("TCP SERVER RUNING ON : "+Inet4Address.getLocalHost().getHostAddress()+":"+this.env.get("SERV_PORT"));
        } catch (UnknownHostException e) {
            log("TCP SERVER RUNING ON : "+"localhost"+":"+this.env.get("SERV_PORT"));
        }
    }
    /**
     * listen and process requests
     * 
     */
    public void listen(){
        while(true){
            try {
                // the accept() methode throw an exception when it reaches the maximum amount of connections
                // the exception is handled by the catch and is logged
                // the accept() methode create a new instance of Socket 
                Socket client = this.chat_socket.accept(); 
                log("client " + String.valueOf(client.getPort()) +
                        client.getInetAddress().getHostAddress() +
                        " Connected");
                new Thread(()->{
                    // we save the socket client reference  
                    // car elle va etre ecraser a la prochain connection 
                    Client thread_client = new Client(client);
                    try {
                        while(true){
                            BufferedReader br = new BufferedReader(new InputStreamReader(thread_client.use_sock().getInputStream()));
                            PrintWriter pw = new PrintWriter(new OutputStreamWriter(thread_client.use_sock().getOutputStream()));
                            String message = br.readLine();
                            System.out.println(message+"of len" + message.length());
                            
                            if( message == null ) { 
                                log("client " + thread_client +" Disconnected");
                                thread_client.use_sock().close();
                                break;
                            }
                            
                            System.out.println("received this : "+message);
                            Data data = Checker.check(message);
                            if(data instanceof Checker.MessData){
                                this.messages.add_message(((MessData) data).getMessage(), ((MessData) data).getId());
                                thread_client.setClient_id(  ((MessData) data).getId()  );
                                this.last_message_sent=false;
                                pw.print(_ACKMTOKEN);
                                pw.flush();
                                thread_client.use_sock().close();
                                break;
                            }else if(data instanceof Checker.LastData){
                                int nb = ((Checker.LastData) data).getNb();
                                System.out.println(nb);
                                for (int i = 0;i<messages.length() && i < nb; i++) {
                                    pw.print("OLDM" + new String(messages.get_message( messages.length()-1-i)).substring(4) );
                                    pw.flush();
                                }
                                pw.print(_ENDTOKEN);
                                pw.flush();
                            }else{
                                logerr("received bad formated message from client: " + thread_client+" : "+ message);
                            }
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

    private Socket register(String host,int port) throws IOException{
        InetSocketAddress ia = new InetSocketAddress(host,port);
        this.sid =  "12345678".toCharArray() ;
        String sid2 = "12345678";
        String message = "REGI "+sid2+" "+this.env.get("DIFF_IP")+" "+this.diff_port+" "+ InetAddress.getLocalHost() +" "+this.single_port; 
        Socket soc = new Socket();
        soc.connect(ia);

        PrintWriter pw = new PrintWriter(new OutputStreamWriter(soc.getOutputStream()));
        BufferedReader br = new BufferedReader(new InputStreamReader(soc.getInputStream()));
        pw.write(message);
        pw.flush();
        
        String response = br.readLine();
        if( response.strip().equals(this._REOK)){
            log("Connected to the gestionnaire "+host+":"+port);
            this.gestionnaire = ia;
            return soc;
        }else if( response.equals(this._RENO)){
            logerr("Connection to the gestionnaire "+host+":"+port+" Refused");
        }
        else{
            logerr("Wrong Response from gestionnaire "+host+":"+port);
        }
        
        soc.close();//en ferme la connexion
        return null;
    }
    public void connect_gestio(String host,int port) throws IOException{
        Socket gestio_sock = register(host, port);
       
        if( gestio_sock != null ){
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(gestio_sock.getOutputStream()));
            BufferedReader br = new BufferedReader(new InputStreamReader(gestio_sock.getInputStream()));
            while(true){
                String mess = br.readLine();
                if(mess.equals(this._RUOK)){
                    pw.write(this._IMOK);
                    pw.flush();   
                }
            }
        }
    }
    /**
     * log the states in the terminals
     * @param _log
     */
    public static void log(String _log){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        System.out.println("\u001B[32m [+] \u001B[0m"+formatter.format(new Date())+" "+_log);
    }
    /**
     * log the errors in the terminal
     * @param err 
     */
    public static void logerr(String err){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        System.out.println("\u001B[31m [-] \u001B[0m"+formatter.format(new Date())+" "+err);
    }
    /**
     * @return {@link #frequencey}
     */
    public int getFrequencey() {
        return frequencey;
    }
}