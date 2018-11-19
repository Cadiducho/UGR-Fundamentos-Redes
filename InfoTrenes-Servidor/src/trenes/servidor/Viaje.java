package trenes.servidor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Viaje {
    
    private final String inicio;
    private final String destino;
    private final LocalTime salida;
    private final LocalTime llegada;

    public Viaje(String inicio, String destino, LocalTime salida, LocalTime llegada) {
        this.inicio = inicio;
        this.destino = destino;
        this.salida = salida;
        this.llegada = llegada;
    }

    public String getInicio() {
        return inicio;
    }

    public String getDestino() {
        return destino;
    }

    public LocalTime getSalida() {
        return salida;
    }

    public LocalTime getLlegada() {
        return llegada;
    }
    
    private static final DateTimeFormatter formato = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public String toString() {
        return "Tren " + inicio + " (" + salida.format(formato) + ") ---> " + destino + " (" + llegada.format(formato) + ")";
    }
    
}
