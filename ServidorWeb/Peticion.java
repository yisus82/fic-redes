
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class Peticion extends Thread {

    private Socket sCliente = null;

    private DataOutputStream out = null;

    private String directorio = null;

    private String alias = null;

    private String indice = null;

    private File ficheroLog;

    private FileOutputStream salida;

    private String error = null;

    public Peticion(Socket sCliente, String directorio, String alias,
            String indice, FileOutputStream salida, String error) {
        this.sCliente = sCliente;
        this.directorio = directorio;
        this.alias = alias;
        this.indice = indice;
        this.salida = salida;
        this.error = error;
        setPriority(NORM_PRIORITY - 1);
    }

    void log(String mensaje) {
        String string = currentThread().toString() + " - " + mensaje;
        byte[] bytes = string.getBytes();
        try {
            salida.write(bytes);
            salida.write('\n');
        } catch (Exception e) {
            System.out.println("Error al escribir el fichero de log");
        }
    }

    public void run() {
        byte[] mensaje;
        log("Procesamos conexion");
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    sCliente.getInputStream()));
            out = new DataOutputStream(sCliente.getOutputStream());
            String cadena = "";
            int i = 0;
            do {
                cadena = in.readLine();
                if (cadena != null) {
                    log("--" + cadena + "-");
                }
                if (i == 0) {
                    i++;
                    StringTokenizer st = new StringTokenizer(cadena);
                    String token = st.nextToken();
                    if ((st.countTokens() > 0)
                            && (token.equals("GET") || token.equals("HEAD"))) {
                        boolean todo = token.equals("GET");
                        String ruta = st.nextToken();
                        String fecha = "";
                        for (int j = 0; j < st.countTokens(); j++) {
                            st.nextToken();
                            if (token.equals("If-modified-since:")) {
                                for (int k = 0; k < 6; k++) {
                                    fecha = fecha + st.nextToken() + " ";
                                }
                                break;
                            }
                        }
                        procesa(ruta, todo, fecha);
                    } else {
                        mensaje = (new String("HTTP/1.0 400 Bad Request\r\n"))
                                .getBytes();
                        out.write(mensaje);
                        out.close();
                        log("Conexion terminada");
                    }
                }

            } while (cadena != null && cadena.length() != 0);

        } catch (Exception e) {
            mensaje = (new String("HTTP/1.0 400 Bad Request\r\n")).getBytes();
            try {
                out.write(mensaje);
                out.close();
                log("Conexion terminada");
            } catch (Exception e2) {

            }
        }

        log("Conexion terminada");
    }

void procesa(String ruta, boolean todo, String fecha) {
        if (ruta.equals("/")) {
        	ruta = alias + "/" + indice;
        }
        ruta = ruta.replaceAll(alias, directorio);
        log("Buscando el fichero " + ruta + " ...");
        try {
            File fichero = new File(ruta);
            if (fichero.exists()) { 
                if (!fichero.isDirectory()){
                    if (fecha.equals("")) {
                        String tipo;
                        if (ruta.endsWith("jpg") || ruta.endsWith("jpeg"))
                            tipo = "image/jpg";
                        else if (ruta.endsWith("gif"))
                            tipo = "image/gif";
                        else if (ruta.endsWith("txt"))
                            tipo = "text/plain";
                        else if (ruta.endsWith("html") || ruta.endsWith("htm"))
                            tipo = "text/html";
                        else
                            tipo = "application/octet-stream";
                        String string = new String("HTTP/1.0 200 OK\r\n"
                                + "Server: Redes Server/1.0\r\n" + "Date: "
                                + new Date() + "\r\n" + "Content-Type:" + tipo + "\r\n"
                                + "Content-Length: " + fichero.length() + "\r\n\r\n");
                        byte[] cabecera = string.getBytes();
                        FileInputStream file = new FileInputStream(fichero);
                        if (todo) {
                            byte[] archivo = new byte[file.available()];
                            file.read(archivo);
                            byte[] respuesta = new byte[cabecera.length + archivo.length];
                            int i, j;
                            for (i = 0; i < cabecera.length; i++) {
                                respuesta[i] = cabecera[i];
                            }
                            for (j = 0; j < archivo.length; j++) {
                                respuesta[i++] = archivo[j];
                            }
                            out.write(respuesta);
                            log("Peticion procesada");
                            out.close();
                            log("Conexion terminada");
                        }
                        else {
                            out.write(cabecera);
                            log("Peticion procesada");
                            out.close();
                            log("Conexion terminada");
                        }
                    }
                    else {
                        Date fechaNavegador = (new SimpleDateFormat()).parse(fecha);
                        Date fechaFichero = new Date(fichero.lastModified());
                        System.out.println("Navegador " + fechaNavegador + 
                                "\nFichero " + fechaFichero); 
                        if (fechaFichero.after(fechaNavegador)) procesa (ruta,todo,"");
                        String string = "HTTP/1.0 304 Not modified\r\nDate: " 
                            + new Date() + "\r\nServer: Redes Server/1.0\r\n";
                        byte[] bytes = string.getBytes();
                        out.write(bytes);
                        log("Peticion procesada");
                        out.close();
                        log("Conexion terminada");
                    }
                }
                else {
                	FileFilter filtro = new FileFilter() {
                		public boolean accept(File fichero) {
                			return !((fichero.getName()).startsWith("."));
                		}
                	};
                	out.write(("HTTP/1.0 200 OK\r\n" +
                               "Content-Type: text/html\r\n" +
                               "Server: Redes Server/1.0\r\n" +
                               "Date: " + new Date() + "\r\n" +
                               "\r\n").getBytes());
                        if (todo) {
                        	out.write(("<h1>Listado de ficheros de </h1>" +
                               "<h3>" + ruta + "</h3>" +
                               "<table border=\"0\" cellspacing=\"8\">" +
                               "<tr><td><b>Nombre de archivo</b><br></td>" +
			       "<td align=\"right\"><b>Tama" + "\u00f1" + "o</b></td><td><b>" + 
			       "Modificado</b></td></tr>" +
                               "<tr><td><b><a href=\"../\">../</b><br></td><td>" + 
				"</td><td></td></tr>").getBytes());
                		File hijo;
                    		File[] hijos = fichero.listFiles(filtro);
                    		for (int i = 0; i < hijos.length; i++) {
                        		hijo = hijos[i];
                        		if (hijo.isDirectory()) {
                            			out.write(("<tr><td><b><a href=\"" + ruta +
					 	hijo.getName() + "/\">" + hijo.getName() + 
						"/</a></b></td><td></td><td></td></tr>").getBytes());
                       			}
                        		else {
                            			out.write(("<tr><td><a href=\"" + ruta + 
						hijo.getName() + "\">" + hijo.getName() + 
						"</a></td><td align=\"right\">" + hijo.length() + 
						"</td><td>" + new Date(hijo.lastModified()).toString() 
						+ "</td></tr>").getBytes());
                        		}
                        	}
                        }
                        log("Peticion procesada");
                        out.close();
                        log("Conexion terminada");
                }
            }
            else {
                log("No se encuentra el fichero " + fichero.toString());
                procesa(error, todo,""); 
            }
        } catch (Exception e) {
            log("Error al procesar la peticion");
            log("Conexion terminada");
        }
    }}
