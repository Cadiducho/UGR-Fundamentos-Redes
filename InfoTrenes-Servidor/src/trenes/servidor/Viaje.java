package trenes.servidor;

import java.time.LocalTime;

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

    @Override
    public String toString() {
        return "Viaje{" + "inicio=" + inicio + ", destino=" + destino + ", salida=" + salida + ", llegada=" + llegada + '}';
    }
    
}
