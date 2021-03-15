package unet.uncentralized.betterudpsocket;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;

public class Test {

    public static void main(String[] args)throws Exception {
        UDPServerSocket server = new UDPServerSocket(8080);
        server.addUDPListener(new UDPServerSocket.UDPListener(){
            @Override
            public void accept(UDPSocket socket){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            InputStream in = socket.getInputStream();

                            byte[] b = new byte[4096];
                            int l = in.read(b);

                            System.out.println(new String(b, 0, l));

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        Thread.sleep(1000);


        UDPServerSocket socket = new UDPServerSocket(8081);

        UDPSocket s = socket.create(InetAddress.getLocalHost(), 8080);
        OutputStream o = s.getOutputStream();
        o.write("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
        o.flush();

        s.close();

        Thread.sleep(1000);

        socket.close();

        server.close();
    }
}
