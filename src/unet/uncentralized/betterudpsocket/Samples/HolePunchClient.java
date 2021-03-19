package unet.uncentralized.betterudpsocket.Samples;

import unet.uncentralized.betterudpsocket.UDPServerSocket;
import unet.uncentralized.betterudpsocket.UDPSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;

public class HolePunchClient {

    public static void main(String[] args)throws IOException {
        //CREATE A PEER CLIENT
        UDPServerSocket server = new UDPServerSocket();
        server.addUDPListener(new UDPServerSocket.UDPListener(){
            @Override
            public void accept(UDPSocket socket){
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        socket.setNoDelay(true);
                        DataInputStream in = new DataInputStream(socket.getInputStream());

                        //START READING FROM PEER
                        while(!socket.isClosed()){
                            try{
                                byte[] b = new byte[4096];
                                int l = in.read(b);
                                System.out.println(new String(b, 0, l));
                            }catch(IOException e){
                            }
                        }
                    }
                }).start();
            }
        });

        //CONNECT TO THE HOLE-PUNCH-SERVER
        UDPSocket socket = server.create(InetAddress.getByName("192.168.0.132"), 8080);

        DataInputStream in = new DataInputStream(socket.getInputStream());
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        //SEND SOME RANDOM BYTE TO SEND A PACKET
        out.writeByte(0x00);
        out.flush();

        //WAIT FOR RESPONSE
        while(in.available() < 1){
        }

        //READ IF WE WHERE FIRST OR SECOND
        byte aorb = in.readByte();

        //READ IP ADDRESS
        byte[] buffer = null;

        switch(in.readByte()){
            case 0x04:
                buffer = new byte[4];
                break;

            case 0x06:
                buffer = new byte[16];
                break;
        }

        in.read(buffer);

        InetAddress address = InetAddress.getByAddress(buffer);
        int port = in.readInt(); //READ PORT

        //WE NO LONGER NEED THE SERVER
        socket.close();

        //WE WILL NEED TO SEND A PUNCH PACKET TO THE PEER - THIS PACKET WILL BE DROPPED
        UDPSocket punch = server.create(address, port);
        punch.setNoDelay(true);
        out = new DataOutputStream(punch.getOutputStream());

        //WE WILL SEND 10 PACKETS TO ENSURE THAT THEY GET A DROP PACKET MESSAGE
        for(int i = 0; i < 10; i++){
            punch.sendPunch(); //SEND THE DROP PACKET - THIS WILL ALLOW THE PEER TO CONNECT TO US
            try{
                Thread.sleep(50);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        //A WILL BE KILLED AS WE DONT NEED 2 SOCKETS OPEN - B WILL BE THE SOCKET THAT STAYS ALIVE
        if(aorb == 0x01){
            int i = 0;
            while(!punch.isClosed()){
                out.write(("ASDASDA "+i).getBytes());
                i++;

                try{
                    Thread.sleep(100);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        punch.close();
    }
}
