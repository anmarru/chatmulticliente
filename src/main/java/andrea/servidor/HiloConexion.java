package andrea.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Clase que maneja la conexión entre un cliente y el servidor
 * Implementa la interfaz Runnable para ejecutarse en un hilo separado
 */
public class HiloConexion implements Runnable {

    private final Cliente cliente;
    private final List<Cliente> listaClientes;

    // constantes para los comandos del protocolo de comunicacion
    private static final String CON = "CON";
    private static final String MSG = "MSG";
    private static final String EXI = "EXI";
    private static final String LUS = "LUS";
    private static final String PRV = "PRV";
    private static final String NOP = "NOP";
    private static final String OK = "OK";
    private static final String NOK = "NOK";
    private static final String LST = "LST";
    private static final String POK = "POK";
    private static final String CHT = "CHT";

    /**
     * Constructor que inicializa el hilo de conexión con un cliente y la lista
     * compartida de clientes conectados
     * 
     * @param cliente       El cliente que se conectará al servidor
     * @param listaClientes Lista compartida de clientes conectados al servidor
     */
    public HiloConexion(Cliente cliente, List<Cliente> listaClientes) {
        this.cliente = cliente;
        this.listaClientes = listaClientes;
    }

    /**
     * Método que ejecuta la lógica de comunicación entre el cliente y el servidor.
     * Escucha mensajes del cliente y los procesa según el protocolo definido.
     */
    @Override
    public void run() {
        // canales de entrada y salida al cliente
        try (DataInputStream entrada = new DataInputStream(cliente.getSocketCliente().getInputStream());
                DataOutputStream salida = new DataOutputStream(cliente.getSocketCliente().getOutputStream())) {

            cliente.setSalida(salida);
            String mensajeRecibido = entrada.readUTF();

            // validacion para los mensajes
            if (!mensajeRecibido.matches("^[A-Z]{3}\\s.*$")) {
                salida.writeUTF(NOK);
                return;
            }

            // separo el mansaje en dos partes comado y mensaje
            String[] parametroComandos = mensajeRecibido.split(" ", 2);
            String comando = parametroComandos[0];
            String parametro = parametroComandos.length > 1 ? parametroComandos[1] : " ";

            // validacion de conexion
            if (!CON.equals(comando)) {
                salida.writeUTF(NOK);
                return;
            }

            String nuevoAlias = parametro;
            System.out.println(nuevoAlias);

            if (listaClientes.stream().anyMatch(c -> c.getAlias().equals(nuevoAlias))) {
                salida.writeUTF(NOK);
                return;
            }

            cliente.setAlias(nuevoAlias);
            listaClientes.add(cliente);
            System.out.println(cliente.getAlias() + " conectado.");
            notificarNuevoUsuario();
            salida.writeUTF(OK);
            notificarListaUsuarios(salida);

            while (cliente.getSocketCliente().isConnected()) {
                mensajeRecibido = entrada.readUTF();
                parametroComandos = mensajeRecibido.split(" ", 2);
                comando = parametroComandos[0];
                parametro = parametroComandos.length > 1 ? parametroComandos[1] : " ";

                switch (comando) {
                    case PRV:
                        String[] destinoMensaje = parametro.split(" ", 2);
                        if (destinoMensaje.length < 2) {
                            salida.writeUTF(POK);
                            break;
                        }
                        String destinatario = destinoMensaje[0];
                        String mensaje = destinoMensaje[1];

                        Optional<Cliente> destinatarioEncontrado = listaClientes.stream()
                                .filter(c -> c.getAlias().equals(destinatario))
                                .findFirst();
                        if (destinatarioEncontrado.isPresent()) {
                            DataOutputStream salidaDestino = destinatarioEncontrado.get().getSalida();
                            salidaDestino.writeUTF(PRV + " " + cliente.getAlias() + " " + mensaje);
                        } else {
                            salida.writeUTF(POK);
                        }
                        break;

                    case EXI:

                        try {
                            System.out.println(cliente.getAlias() + " se desconectó");
                            listaClientes.remove(cliente);
                            notificarSalidaUsuario();
                            cliente.getSocketCliente().close();
                            entrada.close();
                            salida.close();
                        } catch (Exception e) {
                            System.out.println("Error al cerrar conexión del cliente " + cliente.getAlias() + ": "
                                    + e.getMessage());
                        }
                        return;

                    case NOP:
                        salida.writeUTF(NOK);
                        break;

                    case LUS:
                        StringBuilder clientes = new StringBuilder();
                        for (Cliente c : listaClientes) {
                            clientes.append(c.getAlias()).append(",");
                        }
                        salida.writeUTF(LST + " " + clientes.toString().trim());
                        break;

                    case MSG:
                        for (Cliente c : listaClientes) {
                            c.getSalida().writeUTF(CHT + " " + cliente.getAlias() + " " + parametro);
                        }
                        break;

                    default:
                        // salida.writeUTF(NOK);
                        break;
                }
            }

        } catch (Exception e) {
            System.out.println("Error con el cliente " + cliente.getAlias() + ": " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Notifica a todos los clientes que un nuevo usuario se ha conectado
     * 
     * @throws IOException Si ocurre un error al enviar el mensaje
     */
    private void notificarNuevoUsuario() throws IOException {
        for (Cliente c : listaClientes) {
            if (c != cliente)
                c.getSalida().writeUTF(CON + " " + cliente.getAlias());
        }
    }

    /**
     * Notifica a todos los clientes que un usuario se ha desconectado.
     * 
     * @throws IOException Si ocurre un error al enviar el mensaje.
     */
    private void notificarSalidaUsuario() throws IOException {
        for (Cliente c : listaClientes) {
            if (c != cliente)
                c.getSalida().writeUTF(EXI + " " + cliente.getAlias());
        }
    }

    /**
     * Envía la lista de usuarios conectados al cliente que se ha conectado.
     * 
     * @param salida Flujo de salida del cliente.
     * @throws IOException Si ocurre un error al enviar la lista.
     */
    private void notificarListaUsuarios(DataOutputStream salida) throws IOException {
        salida.writeUTF(LST + " " + listaClientes.stream()
                .map(Cliente::getAlias)
                .reduce((a, b) -> a + "," + b)
                .orElse(""));
    }
}
