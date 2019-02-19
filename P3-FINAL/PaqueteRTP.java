

public class PaqueteRTP{

  //Tamaño de la cabecera RTP
  static int LONG_CABECERA = 12;

  //Campos de la cabecera RTP
  public int Version;
  public int Padding;
  public int Extension;
  public int CC;
  public int Marker;
  public int PayloadType;
  public int SequenceNumber;
  public int TimeStamp;
  public int Ssrc;
  public int aux;
  public int aux2;
  
  //Array de bits de la cabecera RTP
  public byte[] cabecera;

  //Tamaño de la carga RTP
  public int longCarga;
  //Array de bits de la carga RTP
  public byte[] carga;
  
  //--------------------------
  //Constructor de un paquete RTP, utilizando los campos de la cabecera y la carga
  //--------------------------
  public PaqueteRTP(int PType, int Framenb, int Time, byte[] datos, int longDatos){
    //fill by default cabecera fields:
    Version = 2;
    Padding = 0;
    Extension = 0;
    CC = 0;
    Marker = 0;
    Ssrc = 0;

    //Rellenar los campos que cambian de la cabecera
    SequenceNumber = Framenb;
    TimeStamp = Time;
    PayloadType = PType;
    
    //Construir el array de bytes
    //--------------------------
    cabecera = new byte[LONG_CABECERA];
    
 
    //Rellenar con los campos de la cabecera RTP
    cabecera[0] = (byte) ((Version << 6) + (Padding << 5) + (Extension << 4) + (CC));
    cabecera[1] = (byte) ((Marker << 7) + (PayloadType));
    cabecera[2] = (byte) ((SequenceNumber >> 8) & 0xff);
    cabecera[3] = (byte) (SequenceNumber & 0xff); 
    cabecera[4] = (byte) ((TimeStamp >> 24) & 0xff);
    cabecera[5] = (byte) ((TimeStamp >> 16) & 0xff);
    cabecera[6] = (byte) ((TimeStamp >> 8) & 0xff);
    cabecera[7] = (byte) (TimeStamp & 0xff);
    cabecera[8] = (byte) ((Ssrc >> 24) & 0xff);
    cabecera[9] = (byte) ((Ssrc >> 16) & 0xff);
    cabecera[10] = (byte) ((Ssrc >> 8) & 0xff);
    cabecera[11] = (byte) (Ssrc & 0xff);

    //Rellenar la carga
    //--------------------------
    longCarga = longDatos;
    carga = new byte[longDatos];

    //Rellenar el array de carga
    for (int i=0; i < longDatos; i++) carga[i] = datos[i];
    
    // Sugerencia: descomentar el metodo printheader para depuracion

  }
    
  //--------------------------
  //Constructor de un paquete RTP a partir de un array de bytes
  //--------------------------
  public PaqueteRTP(byte[] paquete, int longPaquete)
  {
    //Rellenar los campos por defecto
    Version = 2;
    Padding = 0;
    Extension = 0;
    CC = 0;
    Marker = 0;
    Ssrc = 0;

    //Comprobar si la longitud total del paquete es menor que la de la cabecera
    if (longPaquete >= LONG_CABECERA) 
      {
	//Obtener los bits de la cabecera
	cabecera = new byte[LONG_CABECERA];
	for (int i=0; i < LONG_CABECERA; i++)
	  cabecera[i] = paquete[i];

	//Obtener los bits de la carga
	longCarga = longPaquete - LONG_CABECERA;
	carga = new byte[longCarga];
	for (int i=LONG_CABECERA; i < longPaquete; i++)
	  carga[i-LONG_CABECERA] = paquete[i];

	//Interpretar los campos que cambian de la cabecera
	PayloadType = cabecera[1] & 127;
	SequenceNumber = unsigned_int(cabecera[3]) + 256*unsigned_int(cabecera[2]);
	TimeStamp = unsigned_int(cabecera[7]) + 256*unsigned_int(cabecera[6]) + 65536*unsigned_int(cabecera[5]) + 16777216*unsigned_int(cabecera[4]);
      }
 }

  //--------------------------
  //Devuelve el array de bits de carga y su tamaño
  //--------------------------
  public int getCarga(byte[] datos) {

    for (int i=0; i < longCarga; i++)
      datos[i] = carga[i];

    return(longCarga);
  }

  //--------------------------
  //Devuelve la longitud de la carga
  //--------------------------
  public int getLongCarga() {
    return(longCarga);
  }

  //--------------------------
  //Devuelve la longitud total del paquete RTP
  //--------------------------
  public int getLong() {
    return(longCarga + LONG_CABECERA);
  }

  //--------------------------
  //Devuelve el paquete y su longitud
  //--------------------------
  public int getPaquete(byte[] paquete)
  {
    //Construye el paquete = cabecera + carga
    for (int i=0; i < LONG_CABECERA; i++)
	paquete[i] = cabecera[i];
    for (int i=0; i < longCarga; i++)
	paquete[i+LONG_CABECERA] = carga[i];

    //Devuelve la longitud total del paquete
    return(longCarga + LONG_CABECERA);
  }

  //--------------------------
  //getTimestamp
  //--------------------------

  public int getTimestamp() {
    return(TimeStamp);
  }

  //--------------------------
  //getNumeroSecuencia
  //--------------------------
  public int getNumeroSecuencia() {
    return(SequenceNumber);
  }

  //--------------------------
  //getTipoCarga
  //--------------------------
  public int getTipoCarga() {
    return(PayloadType);
  }


  //--------------------------
  //Muestra la cabecera
  //--------------------------
  public void printheader()
  {
   /* for (int i=0; i < (LONG_CABECERA-4); i++)
      {
	for (int j = 7; j>=0 ; j--)
	  if (((1<<j) & cabecera[i] ) != 0)
	    System.out.print("1");
	else
	  System.out.print("0");
	System.out.print(" ");
      }

    System.out.println();
  */
  }

  private int unsigned_int(byte b) {
     int actual = new Integer(b).intValue();
     if (actual<0) actual=actual+256;
     return new Integer(actual).intValue();
  }   

}

