package unet.uncentralized.betterudpsocket;

import java.io.InputStream;
import java.util.Date;

public class UDPInputStream extends InputStream {

    private byte[] buffer;
    private long timeout = 5000;

    @Override
    public synchronized int read(){
        if(buffer.length > 0){
            int i = buffer[0]&0xff;
            depend(0, 1);
            return i;
        }
        return 0;
    }

    @Override
    public synchronized int read(byte[] b){
        return read(b, 0, b.length);
    }

    @Override
    public synchronized int read(byte[] b, int off, int len){
        if(buffer == null || off > buffer.length){
            //WAIT
            long now = new Date().getTime();
            while(buffer == null || off > buffer.length){
                if(now+timeout <= new Date().getTime()){
                    return 0;
                }
                try{
                    Thread.sleep(10);
                }catch(InterruptedException e){
                }
            }
        }

        len = (len > buffer.length) ? buffer.length : len;
        System.arraycopy(buffer, off, b, 0, len);
        depend(off, len);

        return len;
    }

    @Override
    public synchronized int available(){
        return buffer.length;
    }

    public void setTimeout(long timeout){
        this.timeout = timeout;
    }

    public synchronized void append(byte[] b){
        append(b, 0, b.length);
    }

    public synchronized void append(byte[] b, int off, int len){
        if(buffer == null){
            buffer = new byte[len];
            System.arraycopy(b, off, buffer, 0, len);

        }else{
            byte[] result = new byte[len+buffer.length];
            System.arraycopy(buffer, 0, result, 0, buffer.length);
            System.arraycopy(b, off, result, buffer.length, len);
            buffer = result;
        }
    }

    private synchronized void depend(int off, int len){
        if(buffer.length-(len+off) > 0){
            byte[] result = new byte[buffer.length-(len+off)];
            System.arraycopy(buffer, (len+off), result, 0, result.length);
            buffer = result;
        }else{
            buffer = null;
        }
    }
}
