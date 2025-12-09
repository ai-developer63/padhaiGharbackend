package app.nepaliapp.padhaighar.api_model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileModelForAPI {
	String name;
	String country;
	String email;
	Boolean isPaid;
	String days;
	String url;
}