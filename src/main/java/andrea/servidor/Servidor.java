package andrea.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal que representa el servidor del sistema
 * Se encarga de aceptar conexiones de clientes y gestionar la comunicación
 * entre ellos
 */
public class Servidor {
    private static final int PUERTO = 4444;
    private final List<Cliente> listaClientes;

    /**
     * Constructor que inicializa la lista de clientes conectados
     */
    public Servidor() {
        this.listaClientes = new ArrayList<>();
    }

    /**
     * Inicia el servidor y espera conexiones entrantes de clientes
     */
    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            while (true) {
                // aceptar nuevas conexiones
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nuevo cliente conectado desde " + socketCliente.getInetAddress());

                // crear un objeto Cliente
                Cliente nuevoCliente = new Cliente(socketCliente);

                // crear un hilo para gestionar al cliente
                HiloConexion hiloConexion = new HiloConexion(nuevoCliente, listaClientes);
                Thread hiloCliente = new Thread(hiloConexion);
                hiloCliente.start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Método principal que inicia el servidor.
     * 
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }
}
