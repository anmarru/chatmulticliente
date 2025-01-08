package andrea.servidor;

public class ServidorChat {
    
    
    public ServidorChat(){
        //new Thread(new LanzadorServidor(this)).start();
    }

    public void escribirMensaje(String mansaje){
        System.err.println(mansaje);
    }
}
