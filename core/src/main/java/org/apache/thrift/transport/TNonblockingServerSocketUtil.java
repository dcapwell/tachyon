package org.apache.thrift.transport;

import com.google.common.base.Throwables;

import java.lang.reflect.Field;
import java.net.ServerSocket;

public final class TNonblockingServerSocketUtil {
  private TNonblockingServerSocketUtil() {}

  public static int getPort(TNonblockingServerSocket thriftSocket) throws TTransportException {
    return getSocket(thriftSocket).getLocalPort();
  }

  public static ServerSocket getSocket(final TNonblockingServerSocket thriftSocket) {
    try {
      Field field = TNonblockingServerSocket.class.getDeclaredField("serverSocket_");
      field.setAccessible(true);
      return (ServerSocket) field.get(thriftSocket);
    } catch (NoSuchFieldException e) {
      throw Throwables.propagate(e);
    } catch (IllegalAccessException e) {
      throw Throwables.propagate(e);
    }
  }
}
