import java.io.*;
import java.net.*;
import java.util.*;

public class servidorWeb
{
	int puerto = 90;
	
	final int ERROR = 0;
	final int WARNING = 1;
	final int DEBUG = 2;
	
		
	// funcion para centralizar los mensajes de depuracion

	void depura(String mensaje)
	{
		depura(mensaje,DEBUG);
	}	

	void depura(String mensaje, int gravedad)
	{
		System.out.println("Mensaje: " + mensaje);
	}	
		
	// punto de entrada a nuestro programa
	public static void main(String [] array)	
	{
		servidorWeb instancia = new servidorWeb(array);	
		instancia.arranca();
	}
	
	// constructor que interpreta los parameros pasados
	servidorWeb(String[] param)
	{
		procesaParametros();	
	}
	
	// parsearemos el fichero de entrada y estableceremos las variables de clase
	boolean procesaParametros()
	{
		return true;	
	}
	
	boolean arranca()
	{
		depura("Arrancamos nuestro servidor",DEBUG);
		
		try
		{
		
			
			ServerSocket s = new ServerSocket(90);

			depura("Quedamos a la espera de conexion");
			
			while(true)
			{
				Socket entrante = s.accept();
				peticionWeb pCliente = new peticionWeb(entrante);
				pCliente.start();
			}
			
		}
		catch(Exception e)
		{
			depura("Error en servidor\n" + e.toString());
		}
		
		return true;
	}
	
}



class peticionWeb extends Thread
{
	int contador = 0;

	final int ERROR = 0;
	final int WARNING = 1;
	final int DEBUG = 2;

	void depura(String mensaje)
	{
		depura(mensaje,DEBUG);
	}	

	void depura(String mensaje, int gravedad)
	{
		System.out.println(currentThread().toString() + " - " + mensaje);
	}	

	private Socket scliente 	= null;		// representa la petición de nuestro cliente
   	private PrintWriter out 	= null;		// representa el buffer donde escribimos la respuesta

   	peticionWeb(Socket ps)
   	{
		depura("El contador es " + contador);
		
		contador ++;
		
		
		
		scliente = ps;
		setPriority(NORM_PRIORITY - 1); // hacemos que la prioridad sea baja
   	}

	public void run() // emplementamos el metodo run
	{
		depura("Procesamos conexion");

		try
		{
			BufferedReader in = new BufferedReader (new InputStreamReader(scliente.getInputStream()));
  			out = new PrintWriter(new OutputStreamWriter(scliente.getOutputStream(),"8859_1"),true) ;


			String cadena = "";		// cadena donde almacenamos las lineas que leemos
			
			
			cadena = in.readLine();

			if (cadena != null )
			{
				depura("--" + cadena + "-");
			}



			StringTokenizer st = new StringTokenizer(cadena);
			String getpost = st.nextToken();

			if ((st.countTokens() >= 2) && getpost.equals("GET")) 
			{
				retornaFichero(st.nextToken());
			}
			else if (getpost.equals("POST"))
			{
				String line = null;
				List<String> headers = new LinkedList<>();
				String body = "";
				int contentLength = 0;

				do{
					line = in.readLine();
					if (line.isEmpty()){
						headers.add(line);

						int value=0;
						int contador = 0;
						line = "";
						while((value = in.read()) != -1){
							char c = (char)value;
							line = line + c;
							contador++;
							if(contador == contentLength){
								break;
							}
						}
						body = body + line + '\n';
					}
					else{
						st = new StringTokenizer(line);
						if (st.nextToken().equals("Content-Length:")){
							contentLength = Integer.parseInt(st.nextToken());
						}
						headers.add(line);
					}
				}while (!line.isEmpty());
				
				String nombre=null, ip=null, puerto=null;
				
				String[] keys = body.split("&");
			    for (int i = 0; i < keys.length; i++) {
			    	String[] valores = keys[i].split("=");
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
				    fw.write("<a href='#' class='list-group-item'>" + nombre + "</a><br>\n");
				    fw.close();
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			
			out.close(); 
			in.close();
			scliente.close();


		}
		catch(Exception e)
		{
			depura("Error en servidor\n" + e.toString());
		}
			
		depura("Hemos terminado");
	}
	
	
	void retornaFichero(String sfichero)
	{
		depura("Recuperamos el fichero " + sfichero);
		
		// comprobamos si tiene una barra al principio
		if (sfichero.startsWith("/"))
		{
			sfichero = sfichero.substring(1) ;
		}
        
        // si acaba en /, le retornamos el index.html de ese directorio
        if (sfichero.endsWith("/") || sfichero.equals(""))
        {
        	sfichero = sfichero + "index.html" ;
        }
        
        try
        {
	        
		    // Ahora leemos el fichero y lo retornamos
		    File mifichero = new File(sfichero) ;
		        
		    if (mifichero.exists()) 
		    {
	      		out.println("HTTP/1.0 200 ok");
				out.println("Server: Roberto Server/1.0");
				out.println("Date: " + new Date());
				out.println("Content-Type: text/html");
				out.println("Content-Length: " + mifichero.length());
				out.println("\n");
			
				BufferedReader ficheroLocal = new BufferedReader(new FileReader(mifichero));
				
				
				String linea = "";
				
				do			
				{
					linea = ficheroLocal.readLine();
	
					if (linea != null )
					{
						// sleep(500);
						out.println(linea);
					}
				}
				while (linea != null);
				
				depura("fin envio fichero");
				
				ficheroLocal.close();
				out.close();
				
			}  // fin de si el fiechero existe 
			else
			{
				depura("No encuentro el fichero " + mifichero.toString());	
	      		out.println("HTTP/1.0 400 ok");
	      		out.close();
			}
			
		}
		catch(Exception e)
		{
			depura("Error al retornar fichero");	
		}

	}
	
}