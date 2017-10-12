package Server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	public static void main(String args[]){
		try {
			@SuppressWarnings("resource")
			ServerSocket server=new ServerSocket(6666);
			System.out.println("服务器已启动...");
			Socket socket=null;
			int i=0;
			while(true){
				 socket=server.accept();
				 String uid=socket.getLocalAddress().getHostAddress()+":"+socket.getLocalPort()
				 	+Integer.toString(i);
				 ServerThread serverThread=new ServerThread(socket,uid);
				 serverThread.start();
				 i++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
