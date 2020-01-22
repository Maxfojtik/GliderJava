import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


public class GliderComms 
{
	static JSONObject lastTelem = new JSONObject();
	static long lastTeleRece = 0;
	static long lastTrySend = 0;
	static boolean toArm = false;
	static boolean toEnable = false;
	public static void main(String[] args) throws Exception
	{
		new Frame();
        new MicroConnection();
//        Joystick stick = new Joystick();
		HttpServer server = HttpServer.create(new InetSocketAddress(1025), 0);
        server.createContext("/main.html", new GeneralHandler("src/website/main.html"));
        server.createContext("/telem.html", new Info());
        server.createContext("/key.html", new Key());
        server.setExecutor(null); // creates a default executor
        server.start();
//        System.out.println("Waiting for connections");
        Frame.print("HTTP Server Up");
        long lastTelemTime = 0;
		while(true)
		{
			Thread.sleep(1);
			if(System.currentTimeMillis()-lastTelemTime>4000 && MicroConnection.canSend)
			{
				lastTelemTime = System.currentTimeMillis();
				MicroConnection.send("ping");
			}
			if(System.currentTimeMillis()-lastTrySend>5000)
			{
				MicroConnection.canSend = true;
				lastTrySend = System.currentTimeMillis();
			}
			if(System.currentTimeMillis()-lastTeleRece>2500)
			{
				lastTelem.put("Connected", false);
			}
	    	MicroConnection.logic();
	    	
//	    	stick.logic();//only need to update joystick 10 times a second
		}
	}
	static class GeneralHandler implements HttpHandler
	{
		String path = "";
		public GeneralHandler(String path) 
		{
			this.path = path;
		}
	    @Override
	    public void handle(HttpExchange t) throws IOException {
			String HTML = loadString(path);
	    	InputStream requestBody = t.getRequestBody();
	    	int tempChar = requestBody.read();
	    	String text = ""; 
	    	while(tempChar!=-1)
	    	{
	    		text = text+(char)tempChar;
	    		tempChar = requestBody.read();
	    	}
//	    	System.out.println("HTML: "+HTML);
//	    	System.out.println("connection from: "+t.getRemoteAddress());
	    	String response = HTML;
	    	t.sendResponseHeaders(200, response.length());
	        OutputStream os = t.getResponseBody();
	        os.write(response.getBytes());
	        os.close();
	    }
	}
	static class Info implements HttpHandler
	{
		public Info() 
		{
			
		}
	    @Override
	    public void handle(HttpExchange t) throws IOException 
	    {
//	    	System.out.println("Handle");
	    	try
	    	{
	    		InputStream requestBody = t.getRequestBody();
		    	int tempChar = requestBody.read();
		    	String text = "";
		    	while(tempChar!=-1)
		    	{
		    		text = text+(char)tempChar;
		    		tempChar = requestBody.read();
		    	}
//		    	System.out.println("WEBINPUT: "+text);
		    	String responce = lastTelem.toString();
		    	if(responce==null)
		    	{
		    		responce = "{}";
		    	}
//		    	System.out.println("Responding with "+responce);
		    	t.sendResponseHeaders(200, responce.length());
		        OutputStream os = t.getResponseBody();
		        os.write(responce.getBytes());
		        os.close();
	    	}
	    	catch(Exception e)
	    	{
	    		String response = e.getMessage();
		    	t.sendResponseHeaders(200, response.length());
		        OutputStream os = t.getResponseBody();
		        os.write(response.getBytes());
		        os.close();
	    		e.printStackTrace();
	    	}
	    }
	}
	static class Key implements HttpHandler
	{
		public Key() 
		{
			
		}
	    @Override
	    public void handle(HttpExchange t) throws IOException 
	    {
//	    	System.out.println("Handle");
    		URI uri = t.getRequestURI();
    		Frame.print(uri.toASCIIString());
    		if(uri.toASCIIString().contains("keyD"))
    		{
    			keyDown(Integer.parseInt(uri.toASCIIString().split("=")[1]));
    		}
    		else
    		{
    			keyUp(uri.toASCIIString().split("=")[1]);
    		}
    		String responce = "ok";
    		t.sendResponseHeaders(200, responce.length());
	        OutputStream os = t.getResponseBody();
	        os.write(responce.getBytes());
	        os.close();
	    }
	}
	static void keyDown(int key)
	{
		Frame.print("KEY: "+key);
		if(key==13)//[enter]
		{
			MicroConnection.send("ARMT");
			toArm = true;
		}
		if(key==32)//[space]
		{
//			DataLogging.stop();
			MicroConnection.send("ARMF");
			toArm = false;
		}
		if(key==69)//E
		{
			MicroConnection.send("ENAT");
			toEnable = true;
		}
		if(key==38)//[up]
		{
		}
		if(key==40)//[down]
		{
		}
		if(key==37)//[left]
		{
		}
		if(key==39)//[right]
		{
			MicroConnection.send("TESTRIGHT");
		}
		if(key==71)//[G]
		{
			MicroConnection.send("G");
		}
		if(key==67)//[C]
		{
			MicroConnection.send("CALG");
		}
	}
	static void keyUp(String key)
	{
		
	}
	public static String loadString(String filename)
	{
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = "";
			String info = "";
			line = reader.readLine();
			while(line!=null)
			{
				info = info + line + "\n";
				line = reader.readLine();
			}
			reader.close();
			return info;
		}
		catch (Exception e) 
		{
			//CrashGUI G = new CrashGUI(e.toString()); G.printStackTrace(e.getStackTrace());
		}
		return null;
	}
}