package unet.uncentralized.betterudpsocket;

import java.net.InetAddress;

public class UDPKey {

    private InetAddress address;
    private int port, key;

    public UDPKey(int key, InetAddress address, int port){
        this.address = address;
        this.port = port;
        this.key = key;
    }

    public InetAddress getAddress(){
        return address;
    }

    public int getPort(){
        return port;
    }

    public int getKey(){
        return key;
    }

    public void setKey(int key){
        this.key = key;
    }

    public String hash(){
        return key+":"+address.getHostAddress()+":"+port;
    }

    public boolean equals(Object o){
        if(o instanceof UDPKey){
            return hash().equals(((UDPKey) o).hash());
        }
        return false;
    }
}
