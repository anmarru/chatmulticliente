package andrea.servidor;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class LanzadorServidor {

    ArrayList <HiloConexion> conexiones;




    public static void main () {
        // TODO Auto-generated method stub
        
        try (DatagramSocket serverSoket = new DatagramSocket(9876)) {
            //servidor.escribirMensaje("### Servidor iniciado");

            
            
        } catch (SocketException e) {
            
            e.printStackTrace();
        }
        
    }
    
    


}
