package unet.uncentralized.betterudpsocket.Samples;

import unet.uncentralized.betterudpsocket.UDPServerSocket;
import unet.uncentralized.betterudpsocket.UDPSocket;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.InetAddress;

public class Test {

    //AVERAGE NAT WILL SHUT OFF AT AROUND 30-60 SECONDS
    //FOR KEEP ALIVE WE MUST SEND 0x03 EVERY 25 SECONDS

    public static void main(String[] args)throws Exception {
        UDPServerSocket server = new UDPServerSocket(8080);
        server.setSafeMode(true);
        server.addUDPListener(new UDPServerSocket.UDPListener(){
            @Override
            public void accept(UDPSocket socket){
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            socket.setKeepAlive(true);
                            DataInputStream in = new DataInputStream(socket.getInputStream());

                            System.out.println(in.readByte());

                            byte[] b = new byte[26];
                            int l = in.read(b);

                            System.out.println(new String(b, 0, l));


                            System.out.println(in.readByte());

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        UDPServerSocket socket = new UDPServerSocket();
        socket.setSafeMode(true);

        UDPSocket s = socket.create(InetAddress.getByName("192.168.0.132"), 8080);
        s.setKeepAlive(true);
        s.setNoDelay(true);
        OutputStream o = s.getOutputStream();
        o.write(0xcc);
        //o.flush();

        o.write("ABCDEFGHIJKLMNOPQRSTUVWXYZ".getBytes());
        //o.flush();

        o.write(0xcc);
        //o.flush();


        Thread.sleep(1000);

        s.close();

        socket.close();
        server.close();
    }
}
