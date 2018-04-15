package com.shortn0tes.povdisplay;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Objects;

/**
 * Created on 11/10/2017.
 */
public class SerialPortWrapper {

	private final SerialPort port;

	public SerialPortWrapper(SerialPort port) {
		this.port = port;
	}

	public SerialPort getPort() {
		return port;
	}

	@Override
	public String toString() {
		return port.getDescriptivePortName();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		SerialPortWrapper that = (SerialPortWrapper) o;

		return port != null ? Objects.equals(port.getSystemPortName(), that.port.getSystemPortName())
			: that.port == null;
	}

	@Override
	public int hashCode() {
		return port != null ? port.hashCode() : 0;
	}
}
