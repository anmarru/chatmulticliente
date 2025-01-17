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

    //inicializo con el cliente y la lista compartida de los clientes
    public HiloConexion(Cliente cliente, List<Cliente> listaClientes) {
        this.cliente = cliente;
        //esta es la lista que comparte los clientes
        this.listaClientes = listaClientes;
    }

    @Override
    public void run() {
        //canales de entrada y salida al cliente
        try (DataInputStream entrada = new DataInputStream(cliente.getSocketCliente().getInputStream());
                DataOutputStream salida = new DataOutputStream(cliente.getSocketCliente().getOutputStream())) {

            //asigno la salida del cliente para que pueda recibir mensajes
            cliente.setSalida(salida);

            String mensajeRecibido = entrada.readUTF();

            //validacion para los mensajes
            if (!mensajeRecibido.matches("^[A-Z]{3}\\s.*$")) {
                salida.writeUTF(NOK);
                return;
            }

            //separo el mansaje en dos partes comado y mensaje
            String[] parametroComandos = mensajeRecibido.split(" ", 2);
            String comando = parametroComandos[0];
            String parametro = parametroComandos.length > 1 ? parametroComandos[1] : " ";

            // validacion de conexion si no enviamos al cliente NOK
            if (!CON.equals(comando)) {
                salida.writeUTF(NOK);
                return;
            }

            String nuevoAlias=parametro;
            System.out.println(nuevoAlias);
            
            if(listaClientes.stream().anyMatch(c->c.getAlias().equals(nuevoAlias))){
                salida.writeUTF(NOK);
                return;
            }
             // El cliente se conecta con su alias
            //if (CON.equals(comando)) {
            cliente.setAlias(nuevoAlias);
            listaClientes.add(cliente);
            System.out.println(cliente.getAlias() + " conectado.");
            notificarListaUsuarios(); // Envía la lista de usuarios a todos
            salida.writeUTF(CON +" "+cliente.getAlias());
            //continue; // Salta al siguiente ciclo
            //}
            //cliente.setAlias(parametro);

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
                    /*  System.out.println(cliente.getAlias() + " Se desconecto");
                        listaClientes.remove(cliente);
                        notificarListaUsuarios();
                        return;*/
                        try {
                            System.out.println(cliente.getAlias() + " se desconectó");
                            listaClientes.remove(cliente); // Elimina al cliente de la lista
                            notificarListaUsuarios(); // Notifica a los demás usuarios
                            cliente.getSocketCliente().close(); // Cierra el socket
                            entrada.close(); // Cierra el flujo de entrada
                            salida.close(); // Cierra el flujo de salida
                        } catch (Exception e) {
                            System.out.println("Error al cerrar conexión del cliente " + cliente.getAlias() + ": " + e.getMessage());
                        }
                        return; // Sale del método y rompe el ciclo

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
                                c.getSalida().writeUTF(CHT + " " + cliente.getAlias() + ": " + parametro);
                            }
                        }
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

    private void notificarListaUsuarios() {
        StringBuilder clientes = new StringBuilder();
        for (Cliente c : listaClientes) {
            clientes.append(c.getAlias()).append(", ");
        }
        if (clientes.length() > 0) {
            clientes.setLength(clientes.length() - 2); //elimina la última coma y espacio
        }
        String mensaje = LST + " " + clientes.toString();
    
        for (Cliente c : listaClientes) {
            try {
                c.getSalida().writeUTF(mensaje);
            } catch (Exception e) {
                System.out.println("Error al enviar lista de usuarios a " + c.getAlias());
            }
        }
    }


    

}