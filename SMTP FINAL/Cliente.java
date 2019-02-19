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


class Cliente {

  String host;
  String remitente;
  String destinatario;
  String mensaje;
  String subject;
  String campoCc;
  Vector destinos;
  StringTokenizer separador;
  Ventana ventana;
  Dialog dialogo;
  Button boton;



  Cliente(String rem,String dest,String cc,String mens,String asunto,Ventana vent) {
        
     host = "localhost";
     remitente = rem;
     destinatario = dest;
     campoCc = cc;
     mensaje = mens;
     subject = asunto; 
     int puerto = 25;
     destinos = new Vector();
     ventana = vent;
     dialogo = new Dialog(ventana,"Mensaje enviado");
     boton =  new Button("Aceptar");

     Socket socket = null;
     
    for (int i = 10; i > 0; i--) {
    	try {
		socket = new Socket(host,puerto);
    	} catch (IOException e) {
		System.out.println("Imposible conectar con el servidor. Se intentaran " + i + " veces mas");	
		try { new Thread().sleep(5000);
			continue;
    		} catch (Exception ex) {
			System.out.println("Imposible conectar con el servidor");
      			System.exit(1);
		}
       }
    }

    try {	
  
      
      if (destinatario.compareTo(" ") != 0) {
                    destinatario = destinatario.trim();
                    destinos.addElement(destinatario);
      }

      if (campoCc.compareTo(" ") != 0) {
			  campoCc = campoCc.trim();	
			  separador = new StringTokenizer(campoCc,",");
                    while (separador.hasMoreTokens()) {
                               destinos.addElement(separador.nextToken());
                    }
      }			

      BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
       
      PrintWriter salida = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()),true);

      entrada.readLine();
      salida.println("HELO " + host);
      System.out.println(entrada.readLine());  

      salida.println("MAIL From: " + remitente);
      System.out.println(entrada.readLine());


      if (!destinos.isEmpty()) {
          for (int i=0;i<destinos.size();i++) {
       
                  destinatario = (String)destinos.elementAt(i);
      
			salida.println("RCPT To: " + destinatario);
     		 	System.out.println(entrada.readLine());

         }
       }
      		
      salida.println("DATA");
      System.out.println(entrada.readLine());
      salida.println("From: " + remitente);
      salida.println("Subject: " + subject);
      salida.println(mensaje);
      salida.println(".");
      System.out.println(entrada.readLine());

	salida.println("QUIT");
	System.out.println(entrada.readLine());

      boton.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                   botonMouseClicked(evt);
              }
             });

      boton.setBounds(50,25,100,40);
      dialogo.add(boton);
      dialogo.setSize(350,250);
      dialogo.show();
      
      
	socket.close();
      salida.close();
      entrada.close();
         
      

    } catch( UnknownHostException e ) {
      e.printStackTrace();
      System.out.println("No hay conexion");
    } catch( IOException e ) {
      System.out.println("Error de entrada/salida");
      System.exit(1);
    } catch (Exception ex) {
			System.out.println("Imposible conectar con el servidor");
      			System.exit(1);
		}

  }
  
  private void botonMouseClicked(MouseEvent evt) {
             dialogo.hide();
             ventana.borrarTodo();                     
  }



}
 
 