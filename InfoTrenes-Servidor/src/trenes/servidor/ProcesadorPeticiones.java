package trenes.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

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
                String code = inputLine.substring(0, 3);
                String body = inputLine.substring(3);

                int nCode;
                try {
                    nCode = Integer.parseInt(code);
                } catch (NumberFormatException ex) {
                    System.err.println("Mensaje con formato incorrecto");
                    System.err.println(ex.getMessage());
                    return;
                }

                System.out.println("Recibido " + nCode + " : " + body);

                switch (nCode) {
                    case 100:
                        hello();
                        // Saludar servidor, retorna 200
                        break;
                    case 110:
                        String[] parsedBody = body.split("#");
                        String user = parsedBody[0];
                        String passphrase = parsedBody[1];
                        auth(user, passphrase);
                        // Autentificar usuario con su body
                        break;
                    case 130:
                        listarEstaciones();
                        break;
                    case 140:
                        listarViajes();
                        break;
                    case 150:
                        listSalidasDe(body);
                        // Preguntar por salidas de estación body
                        break;
                    case 160:
                        // Preguntar por llegadas a estación body
                        listLlegadasDe(body);
                        break;
                        
                }
                if (server.loggedIn(socket)) {
                    System.out.println("Este socket está inicado sesión");
                } else {
                    System.out.println("Este socket NO es conocido");
                }
            }

            
            System.out.println("Cerrado thread");
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
    
    private void listSalidasDe(String body) throws IOException {
        if (!server.loggedIn(socket)) {
            send(310, "No autorizado");
            return;
        }
        
        String salidas = "";
        salidas = server.findViajesSaliendoDe(body).stream().map((viaje) -> viaje + "#").reduce(salidas, String::concat);
        send(250, salidas);
    }

    private void listLlegadasDe(String body) throws IOException {
        if (!server.loggedIn(socket)) {
            send(310, "No autorizado");
            return;
        }
        
        String llegadas = "";
        llegadas = server.findViajesDestinoA(body).stream().map((viaje) -> viaje + "#").reduce(llegadas, String::concat);
        send(260, llegadas);
    }
    
    private void auth(String user, String passphrase) throws IOException {
        if (user.equals("usuario") && passphrase.equals("hola")) { //todo el login de verdad
            server.autorizarCliente(socket);
            send(201, "Logged in");
        } else {
            send(301, "Username or passphrase incorrect");
        }
    }
}
