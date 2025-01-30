package andrea.cliente;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
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

/**
 * Controlador de la interfaz del cliente para interactuar con servidor chat.
 * Gestiona la conexión al servidor, envío y recepción de mensajes, y la
 * interacción con los elementos de la interfaz.
 */
public class ControladorCliente {

    @FXML
    private TextField inputAlias;
    @FXML
    private Button btnConectar;
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField inputMensaje;
    @FXML
    private TextField inputDestino;
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

    /**
     * Conecta al cliente con el servidor utilizando el alias proporcionado
     * Valida el alias, establece la conexión con el servidor y habilita los
     * controles
     *
     * @param event El evento que activa la acción de conexión
     */
    @FXML
    private void conectar(ActionEvent event) {
        alias = inputAlias.getText().trim();
        if (alias.isEmpty()) {
            mostrarMensaje("Debes ingresar un alias antes de conectarte.");
            return;
        }

        try {
            // socket = new Socket("79.170.148.110", 4444);
            socket = new Socket("localhost", 4444);

            entrada = new DataInputStream(socket.getInputStream());
            salida = new DataOutputStream(socket.getOutputStream());

            // enviar comando de conexión
            salida.writeUTF("CON " + alias);
            
            // inicia un hilo para escuchar mensajes del servidor
            Thread hiloEscuchaCliente = new Thread(new HiloEscuchaCliente(entrada, this));
            // hilo de usuario no se ve
            // hiloEscuchaCliente.setDaemon(true);
            hiloEscuchaCliente.start();

            // mostrarMensaje("Conectado al servidor como: " + alias);
            btnConectar.setDisable(true);
            inputAlias.setDisable(true);
            listaUsuarios.setVisible(true);
            btnEnviar.setDisable(false);
            btnDesconectar.setDisable(false);

        } catch (Exception e) {
            mostrarMensaje("Error al conectar al servidor: " + e.getMessage());
        }
    }

    /**
     * Envía un mensaje al servidor. Puede ser un mensaje público o privado
     *
     * @param event El evento que activa el envío del mensaje
     */
    @FXML
    private void enviarMensaje(ActionEvent event) {
        String mensaje = inputMensaje.getText().trim();

        String destinatario = inputDestino.getText().trim();

        if (mensaje.isEmpty()) {
            return;
        }

        try {
            if (!destinatario.isEmpty()) {
                // mensaje privado
                salida.writeUTF("PRV " + destinatario + " " + mensaje);
                chatArea.appendText("Tú -> " + destinatario + ": " + mensaje + "\n");
            } else {
                // mensaje público

                salida.writeUTF("MSG " + mensaje);
            }
            inputMensaje.clear();
            inputDestino.clear(); // limpiar el destinatario después de enviar
        } catch (Exception e) {
            mostrarMensaje("Error al enviar el mensaje: " + e.getMessage());
        }
    }

    /**
     * Desconecta al cliente del servidor con una confirmación previa por parte del
     * usuario
     * Muestra un mensaje de confirmación y cierra la ventana si el usuario acepta
     *
     * @param event El evento que activa la acción de desconexión.
     */
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
                    salida.writeUTF("EXI"); // notificar al servidor que este cliente se desconecta
                    socket.close();
                }
                mostrarMensaje("Desconectado del servidor.");
            } catch (Exception e) {
                mostrarMensaje("Error al desconectar: " + e.getMessage());
            } finally {
                Platform.runLater(() -> {
                    Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                    stage.close(); //cerrar la ventana
                });
            }
        } else {
            // usuario canceló la desconexión
            mostrarMensaje("La desconexión fue cancelada.");
        }
    }

    /**
     * Muestra un mensaje en el área de chat
     *
     * @param mensaje El mensaje que se mostrará en el área de chat
     */
    private void mostrarMensaje(String mensaje) {
        chatArea.appendText(mensaje + "\n");
    }

    /**
     * Inicializa el controlador. Configura los botones y campos de la interfaz al
     * inicio
     */
    @FXML
    public void initialize() {
        btnEnviar.setDisable(true);
        btnDesconectar.setDisable(true);
    }

    /**
     * Añade un nuevo usuario a la lista de usuarios conectados
     *
     * @param usuario El nombre del nuevo usuario que se añade a la lista
     */
    public void addUsuarioNuevo(String usuario) {
        listaUsuarios.getItems().add(usuario);
    }

    /**
     * Recibe un mensaje general y lo muestra en el área de chat
     *
     * @param mensaje El mensaje que se recibirá y mostrará en el chat
     */
    public void recibirMensaje(String mensaje) {
        chatArea.appendText(mensaje + "\n");
    }

    /**
     * Recibe un mensaje privado y lo muestra en el área de chat
     *
     * @param mensaje El mensaje privado que se recibirá y mostrará en el chat
     */
    public void recibirMensajePrivado(String mensaje) {
        chatArea.appendText(mensaje + "\n");
    }

    /**
     * Actualiza la lista de usuarios conectados
     *
     * @param usuarios Una cadena de texto que contiene los usuarios conectados,
     *                 separados por comas
     */
    public void recibirUsuarios(String usuarios) {
        List<String> lista = Arrays.asList(usuarios.split(","));
        listaUsuarios.getItems().clear();
        listaUsuarios.getItems().addAll(lista);
    }

    /**
     * Elimina un usuario de la lista de usuarios conectados
     *
     * @param usuario El nombre del usuario que se debe eliminar de la lista
     */
    public void deleteUsuario(String usuario) {
        listaUsuarios.getItems().remove(usuario);
    }

    /**
     * Muestra un mensaje en el área de chat indicando que la conexión ha sido
     * establecida correctamente
     */
    public void conexionOk() {
        chatArea.appendText(" conexion establecida\n");
    }

    public void conexionNOk() {
        chatArea.appendText("ERROR: Usuario con el mismo alias!!\n");
        btnEnviar.setDisable(true);
        btnDesconectar.setDisable(true);
        inputAlias.setDisable(false);
        btnConectar.setDisable(false);
    }
}