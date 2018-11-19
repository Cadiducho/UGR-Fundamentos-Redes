package trenes.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

public class ProcesadorPeticiones extends Thread {
   
    private final Socket socket;
    private final ServidorTrenes server;
    
    private PrintWriter out;
    private BufferedReader in;
    
    public ProcesadorPeticiones(Socket socketServicio, ServidorTrenes servidorTrenes) throws IOException {
        this.socket = socketServicio;
        this.server = servidorTrenes;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                
                // Dividir mensaje en 3 primeras cifras, código; y el resto, cuerpo.
                String code = inputLine.substring(0, 3);
                String body = inputLine.substring(3);

                int nCode;
                try {
                    nCode = Integer.parseInt(code);
                } catch (NumberFormatException ex) {
                    System.err.println("Mensaje con formato incorrecto");
                    System.err.println(ex.getMessage());
                    send(500, "Mensaje malformado");
                    return;
                }

                // Evaluar qué hacer por su código
                switch (nCode) {
                    case 100:
                        // Saludar servidor, retorna 200
                        hello();
                        break;
                    case 110:
                        // Autentificar usuario con su body
                        String[] parsedBody = body.split("#");
                        String user = parsedBody[0];
                        String passphrase = parsedBody[1];
                        auth(user, passphrase);
                        break;
                    case 130:
                        // Pide lista de estaciones
                        listarEstaciones();
                        break;
                    case 140:
                        // Pide lista de viajes
                        listarViajes();
                        break;
                    case 150:
                        // Preguntar por salidas de estación body
                        listSalidasDe(body);
                        break;
                    case 160:
                        // Preguntar por llegadas a estación body
                        listLlegadasA(body);
                        break;
                    default:
                        send(500, "Codigo desconocido");
                        break;
                }
            }
        } catch (SocketException e) {
            if (e.getMessage().equals("Connection reset")) {
                System.out.println("Cerrando sesión de socket");
                server.desautorizarCliente(socket);
            } else {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send(int code, String body) throws IOException {
        out.println(code + body);
    }
    
    void hello() throws IOException {
        send(200, "OK");
    }
    
    private void auth(String user, String passphrase) throws IOException {
        if (user.equals("usuario") && passphrase.equals("hola")) { //todo el login de verdad
            server.autorizarCliente(socket);
            send(201, "Logged in");
        } else {
            send(301, "Username or passphrase incorrect");
        }
    }
    
    private void listarEstaciones() throws IOException {
        if (!server.loggedIn(socket)) {
            send(310, "No autorizado");
            return;
        }
        
        String estaciones = "";
        estaciones = server.estaciones.stream().map((nombre) -> nombre + "#").reduce(estaciones, String::concat);
        send(230, estaciones);
    }
    
    private void listarViajes() throws IOException {
        if (!server.loggedIn(socket)) {
            send(310, "No autorizado");
            return;
        }
        
        String viajes = "";
        viajes = server.viajes.stream().map((nombre) -> nombre + "#").reduce(viajes, String::concat);
        send(240, viajes);
    }
    
    private void listSalidasDe(String estacion) throws IOException {
        if (!server.loggedIn(socket)) {
            send(310, "No autorizado");
            return;
        }
        
        List<Viaje> listaSalidas = server.findViajesSaliendoDe(estacion);
        if (listaSalidas.isEmpty()) {
            send(350, "No hay salidas desde " + estacion);
            return;
        }
        String salidas = "";
        salidas = listaSalidas.stream().map((viaje) -> viaje + "#").reduce(salidas, String::concat);
        send(250, salidas);
    }

    private void listLlegadasA(String estacion) throws IOException {
        if (!server.loggedIn(socket)) {
            send(310, "No autorizado");
            return;
        }
        
        List<Viaje> listaLlegadas =  server.findViajesDestinoA(estacion);
        if (listaLlegadas.isEmpty()) {
            send(360, "No hay llegadas a " + estacion);
            return;
        }
        String llegadas = "";
        llegadas = listaLlegadas.stream().map((viaje) -> viaje + "#").reduce(llegadas, String::concat);
        send(260, llegadas);
    }
}
