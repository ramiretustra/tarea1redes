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
			ServerSocket s = new ServerSocket(90);

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


			String cadena = "";		// cadena donde almacenamos las lineas que leemos
			
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
					
					List<String> lista_lineas = new LinkedList<>();
					String contenido = "";
					String linea = null;
					int contentLength = 0;
					StringTokenizer st2;
					
					
					while(true) {
						linea = url.readLine();
						if (linea.isEmpty()){
							lista_lineas.add(linea);
							
	
							int ultimo=0;
							int i = 0;
					        linea = "";
					        while((ultimo = url.read()) != -1)
					        {
					            char c = (char)ultimo;
					            linea += c;
					            i++;
					            if(i == contentLength) break;
					        }
							contenido += linea + '\n';
							break;
						}
						else
						{
							st2 = new StringTokenizer(linea);
							if (st2.nextToken().equals("Content-Length:"))
							{
								contentLength = Integer.parseInt(st2.nextToken());
							}
							lista_lineas.add(linea);
						}
					}
					
					
					String nombre=null, ip=null, puerto=null;
					String[] parametros = contenido.split("&");
				    for (int j = 0; j < parametros.length; j++) {
				    	String[] datos = parametros[j].split("=");
				    	if(datos[0].equals("nombre")){
				    		nombre=datos[1];
				    	}
				    	else if(datos[0].equals("ip")){
				    		ip=datos[1];
				    	}
				    	else if(datos[0].equals("puerto")){
				    		puerto=datos[1];
				    	}
				    }
	
					try {
					    FileWriter fw = new FileWriter("Contactos.txt",true);
					    fw.write("<a class='list-group-item'>" + "Nombre: " + nombre + " IP: " + ip + " Puerto: " + puerto + "</a><br>\n");
					    fw.close();
					} catch (FileNotFoundException | UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					
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
				
				
				String linea = "";
				
				do			
				{
					linea = BufferFichero.readLine();
	
					if (linea != null )
					{
						out.println(linea);
					}
				}
				while (linea != null);
				
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