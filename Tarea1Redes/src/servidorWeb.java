import java.io.*;
import java.net.*;
import java.util.*;

public class servidorWeb
{	
	public static void main(String [] array)	
	{
		System.out.println("Arrancamos nuestro servidor");
		
		try
		{
			ServerSocket s = new ServerSocket(80);

			System.out.println("Quedamos a la espera de conexion");
			
			while(true)
			{
				Socket entrante = s.accept();
				peticionWeb pCliente = new peticionWeb(entrante);
				pCliente.start();
			}
			
		}
		catch(Exception e)
		{
			System.out.println("Error en servidor\n" + e.toString());
		}
	}
	
}


class peticionWeb extends Thread
{
	private Socket scliente = null;
   	private PrintWriter out = null;

   	peticionWeb(Socket ps)
   	{
   		scliente = ps;
		setPriority(NORM_PRIORITY - 1);
   	}

	public void run()
	{
		System.out.println(currentThread().toString() + " " + "Procesando conexión");

		try
		{
			BufferedReader url = new BufferedReader (new InputStreamReader(scliente.getInputStream()));
  			out = new PrintWriter(new OutputStreamWriter(scliente.getOutputStream(),"8859_1"),true) ;


			String cadena = "";
			
			cadena = url.readLine();
			
			if (cadena != null)
			{
		        StringTokenizer st = new StringTokenizer(cadena);
		        String getpost = st.nextToken(); 
	            
	            if ((st.countTokens() >= 2) && getpost.equals("GET")) 
	            {
	            	retornaFichero(st.nextToken()) ;
	            }
	            else if ((st.countTokens() >= 2) && getpost.equals("POST"))
				{
					String parametros = "", readline = null;
					int largocontenido = 0;
					StringTokenizer st2;
					
					
					while(true) {
						readline = url.readLine();
						if (!(readline.isEmpty())){
							st2 = new StringTokenizer(readline);
							if (st2.nextToken().equals("Content-Length:"))
							{
								largocontenido = Integer.parseInt(st2.nextToken());
							}
						}
						else
						{
							for (int i = 0; i < largocontenido; i++)
					        {
					            parametros += (char)url.read();
					        }
							break;
						}
					}
					
					
					String nombre=null, ip=null, puerto=null;
					String[] parametro = parametros.split("&");
				    for (int i = 0; i < parametro.length; i++) {
				    	if (parametro[i].startsWith("nombre")){
				    		nombre = parametro[i].substring(7);
				    	}
				    	else if (parametro[i].startsWith("ip")){
				    		ip = parametro[i].substring(3);
				    	}
				    	else if (parametro[i].startsWith("puerto")){
				    		puerto = parametro[i].substring(7);
				    	}
				    }
				    
					FileWriter fw = new FileWriter("Contactos.txt",true);
					fw.write("<a class='list-group-item'>" + "Nombre: " + nombre + " IP: " + ip + " Puerto: " + puerto + "</a><br>");
					fw.close();
					
					retornaFichero(st.nextToken());
				}
	            else 
	            {
	            	System.out.println(currentThread().toString() + " 400 Petición Incorrecta");
	            	out.println("400 Petición Incorrecta") ;
	            }
				
				while (cadena != null && cadena.length() != 0)
				{
					System.out.println(currentThread().toString() + " " + cadena);
					cadena = url.readLine();
				}
				System.out.println(currentThread().toString() + " Fin Thread\n");
			}
			else
			{
				System.out.println(currentThread().toString() + " 400 Petición Vacia");
				out.println("400 Petición Vacia");
			}
		}
		catch(Exception e)
		{
			System.out.println(currentThread().toString() + " - " + "Error en servidor\n" + e.toString());
		}
	}
	
	
	void retornaFichero(String fichero)
	{
		if (fichero.startsWith("/"))
		{
			fichero = fichero.substring(1) ;
		}
        

        if (fichero.endsWith("/") || fichero.equals(""))
        {
        	fichero = fichero + "index.html" ;
        }
        
        try
        {
	        
		    File archivo = new File(fichero);
		    
		    if (archivo.exists()) 
		    {
		    	if (fichero.endsWith("html")){
		      		out.println("HTTP/1.0 200 ok");
					out.println("Server: Roberto Server/1.0");
					out.println("Date: " + new Date());
					out.println("Content-Type: text/html");
					out.println("Content-Length: " + archivo.length());
					out.println("\n");
		    	}
			
				BufferedReader BufferFichero = new BufferedReader(new FileReader(archivo));
				
				
				String readline = "";
				
				do			
				{
					readline = BufferFichero.readLine();
	
					if (readline != null )
					{
						out.println(readline);
					}
				}
				while (readline != null);
				
				BufferFichero.close();
				out.close();
				
			}
			else
			{
	      		out.println("HTTP/1.0 400 ok");
	      		out.close();
			}
			
		}
		catch(Exception e)
		{
			System.out.println(currentThread().toString() + " - " + "Error al retornar fichero");	
		}

	}
	
}