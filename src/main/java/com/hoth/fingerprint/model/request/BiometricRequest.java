package com.hoth.fingerprint.model.request;

import java.util.ArrayList;

public class BiometricRequest {
	String TpAccion = null;
	ArrayList<Object> FingerPrintToVerify = new ArrayList<Object>();
	public String getTpAccion() {
		return TpAccion;
	}
	public void setTpAccion(String tpAccion) {
		TpAccion = tpAccion;
	}
	public ArrayList<Object> getFingerPrintToVerify() {
		return FingerPrintToVerify;
	}
	public void setFingerPrintToVerify(ArrayList<Object> fingerPrintToVerify) {
		FingerPrintToVerify = fingerPrintToVerify;
	}
	@Override
	public String toString() {
		return "{\"TpAccion\":\"" + TpAccion + "\", \"FingerPrintToVerify\":\"" + FingerPrintToVerify + "\"}";
	}
	
}
