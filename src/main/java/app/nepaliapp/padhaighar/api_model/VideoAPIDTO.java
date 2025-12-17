package app.nepaliapp.padhaighar.api_model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoAPIDTO {

	String videoTitle;
	String url;
	boolean isUnlocked;
	boolean isStudied;
	
}
