import java.util.Date;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.text.ParseException;

public class Response implements Serializable {

	Date expires;
	Integer maxAge; // max age in milliseconds
	Date accessed;
	byte[] data;

	public Response(String expires, String maxAge,
			byte[] data) {
        
		System.out.println("Expires is in Response constructor: " + expires);
		
		if (expires != null && expires != "") {
			this.expires = convertStringToDate(expires);
			System.out.println("expires is: " + expires);
		} else {
			this.expires = null;
		}

		if (maxAge != null && maxAge != "") {

			this.maxAge = Integer.parseInt(maxAge) * 1000; // convert max age to
															// milliseconds
			System.out.println("max age is: " + this.maxAge);

		} else {
			this.maxAge = null;
		}

		this.accessed = new Date();

		this.data = data;
	}

	public static Date convertStringToDate(String date) {
		try {
			// Sun, 19 Nov 1978 05:00:00 GMT
			DateFormat format = new SimpleDateFormat(
					"E, d MMMM yyyy HH:mm:ss z", Locale.ENGLISH);
			System.out.println("Date is: " + format.parse(date));
			return format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
			System.out.println("Couldn't parse date!!!");
			return null;
		}

	}
	

}