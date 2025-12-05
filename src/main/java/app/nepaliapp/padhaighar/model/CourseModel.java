package app.nepaliapp.padhaighar.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Getter
@Setter
@ToString
public class CourseModel {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long id;
    String name;
    String logo;
    Long categoryId;
    String categoryName;
    String teacherName;
    Long teacherId;
    @Lob
    String description;
    String searchTags;
    String Price;
    Boolean isActive;
    String discount;
}
