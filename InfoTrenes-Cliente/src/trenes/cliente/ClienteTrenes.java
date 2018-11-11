package trenes.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;

/**
 *
 * @author cadid
 */
public class ClienteTrenes {

    /**
     * @param args the command line arguments
     */
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
        // Enviar peticion
        String respuesta = send(100, "hola trenes");

        // Mostremos la cadena de caracteres recibidos:
        System.out.println("Respuesta del servidor: " + respuesta);

        respuesta = send(140, "");
        System.out.println("r " + respuesta);
        
        respuesta = send(110, "usuario#hola");
        System.out.println(respuesta);

        respuesta = send(140, "");
        System.out.println("Viajes: ");
        System.out.println(respuesta);
        
        Scanner keyboard = new Scanner(System.in);
        System.out.println("Introducir estación de inicio: ");
        String estacion = keyboard.nextLine();
        respuesta = send(150, estacion);
        System.out.println(respuesta);
        
        System.out.println("Introducir estación de destino: ");
        estacion = keyboard.nextLine();
        respuesta = send(160, estacion);
        System.out.println(respuesta);
        
        send(900, "");
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
    private String send(int code, String body) throws IOException {
        try {
            System.out.println("Enviando " + code + body);
            out.println(code + body);
            return in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public void close() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

}
