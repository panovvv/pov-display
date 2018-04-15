package com.shortn0tes.povdisplay;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_WRITE_BLOCKING;

import com.fazecast.jSerialComm.SerialPort;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.PopOver;

public class Controller {

	private static final Logger LOGGER = Logger.getLogger(Controller.class.getName());

	// Non-UI constants
	private static final int BAUD_RATE = 115200;
	private static final int DATA_BITS = 8;
	private static final int STOP_BITS = SerialPort.ONE_STOP_BIT;
	private static final int PARITY = SerialPort.NO_PARITY;
	private static final int SERIAL_WRITE_TIMEOUT_MILLIS = 1000;

	private static final String EMPTY_STRING = "";
	private static final String SET_NUMBER_PREFIX = "sn";

	private static final String FAILED_TIMEOUT_MESSAGE = "Failed due to timeout!";

	@FXML
	private ComboBox<SerialPortWrapper> uartComboBox;
	@FXML
	private Button uartRefreshBtn;
	@FXML
	private Button uartConnectBtn;

	@FXML
	private NumericTextField numberBox;
	@FXML
	private Button setNumberBtn;

	private final PopOver popOver = new PopOver();

	// Non-UI variables
	private SerialPort serialPort;

	@FXML
	void uartConnectBtnClicked(ActionEvent event) {
		if (!isSerialPortOpen()) {
			serialPort = uartComboBox.getSelectionModel().getSelectedItem().getPort();
			serialPort.setComPortParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY);
			serialPort.setComPortTimeouts(TIMEOUT_WRITE_BLOCKING, 0, SERIAL_WRITE_TIMEOUT_MILLIS);
			if (serialPort.openPort()) {
				setSerialPortConnectedOnUI(true);
				numberBox.requestFocus();
			}
		} else {
			if (serialPort.closePort()) {
				setSerialPortConnectedOnUI(false);
				refreshSerialPortList();
			}
		}
	}

	private void setSerialPortConnectedOnUI(boolean isConnected) {
		uartConnectBtn.setText(isConnected ? "Disconnect" : "Connect");
		uartRefreshBtn.setDisable(isConnected);
		uartComboBox.setDisable(isConnected);
		setNumberBtn.setDisable(!isConnected);
		numberBox.setDisable(!isConnected);
		if (!isConnected) {
			numberBox.setText(EMPTY_STRING);
		}
	}

	@FXML
	void uartRefreshBtnClicked(ActionEvent event) {
		refreshSerialPortList();
	}

	@FXML
	void setNumberBtnClicked(ActionEvent event) {
		sendCommand();
	}

	@FXML
	void numberTextBoxKeyPressed(KeyEvent event) {
		if (Objects.equals(event.getCode(), KeyCode.ENTER)) {
			sendCommand();
		}
	}

	private void sendCommand() {
		if (isSerialPortOpen()) {
			long number = numberBox.getNumber();
			LOGGER.info(String.format("Trying to set number to %d", number));
			String numberStr = Long.toString(number);
			if (numberStr.length() < 3) {
				int oldLength = numberStr.length();
				for (int i = 0; i < 3 - oldLength; i++) {
					numberStr = '_' + numberStr;
				}
			} else if (numberStr.length() > 3) {
				numberStr = numberStr.substring(0, 3);
			}
			byte[] numberBytes = String.format("%s%s", SET_NUMBER_PREFIX, numberStr).getBytes();

			new Thread(() -> {
				Platform.runLater(() -> setNumberBtn.setDisable(true));
				if (serialPort.writeBytes(numberBytes, numberBytes.length) < 0) {
					Platform.runLater(() -> showPopover(FAILED_TIMEOUT_MESSAGE, setNumberBtn));
				} else {
					Platform.runLater(() -> showPopover(String.format("Successfully set number to %d", number),
						setNumberBtn));
				}
				Platform.runLater(() -> setNumberBtn.setDisable(false));
			}).start();
		}
	}

	void close() {
		popOver.setFadeOutDuration(Duration.ZERO);
		popOver.hide();
		if (isSerialPortOpen()) {
			serialPort.closePort();
		}
	}

	void refreshSerialPortList() {
		List<SerialPortWrapper> ports = Arrays.stream(SerialPort.getCommPorts())
			.map(SerialPortWrapper::new)
			.collect(Collectors.toList());
		SerialPortWrapper selectedItem = uartComboBox.getSelectionModel().getSelectedItem();
		uartComboBox.setItems(FXCollections.observableArrayList(ports));
		if (ports.contains(selectedItem)) {
			uartComboBox.getSelectionModel().select(selectedItem);
		} else {
			uartComboBox.getSelectionModel().selectFirst();
		}
		selectedItem = uartComboBox.getSelectionModel().getSelectedItem();
		uartConnectBtn.setDisable(selectedItem == null);
	}

	private boolean isSerialPortOpen() {
		return Objects.nonNull(serialPort) && serialPort.isOpen();
	}

	private void showPopover(String message, Node owner) {
		popOver.setContentNode(new VBox(new Label(EMPTY_STRING),
			new Label(message),
			new Label(EMPTY_STRING)));
		popOver.setFadeInDuration(Duration.valueOf("1s"));
		popOver.setFadeOutDuration(Duration.valueOf("1s"));
		popOver.show(owner);
		Timeline idlestage = new Timeline(new KeyFrame(Duration.seconds(2), event1 -> popOver.hide()));
		idlestage.setCycleCount(1);
		idlestage.play();
	}
}
