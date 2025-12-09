package app.nepaliapp.padhaighar.api_model;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class SubjectDTO {

	String subjectLogo;
	String categoryName;
	String subjectName;
	String teacherName;
	BigDecimal rating; 
	String description;
	BigDecimal originalprice;
	
}
