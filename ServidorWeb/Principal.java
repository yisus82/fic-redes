
public class Principal {

    public static void main(String[] array) {
        int puerto = (Integer.valueOf(ConfigurationParametersManager
                .getParameter("PUERTO"))).intValue();
        String directorio = ConfigurationParametersManager
                .getParameter("ALIAS/DIRECTORIO");
        String alias = ConfigurationParametersManager
                .getParameter("ALIAS/ALIAS");
        String indice = ConfigurationParametersManager.getParameter("INDICE");
        String log = ConfigurationParametersManager.getParameter("LOG");
        String error = ConfigurationParametersManager.getParameter("ERROR");
        Servidor servidor = new Servidor(array, puerto, directorio, alias,
                indice, log, error);
        servidor.arranca();
    }

}