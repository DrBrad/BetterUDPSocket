package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;

public class UDPOutputStream extends OutputStream {

    private UDPSocket socket;
    private int key, order = 0;
    private ByteBuffer buffer = new ByteBuffer(65535);
    private boolean closed, ackReady = true;

    public UDPOutputStream(UDPSocket socket, int key)throws IOException {
        this.socket = socket;
        this.key = key;
        writeKey();
    }

    @Override
    public synchronized void write(int b)throws IOException {
        byte[] buf = { (byte)b };
        write(buf, 0, buf.length);
    }

    @Override
    public synchronized void write(byte[] buf)throws IOException {
        write(buf, 0, buf.length);
    }

    @Override
    public synchronized void write(byte[] buf, int off, int len)throws IOException {
        if(!closed){
            if(buffer.getLength()+len >= buffer.getCapacity()){
                while(len > buffer.getRemaining()){
                    buffer.put(buf, off, buffer.getRemaining());
                    flush();
                    off += buffer.getRemaining();
                    len -= buffer.getRemaining();
                }

            }else{
                buffer.put(buf, off, len);
            }

        }else{
            throw new IOException("OutputStream is closed.");
        }
    }

    private synchronized void writeKey()throws IOException {
        if(!closed){
            buffer.put(new byte[]{
                    (byte) (0xff & (key >> 24)),
                    (byte) (0xff & (key >> 16)),
                    (byte) (0xff & (key >> 8)),
                    (byte) (0xff & key) });

            if(socket.isSafeMode()){
                buffer.put(new byte[]{
                        0x02,
                        (byte) (0xff & (order >> 24)),
                        (byte) (0xff & (order >> 16)),
                        (byte) (0xff & (order >> 8)),
                        (byte) (0xff & order)
                });
                order++;
            }
        }else{
            throw new IOException("OutputStream is closed.");
        }
    }

    public synchronized boolean isClosed(){
        return closed;
    }

    public void setAckReady(){
        ackReady = true;
    }

    @Override
    public void flush()throws IOException {
        if(buffer.getLength() > 4){
            if(socket.isSafeMode()){
                long now = System.currentTimeMillis();
                while(!ackReady){
                    if(now+socket.getTimeout() <= System.currentTimeMillis()){
                        closed = true;
                        return;
                    }
                }
                ackReady = false;
            }

            byte[] b = new byte[buffer.getLength()];
            buffer.get(b);

            socket.send(new DatagramPacket(b, b.length, socket.getAddress(), socket.getPort()));
            writeKey();
        }
    }

    public synchronized void close(){
        closed = true;
    }
}
