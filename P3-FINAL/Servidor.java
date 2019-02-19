/* ------------------
   Servidor
   utilizacion: java Servidor [puerto RTSP]
   ---------------------- */


import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.Timer;

public class Servidor extends JFrame implements ActionListener {

  //Variables RTP
  //----------------
  DatagramSocket socketRTP; //Socket para enviar y recibir datagramas RTP
  DatagramPacket pqtEnviar; //Paquete UDP con los frames de video

  InetAddress direccionIPCliente; //Direccion IP del Cliente
  int puertoDestinoRTP = 0; //Puerto destino para los paquetes RTP(proporcionado por el cliente RTSP)

  //GUI
  //----------------
  JLabel etiqueta;

  //Variable de video
  //----------------
  int numImagen = 0; //Numero de imagen transmitida en este momento
  VideoStream video; //VideoStream utilizado para acceder a los frames de video
  static int TIPO_MJPEG = 26; //Tipo de carga RTP para video MJPEG
  static int PERIODO_FRAME = 100; //Período de frames a transmitir, en milisegundos
  static int LONG_VIDEO = Integer.MAX_VALUE; //Longitud del vídeo en frames

  Timer temporizador; //Temporizador utilizado para enviar las imagenes del video al ratio adecuado
  byte[] buffer; //Buffer utilizado para almacenar las imágenes a enviar al cliente

  //Variables RTSP
  //----------------
  //Estados RTSP
  final static int INIT = 0;
  final static int READY = 1;
  final static int PLAYING = 2;
  //Tipos de mensajes RTSP
  final static int SETUP = 3;
  final static int PLAY = 4;
  final static int PAUSE = 5;
  final static int TEARDOWN = 6;

  static int estado; //Estado del servidor RTSP == INIT o READY o PLAY
  Socket RTSPsocket; //Socket utilizado para recibir y enviar mensajes RTSP
  //Canales de entrada y salida
  static BufferedReader RTSPBufferedReader;
  static BufferedWriter RTSPBufferedWriter;
  static String ficheroVideo; //Fichero de video solicitado por el cliente
  static int RTSP_ID = 123456; //ID of the RTSP session
  int RTSPNumSeq = 0; //Sequence number of RTSP messages within the session
  InetAddress grupo;  
  double precision = 0.5;

  final static String CRLF = "\r\n";

