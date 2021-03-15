package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

abstract class UDPSocket {

    private UDPServerSocket server;
    private UDPKey key;
    private UDPInputStream in;
    private UDPOutputStream out;
    private boolean safeMode;
    private int inOrder = 0, timeout = 5000;
    private DatagramPacket lastPacket;

    public UDPSocket(UDPServerSocket server, UDPKey key, boolean safeMode)throws IOException {
        this.server = server;
        this.key = key;
        this.safeMode = safeMode;

        in = new UDPInputStream(this);
        out = new UDPOutputStream(this, key.getKey());
    }

    public boolean isSafeMode(){
        return safeMode;
    }

    public InetAddress getAddress(){
        return key.getAddress();
    }

    public int getPort(){
        return key.getPort();
    }

    public DatagramSocket getServer(){
        return server.getServer();
    }

    public UDPInputStream getInputStream(){
        return in;
    }

    public UDPOutputStream getOutputStream(){
        return out;
    }

    public boolean isInputStreamShutdown(){
        return out.isClosed();
    }

    public boolean isOutputStreamShutdown(){
        return in.isClosed();
    }

    public void setTimeout(int timeout){
        this.timeout = timeout;
    }

    public int getTimeout(){
        return timeout;
    }

    public void receive(byte[] buf)throws IOException {
        receive(buf, 0, buf.length);
    }

    public void receive(byte[] buf, int off, int len)throws IOException {
        if(!in.isClosed()){
            if(safeMode){
                byte ack = buf[off];

                switch(ack){
                    case 0x00: //SUCCESSFUL ACKNOWLEDGMENT
                        out.setAckReady();
                        break;

                    case 0x01: //FAILURE ACKNOWLEDGMENT
                        send(lastPacket);
                        break;

                    case 0x02: //PACKET TO BE RECEIVED
                        int pos = (((buf[off+1] & 0xff) << 24) |
                                ((buf[off+2] & 0xff) << 16) |
                                ((buf[off+3] & 0xff) << 8) |
                                (buf[off+4] & 0xff));

                        if(inOrder == pos){
                            in.append(buf, off+5, len-5);
                            inOrder++;
                            sendSuccessAcknowledgment();

                        }else{
                            sendFailureAcknowledgment();
                        }

                        break;
                }

            }else{
                in.append(buf, off, len);
            }
        }
    }

    public void send(DatagramPacket packet)throws IOException {
        if(!out.isClosed()){
            lastPacket = packet;
            server.getServer().send(packet);
        }
    }

    private void sendSuccessAcknowledgment()throws IOException {
        if(!out.isClosed()){
            byte[] b = new byte[]{
                    (byte) (0xff & (key.getKey() >> 24)),
                    (byte) (0xff & (key.getKey() >> 16)),
                    (byte) (0xff & (key.getKey() >> 8)),
                    (byte) (0xff & key.getKey()),
                    0x00
            };
            server.getServer().send(new DatagramPacket(b, b.length, key.getAddress(), key.getPort()));
        }
    }

    private void sendFailureAcknowledgment()throws IOException {
        if(!out.isClosed()){
            byte[] b = new byte[]{
                    (byte) (0xff & (key.getKey() >> 24)),
                    (byte) (0xff & (key.getKey() >> 16)),
                    (byte) (0xff & (key.getKey() >> 8)),
                    (byte) (0xff & key.getKey()),
                    0x01
            };
            server.getServer().send(new DatagramPacket(b, b.length, key.getAddress(), key.getPort()));
        }
    }

    public void shutdownInputStream()throws IOException {
        if(!in.isClosed()){
            in.close();
        }
    }

    public void shutdownOutputStream()throws IOException {
        if(!in.isClosed()){
            out.close();
        }
    }

    abstract void close();
}
