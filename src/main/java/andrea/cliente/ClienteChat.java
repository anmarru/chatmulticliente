package andrea.cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Optional;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClienteChat {

    @FXML
    private TextField inputAlias;
    @FXML
    private Button btnConectar;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField inputMensaje;
    @FXML
    private Button btnEnviar;
    @FXML
    private ListView<String> listaUsuarios;
    @FXML
    private Button btnDesconectar;

    private Socket socket;// para conetarse al servidor
    private DataInputStream entrada;// flujo de entrada para recibir los mensajes del servidor
    private DataOutputStream salida;// flujo de salida para enviar enviar mej al servidor
    private String alias;

    @FXML
    private void conectar(ActionEvent event) {
        alias = inputAlias.getText().trim();
        if (alias.isEmpty()) {
            mostrarMensaje("Debes ingresar un alias antes de conectarte.");
            return;
        }

        try {
            socket = new Socket("172.18.184.85", 4444);
            //socket = new Socket("localhost", 4444);
            
            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());

            // enviar comando de conexión al servidor
            salida.writeUTF("CON " + alias);

            //escuchar respuesta del servidor para validar la conexión
          /*   String respuesta = entrada.readUTF();
            if (respuesta.equals("NOK Alias duplicado")) {
                mostrarMensaje("Alias duplicado, elige otro.");
                socket.close();
                return;
            } */

            // inicia un hilo para escuchar mensajes del servidor
            Thread listenerThread = new Thread(this::escucharMensajes);
            // hilo de usuario no se ve
            listenerThread.setDaemon(true);
            listenerThread.start();

            //mostrarMensaje("Conectado al servidor como: " + alias);
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
            salida.writeUTF("MSG "+mensaje);
            inputMensaje.clear();
        } catch (Exception e) {
            mostrarMensaje("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    /*@FXML
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
    }*/
    @FXML
private void desconectar(ActionEvent event) {
    // Mostrar una alerta de confirmación antes de proceder
    Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
    alerta.setTitle("Confirmar desconexión");
    alerta.setHeaderText("¿Estás seguro de que deseas desconectarte?");
    alerta.setContentText("Si te desconectas, se cerrará la ventana.");

    // Esperar la respuesta del usuario
    Optional<ButtonType> resultado = alerta.showAndWait();
    if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
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
                // Obtener el Stage desde el evento o algún nodo
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.close(); // Cerrar la ventana
            });
        }
    } else {
        // El usuario canceló la desconexión
        mostrarMensaje("La desconexión fue cancelada.");
    }
}


    // se mantiene escuchando los mensajes del servidormientras esta conectado
    // los mensajes recibidos se pueden enseñar en la ui usando Platform.runLater()
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
        if (mensaje.startsWith("LST")) {
            // Actualizar lista de usuarios conectados
            String[] usuarios = mensaje.substring(4).split(", ");
            // listaUsuarios.getItems().setAll(usuarios);
            Platform.runLater(() -> listaUsuarios.getItems().setAll(usuarios));

        } else {
            // Mostrar mensaje en el área de chat
            chatArea.appendText(mensaje + "\n");
        }
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