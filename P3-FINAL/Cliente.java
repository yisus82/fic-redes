/* ------------------
   Cliente
   utilizacion: java Cliente [host servidor] [puerto servidor RTSP] [Fichero de video solicitado]
   ---------------------- */

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class Cliente{

  //GUI
  //----
  JFrame f = new JFrame("Cliente");
  JButton botonPlay = new JButton("Play");
  JButton botonPausa= new JButton("Pausa");
  JButton botonFinalizar= new JButton("Stop");
  JPanel panelPrincipal = new JPanel();
  JPanel panelBotones = new JPanel();
  JLabel etiquetaIcono = new JLabel();
  ImageIcon icono;
  


  //Variables RTP:
  //----------------
  DatagramPacket rcvdp; //Paquete UDP recibido del servidor
  //DatagramSocket RTPsocket; //Socket a utilizar para enviar y recibir datagramas UDP
  static int PUERTO_RCV_RTP = 25000; //Puerto donde el cliente va a recibir los paquetes RTP
    


  Timer temporizador; //Temporizador utilizado para recibir los datos del socket UDP
  byte[] buffer; //Buffer para almacenar los datos recibidos del servidor
 
  //RTSP variables
  //----------------
  //rtsp states 
  final static int INIT = 0;
  final static int READY = 1;
  final static int PLAYING = 2;
  static int estado; //RTSP estado == INIT or READY or PLAYING
  Socket socketRTSP; //Socket utilizado para recibir y enviar mensajes RTSP
  //Canales de entrada y salida
  static BufferedReader RTSPBufferedReader;
  static BufferedWriter RTSPBufferedWriter;
  static String ficheroVideo; //Fichero de video solicitado al servidor
  int RTSPNumSeq = 0; //Numero de secuencia de los mensajes RTSP en la sesión
  int RTSPid = 0; //ID de la sesion RTSP (proporcionado por el servidor RTSP)
  int numVez = 0;
  InetAddress grupo;
  MulticastSocket s;
  static boolean primero = true;
  PaqueteRTP paqAct;
  int anterior = -1;
  int ultimo = -1;
  int penultimo = 0;
  int retardo = 0;
  int paquetesRecibidos = 0;
  int paquetesPerdidos = 0;
  int ultimaSecuencia = 0;
  int tamanho = 0;
  

  final static String CRLF = "\r\n";

  //Constantes de video:
  //------------------
  static int TIPO_MJPEG = 26; //Tipo de carga RTP para video MJPEG
 
  //--------------------------
  //Constructor
  //--------------------------
  public Cliente() {

    //Definición GUI
    //--------------------------
 
    //Frame
    f.addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) {
	 		System.exit(0);
       }
    });

	 try {
    	       grupo = InetAddress.getByName("226.123.21.23");
	 } catch (Exception e) {}

    //Botones
    panelBotones.setLayout(new GridLayout(1,0));
    panelBotones.add(botonPlay);
    panelBotones.add(botonPausa);
    panelBotones.add(botonFinalizar);
    botonPlay.addActionListener(new playButtonListener());
    botonPausa.addActionListener(new pausaButtonListener());
    botonFinalizar.addActionListener(new finalizarButtonListener());

    //Etiqueta para mostrar la imagen
    etiquetaIcono.setIcon(null);
    
    //Layout del frame
    panelPrincipal.setLayout(null);
    panelPrincipal.add(etiquetaIcono);
    panelPrincipal.add(panelBotones);
    etiquetaIcono.setBounds(0,0,380,280);
    panelBotones.setBounds(0,280,380,50);

    f.getContentPane().add(panelPrincipal, BorderLayout.CENTER);
    f.setSize(new Dimension(390,370));
    f.setVisible(true);

    //Inicialización temporizadores
    //--------------------------
    temporizador = new Timer(20, new temporizadorListener());
    temporizador.setInitialDelay(0);
    temporizador.setCoalesce(true);

    //Reserva de memoria en el buffer, para recibir los datos del servidor
    buffer = new byte[15000];    
  }

  //------------------------------------
  //main
  //------------------------------------
  public static void main(String argv[]) throws Exception
  {
    //Crear el objeto Cliente
    Cliente cliente = new Cliente();    
    //Obtener la dirección IP y puerto del servidor RTSP
    //------------------
    int puertoServidorRTSP = Integer.parseInt(argv[1]);
    String hostServidorRTSP = argv[0];
    InetAddress direccionIPServidorRTSP = InetAddress.getByName(hostServidorRTSP);

    //Obtener el nombre del video a recuperar
    ficheroVideo = argv[2];

    //Establecer conexion TCP con el servidor para intercambiar los mensajes RTSP
    //------------------
    try {
     
    	cliente.socketRTSP = new Socket(direccionIPServidorRTSP, puertoServidorRTSP);
     
     	//Establecer los canales de entrada y salida
    	RTSPBufferedReader = new BufferedReader(new InputStreamReader(cliente.socketRTSP.getInputStream()) );
    	RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(cliente.socketRTSP.getOutputStream()) );
    }catch(SocketException ex){
		primero = false;
	 }
    //Inicializar el estado RTSP
    estado = INIT;
  }


  //------------------------------------
  //Manejadores para los botones
  //------------------------------------


  //Manejador del boton de play
  //-----------------------
  class playButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e){

      System.out.println("Boton play pulsado!"); 

      if (numVez == 0)  
      {
	  		try{
	    		//Crear un nuevo DatagramSocket para recibir los paquetes RTP del servidor en el puerto PUERTO_RCV_RTP
	    		//RTPsocket = new DatagramSocket(PUERTO_RCV_RTP);

		    	//Establecer el timeout a 5 milisegundos
		    	//RTPsocket.setSoTimeout(5);
	         
          	s = new MulticastSocket(PUERTO_RCV_RTP);
          	s.joinGroup(grupo); 
          	s.setSoTimeout(5);
           
          
	  		}
	  		catch (SocketException se)
	    	{
	      	System.out.println("Excepcion en el socket: " + se);
	      	System.exit(0);
	    	}
        	catch(IOException ex){}   
     }
     if (estado == INIT) {
     	numVez ++; 

 	  	//Inicializar el número de secuencia RTSP
	  	RTSPNumSeq = 1;

	  	//Enviar el mensaje de SETUP al servidor
	  	if (primero) {  
      	enviarSolicitudRTSP("SETUP");

    	   //Esperar por la respuesta
	    	if (parseRespuestaServidor() != 200)
	      	System.out.println("Respuesta del servidor invalida");
	    	else 
	      {
	        	//Cambiar el estado RTSP y mostrar el nuevo estado
	        	estado = READY;
	        	System.out.println("El nuevo estado es: READY");
	      }
      }else {
      	//Cambiar el estado RTSP y mostrar el nuevo estado
	      estado = READY;
	      System.out.println("El nuevo estado es: READY");
      }
    }       
 
    if (estado == READY) {
    	//Incrementar el número de secuencia RTSP
      RTSPNumSeq ++;

	  	//Enviar mensaje PLAY al servidor
	  	if (primero) {
      	enviarSolicitudRTSP("PLAY");
        
	  		//Esperar por la respuesta 
	  		if (parseRespuestaServidor() != 200)
		  		System.out.println("Respuesta del servidor inválida");
         else 
	    	{
	      	//Cambiar estado RTSP y mostrar el nuevo estado
	      	estado = PLAYING;
	      	System.out.println("El nuevo estado es: PLAYING");

	      	//Iniciar el temporizador
	      	temporizador.start();
	    	}
      }else
      {
      	//Cambiar estado RTSP y mostrar el nuevo estado
	      estado = PLAYING;
	      System.out.println("El nuevo estado es: PLAYING");

	      //Iniciar el temporizador
	      temporizador.start();
      }
         
  	 }//else if estado != READY => Nada
  }
}

  //Manejador del boton de pausa
  //-----------------------
  class pausaButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e){

      System.out.println("Boton pausa pulsado!");   

      if (estado == PLAYING) 
		{
	  //Incrementar el número de secuencia RTSP
      RTSPNumSeq ++;
	  
	  //Enviar mensaje PAUSE al servidor
	
      if (primero) {
       enviarSolicitudRTSP("PAUSE");
	
	  //Esperar por la respuesta
	 if (parseRespuestaServidor() != 200)
		  System.out.println("Respuesta del servidor invalida");
	  else 
	    {
	      //Cambiar estado RTSP y mostrar el nuevo estado
		  estado = READY;
		  System.out.println("El nuevo estado es: READY");
	     
	      //Parar el temporizador
	      temporizador.stop();
	    }
         }
         else 
            {
              //Cambiar estado RTSP y mostrar el nuevo estado
		  estado = READY;
		  System.out.println("El nuevo estado es: READY");
	     
	      //Parar el temporizador
	      temporizador.stop();
	    }
        
	}
      //else if estado != PLAYING => Nada
    }
  }

  //Manejador boton de finalizar
  //-----------------------
  class finalizarButtonListener implements ActionListener {
    public void actionPerformed(ActionEvent e){

      System.out.println("Boton finalizar pulsado!");  

      if (estado == INIT) {
		estadisticas();
		System.exit(0);
	}

      //Incrementar el número de secuencia RTSP
      RTSPNumSeq ++;

      //Enviar mensaje TEARDOWN al servidor
       if (primero) {
       enviarSolicitudRTSP("TEARDOWN");
       //Esperar por la respuesta
       if (parseRespuestaServidor() != 200)
	 System.out.println("Respuesta del servidor invalida");
       else 
	 {     
	   //Cambiar estado RTSP y mostrar el nuevo estado
	   estado = INIT;
         System.out.println("El nuevo estado es: INIT" );
	     
	   //Parar el temporizador
	   temporizador.stop();

       }
      }else {
             //Cambiar estado RTSP y mostrar el nuevo estado
	   estado = INIT;
         System.out.println("El nuevo estado es: INIT" );
	     
	   //Parar el temporizador
	   temporizador.stop();
	}
    }
  }



  //------------------------------------
  //Manejador del temporizador
  //------------------------------------

  class temporizadorListener implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      
      //Construir un DatagramPacket para recibir datos del socket UDP
      rcvdp = new DatagramPacket(buffer, buffer.length);

      try{
	       //Recibir el DP del socket
             s.receive(rcvdp);
             paquetesRecibidos++;

	       //Crear un paquete RTP del DP
	       PaqueteRTP paqueteRTP = new PaqueteRTP(rcvdp.getData(), rcvdp.getLength());
              
             tamanho = tamanho + paqueteRTP.getLong();
		 ultimo = paqueteRTP.getTimestamp(); 
             ultimaSecuencia = ultimo;
             retardo = retardo + (ultimo - penultimo);
             penultimo = ultimo;           

 	       if (paqueteRTP.getNumeroSecuencia() > anterior) {
              paquetesPerdidos += paqueteRTP.getNumeroSecuencia() - anterior - 1; 
              anterior = paqueteRTP.getNumeroSecuencia();
		  	            //Mostrar informacion del paquete RTP recibido 
	            System.out.println("Recibido paquete RTP con NumSeq # "+anterior+" TimeStamp "+paqueteRTP.getTimestamp()+" ms, de tipo "+paqueteRTP.getTipoCarga());
	
	            //Mostrar la cabecera
			 		paqueteRTP.printheader();

  	   	 	   //Recuperar el tipo de carga del paquete RTP
			 		int payload_length = paqueteRTP.getLongCarga();
			 		byte [] payload = new byte[payload_length];
			 		paqueteRTP.getCarga(payload);

			 	   //Recuperar un objeto Image de la carga del paquete RTP
			 		Toolkit toolkit = Toolkit.getDefaultToolkit();
			 		Image image = toolkit.createImage(payload, 0, payload_length);
	
		 		   //Mosrtrar la imagen como un objeto ImageIcon
			 		icono = new ImageIcon(image);
			 		etiquetaIcono.setIcon(icono);
           } 
     }
      catch (InterruptedIOException iioe){
	    System.out.println("Nada para leer");
      }
      catch (IOException ioe) {
	System.out.println("Excepcion: "+ioe);
      }
    }
  }


  //------------------------------------
  //Parsear la respuesta del servidor
  //------------------------------------
  private int parseRespuestaServidor() 
  {
    int codigoRespuesta = 0;

    try{
      //Analizar la linea de estado y extraer el codigo de respuesta
      String lineaEstado = RTSPBufferedReader.readLine();
      //System.out.println("Cliente RTSP - Recibido del servidor:");
      System.out.println(lineaEstado);
    
      StringTokenizer tokens = new StringTokenizer(lineaEstado);
      tokens.nextToken(); //Saltarse la version RTSP
      codigoRespuesta = Integer.parseInt(tokens.nextToken());
      
      //Si el codigo de respuesta es OK => Recuperar y mostrar las otras 2 lineas
      if (codigoRespuesta == 200)
	{
	  String lineaNumSeq = RTSPBufferedReader.readLine();
	  System.out.println(lineaNumSeq);
	  
	  String lineaSesion = RTSPBufferedReader.readLine();
	  System.out.println(lineaSesion);
	
	  //Si estado == INIT obtener el identificador de sesion de la linea de sesion
	  tokens = new StringTokenizer(lineaSesion);
	  tokens.nextToken(); //skip over the Session:
	  RTSPid = Integer.parseInt(tokens.nextToken());
	}
    }
    catch(Exception ex)
      {
	System.out.println("Excepcion2: "+ex);
	System.exit(0);
      }
    
    return(codigoRespuesta);
  }

  //------------------------------------
  //Enviar solicitud RTSP:
  //------------------------------------

  private void enviarSolicitudRTSP(String request_type)
  {
    try{
      //Utilizar el RTSPBufferedWriter para escribir en el socket RTSP

      //Escribir la linea de solicitud
      RTSPBufferedWriter.write(request_type + " " + ficheroVideo + " " + "RTSP/1.0" + CRLF);
      //Escribir la linea CSeq
      RTSPBufferedWriter.write("CSeq: " + RTSPNumSeq + CRLF);
      //Comprobar si el tipo de solicitud es SETUP, y en ese caso escribir el Transport:
      //Linea que notifica al servidor el puerto utilizado para recibir los paquetes RTP (PUERTO_RCV_RTP)
      if (request_type.compareTo("SETUP") == 0)         
      	   RTSPBufferedWriter.write("Transport: RTP/UDP; client_port= " + PUERTO_RCV_RTP + CRLF);
            
	  // Sino escribir la linea de sesion del campo id de RTSP
      else 
      	   RTSPBufferedWriter.write("Session: " + RTSPid + CRLF);
      
      RTSPBufferedWriter.flush();
    }
    catch(Exception ex)
      {
	System.out.println("Excepcion3: "+ex);
	System.exit(0);
      }
  }

    public void estadisticas() {
	System.out.println("\n\nEstadisticas\n\nLa media del retardo es : " + (retardo/(paquetesRecibidos-1)) + " ms\n" 
                         + "La velocidad media del video es: " + (tamanho/ultimo)*1000 + " bytes/s\n"  
                         + "El numero de paquetes enviados es: " + anterior + "\n"  
                         + "El porcentaje de paquetes perdidos es: (" + paquetesPerdidos + "/" + anterior + ") ---> " + 
				((float)paquetesPerdidos/anterior)*100 + " %\n"); 
   
    }
}