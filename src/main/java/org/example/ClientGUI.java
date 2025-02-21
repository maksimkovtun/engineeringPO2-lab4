package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import jakarta.xml.bind.*;

public class ClientGUI {
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 8080;
    private final JTextField xField, yField, radiusField;
    private final JTextArea logArea;
    private final JLabel statusLabel;
    public ClientGUI() {
        JFrame frame = new JFrame("TCP XML Client");
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("X:"));
        xField = new JTextField();
        inputPanel.add(xField);
        inputPanel.add(new JLabel("Y:"));
        yField = new JTextField();
        inputPanel.add(yField);
        inputPanel.add(new JLabel("Radius:"));
        radiusField = new JTextField();
        inputPanel.add(radiusField);
        JButton sendButton = new JButton("Отправить");
        inputPanel.add(sendButton);
        statusLabel = new JLabel("Статус: Ожидание");
        inputPanel.add(statusLabel);
        logArea = new JTextArea();
        logArea.setEditable(false);
        frame.add(inputPanel, BorderLayout.NORTH);
        frame.add(new JScrollPane(logArea), BorderLayout.CENTER);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendCircle();
            }
        });
        frame.setVisible(true);
    }
    private void sendCircle() {
        try {
            int x = Integer.parseInt(xField.getText());
            int y = Integer.parseInt(yField.getText());
            int radius = Integer.parseInt(radiusField.getText());
            Circle circle = new Circle(x, y, radius);
            String xmlData = serializeToXML(circle);
            logArea.append("Отправлено: " + xmlData + "\n");
            statusLabel.setText("Статус: Отправка...");
            try (Socket socket = new Socket(SERVER_IP, SERVER_PORT);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out.println(xmlData);
                String response = in.readLine();
                logArea.append("Ответ сервера: " + response + "\n");
                statusLabel.setText("Статус: Успешно отправлено");
            }
        } catch (Exception ex) {
            logArea.append("Ошибка: " + ex.getMessage() + "\n");
            statusLabel.setText("Статус: Ошибка");
        }
    }
    private String serializeToXML(Circle circle) {
        try {
            JAXBContext context = JAXBContext.newInstance(Circle.class);
            StringWriter writer = new StringWriter();
            context.createMarshaller().marshal(circle, writer);
            return writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ClientGUI::new);
    }
}
