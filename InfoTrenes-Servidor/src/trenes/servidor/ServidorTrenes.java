package trenes.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Servidor de información sobre trenes
 * Codigos aceptados:
 * - 100: Saludo
 * - 110: Iniciar sesión
 * - 130: Listar estaciones
 * - 140: Listar viajes activos
 * - 150: Listar salidas desde estación
 * - 160: Listar llegadas a estación
 * 
 * Codigos devueltos:
 * - 200: Aceptar saludo
 * - 201: Inicio de sesión válido
 * - 230: Lista de nombres de estaciones
 * - 240: Lista de viajes activos
 * - 250: Lista de trenes que saldrán de esa estación
 * - 260: Lista de trenes destino a esa estación
 * 
 * - 301: Error. Datos de autentificación inválidos
 * - 310: Error. No autentificado
 * - 350: No hay viajes que saldrán de esa estación
 * - 360: No hay viajes que van destino a esa estación
 * - 500: Error. Mensaje malformado
 */
public class ServidorTrenes {

    static final int port = 8989;
    
    /**
     * @param args the command line arguments
     */   
    public static void main(String[] args) {
        try {
            ServidorTrenes server = new ServidorTrenes();
            server.start(port);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
    
    
    private final ServerSocket serverSocket;
    public final List<Viaje> viajes;
    private final List<Socket> loggedClients;
    
    public ServidorTrenes() throws IOException {
        this.viajes = new ArrayList<>();
        this.loggedClients = new CopyOnWriteArrayList<>(); 
        this.serverSocket = new ServerSocket(port);
        
        generarViajes();
        System.out.println("Esperando peticiones en el puerto " + serverSocket.getLocalPort());
    }
    
    /**
     * Iniciar servidor
     * @param port Puerto de escucha
     * @throws Exception 
     */
    public void start(int port) throws Exception {
        do {
            Socket socketServicio = serverSocket.accept();
      
            new ProcesadorPeticiones(socketServicio, this).start();
        } while(true);
    }
    
    /**
     * Cerrar el servidor
     */
    public void stop() {
         try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param socket El socket
     * @return verdadero si este socket cliente está iniciado sesión
     */
    public boolean loggedIn(Socket socket) {
        return this.loggedClients.contains(socket);
    }
    
    /**
     * Iniciar sesión para un socket
     * @param socket el socket de ese cliente
     */
    public void autorizarCliente(Socket socket) {
        loggedClients.add(socket);
    }
    
    /**
     * Desautorizar sesión de un socket
     * @param socket el socket de ese cliente
     */
    public void desautorizarCliente(Socket socket) {
        loggedClients.remove(socket);
    }
    
    /**
     * @param estacion Estación de salida
     * @return Lista de viajes que salen desde la estación
     */
    public List<Viaje> findViajesSaliendoDe(String estacion) {
        return viajes.stream().filter(v -> v.getInicio().equalsIgnoreCase(estacion)).collect(Collectors.toList());
    }
    
    /**
     * @param estacion Estación de llegada
     * @return Lista de viajes con llegada a la estación
     */
    public List<Viaje> findViajesDestinoA(String estacion) {
        return viajes.stream().filter(v -> v.getDestino().equalsIgnoreCase(estacion)).collect(Collectors.toList());
    }
    
    
    List<String> estaciones = Arrays.asList("Madrid - Puerta de Atocha", "Madrid - Chamartín", "Sevilla - Santa Justa", "Málaga - Maria Zambrano",
            "Albacete - Los Llanos", "Segovia - Guiomar", "Valladolid - Campo Grande", "Palencia", "León", "Zamora", "Gijón", "Valencia - Joaquín Sorolla", 
            "Zaragoza - Delicias", "Huesca", "Pamplona", "Vitoria/Gasteiz", "San Sebastián/Donosti", "Bilbao",
            "Irún", "Pontevedra", "Santiago de Compostela", "A Coruña", "Vigo - Guixar", "Mérida", "Badajoz", "Murcia", "Alicante", "Toledo", "Ciudad Real", "Puerto Llano",
            "Guadalajara", "Soria", "Burgos - Rosa de Lima", "Barcelona - Sants", "Barcelona - França", "Girona", "Camp da Tarragona", "Granada");
    /**
     * Generar aleatoriamente viajes
     */
    private void generarViajes() {
        Random random = new Random();
        for (int i = 0; i < ( 5 + random.nextInt(5)); i++) {
            viajes.add(new Viaje(
                    estaciones.get(random.nextInt(estaciones.size() / 2)), 
                    estaciones.get((estaciones.size() / 2) + random.nextInt(estaciones.size() / 2)),
                    LocalTime.now().minusHours(random.nextInt(6)).minusMinutes(random.nextInt(40)), 
                    LocalTime.now().plusHours(random.nextInt(6)).plusMinutes(random.nextInt(40))));
        }
    }    
}
