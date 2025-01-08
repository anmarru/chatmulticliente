package andrea.cliente;

import java.io.DataInputStream;

public class HiloCliente implements Runnable{
    private final DataInputStream entrada;
    


    public HiloCliente(DataInputStream entrada) {
        this.entrada = entrada;
    }



    @Override
    public void run() {
        while (true) {
            try {
                String mensajeRecibido = entrada.readUTF();
                System.out.println(mensajeRecibido);
            } catch (Exception e) {
                System.out.println("Error al recibir mensaje: " + e.getMessage());
                break;
            }
        }
        
    }
        
}
