package andrea.servidor;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServidorChat {
    

    public ServidorChat(){
        new Thread(new LanzadorServidor(this)).start();
    }

    public void escribirMensaje(String mansaje){
        System.err.println(mansaje);
    }
}
