package app.nepaliapp.padhaighar.serviceimp;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import app.nepaliapp.padhaighar.service.CommonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class CommonServiceImp implements CommonService {

	@Autowired
	private JavaMailSender mailSender;

	@Value("${upload_location}")
	private String uploadLocation;

	// Update file (delete old + upload new)
	@Override
	public String updateFile(String folder, MultipartFile newFile, String oldFile) throws IOException {

		if (newFile == null || newFile.isEmpty()) {
			return oldFile; // keep old one
		}

		// Delete old
		deleteFile(folder, oldFile);

		// Upload new
		return uploadFile(folder, newFile);
	}

	// Delete file
	@Override
	public boolean deleteFile(String folder, String fileName) {

		try {
			if (fileName == null)
				return false;

			String filePath = uploadLocation + File.separator + folder + File.separator + fileName;
			Path path = Paths.get(filePath);

			return Files.deleteIfExists(path);

		} catch (Exception e) {
			return false;
		}
	}

	// Upload file to dynamic folder
	@Override
	public String uploadFile(String folder, MultipartFile file) throws IOException {

		if (file.isEmpty()) {
			return null;
		}

		// Path: upload_location/folder/
		String uploadDir = uploadLocation + File.separator + folder;

		File dir = new File(uploadDir);
		if (!dir.exists())
			dir.mkdirs();

		// Generate unique name
		String originalFilename = file.getOriginalFilename();
		String ext = "";

		int dot = originalFilename.lastIndexOf(".");
		if (dot >= 0)
			ext = originalFilename.substring(dot);

		String newFileName = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 6) + ext;

		Path path = Paths.get(uploadDir, newFileName);
		Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

		return newFileName;
	}

	@Override
	public void removeSessionMessage() {
		HttpServletRequest request = ((ServletRequestAttributes) (RequestContextHolder.getRequestAttributes()))
				.getRequest();
		HttpSession session = request.getSession();
		session.removeAttribute("succMsg");
		session.removeAttribute("errorMsg");
	}

	@Override
	public boolean checkIsloggedin() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean isLoggedIn = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
		return isLoggedIn;
	}

	@Override
	public boolean sendEmail(String to, String subject, String text) {
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom("Subhakhar Software <subhakharsoftware@gmail.com>");
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);
			mailSender.send(message);
			return true;
		} catch (MailException e) {
			return false;
		}
	}

	@Override
	public String formateTextForOTPSend(String userName, String otp) {
		StringBuilder sb = new StringBuilder();

		sb.append("Hello ").append(userName).append(",\n\n");
		sb.append("Your OTP code for password reset is: ").append(otp).append("\n\n");
		sb.append("This code is valid for 10 minutes.\n\n");
		sb.append("If you did not request a password reset, please ignore this email.\n\n");
		sb.append("Thanks,\n");
		sb.append("Subhakhar Software");

		return sb.toString();
	}

	@Override
	public String otpGenerator() {
		int otp = ThreadLocalRandom.current().nextInt(10000, 100000);
		return String.valueOf(otp);
	}

	@Override
	public Model modelForAuth(Model model) {
		Boolean isLoggedIn = checkIsloggedin();
		model.addAttribute("isLoggedIn", isLoggedIn);
		model.addAttribute("isAdmin", true);
		return model;
	}

	@Override
	public String buildUrlString(String folderPath, String fileName) {
		return ServletUriComponentsBuilder.fromCurrentContextPath().scheme("https") // auto force https
				.path("/").path(folderPath.endsWith("/") ? folderPath : folderPath + "/").path(fileName).toUriString();
	}

}