package com.hoth.fingerprint.model.response;

public class BiometricResponse {
	String Name = null;
	Integer Result = null;
    String Message = null;
    Integer LastCodeError = null;
    String LastMessageError = null;
    String BiometricData1 = null;
    String BiometricData2 = null;
    Boolean VerifyBiometricData = false;
    
	
    public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	public Integer getResult() {
		return Result;
	}
	public void setResult(Integer result) {
		Result = result;
	}
	public String getMessage() {
		return Message;
	}
	public void setMessage(String message) {
		Message = message;
	}
	public Integer getLastCodeError() {
		return LastCodeError;
	}
	public void setLastCodeError(Integer lastCodeError) {
		LastCodeError = lastCodeError;
	}
	public String getLastMessageError() {
		return LastMessageError;
	}
	public void setLastMessageError(String lastMessageError) {
		LastMessageError = lastMessageError;
	}
	public String getBiometricData1() {
		return BiometricData1;
	}
	public void setBiometricData1(String biometricData1) {
		BiometricData1 = biometricData1;
	}
	public String getBiometricData2() {
		return BiometricData2;
	}
	public void setBiometricData2(String biometricData2) {
		BiometricData2 = biometricData2;
	}
	public Boolean getVerifyBiometricData() {
		return VerifyBiometricData;
	}
	public void setVerifyBiometricData(Boolean verifyBiometricData) {
		VerifyBiometricData = verifyBiometricData;
	}
}
