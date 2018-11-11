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
 * - 160: Listar llegadas desde estación
 * Codigos devueltos:
 * - 200: Aceptar saludo
 * - 201: Inicio de sesión válido
 * - 230: Lista de nombres de estaciones
 * - 240: Lista de viajes activos
 * - 250: Lista de trenes que saldrán a esa estación
 * - 260: Lista de trenes destino a esa estación
 * - 301: Autentificación errónea
 * - 310: No autentificado
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
    
    public void start(int port) throws Exception {
        do {
            Socket socketServicio = serverSocket.accept();
      
            new ProcesadorPeticiones(socketServicio, this).start();
        } while(true);
    }
    
    public void stop() {
         try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean loggedIn(Socket socket) {
        return this.loggedClients.contains(socket);
    }
    
    public void autorizarCliente(Socket socket) {
        loggedClients.add(socket);
    }
    
    public void desautorizarCliente(Socket socket) {
        loggedClients.remove(socket);
    }
    
    List<String> estaciones = Arrays.asList("Madrid - Atocha", "Madrid - Chamartín", "Sevilla - Santa Justa", "Málaga - Maria Zambrano",
            "Albacete - Los Llanos", "Segovia - Guiomar", "Valladolid - Campo Grande", "Palencia", "León", "Zamora", "Gijón", "Valencia - Joaquín Sorolla", 
            "Barcelona - França", "Camp da Tarragona", "Zaragoza - Delicias", "Huesca", "Pamplona", "Vitoria/Gasteiz", "San Sebastián/Donosti", "Bilbao",
            "Irún", "Pontevedra", "Santiago de Compostela", "A Coruña", "Vigo", "Mérida", "Badajoz", "Murcia", "Alicante", "Toledo", "Ciudad Real", "Puerto Llano",
            "Guadalajara", "Soria", "Burgos - Rosa de Lima", "Barcelona - Sants", "Granada");
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
    
    public List<Viaje> findViajesSaliendoDe(String estacion) {
        return viajes.stream().filter(v -> v.getInicio().equalsIgnoreCase(estacion)).collect(Collectors.toList());
    }
    
    public List<Viaje> findViajesDestinoA(String estacion) {
        return viajes.stream().filter(v -> v.getDestino().equalsIgnoreCase(estacion)).collect(Collectors.toList());
    }
    
}
