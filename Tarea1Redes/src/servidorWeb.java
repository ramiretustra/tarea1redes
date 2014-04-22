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
			BufferedReader in = new BufferedReader (new InputStreamReader(scliente.getInputStream()));
  			out = new PrintWriter(new OutputStreamWriter(scliente.getOutputStream(),"8859_1"),true) ;


			String cadena = "";		// cadena donde almacenamos las lineas que leemos
			
			cadena = in.readLine();
			
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
					
					List<String> headers = new LinkedList<>();
					String body = "";
					String line = null;
					int contentLength = 0;
					StringTokenizer st2;
					
					
					boolean continuar = true;
					while(continuar) {
						line = in.readLine();
						if (line.isEmpty()){
							headers.add(line);
							continuar = false;
							
	
							int value=0;
							int cont = 0;
					        line = "";
					        while((value = in.read()) != -1){
					            char c = (char)value;
					            line = line + c;
					            cont++;
					            if(cont == contentLength) break;
					        }
							body = body + line + '\n';
						}
						else{
							st2 = new StringTokenizer(line);
							if (st2.nextToken().equals("Content-Length:")){
								contentLength = Integer.parseInt(st2.nextToken());
							}
							headers.add(line);
						}
					}
					
					
					String nombre=null, ip=null, puerto=null;
					String[] keys = body.split("&");
				    for (int j = 0; j < keys.length; j++) {
				    	String[] valores = keys[j].split("=");
				    	if(valores[0].equals("nombre")){
				    		nombre=valores[1];
				    	}
				    	else if(valores[0].equals("ip")){
				    		ip=valores[1];
				    	}
				    	else if(valores[0].equals("puerto")){
				    		puerto=valores[1];
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
					cadena = in.readLine();
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
	
	
	void retornaFichero(String sfichero)
	{
		if (sfichero.startsWith("/"))
		{
			sfichero = sfichero.substring(1) ;
		}
        

        if (sfichero.endsWith("/") || sfichero.equals(""))
        {
        	sfichero = sfichero + "index.html" ;
        }
        
        try
        {
	        
		    File mifichero = new File(sfichero);
		    
		    if (mifichero.exists()) 
		    {
		    	if (sfichero.endsWith("html")){
		      		out.println("HTTP/1.0 200 ok");
					out.println("Server: Roberto Server/1.0");
					out.println("Date: " + new Date());
					out.println("Content-Type: text/html");
					out.println("Content-Length: " + mifichero.length());
					out.println("\n");
		    	}
			
				BufferedReader ficheroLocal = new BufferedReader(new FileReader(mifichero));
				
				
				String linea = "";
				
				do			
				{
					linea = ficheroLocal.readLine();
	
					if (linea != null )
					{
						out.println(linea);
					}
				}
				while (linea != null);
				
				ficheroLocal.close();
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