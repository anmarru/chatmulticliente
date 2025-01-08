package andrea.cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ClienteChat {

    @FXML
    private TextField mensajeTextField;
    @FXML
    private Button enviarBoton;
    @FXML
    private ListView listachatListview;

    //---------------------------------------------------------------------

     @FXML
    private TextField inputAlias; // Campo para ingresar alias
    @FXML
    private Button btnConectar; // Botón para conectarse
    @FXML
    private TextField inputMensaje; // Campo para escribir mensajes
    @FXML
    private Button btnEnviar; // Botón para enviar mensaje
    @FXML
    private TextArea chatArea; // Área para mostrar mensajes de chat
    @FXML
    private ListView<String> listaUsuarios; // Lista de usuarios conectados
    @FXML
    private Button btnDesconectar; // Botón para desconectarse

    private Socket socketCliente;
    private DataInputStream entrada;
    private DataOutputStream salida;

    private static final String HOST = "127.0.0.1"; // Dirección del servidor
    private static final int PUERTO = 4444; // Puerto del servidor

    private Thread hiloLectura;

    @FXML
    public void initialize() {
        // Inicializar botones y campos
        btnEnviar.setDisable(true);
        btnDesconectar.setDisable(true);
    }

    @FXML
    private void conectar() {
        try {
            String alias = inputAlias.getText().trim();
            if (alias.isEmpty()) {
                chatArea.appendText("Debes ingresar un alias para conectarte.\n");
                return;
            }

            // Conectarse al servidor
            socketCliente = new Socket(HOST, PUERTO);
            entrada = new DataInputStream(socketCliente.getInputStream());
            salida = new DataOutputStream(socketCliente.getOutputStream());

            // Enviar comando de conexión
            salida.writeUTF("CON " + alias);

            // Leer respuesta del servidor
            String respuesta = entrada.readUTF();
            if (respuesta.equals("OK")) {
                chatArea.appendText("Conectado como: " + alias + "\n");

                // Habilitar botones de chat
                btnEnviar.setDisable(false);
                btnDesconectar.setDisable(false);
                btnConectar.setDisable(true);
                inputAlias.setDisable(true);

                // Iniciar hilo para leer mensajes
                iniciarLectura();
            } else {
                chatArea.appendText("Error al conectarse: " + respuesta + "\n");
            }
        } catch (IOException e) {
            chatArea.appendText("Error al conectar con el servidor: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void enviarMensaje() {
        try {
            String mensaje = inputMensaje.getText().trim();
            if (!mensaje.isEmpty()) {
                salida.writeUTF("MSG " + mensaje); // Enviar mensaje al servidor
                inputMensaje.clear();
            }
        } catch (IOException e) {
            chatArea.appendText("Error al enviar el mensaje: " + e.getMessage() + "\n");
        }
    }

    @FXML
    private void desconectar() {
        try {
            salida.writeUTF("EXI"); // Enviar comando de salida al servidor
            socketCliente.close();

            chatArea.appendText("Desconectado del servidor.\n");

            // Deshabilitar botones de chat
            btnEnviar.setDisable(true);
            btnDesconectar.setDisable(true);
            btnConectar.setDisable(false);
            inputAlias.setDisable(false);

            if (hiloLectura != null) {
                hiloLectura.interrupt();
            }
        } catch (IOException e) {
            chatArea.appendText("Error al desconectarse: " + e.getMessage() + "\n");
        }
    }

    private void iniciarLectura() {
        hiloLectura = new Thread(() -> {
            try {
                while (true) {
                    String mensaje = entrada.readUTF();
                    Platform.runLater(() -> chatArea.appendText(mensaje + "\n"));

                    // Si es un comando LST, actualizar lista de usuarios
                    if (mensaje.startsWith("LST")) {
                        String[] usuarios = mensaje.substring(4).split(", ");
                        Platform.runLater(() -> {
                            listaUsuarios.getItems().clear();
                            listaUsuarios.getItems().addAll(usuarios);
                        });
                    }
                }
            } catch (IOException e) {
                Platform.runLater(() -> chatArea.appendText("Conexión cerrada.\n"));
            }
        });
        hiloLectura.setDaemon(true);
        hiloLectura.start();
    }
    
}
