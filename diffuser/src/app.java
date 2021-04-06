// import java.net.SocketException;

import diffuser.Diffuser;

public class app {
   public static void main(String[] args) {
    try {
        Diffuser room = new Diffuser();
        new Thread(
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
        ).start();
        new Thread(()->{
            room.start_tcp_server();
            while(true){
                try {
                    room.listen();
                } catch (Exception e) {
                    //TODO: handle exception
                }
            }
        }).start();
    } catch (Exception e) {
        e.printStackTrace();
        Diffuser.logerr("coulden't open the Datagram sockt.");
    }
          
   } 
}
