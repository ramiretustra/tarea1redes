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
			
			while(true)  // bucle infinito .... ya veremos como hacerlo de otro modo
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
			int i=0;				// lo usaremos para que cierto codigo solo se ejecute una vez
	
			do			
			{
				cadena = in.readLine();

				if (cadena != null )
				{
					// sleep(500);
					depura("--" + cadena + "-");
				}


				if(i == 0)
				{
			        i++;
			        
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
                    	out.println("400 Petición Incorrecta") ;
                    }
				}
				
			}
			while (cadena != null && cadena.length() != 0);

		}
		catch(Exception e)
		{
			depura("Error en servidor\n" + e.toString());
		}
			
		depura("Hemos terminado");
	}
	
	
	void retornaFichero(String sfichero)
	{
		//depura("Recuperamos el fichero " + sfichero);
		
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
				
				depura("fin envio fichero");
				
				ficheroLocal.close();
				out.close();
				
			}
			else
			{
				//depura("No encuentro el fichero " + mifichero.toString());	
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