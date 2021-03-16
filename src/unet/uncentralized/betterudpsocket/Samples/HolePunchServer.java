package unet.uncentralized.betterudpsocket.Samples;

import unet.uncentralized.betterudpsocket.UDPKey;
import unet.uncentralized.betterudpsocket.UDPServerSocket;
import unet.uncentralized.betterudpsocket.UDPSocket;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;

public class HolePunchServer {

    public static void main(String[] args)throws IOException {
        //INITIALIZE THE SERVER
        UDPServerSocket server = new UDPServerSocket(8080);
        server.addUDPListener(new UDPServerSocket.UDPListener(){
            private UDPSocket clientA;

            @Override
            public void accept(UDPSocket socket){
                socket.setKeepAlive(true);

                //LOG PEER A SO THAT WHEN PEER B CONNECTS WE CAN TRADE IP AND PORT
                if(clientA == null){
                    System.out.println("CLIENT A CONNECT");
                    clientA = socket;

                }else{
                    System.out.println("CLIENT B CONNECT");
                    try{
                        writeIPAndPort(true, clientA, socket.getKey()); //GIVE PEER A - PEER B IP & PORT
                        writeIPAndPort(false, socket, clientA.getKey()); //GIVE PEER B - PEER A IP & PORT
                    }catch(IOException e){
                        e.printStackTrace();
                    }

                    clientA = null;

                    server.close();
                }
            }
        });
    }

    public static void writeIPAndPort(boolean aorb, UDPSocket socket, UDPKey key)throws IOException {
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());

        if(aorb){
            out.writeByte(0x00); //SEND A
        }else{
            out.writeByte(0x01); //SEND B
        }

        if(key.getAddress() instanceof Inet4Address){
            out.writeByte(0x04);
        }else if(key.getAddress() instanceof Inet6Address){
            out.writeByte(0x06);
        }

        out.write(key.getAddress().getAddress());
        out.writeInt(key.getPort());

        out.flush();
    }
}
