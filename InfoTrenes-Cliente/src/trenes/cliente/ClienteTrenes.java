package trenes.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class ClienteTrenes {

    public static void main(String[] args) {
        try {
            new ClienteTrenes().startClient();
        } catch (UnknownHostException e) {
            System.err.println("Error: Nombre de host no encontrado.");
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println("Error de entrada/salida al abrir el socket.");
            System.err.println(e.getMessage());
        }
    }
    
    private final Socket clientSocket;
    private final PrintWriter out;
    private final BufferedReader in;
    
    public ClienteTrenes() throws IOException {
        String host = "localhost";
        int port = 8989;
        clientSocket = new Socket(host, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
        System.out.println("Iniciado socket en el puerto " + port);
    }

    public void startClient() throws IOException {
        // Enviar peticion inicial
        Respuesta respuesta = send(100, "");
        
        if (respuesta.code == 200) { // El servidor responde de manera correcta
            System.out.println("-------------------------------------");
            System.out.println("Conexión establecida con el servidor.");
            System.out.println("-------------------------------------");
            printMenu();
            
            Scanner keyboard = new Scanner(System.in);
            String accion = keyboard.nextLine();
            while (!"0".equals(accion)) {
                switch (accion) {
                    case "1":
                        // Intentar iniciar sesión y procesar la respuesta del servidor
                        System.out.println("Introduce tu nombre de usuario:");
                        String username = keyboard.nextLine();
                        System.out.println("Introduce tu contraseña: ");
                        String passphrase = keyboard.nextLine();
                        respuesta = send(110, username + "#" + passphrase);
                        if (respuesta.code == 201) {
                            System.out.println("Sesión iniciada correctamente.");
                        } else if (respuesta.code == 301) {
                            System.out.println("Nombre o contraseña errónea");
                        }
                        break;
                    case "2":
                        // Pedir lista de estaciones
                        respuesta = send(130, "");
                        if (respuesta.code == 230) {
                            String[] estaciones = respuesta.body.split("#");
                            for (String estacion : estaciones) {
                                System.out.println(estacion);
                            }
                        } else if (respuesta.code == 310) {
                            System.out.println(respuesta.body);
                        }
                        break;
                    case "3":
                        // Pedir lista de viajes
                        respuesta = send(140, "");
                        if (respuesta.code == 240) {
                            String[] viajes = respuesta.body.split("#");
                            for (String viaje : viajes) {
                                System.out.println(viaje);
                            }
                        } else if (respuesta.code == 310) {
                            System.out.println(respuesta.body);
                        }
                        break;
                    case "4":
                        // Preguntar por salidas desde una estación
                        System.out.println("Introduce la estación de salida:");
                        String estacionSalidas = keyboard.nextLine();
                        respuesta = send(150, estacionSalidas);
                        if (respuesta.code == 250) {
                            String[] estaciones = respuesta.body.split("#");
                            for (String estacion : estaciones) {
                                System.out.println(estacion);
                            }
                        } else if (respuesta.code == 350) {
                            System.out.println(respuesta.body);
                        } else if (respuesta.code == 310) {
                            System.out.println(respuesta.body);
                        }
                        break;
                    case "5":
                        // Preguntar por estación de llegada
                        System.out.println("Introduce la estación de llegada:");
                        String estacionLlegadas = keyboard.nextLine();
                        respuesta = send(160, estacionLlegadas);
                        if (respuesta.code == 260) {
                            String[] estaciones = respuesta.body.split("#");
                            for (String estacion : estaciones) {
                                System.out.println(estacion);
                            }
                        } else if (respuesta.code == 360) {
                            System.out.println(respuesta.body);
                        } else if (respuesta.code == 360) {
                            System.out.println(respuesta.body);
                        }
                        break;
                }
                printMenu();
                accion = keyboard.nextLine();
            }
        }
        
        close();
        System.out.println("Cliente finalizado");
    }

    
    /**
     * Envia un mensaje por el socket y espera a su respuesta
     * @param code El codigo de mensaje
     * @param body El cuerpo del mensaje
     * @return la respuesta del servidor
     * @throws IOException 
     */
    private Respuesta send(int code, String body) throws IOException {
        try {
            out.println(code + body);
            
            String inputLine = in.readLine();
            return new Respuesta(inputLine.substring(0, 3), inputLine.substring(3));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Cerrar la conexión
     * @throws IOException 
     */
    public void close() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }
    
    /**
     * Escribir menú del cliente
     */
    private void printMenu() {
        System.out.println("");
        System.out.println("Escoge una acción (un número):");
        System.out.println("1. Iniciar sesión");
        System.out.println("2. Ver lista de estaciones");
        System.out.println("3. Ver lista de viajes");
        System.out.println("4. Ver salidas desde una estación deseada");
        System.out.println("5. Ver llegadas a una estación deseada");
        System.out.println("0. Salir");
        System.out.println("");
    }
    

    /** 
     * Pequeña clase para procesar facilmente la respuesta del servidor
     */
    private class Respuesta {

        Integer code;
        String body;
        
        Respuesta(String code, String body) {
            this.body = body;
            this.code = Integer.parseInt(code);
        }
    }

}
