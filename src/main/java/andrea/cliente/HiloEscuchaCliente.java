 package andrea.cliente;

import javafx.application.Platform;

import java.io.DataInputStream;

public class HiloEscuchaCliente implements Runnable{
    private final DataInputStream entrada;
    private final ControladorCliente clienteController;
     

    public HiloEscuchaCliente(DataInputStream entrada, ControladorCliente clienteController) {
        this.entrada = entrada;
        this.clienteController=clienteController;
    }



    @Override
    public void run() {
        while (true) {
            try {
                String mensajeRecibido = entrada.readUTF();
                System.out.println(mensajeRecibido);

                switch (mensajeRecibido.substring(0,3){
                    case "CON":
                        Platform.runLater(()->clienteController.addUsuarioNuevo(mensajeRecibido.substring(4)));
                        break;
                    case "CHT":
                        String[] mensajeSplit= mensajeRecibido.split(" ",3);
                        Platform.runLater(()->clienteController.recibirMensaje(mensajeSplit[1]+ ": "+ mensajeSplit[2]));
                        break;
                    case "PRV":
                        String[] mensajeSplit= mensajeRecibido.split(" ",3);
                        Platform.runLater(()->clienteController.recibirMensajePrivado(mennsaje));
                        break;
                }

            } catch (Exception e) {
                System.out.println("Error al recibir mensaje: " + e.getMessage());
                break;
            }
        }
        
    }
        
}
