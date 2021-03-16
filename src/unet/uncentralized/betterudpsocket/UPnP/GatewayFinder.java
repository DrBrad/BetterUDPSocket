package unet.uncentralized.betterudpsocket.UPnP;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;

public class GatewayFinder {

    private LinkedList<GatewayListener> listeners = new LinkedList<>();
    private Gateway gateway;

    private String[] SEARCH_MESSAGES = {
            "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nST: urn:schemas-upnp-org:device:InternetGatewayDevice:1\r\nMAN: \"ssdp:discover\"\r\nMX: 2\r\n\r\n",
            "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nST: urn:schemas-upnp-org:service:WANIPConnection:1\r\nMAN: \"ssdp:discover\"\r\nMX: 2\r\n\r\n",
            "M-SEARCH * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nST: urn:schemas-upnp-org:service:WANPPPConnection:1\r\nMAN: \"ssdp:discover\"\r\nMX: 2\r\n\r\n"
    };

    public GatewayFinder(){
        for(Inet4Address ip : getLocalIPs()){
            for(String req : SEARCH_MESSAGES){
                GatewayListener l = new GatewayListener(ip, req);
                l.start();
                listeners.add(l);
            }
        }
    }

    public boolean isSearching(){
        return (listeners.size() > 0) ? true : false;
    }

    public Gateway getGateway(){
        return gateway;
    }

    private Inet4Address[] getLocalIPs(){
        ArrayList<Inet4Address> ret = new ArrayList<>();
        try{
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while(ifaces.hasMoreElements()){
                try{
                    NetworkInterface iface = ifaces.nextElement();
                    if(!iface.isUp() || iface.isLoopback() || iface.isVirtual() || iface.isPointToPoint()){
                        continue;
                    }
                    Enumeration<InetAddress> addrs = iface.getInetAddresses();
                    if(addrs == null){
                        continue;
                    }
                    while(addrs.hasMoreElements()){
                        InetAddress addr = addrs.nextElement();
                        if(addr instanceof Inet4Address){
                            ret.add((Inet4Address) addr);
                        }
                    }
                }catch(Throwable t){
                }
            }
        }catch(Throwable t){
        }
        return ret.toArray(new Inet4Address[]{});
    }

    private class GatewayListener extends Thread {

        private Inet4Address ip;
        private String req;

        public GatewayListener(Inet4Address ip, String req){
            //setName("WaifUPnP - Gateway Listener");
            this.ip = ip;
            this.req = req;
        }

        @Override
        public void run(){
            try{
                byte[] req = this.req.getBytes();
                DatagramSocket s = new DatagramSocket(new InetSocketAddress(ip, 0));
                s.send(new DatagramPacket(req, req.length, new InetSocketAddress("239.255.255.250", 1900)));
                s.setSoTimeout(3000);
                while(true){
                    try{
                        DatagramPacket recv = new DatagramPacket(new byte[1536], 1536);
                        s.receive(recv);
                        gateway = new Gateway(recv.getData(), ip);
                    }catch(SocketTimeoutException e){
                        break;
                    }catch(Exception e){
                    }
                }
            }catch(Exception e){
            }
            listeners.remove(this);
        }
    }
}
