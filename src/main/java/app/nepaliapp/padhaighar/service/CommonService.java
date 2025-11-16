package app.nepaliapp.padhaighar.service;


public interface CommonService {

	public void removeSessionMessage();
	public boolean checkIsloggedin();
	public boolean sendEmail(String to, String subject,String text);
	public String formateTextForOTPSend(String userName,String otp);
	public String otpGenerator(); 
	
}