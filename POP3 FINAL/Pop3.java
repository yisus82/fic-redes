import java.io.*;
import java.util.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



class Pop3 {

  private String user;
  private String password;
  private String host;
  private Vector mensaje;
  private Vector detecta_fallos;
  private StringTokenizer num_mens;
  private StringTokenizer fallo;
  private String recibido;
  private String punto;
  private int numero;
  private VentanaPOP3 ventana;
  private String mensajesABorrar;
  private Socket socket;
  private BufferedReader entrada;
  private PrintWriter salida;
  private String resultado;
  private Dialog dialogo;
  private Button boton;

  Pop3(String login,char[] pass,VentanaPOP3 vent) {
      host = "localhost";
      user = login.trim();
      password = new String(pass);
      int puerto = 110;
      mensaje = new Vector();
      detecta_fallos = new Vector();
      ventana = vent;
      boton = new Button("Aceptar");   

      try {
             socket = new Socket(host,puerto);   
		 entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       	 salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);
		
             entrada.readLine();
		 salida.println("USER " + user);
             System.out.println(entrada.readLine());  

             salida.println("PASS " + password);
             resultado = entrada.readLine();
             System.out.println(resultado);
             fallo = new StringTokenizer(resultado);
             while (fallo.hasMoreTokens()) {
                        detecta_fallos.addElement(fallo.nextToken());
 
             }

             if (((String)detecta_fallos.elementAt(0)).compareTo("-ERR") != 0) {
               
		            salida.println("STAT"); 
                        recibido = entrada.readLine();
		            System.out.println(recibido);
             		num_mens = new StringTokenizer(recibido);
            	      while (num_mens.hasMoreTokens()) {
  	                       mensaje.addElement(num_mens.nextToken());
      		       }

             		numero = new Integer ((String)mensaje.elementAt(1)).intValue();

                         
             		salida.println("LIST");
			      punto = entrada.readLine();
             		while(punto.compareTo(".") != 0) { 
                         		System.out.println(punto);
                         		punto = entrada.readLine();
             		}
             		System.out.println(punto);
             		System.out.println("\n"); 

		 

		 		for(int i=0;i<numero;i++) {
                			salida.println("RETR " + (i+1));
                			punto = entrada.readLine();
                			while(punto.compareTo(".") != 0) { 
                    			ventana.anhadeTexto(punto);
                    			ventana.anhadeTexto("\n");
                    			punto = entrada.readLine();
                			}
                 			ventana.anhadeTexto(punto);
                		      ventana.anhadeTexto("\n");
             		}

				if (ventana.esSeleccionado()) { 
		       		for(int i=0;i<numero;i++) {
                        		salida.println("DELE " + (i+1));
                        		System.out.println(entrada.readLine());
                  		 }
    		            }
     
            		salida.println("QUIT");
            		System.out.println(entrada.readLine());

            		ventana.setEstado(false);
               }

            else {

                        boton.addMouseListener(new MouseAdapter() {
                        public void mouseClicked(MouseEvent evt) {
                                botonMouseClicked(evt);      
                        }
                        });

                        dialogo = new Dialog(ventana,"Usuario no conectado: password incorrecto");
                        dialogo.add(boton);
                        dialogo.setSize(350,250);
                        dialogo.show(); 
                    
                        salida.println("QUIT");
            		System.out.println(entrada.readLine());

                 }

      } catch( UnknownHostException e ) {
           e.printStackTrace();
           System.out.println("No hay conexion");
    } catch( IOException e ) {
          e.printStackTrace();
    }

   }
 
   private void botonMouseClicked(MouseEvent evt) {
             dialogo.hide();
             ventana.borrarTodo();                     
  }


}

      