package app.nepaliapp.padhaighar.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;
// ... Lombok annotations

@Entity
@Data
public class CourseVideoModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    // ✅ NEW FIELD: Link to the course
	private Long courseId; 
    private String courseName; // Stored for display convenience
	
	private String title;
	private String authorname;
	
	@Lob 
	private String short_description;
	
	private String video; // Filename/path
	
	Boolean isPaid;
	
}