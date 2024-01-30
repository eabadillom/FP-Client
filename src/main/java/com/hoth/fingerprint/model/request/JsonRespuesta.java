package com.hoth.fingerprint.model.request;

public class JsonRespuesta {

	
	Integer numEmpleado;
	String token;
	String huella;
	String huella2;
	
	
	public JsonRespuesta() {
	
	}
	public Integer getNumEmpleado() {
		return numEmpleado;
	}
	public void setNumEmpleado(Integer numEmpleado) {
		this.numEmpleado = numEmpleado;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public String getHuella() {
		return huella;
	}
	public void setHuella(String huella) {
		this.huella = huella;
	}
	public String getHuella2() {
		return huella2;
	}
	public void setHuella2(String huella2) {
		this.huella2 = huella2;
	}
	@Override
	public String toString() {
		return "JsonRespuesta [numEmpleado=" + numEmpleado + ", token=" + token + ", huella=" + huella + ", huella2="
				+ huella2 + "]";
	}
	
	
	
	
}


