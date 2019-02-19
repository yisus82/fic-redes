//VideoStream


import java.io.*;

public class VideoStream {

  FileInputStream fis; //Fichero de video
  int numFrame; //Numero de frame actual

  //-----------------------------------
  //constructor
  //-----------------------------------
  public VideoStream(String filename) throws Exception{

    //Inicializar variables
    fis = new FileInputStream(filename);
    numFrame = 0;
  }

  //-----------------------------------
  // getnextframe
  //Devuelve el siguiente frame como un array de bytes y la longitud del frame
  //-----------------------------------
  public int getnextframe(byte[] frame) throws Exception
  {
    int longitud = 0;
    String longString;
    byte[] longFrame = new byte[5];

    //Leer la longitud del frame actual
    fis.read(longFrame,0,5);
	
    //Transformar la longitud del frame a entero
    longString = new String(longFrame);
    longitud = Integer.parseInt(longString);
	
    return(fis.read(frame,0,longitud));
  }
}