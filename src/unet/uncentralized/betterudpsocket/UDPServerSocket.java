package unet.uncentralized.betterudpsocket;

import unet.uncentralized.betterudpsocket.UPnP.UPnP;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UDPServerSocket {

    private DatagramSocket server;
    private ArrayList<UDPListener> listeners = new ArrayList<>();
    private HashMap<String, UDPSocket> sockets = new HashMap<>();
    private ConcurrentLinkedQueue<DatagramPacket> packetPool = new ConcurrentLinkedQueue<>();
    private Timer timer;
    private TimerTask task;
    private boolean safeMode;

    public UDPServerSocket()throws SocketException {
        server = new DatagramSocket();
        init();
    }

    public UDPServerSocket(int port)throws SocketException {
        server = new DatagramSocket(port);
        init();
    }

    private void init(){
        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    try{
                        DatagramPacket packet = new DatagramPacket(new byte[65535], 65535);
                        server.receive(packet);

                        if(packet != null){
                            packetPool.offer(packet);
                        }
                    }catch(IOException e){
                    }
                }
            }
        }).start();

        new Thread(new Runnable(){
            @Override
            public void run(){
                while(!server.isClosed()){
                    if(!packetPool.isEmpty()){
                        DatagramPacket packet = packetPool.poll();
                        try{
                            UUID uuid = new UUID(((long)(packet.getData()[packet.getOffset()] & 0xff) << 56) |
                                    ((long)(packet.getData()[packet.getOffset()+1] & 0xff) << 48) |
                                    ((long)(packet.getData()[packet.getOffset()+2] & 0xff) << 40) |
                                    ((long)(packet.getData()[packet.getOffset()+3] & 0xff) << 32) |
                                    ((long)(packet.getData()[packet.getOffset()+4] & 0xff) << 24) |
                                    ((long)(packet.getData()[packet.getOffset()+5] & 0xff) << 16) |
                                    ((long)(packet.getData()[packet.getOffset()+6] & 0xff) <<  8) |
                                    ((long)(packet.getData()[packet.getOffset()+7] & 0xff)),

                                    (((long)(packet.getData()[packet.getOffset()+8] & 0xff) << 56) |
                                    ((long)(packet.getData()[packet.getOffset()+9] & 0xff) << 48) |
                                    ((long)(packet.getData()[packet.getOffset()+10] & 0xff) << 40) |
                                    ((long)(packet.getData()[packet.getOffset()+11] & 0xff) << 32) |
                                    ((long)(packet.getData()[packet.getOffset()+12] & 0xff) << 24) |
                                    ((long)(packet.getData()[packet.getOffset()+13] & 0xff) << 16) |
                                    ((long)(packet.getData()[packet.getOffset()+14] & 0xff) <<  8) |
                                    ((long)(packet.getData()[packet.getOffset()+15] & 0xff))));

                            UDPKey key = new UDPKey(uuid, packet.getAddress(), packet.getPort());

                            if(sockets.containsKey(key.hash())){
                                sockets.get(key.hash()).receive(packet.getData(), packet.getOffset()+16, packet.getLength()-16);

                            }else{
                                UDPSocket socket = create(key);
                                socket.receive(packet.getData(), packet.getOffset()+16, packet.getLength()-16);

                                if(listeners.size() > 0){
                                    for(UDPListener listener : listeners){
                                        listener.accept(socket);
                                    }
                                }
                            }
                        }catch(IOException e){
                        }
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
        return create(new UDPKey(address, port));
    }

    private UDPSocket create(UDPKey key)throws IOException {
        UDPSocket socket = new UDPSocket(this, key, safeMode){
            private boolean closed = false;

            @Override
            public boolean isClosed(){
                if(server.isClosed()){
                    return true;
                }
                return closed;
            }

            @Override
            public void close(){
                if(sockets.containsKey(key.hash())){
                    closed = true;
                    sockets.remove(key.hash());

                    try{
                        sendClose(this);
                        shutdownInputStream();
                        shutdownOutputStream();
                    }catch(IOException e){
                    }
                }
            }

            @Override
            public void peerClosed(){
                if(sockets.containsKey(key.hash())){
                    closed = true;
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

    public boolean openPort(){
        if(UPnP.isUPnPAvailable()){
            if(UPnP.isMappedUDP(server.getLocalPort())){
                UPnP.openPortUDP(server.getLocalPort());
                return true;
            }
        }
        return false;
    }

    public boolean closePort(){
        if(UPnP.isUPnPAvailable()){
            if(UPnP.isMappedUDP(server.getLocalPort())){
                UPnP.closePortUDP(server.getLocalPort());
                return true;
            }
        }
        return false;
    }

    public InetAddress getExternalIP(){
        if(UPnP.isUPnPAvailable()){
            return UPnP.getExternalIP();
        }
        return null;
    }

    public int getPort(){
        return server.getLocalPort();
    }

    private void sendKeepAlive(UDPSocket socket)throws IOException {
        if(!socket.getOutputStream().isClosed()){
            byte[] b = new byte[]{
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 56)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 48)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 40)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 32)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 24)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 16)),
                    (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >>  8)),
                    (byte) (0xff & socket.getKey().getUUID().getMostSignificantBits()),

                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 56)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 48)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 40)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 32)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 24)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 16)),
                    (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >>  8)),
                    (byte) (0xff & socket.getKey().getUUID().getLeastSignificantBits()),

                    0x01
            };
            server.send(new DatagramPacket(b, b.length, socket.getAddress(), socket.getPort()));
        }
    }

    private void sendClose(UDPSocket socket)throws IOException {
        byte[] b = new byte[]{
                (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 56)),
                (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 48)),
                (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 40)),
                (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 32)),
                (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 24)),
                (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >> 16)),
                (byte) (0xff & (socket.getKey().getUUID().getMostSignificantBits() >>  8)),
                (byte) (0xff & socket.getKey().getUUID().getMostSignificantBits()),

                (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 56)),
                (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 48)),
                (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 40)),
                (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 32)),
                (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 24)),
                (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >> 16)),
                (byte) (0xff & (socket.getKey().getUUID().getLeastSignificantBits() >>  8)),
                (byte) (0xff & socket.getKey().getUUID().getLeastSignificantBits()),

                0x05
        };
        server.send(new DatagramPacket(b, b.length, socket.getAddress(), socket.getPort()));
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
