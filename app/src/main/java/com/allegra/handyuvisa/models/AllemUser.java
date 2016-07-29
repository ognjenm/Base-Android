package com.allegra.handyuvisa.models;

/**
 * Created by victor on 28/02/15.
 * com.allem.allemevent.models
 */
public class AllemUser {
    public String saludo;
    public String nombre;
    public String apellido;
    public String email;
    public String hashpassword;
    public String idSesion;
    public int idCuenta;
    public boolean estado;
    public String channel;
    public String celular;

    /**
     *
     * @param saludo
     * @param nombre
     * @param apellido
     * @param email
     * @param hashpassword
     * @param idSesion
     * @param idCuenta
     * @param estado
     */

    public AllemUser(String saludo, String nombre, String apellido, String email, String hashpassword, String idSesion, int idCuenta, boolean estado, String celular){
        this.saludo = saludo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.hashpassword = hashpassword;
        this.idSesion = idSesion;
        this.idCuenta = idCuenta;
        this.estado = estado;
        this.celular= celular;
    }

}