package andrea.servidor;

import java.io.DataOutputStream;
import java.net.Socket;

public class Cliente {
    
    private String alias;
    //mi clase socket que conecta el cliente al servidor (la comunicacion entre el cliente y el servidor)
    private final Socket socketCliente;
    //salida de los datos desde el cliente al servidor (salida de datos primitivos)
    private DataOutputStream salida;
    
    public Cliente(Socket socketCliente) {
        this.socketCliente = socketCliente;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Socket getSocketCliente() {
        return socketCliente;
    }

    public DataOutputStream getSalida() {
        return salida;
    }

    public void setSalida(DataOutputStream salida) {
        this.salida = salida;
    }

    @Override
    public String toString() {
        return "Cliente [alias=" + alias + "]";
    }

    

}
