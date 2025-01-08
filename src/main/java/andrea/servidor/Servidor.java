package andrea.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Servidor {
    private static final int PUERTO= 4444;
    private final List<Cliente> listaClientes;

    public Servidor() {
        this.listaClientes = new ArrayList<>();
    }

    public void iniciar() {
        try (ServerSocket serverSocket = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            while (true) {
                // Aceptar nuevas conexiones
                Socket socketCliente = serverSocket.accept();
                System.out.println("Nuevo cliente conectado desde " + socketCliente.getInetAddress());

                // Crear un objeto Cliente
                Cliente nuevoCliente = new Cliente(socketCliente);

                // Añadir el cliente a la lista de clientes
                synchronized (listaClientes) {
                    listaClientes.add(nuevoCliente);
                }

                // Crear un hilo para gestionar al cliente
                HiloConexion hiloConexion = new HiloConexion(nuevoCliente, listaClientes);
                Thread hiloCliente = new Thread(hiloConexion);
                hiloCliente.start();
            }
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Servidor servidor = new Servidor();
        servidor.iniciar();
    }
}
