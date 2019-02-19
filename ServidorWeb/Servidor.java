
import java.io.File;
import java.io.FileOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private int puerto;

    private String directorio = null;

    private String alias = null;

    private String indice = null;

    private File ficheroLog;

    private String error = null;
    
    private FileOutputStream salida;

    public Servidor(String[] param, int puerto, String directorio,
            String alias, String indice, String log, String error) {
        this.puerto = puerto;
        this.directorio = directorio;
        this.alias = alias;
        this.indice = indice;
        ficheroLog = new File(log);
        this.error = error;

        try {
            ficheroLog.createNewFile();
            salida = new FileOutputStream(ficheroLog);
        } catch (Exception e) {
                System.out.println("Error al crear el fichero de log");
        }
        
                
    }

    public void log(String mensaje) {
        String string = "Mensaje: " + mensaje;
        byte[] bytes = string.getBytes();
        try {
            salida.write(bytes);
            salida.write('\n');
        } catch (Exception e) {
            System.out.println("Error al escribir el fichero de log");
        }
    }

    public boolean arranca() {
        log("Arranca el servidor");
        try {
            ServerSocket sServidor = new ServerSocket(puerto);
            log("A la espera de conexiones");
            while (true) {
                Socket sEntrante = sServidor.accept();
                Peticion pCliente = new Peticion(sEntrante, directorio, alias,
                        indice, salida, error);
                pCliente.start();
            }

        } catch (Exception e) {
            log("Error en el servidor\n" + e.toString());
        }
        return true;
    }

}