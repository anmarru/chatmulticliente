package andrea.cliente;

import java.io.DataInputStream;

import javafx.application.Platform;

/**
 * Clase que representa un hilo de escucha para el cliente
 * Se encarga de recibir y procesar los mensajes enviados por el servidor
 */
public class HiloEscuchaCliente implements Runnable {
    private final DataInputStream entrada;
    private final ControladorCliente clienteController;

    /**
     * Constructor de la clase HiloEscuchaCliente
     * 
     * @param entrada           Flujo de entrada para recibir mensajes del servidor
     * @param clienteController Controlador del cliente que maneja la interfaz y la
     *                          lógica de usuario
     */
    public HiloEscuchaCliente(DataInputStream entrada, ControladorCliente clienteController) {
        this.entrada = entrada;
        this.clienteController = clienteController;
    }

    /**
     * Método que ejecuta la escucha del servidor.
     * Recibe mensajes y los procesa según el comando recibido.
     */
    @Override
    public void run() {
        while (true) {
            try {
                String mensajeRecibido = entrada.readUTF();
                System.out.println(mensajeRecibido);
                String comando = mensajeRecibido.split(" ")[0];

                switch (comando) {
                    case "CON":
                        Platform.runLater(() -> clienteController.addUsuarioNuevo(mensajeRecibido.substring(4)));
                        break;
                    case "CHT":
                        String[] mensajeSplitchat = mensajeRecibido.split(" ", 3);
                        Platform.runLater(() -> clienteController
                                .recibirMensaje(mensajeSplitchat[1] + ": " + mensajeSplitchat[2]));
                        break;
                    case "PRV":
                        String[] mensajeSplit = mensajeRecibido.split(" ", 3);
                        String mensaje = "Mensaje Privado-> " + mensajeSplit[1] + ": " + mensajeSplit[2];
                        Platform.runLater(() -> clienteController.recibirMensajePrivado(mensaje));
                        break;
                    case "LST":
                        Platform.runLater(() -> clienteController.recibirUsuarios(mensajeRecibido.substring(4)));
                        break;
                    case "EXI":
                        Platform.runLater(() -> clienteController.deleteUsuario(mensajeRecibido.substring(4)));
                        break;
                    case "OK":
                        Platform.runLater(() -> clienteController.conexionOk());
                        break;
                        case "NOK":
                        Platform.runLater(() -> clienteController.conexionNOk());
                        break;
                    default:
                        System.out.println("Comando no encontrado " + mensajeRecibido);
                }

            } catch (Exception e) {
                System.out.println("Error al recibir mensaje: " + e.getMessage());
                break;
            }
        }

    }

}
