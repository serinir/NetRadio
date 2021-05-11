// import java.net.SocketException;

import diffuser.Diffuser;

public class app {
   public static void main(String[] args) {
    Diffuser room = new Diffuser();
    Thread diffusion = new Thread(
         ()->{
             while(true){
                 try {
                     room.run();
                     Thread.sleep(room.getFrequencey());
                 } catch (InterruptedException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }
     );
     Thread ecoute = new Thread(()->{
         room.start_tcp_thread_server();
         while(true){
             try {
                 room.listen();
             } catch (Exception e) {
                 //TODO: handle exception
             }
         }
     });
     Thread alive = new Thread( ()->{
         try{
             room.connect_gestio(args[1],Integer.parseInt(args[2]));
         }catch (Exception e){
             e.printStackTrace();
         }
     });
    try {
        diffusion.start();
        ecoute.start();
        alive.start();
    } catch (Exception e) {
        e.printStackTrace();
    }
          
   } 
}
