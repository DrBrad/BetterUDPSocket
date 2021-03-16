package unet.uncentralized.betterudpsocket.UPnP;

import java.net.InetAddress;

public class UPnP {

    private static Gateway gateway;

    private static void waitInit(){
        GatewayFinder finder = new GatewayFinder();

        while(finder.isSearching()){
            try{
                Thread.sleep(50);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        gateway = finder.getGateway();
    }

    public static boolean isUPnPAvailable(){
        waitInit();
        return gateway != null;
    }

    public static boolean openPortTCP(int port){
        if(!isUPnPAvailable()){
            return false;
        }
        return gateway.openPort(port, false);
    }

    public static boolean openPortUDP(int port){
        if(!isUPnPAvailable()){
            return false;
        }
        return gateway.openPort(port, true);
    }

    public static boolean closePortTCP(int port){
        if(!isUPnPAvailable()){
            return false;
        }
        return gateway.closePort(port, false);
    }

    public static boolean closePortUDP(int port){
        if(!isUPnPAvailable()) return false;
        return gateway.closePort(port, true);
    }

    public static boolean isMappedTCP(int port){
        if(!isUPnPAvailable()){
            return false;
        }
        return gateway.isMapped(port, false);
    }

    public static boolean isMappedUDP(int port){
        if(!isUPnPAvailable()){
            return false;
        }
        return gateway.isMapped(port, false);
    }

    public static InetAddress getExternalIP(){
        if(!isUPnPAvailable()){
            return null;
        }
        return gateway.getExternalIP();
    }

    public static String getLocalIP(){
        if(!isUPnPAvailable()){
            return null;
        }
        return gateway.getLocalIP();
    }
}
