package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class UDPServerSocket {

    private DatagramSocket server;
    private ArrayList<UDPListener> listeners = new ArrayList<>();
    private HashMap<String, UDPSocket> sessions = new HashMap<>();

    public UDPServerSocket(int port)throws SocketException {
        server = new DatagramSocket(port);

        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    try{
                        DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                        server.receive(packet);

                        String key = (((packet.getData()[packet.getOffset()] & 0xff) << 24) |
                                ((packet.getData()[packet.getOffset()+1] & 0xff) << 16) |
                                ((packet.getData()[packet.getOffset()+2] & 0xff) << 8) |
                                (packet.getData()[packet.getOffset()+3] & 0xff))+":"+packet.getAddress().getHostAddress()+":"+packet.getPort();

                        if(sessions.containsKey(key)){
                            sessions.get(key).getInputStream().append(packet.getData(), packet.getOffset()+4, packet.getLength()-4);

                        }else{
                            for(UDPListener listener : listeners){
                                UDPSocket socket = new UDPSocket(UDPServerSocket.this, packet);
                                sessions.put(key, socket);
                                listener.accept(socket);
                            }
                        }
                    }catch(Exception e){
                        //e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public UDPSocket create(InetAddress address, int port)throws IOException {
        int key;
        for(key = 0; key < 1000; key++){
            if(!sessions.containsKey(key+":"+address.getHostAddress()+":"+port)){
                break;
            }
        }

        UDPSocket socket = new UDPSocket(UDPServerSocket.this, key, address, port);
        sessions.put(key+":"+address.getHostAddress()+":"+port, socket);
        return socket;
    }

    public DatagramSocket getServer(){
        return server;
    }

    public void close(){
        server.close();
    }

    public void close(String key){
        if(sessions.containsKey(key)){
            sessions.remove(key);
        }
    }

    public int sessionSize(){
        return sessions.size();
    }

    public UDPListener addUDPListener(UDPListener listener){
        listeners.add(listener);
        return listener;
    }

    public interface UDPListener {

        void accept(UDPSocket socket);
    }
}
