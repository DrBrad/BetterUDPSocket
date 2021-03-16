package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class UDPServerSocket {

    private DatagramSocket server;
    private ArrayList<UDPListener> listeners = new ArrayList<>();
    private HashMap<String, UDPSocket> sockets = new HashMap<>();
    private Timer timer;
    private TimerTask task;
    private boolean safeMode;

    public UDPServerSocket(int port)throws SocketException {
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
                            socket.receive(packet.getData(), packet.getOffset()+4, packet.getLength()-4);

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

        timer = new Timer();

        task = new TimerTask(){
            @Override
            public void run(){
                if(sockets.size() > 0){
                    for(UDPSocket socket : sockets.values()){
                        if(socket.isKeepAlive() && !socket.getOutputStream().isClosed()){
                            try{
                                sendKeepAlive(socket);
                            }catch(IOException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        };

        timer.schedule(task, 0, 25000);
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

    private void sendKeepAlive(UDPSocket socket)throws IOException {
        if(!socket.getOutputStream().isClosed()){
            byte[] b = new byte[]{
                    (byte) (0xff & (socket.getKey().getKey() >> 24)),
                    (byte) (0xff & (socket.getKey().getKey() >> 16)),
                    (byte) (0xff & (socket.getKey().getKey() >> 8)),
                    (byte) (0xff & socket.getKey().getKey()),
                    0x01
            };
            server.send(new DatagramPacket(b, b.length, socket.getAddress(), socket.getPort()));
        }
    }

    public DatagramSocket getServer(){
        return server;
    }

    public void close(){
        server.close();
        task.cancel();
        timer.cancel();
        timer.purge();
    }

    public UDPListener addUDPListener(UDPListener listener){
        listeners.add(listener);
        return listener;
    }

    public interface UDPListener {

        void accept(UDPSocket socket);
    }
}