  //--------------------------------
  //Constructor
  //--------------------------------
  public Servidor(){

    //Inicializar Frame
    super("Servidor");

	 try {
	 	grupo = InetAddress.getByName("226.123.21.23");
	 } catch(Exception e) {}

    //Inicializar temporizador
    temporizador = new Timer(PERIODO_FRAME, this);
    temporizador.setInitialDelay(0);
    temporizador.setCoalesce(true);

    //Reservar memoria para el buffer de envio
    buffer = new byte[15000]; 

    //Manejador para cerrar la ventana principal
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
	//Parar el tempororizador y salir
	temporizador.stop();
	System.exit(0);
      }});

    //GUI:
    etiqueta = new JLabel("Enviando frame #        ", JLabel.CENTER);
    getContentPane().add(etiqueta, BorderLayout.CENTER);
  }
          
  //------------------------------------
  //main
  //------------------------------------
  public static void main(String argv[]) throws Exception
  {
    //Crear objeto Servidor
    Servidor servidor = new Servidor();

    //Mostrar GUI:
    servidor.pack();
    servidor.setVisible(true);

    //Obtener el puerto RTSP
    int RTSPport = Integer.parseInt(argv[0]);
   
    //Iniciar la conexión TCP con el cliente para la sesion RTSP
    ServerSocket socketServidor = new ServerSocket(RTSPport);
    servidor.RTSPsocket = socketServidor.accept();
    socketServidor.close();

    //Obtener direccion IP del cliente
    servidor.direccionIPCliente = servidor.RTSPsocket.getInetAddress();

    //Inicializar el estado RTSP
    estado = INIT;

    //Establecer los canales de entrada y salida
    RTSPBufferedReader = new BufferedReader(new InputStreamReader(servidor.RTSPsocket.getInputStream()) );
    RTSPBufferedWriter = new BufferedWriter(new OutputStreamWriter(servidor.RTSPsocket.getOutputStream()) );

    //Esperar por el mensaje SETUP del cliente
     int tipoSolicitud; 
          
     //Bucle para gestionar las solicitudes RTSP
    while(true)
      {

	//Analizar la solicitud
	tipoSolicitud = servidor.parseSolicitudRTSP(); //Bloqueante

      if (tipoSolicitud == SETUP)
	  {
	     //Actualizar estado RTSP
	    estado = READY;
	    System.out.println("Nuevo estado RTSP: READY");
   
	    //Enviar respuesta
	    servidor.enviarRespuestaRTSP();
   
	    //Inicializar el objeto VideoStream
	    servidor.video = new VideoStream(ficheroVideo);

	    //Inicializar el socket RTP
	    servidor.socketRTP = new DatagramSocket();
	  }
      

	    
	if ((tipoSolicitud == PLAY) && (estado == READY))
	  {
	    //Enviar la respuesta
	    servidor.enviarRespuestaRTSP();
	    //Iniciar temporizador
	    servidor.temporizador.start();
	    //Actualizar estado
	    estado = PLAYING;
	    System.out.println("Nuevo estado RTSP: PLAYING");
	  }
	else if ((tipoSolicitud == PAUSE) && (estado == PLAYING))
	  {
	    //Enviar la respuesta
	    servidor.enviarRespuestaRTSP();
	    //Parar temporizador
	    servidor.temporizador.stop();
	    //Actualizar estado
	    estado = READY;
	    System.out.println("Nuevo estado RTSP: READY");
	  }
	else if (tipoSolicitud == TEARDOWN)
	  {
	    //Enviar la respuesta
	    servidor.enviarRespuestaRTSP();
	    //Parar temporizador
	    servidor.temporizador.stop();
	    //Cerrar sockets
	    //servidor.RTSPsocket.close();
	    //servidor.socketRTP.close();
           estado = INIT;  
	     System.out.println("Nuevo estado RTSP: INIT");

	    //System.exit(0);
	  }
      }
  }


  //------------------------
  //Manejador del temporizador
  //------------------------
  public void actionPerformed(ActionEvent e) {

    //Si el numero de imagen es menor que la longitud del video
    if (numImagen < LONG_VIDEO)
      {
	//Incrementar numImagen
	numImagen++;
       
	try {
	  //Obtener el siguiente frame y su tamaño
	  int longImagen = video.getnextframe(buffer);

	  //Construir un paquete RTP con el frame
	  PaqueteRTP paqueteRTP = new PaqueteRTP(TIPO_MJPEG, numImagen, numImagen*PERIODO_FRAME, buffer, longImagen);
	  
	  //Obtener la longitud total del paquete RTP a enviar
	  int longPaquete = paqueteRTP.getLong();

	  //Recuperar el array de bits del paquete y almacenarlo en un array
	  byte[] bitsPaquete = new byte[longPaquete];
	  paqueteRTP.getPaquete(bitsPaquete);

	  //Enviar el paquete como un DatagramPacket sobre un socket UDP
	  pqtEnviar = new DatagramPacket(bitsPaquete, longPaquete, grupo, puertoDestinoRTP);
	  //paquetesEnviados++;
	  if ((precision>0) && (new Random().nextFloat() <= precision)) socketRTP.send(pqtEnviar);
		
        
	  //System.out.println("Enviando frame #"+numImagen);
	  //Mostrar la cabecera
	  paqueteRTP.printheader();

	  //Actualizar GUI
	  etiqueta.setText("Enviando frame #" + numImagen);
	}
	catch(Exception ex)
	  {
	    System.out.println("Excepcion: "+ex);
	    System.exit(0);
	  }
      }
    else
      {
	//Si se ha alcanzado el final del video, parar temporizador
	temporizador.stop();
      }
  }

  //------------------------------------
  //Analizar solicitud RTSP
  //------------------------------------
  private int parseSolicitudRTSP()
  {
    int tipoSolicitud = -1;
    try{
      //Parsear la linea de solicitud y extraer el tipo de solicitud
      String lineaSolicitud = RTSPBufferedReader.readLine();
      //System.out.println("Servidor RTSP - Recibido del cliente:");
      System.out.println(lineaSolicitud);

      StringTokenizer tokens = new StringTokenizer(lineaSolicitud);
      String stringTipoSolicitud = tokens.nextToken();

      //convert to tipoSolicitud structure:
      if ((new String(stringTipoSolicitud)).compareTo("SETUP") == 0)
	tipoSolicitud = SETUP;
      else if ((new String(stringTipoSolicitud)).compareTo("PLAY") == 0)
	tipoSolicitud = PLAY;
      else if ((new String(stringTipoSolicitud)).compareTo("PAUSE") == 0)
	tipoSolicitud = PAUSE;
      else if ((new String(stringTipoSolicitud)).compareTo("TEARDOWN") == 0)
	tipoSolicitud = TEARDOWN;

      if (tipoSolicitud == SETUP)
	{
	  //Extraer el fichero de video de la linea de solicitud
	  ficheroVideo = tokens.nextToken();
	}

      //Parsear la linea de numero de secuencia
      String lineaNumSeq = RTSPBufferedReader.readLine();
      System.out.println(lineaNumSeq);
      tokens = new StringTokenizer(lineaNumSeq);
      tokens.nextToken();
      RTSPNumSeq = Integer.parseInt(tokens.nextToken());
	
      //Obtener la ultima linea
      String ultimaLinea = RTSPBufferedReader.readLine();
      System.out.println(ultimaLinea);

      if (tipoSolicitud == SETUP)
	{
	  //Extraer el puerto destino RTP de la ultima linea
	  tokens = new StringTokenizer(ultimaLinea);
	  for (int i=0; i<3; i++)
	    tokens.nextToken(); //no utilizado
	  puertoDestinoRTP = Integer.parseInt(tokens.nextToken());
	}
      //else la ultima linea es la linea de identificador de sesion,
      //no se analiza por ahora
    }
    catch(Exception ex)
      {
	//System.out.println("Excepcion: "+ex);
	System.exit(0);
      }
    return(tipoSolicitud);
  }

  //------------------------------------
  //Enviar respuesta RTSP
  //------------------------------------
  private void enviarRespuestaRTSP()
  {
    try{
      RTSPBufferedWriter.write("RTSP/1.0 200 OK"+CRLF);
      RTSPBufferedWriter.write("CSeq: "+RTSPNumSeq+CRLF);
      RTSPBufferedWriter.write("Session: "+RTSP_ID+CRLF);
      RTSPBufferedWriter.flush();
      //System.out.println("Servidor RTSP - Enviando respuesta al cliente.");
    }
    catch(Exception ex)
      {
	System.out.println("Excepcion: "+ex);
	System.exit(0);
      }
  }

}