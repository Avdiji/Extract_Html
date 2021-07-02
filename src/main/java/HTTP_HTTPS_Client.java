import java.io.*;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

/**
 * @author Fitor Avdiji
 *
 * Class is used to extract and save html file out of any website (http/https)
 */
public class HTTP_HTTPS_Client {

    /** used HTTP Port 80 **/
    private static final int HTTP_PORT = 80;
    /** target server, from which the html code will be extracted **/
    private final String target_server;
    /** String, in which the html code will be saved **/
    private String HTML_file;

    /** Constructor
     *
     * @param server which this class is trying to connect to
     */
    public HTTP_HTTPS_Client(final String server) {
        target_server = server;
    }

    /** Method connects the client to the server and saves the corresponding html code **/
    public void runClient() {
        System.out.println("Connecting to Server...");
        // initialize socket and in/output stream like this to close everything automatically
        try (Socket socket = new Socket(target_server, HTTP_PORT);
             BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));) {

            System.out.println("connected");

            // write get Request (http Protokoll)
            bw.write(getRequest());
            bw.flush();

            HTML_file = br.readLine() + "\r\n";
            // 301 moved permanently -> https file -> hard to reach with sockets
            if (HTML_file.contains("301 Moved Permanently")) {
                URL_Connection();
            }else{
                HTML_file = getHTML(br);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method is used to establish a https connection to the server (used when the programm fails to establish a http connection
     * @throws IOException
     */
    private void URL_Connection() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("https://" + target_server + getRequest().split(" ")[1]).openConnection();
        connection.setDoOutput(true);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));) {
            HTML_file = getHTML(br);
        } catch (IOException e) {

        }
        connection.disconnect();
    }

    /**
     *
     * @return a String representation of the get request needed
     */
    private String getRequest() {
        StringBuilder result = new StringBuilder();
        // write the get Request
        result.append("GET / HTTP/1.1\r\n");
        result.append("HOST: " + target_server + "\r\n");
        result.append("\r\n");
        return result.toString();
    }

    /**
     * Method saves the body of the website (buffered reader) given.
     * @param br buffered reader, of the corresponding connection
     * @return String, which contains the html code of the website, the client is connected to
     * @throws IOException
     */
    private String getHTML(BufferedReader br) throws IOException {
        StringBuilder result = new StringBuilder();
        String h; // helper variable
        // loop through the lines until the html code starts
        while(!(h = br.readLine()).contains("<html")){
        }
        // save the html code in result
        result.append(h + "\r\n");
        while((h = br.readLine()) != null){
            result.append(h + "\r\n");
        }
        return result.toString();
    }

    /**
     * getter returns a String representation of the html file
     * @return HTML_file
     */
    public String getHTML_file(){
        return HTML_file;
    }

    public static void main(String[] args) {
        final String targetedServer = args[0]; // e.g.: youtube.com, google.com etc...
        HTTP_HTTPS_Client client = new HTTP_HTTPS_Client(targetedServer);
        client.runClient();

        // save the html file
        File f = new File("src/main/HTML_FILES/" + targetedServer + ".html");
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
            bw.write(client.getHTML_file());
        }catch (IOException e){
            e.printStackTrace();
        }


    }


}
