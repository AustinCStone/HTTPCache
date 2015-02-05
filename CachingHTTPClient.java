import java.net.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Locale;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class CachingHTTPClient {

	public static String CACHE_LOCATION = "/tmp/as46569/assignment1/cache.ser";
    
	
	//Returns map from memory, if not found returns null
	public static Map loadMap(String location) {

		Map map = null;
		try {
			FileInputStream fileIn = new FileInputStream(location);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			map = (Map) in.readObject();
			in.close();
			fileIn.close();
		} catch (IOException i) {
			i.printStackTrace();
			return null;
		} catch (ClassNotFoundException c) {
			System.out.println("class not found!?");
			c.printStackTrace();
			return null;
		}
		return map;
	}

	//saves map to memory. should not fail.
	public static void serializeMap(Map map, String location) {
		try {
			FileOutputStream fileOut = new FileOutputStream(location);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(map);
			out.close();
			fileOut.close();
			System.out.println("Serialized data is saved in /tmp/as46569/assignment1/cache.ser");
		} catch (IOException i) {
			i.printStackTrace();
		}
	}
    
	// returns response if response is in cache. else returns null
	// queries the source for if-modified-since if necessary
	public static Response checkCache(Map cache, String url) {

		Response response = (Response) cache.get(url);
		if (response == null) {
			System.out.println("Url not in cache");
			return null;
		} else {
			System.out.println("Found response in cache!");
			Date today = new Date();
			if (response.maxAge != null) {
				// both headers present, max age takes precedence
				System.out.println("Using max-age to determine if resp is valid.");
				Date expireDate = new Date(response.accessed.getTime() + response.maxAge);
				if (today.after(expireDate)) {
					//query origin server
					return null;
				} else {
					//response is valid
					return response;
				}
			}

			if (response.expires != null) {
				System.out.println("Using expires to determine if resp is valid.");
				if (response.expires.after(today)) {
					System.out.println("Expires after today.");
					System.out.println("Today is: " + today);
					System.out.println("Expires is: " + response.expires);
					return response;
				} else {
					System.out.println("Expires before today.");
					System.out.println("Today is: " + today);
					System.out.println("Expires is: " + response.expires);
					//query origin server
					return null;
				}
				
			} 
		   
			System.out.println("Response was cached without expires or max-age?");
			return null;
		}
	}

	// Gets the response from the url, returns a response object
	public static Response getResponse(URL url) {
		try {
			
			URLConnection connection = url.openConnection();

			String expires = connection.getHeaderField("Expires");
			String maxAge = connection.getHeaderField("max-age");

			// TODO: how to allocate buffer size correctly?
			InputStream input = connection.getInputStream();
			byte[] buffer = new byte[4096];
			int n = -1;

			while ((n = input.read(buffer)) != -1) {
				if (n > 0) {
					// System.out.write(buffer, 0, n);
				}
			}
			Response response = new Response(expires, maxAge, buffer);
			return response;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;

	}
	
	public static void printResponse(Response response, boolean fromCache) {
		
		DateFormat df = new SimpleDateFormat(
				"E, d MMMM yyyy HH:mm:ss z", Locale.ENGLISH);
		
		if (fromCache) {
			System.out.println("***** Serving from the cache – start *****");
		} else {
			System.out.println("***** Serving from the source – start *****");
		}
		System.out.println("Max-age: " + response.maxAge);
		System.out.println("Expires:" + df.format(response.expires));
		System.out.println("Need to put response content here.");
		
		if (fromCache) {
			System.out.println("***** Serving from the cache – end *****");
		} else {
			System.out.println("***** Serving from the source – end *****");
		}
	}

	public static void main(String args[]) {

		if (args.length < 1) {
			System.out.println("Usage:");
			System.out.println("java CachingHTTPClient <url>");
			System.exit(0);
		}
		
		Map cache = null; 
		try {

			cache = loadMap(CACHE_LOCATION);
			if (cache==null) {
				System.out.println("Cache was returned from loadMap as null. Creating new cache");
				cache = new HashMap<String, Response>();
			} else {
				System.out.println("Cache was loaded successfully from memory");
			}
		} catch (Exception e) {
			System.out.println("Exception happened when loading cache. Creating new cache.");
			cache = new HashMap<String, Response>();
		}

		URL url = null;

		try {
			url = new URL(args[0]);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		assert url != null;
  
		Boolean fromCache = false;
		Response response = checkCache(cache, url.toString());

		if (response!=null) {
			fromCache = true;
			System.out.println("response found in cache!");
		} else {
			response = getResponse(url);
			if (response.expires==null && response.maxAge==null) {
				System.out.println("both expires and maxAge are null, not caching.");
			} else {
				System.out.println("putting new response into cache.");
				cache.put(url.toString(), response);
				System.out.println("serialized!!");
				serializeMap(cache, CACHE_LOCATION);
			}
		}
		
		printResponse(response, fromCache);
		
	}
}
