package andrea.cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ClienteChat {

    @FXML
    private TextField inputAlias; // Campo para ingresar alias
    @FXML
    private Button btnConectar; // Botón de conectar
    @FXML
    private TextArea chatArea; // Área para mostrar mensajes
    @FXML
    private TextField inputMensaje; // Campo para escribir mensajes
    @FXML
    private Button btnEnviar; // Botón para enviar mensajes
    @FXML
    private ListView<String> listaUsuarios; // Lista de usuarios conectados
    @FXML
    private Button btnDesconectar; // Botón para desconectar

    private Socket socket;
    private DataInputStream entrada;
    private DataOutputStream salida;
    private String alias;

    @FXML
    private void conectar(ActionEvent event) {
        alias = inputAlias.getText().trim();
        if (alias.isEmpty()) {
            mostrarMensaje("Debes ingresar un alias antes de conectarte.");
            return;
        }

        try {
            socket = new Socket("localhost", 4444);
            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());

            // Enviar comando de conexión al servidor
            salida.writeUTF("CON " + alias);

            // Inicia un hilo para escuchar mensajes del servidor
            Thread listenerThread = new Thread(this::escucharMensajes);
            listenerThread.setDaemon(true);
            listenerThread.start();

            mostrarMensaje("Conectado al servidor como: " + alias);
            btnConectar.setDisable(true);
            inputAlias.setDisable(true);
            btnEnviar.setDisable(false);
            btnDesconectar.setDisable(false);

        } catch (Exception e) {
            mostrarMensaje("Error al conectar al servidor: " + e.getMessage());
        }
    }

    @FXML
    private void enviarMensaje(ActionEvent event) {
        String mensaje = inputMensaje.getText().trim();
        if (mensaje.isEmpty()) {
            return;
        }

        try {
            salida.writeUTF(mensaje);
            inputMensaje.clear();
        } catch (Exception e) {
            mostrarMensaje("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    @FXML
    private void desconectar(ActionEvent event) {
        try {
            if (socket != null && !socket.isClosed()) {
                salida.writeUTF("EXI"); // Notificar al servidor que este cliente se desconecta
                socket.close();
            }
            mostrarMensaje("Desconectado del servidor.");
        } catch (Exception e) {
            mostrarMensaje("Error al desconectar: " + e.getMessage());
        } finally {
            Platform.runLater(() -> {
                btnConectar.setDisable(false);
                inputAlias.setDisable(false);
                btnEnviar.setDisable(true);
                btnDesconectar.setDisable(true);
            });
        }
    }

    private void escucharMensajes() {
        try {
            while (socket != null && socket.isConnected()) {
                String mensaje = entrada.readUTF();
                Platform.runLater(() -> procesarMensaje(mensaje));
            }
        } catch (Exception e) {
            Platform.runLater(() -> mostrarMensaje("Conexión cerrada"));
        }
    }

    private void procesarMensaje(String mensaje) {
        if (mensaje.startsWith("LUS")) {
            // Actualizar lista de usuarios conectados
            String[] usuarios = mensaje.substring(4).split(", ");
            //listaUsuarios.getItems().setAll(usuarios);
            Platform.runLater(() -> listaUsuarios.getItems().setAll(usuarios));
        } else {
            // Mostrar mensaje en el área de chat
            chatArea.appendText(mensaje + "\n");
        }
    }

    @FXML
    public void mostrarLista (){

    }

    private void mostrarMensaje(String mensaje) {
        chatArea.appendText(mensaje + "\n");
    }

    @FXML
    public void initialize() {
        btnEnviar.setDisable(true);
        btnDesconectar.setDisable(true);
    }
}