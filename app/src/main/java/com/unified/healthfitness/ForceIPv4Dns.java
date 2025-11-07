package com.unified.healthfitness;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.Dns;

/**
 * A custom Dns implementation for OkHttp that forces resolution to IPv4 addresses.
 * This can help bypass network issues related to IPv6.
 */
public class ForceIPv4Dns implements Dns {
    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        List<InetAddress> allAddresses = Dns.SYSTEM.lookup(hostname);
        List<InetAddress> ipv4Addresses = new ArrayList<>();
        for (InetAddress address : allAddresses) {
            if (address instanceof Inet4Address) {
                ipv4Addresses.add(address);
            }
        }
        if (ipv4Addresses.isEmpty()) {
            throw new UnknownHostException("No IPv4 address found for " + hostname);
        }
        return ipv4Addresses;
    }
}
