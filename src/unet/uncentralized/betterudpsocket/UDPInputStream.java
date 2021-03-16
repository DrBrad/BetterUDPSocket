package unet.uncentralized.betterudpsocket;

import java.io.IOException;
import java.io.InputStream;

public class UDPInputStream extends InputStream {

    private UDPSocket socket;
    private ByteBuffer buffer = new ByteBuffer(65535);
    private boolean closed;

    public UDPInputStream(UDPSocket socket){
        this.socket = socket;
    }

    @Override
    public int read()throws IOException {
        byte[] buf = new byte[1];
        int len = read(buf, 0, 1);
        if(len > 0){
            return buf[0]&0xff;
        }
        return -1;
    }

    @Override
    public int read(byte[] buf)throws IOException {
        return read(buf, 0, buf.length);
    }

    @Override
    public int read(byte[] buf, int off, int len)throws IOException {
        if(!closed && !socket.isClosed()){
            if(buffer.getLength() < 1){
                long now = System.currentTimeMillis();
                while(buffer.getLength() < 1){
                    if(now+socket.getTimeout() <= System.currentTimeMillis()){
                        return 0;
                    }
                }

                if(closed || socket.isClosed()){
                    throw new IOException("InputStream is closed.");
                }
            }

            buffer.get(buf, off, len);

            return len;
        }else{
            throw new IOException("InputStream is closed.");
        }
    }

    @Override
    public int available(){
        return buffer.getLength();
    }

    public void append(byte[] buf){
        append(buf);
    }

    public void append(byte[] buf, int off, int len){
        buffer.put(buf, off, len);
    }

    public boolean isClosed(){
        return closed;
    }

    public void close(){
        closed = true;
    }
}
