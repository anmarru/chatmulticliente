package andrea.servidor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.Optional;

public class HiloConexion implements Runnable {

    private final Cliente cliente;
    private final List<Cliente> listaClientes;

    // constantes para los comandos
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

    public HiloConexion(Cliente cliente, List<Cliente> listaClientes) {
        this.cliente = cliente;
        this.listaClientes = listaClientes;
    }

    @Override
    public void run() {
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
            String parametro = parametroComandos.length > 1 ? parametroComandos[1] : "";

            //validacion de conexion si no enviamos al cliente NOK
            if (!CON.equals(comando)) {
                salida.writeUTF(NOK);
                return;
            }

            while (cliente.getSocketCliente().isConnected()) {
                mensajeRecibido = entrada.readUTF();
                parametroComandos = mensajeRecibido.split(" ", 2);
                comando = parametroComandos[0];
                parametro = parametroComandos.length > 1 ? parametroComandos[1] : " ";

                switch (comando) {
                    case PRV:
                        String[] destinoMensaje = parametro.split(" ", 2);
                        if (destinoMensaje.length < 2) {
                            salida.writeUTF(NOK);
                            continue;
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
                            salida.writeUTF(NOK);
                        }
                        break;

                    case EXI:
                        System.out.println(cliente.getAlias() + " Se desconecto");
                        return;

                    case NOP:
                        // TODO completar
                        salida.writeUTF(NOK);
                        break;
                        // envio la lista de clientes conectados
                    case LST:
                        StringBuilder clientes = new StringBuilder();
                        for (Cliente c : listaClientes) {
                            clientes.append(c.getAlias()).append(", ");
                        }
                        salida.writeUTF(LST + " " + clientes.toString().trim());
                        break;
                    // mensaje a todos
                    case MSG:
                        for (Cliente c : listaClientes) {
                            if (!c.equals(cliente)) {
                                c.getSalida().writeUTF(MSG + " " + cliente.getAlias() + ": " + parametro);
                            }
                        }
                        break;

                    case CHT:
                        //formato "CHT usuario mensaje"
                        String[] splitUsuarioMensaje = parametro.split(" ", 2);
                        if (splitUsuarioMensaje.length < 2) {
                            salida.writeUTF(NOK); //parametros incorrectos
                            continue;
                        }

                        String remitente = splitUsuarioMensaje[0]; //nombre del remitente
                        String mensajeParaTodos = splitUsuarioMensaje[1]; //mensaje para todos

                        //mensaje a todos los clientes conectados
                        for (Cliente c : listaClientes) {
                            DataOutputStream salidaCliente = c.getSalida();
                            salidaCliente.writeUTF(CHT + " " + remitente + ": " + mensajeParaTodos);
                        }
                        break;

                        case POK:
                        salida.writeUTF(POK);
                        break;
                    default:
                    salida.writeUTF(NOK);
                        break;
                }
            }

        } catch (Exception e) {
        System.out.println("Error con el cliente " + cliente.getAlias() + ": " + e.getMessage());
        e.printStackTrace();
        }

    }

}
