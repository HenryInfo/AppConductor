package com.taxipluscajamarca.appconductor.model;

/**
 * Created by hbs on 1/10/16.
 */

public class Ruta {
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdmovilidad() {
        return idmovilidad;
    }

    public void setIdmovilidad(int id) {
        this.idmovilidad= id;
    }


    public Double getLatitudO() {
        return latitudO;
    }

    public void setLatitudO(Double latitudO) {
        this.latitudO = latitudO;
    }

    public Double getLatitudD() {
        return latitudD;
    }

    public void setLatitudD(Double latitudD) {
        this.latitudD = latitudD;
    }

    public Double getLongitudO() {
        return longitudO;
    }

    public void setLongitudO(Double longitudO) {
        this.longitudO = longitudO;
    }

    public Double getLongitudD() {
        return longitudD;
    }

    public void setLongitudD(Double longitudD) {
        this.longitudD = longitudD;
    }

    public char getEstado() {
        return estado;
    }

    public void setEstado(char estado) {
        this.estado = estado;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String estado) {
        this.origen = origen;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String estado) {
        this.destino = destino;
    }

    private int id;
    private int idmovilidad;
    private Double latitudO;
    private Double latitudD;
    private Double longitudO;
    private Double longitudD;
    private Double precio;
    private String origen;
    private String destino;

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    private char estado;

    public Ruta(int id, int idmovilidad, Double latitudO, Double latitudD, Double longitudO, Double longitudD, char estado, double precio, String origen, String destino) {
        this.id = id;
        this.latitudO = latitudO;
        this.latitudD = latitudD;
        this.longitudO = longitudO;
        this.longitudD = longitudD;
        this.estado = estado;
        this.precio = precio;
        this.origen=origen;
        this.destino=destino;
        this.idmovilidad=idmovilidad;
    }
}
