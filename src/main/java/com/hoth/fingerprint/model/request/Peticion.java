package com.hoth.fingerprint.model.request;

public class Peticion {
    
    private String tpAccion;

    private String enrolamiento;

    private String captura;

    private String numeroEmpleado;

    public String getNumeroEmpleado() {
        return numeroEmpleado;
    }

    public void setNumeroEmpleado(String numeroEmpleado) {
        this.numeroEmpleado = numeroEmpleado;
    }

    public String getEnrolamiento() {
        return enrolamiento;
    }

    public void setEnrolamiento(String enrolamiento) {
        this.enrolamiento = enrolamiento;
    }

    public String getCaptura() {
        return captura;
    }

    public void setCaptura(String captura) {
        this.captura = captura;
    }

    public String getTpAccion() {
        return tpAccion;
    }

    public void setTpAccion(String tpAccion) {
        this.tpAccion = tpAccion;
    }

    @Override
    public String toString() {
        return "Peticion [tpAccion=" + tpAccion + ", enrolamiento=" + enrolamiento + ", captura=" + captura + "]";
    }


    
}
