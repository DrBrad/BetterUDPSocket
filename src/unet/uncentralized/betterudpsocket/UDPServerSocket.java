package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class UDPServerSocket {

    private int port;
    private DatagramSocket server;
    private ArrayList<UDPListener> listeners = new ArrayList<>();
    private HashMap<String, UDPSocket> sockets = new HashMap<>();
    private boolean safeMode;

    public UDPServerSocket(int port)throws SocketException {
        this.port = port;
        server = new DatagramSocket(port);

        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    try{
                        DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                        server.receive(packet);

                        int id = (((packet.getData()[packet.getOffset()] & 0xff) << 24) |
                                ((packet.getData()[packet.getOffset()+1] & 0xff) << 16) |
                                ((packet.getData()[packet.getOffset()+2] & 0xff) << 8) |
                                (packet.getData()[packet.getOffset()+3] & 0xff));

                        UDPKey key = new UDPKey(id, packet.getAddress(), packet.getPort());

                        if(sockets.containsKey(key.hash())){
                            sockets.get(key.hash()).receive(packet.getData(), packet.getOffset()+4, packet.getLength()-4);

                        }else{
                            UDPSocket socket = create(key);
                            sockets.get(key.hash()).receive(packet.getData(), packet.getOffset()+4, packet.getLength()-4);

                            if(listeners.size() > 0){
                                for(UDPListener listener : listeners){
                                    listener.accept(socket);
                                }
                            }
                        }
                    }catch(IOException e){
                        //e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    public void setSafeMode(boolean safeMode){
        this.safeMode = safeMode;
    }

    public UDPSocket create(InetAddress address, int port)throws IOException {
        UDPKey key = new UDPKey(0, address, port);

        for(int i = 0; i < 1000; i++){
            key.setKey(i);
            if(!sockets.containsKey(key.hash())){
                break;
            }
        }

        return create(key);
    }

    private UDPSocket create(UDPKey key)throws IOException {
        UDPSocket socket = new UDPSocket(this, key, safeMode){
            @Override
            public void close(){
                if(sockets.containsKey(key.hash())){
                    sockets.remove(key.hash());

                    try{
                        shutdownInputStream();
                        shutdownOutputStream();
                    }catch(IOException e){
                    }
                }
            }
        };

        sockets.put(key.hash(), socket);
        return socket;
    }

    public DatagramSocket getServer(){
        return server;
    }

    public void close(){
        server.close();
    }

    public UDPListener addUDPListener(UDPListener listener){
        listeners.add(listener);
        return listener;
    }

    public interface UDPListener {

        void accept(UDPSocket socket);
    }
}
