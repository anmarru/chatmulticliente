package andrea.servidor;

import java.net.DatagramSocket;
import java.net.SocketException;

public class LanzadorServidor implements Runnable {

    public ServidorChat interfaz;



    @Override
    public void run() {
        // TODO Auto-generated method stub
        
        try (DatagramSocket serverSoket = new DatagramSocket(9876)) {
            
        } catch (SocketException e) {
            
            e.printStackTrace();
        }
        
        
        
        
    }
    
    


}
