package andrea.servidor;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class LanzadorServidor implements Runnable {

    public ServidorChat servidor;
    ArrayList <HiloConexion> conexiones; 

    LanzadorServidor(ServidorChat interfaz){
        this.servidor= servidor;
        this.conexiones= conexiones;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        
        try (DatagramSocket serverSoket = new DatagramSocket(9876)) {
            servidor.escribirMensaje("### Servidor iniciado");

            while () {
                
            }
            
        } catch (SocketException e) {
            
            e.printStackTrace();
        }
        
    }
    
    


}
