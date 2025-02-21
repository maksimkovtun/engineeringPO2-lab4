package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import jakarta.xml.bind.*;
import jakarta.xml.bind.annotation.*;

public class MultiClientServer {
    private static final int PORT = 8080;
    private static final List<Circle> storedCircles = new CopyOnWriteArrayList<>();
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(10);
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Сервер запущен, порт: " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }
        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                String xmlInput = in.readLine();
                System.out.println("Получен XML: " + xmlInput);
                if (xmlInput != null) {
                    Circle circle = deserializeFromXML(xmlInput);
                    if (circle != null && circle.getRadius() > 0) {
                        storedCircles.add(circle);
                        System.out.println("Принят объект: " + circle);
                    } else {
                        System.out.println("Ошибка при разборе XML: " + xmlInput);
                    }
                    String response = serializeToXML(storedCircles);
                    System.out.println("Отправляем XML: " + response);
                    out.println(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static Circle deserializeFromXML(String xml) {
        try {
            JAXBContext context = JAXBContext.newInstance(Circle.class);
            Circle circle = (Circle) context.createUnmarshaller().unmarshal(new StringReader(xml));
            System.out.println("Десериализованный объект: " + circle);
            return circle;
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
    private static String serializeToXML(List<Circle> circles) {
        try {
            JAXBContext context = JAXBContext.newInstance(CircleListWrapper.class);
            StringWriter writer = new StringWriter();
            context.createMarshaller().marshal(new CircleListWrapper(circles), writer);
            return writer.toString();
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }
}
@XmlRootElement
class CircleListWrapper {
    private List<Circle> circles;
    public CircleListWrapper() {}
    public CircleListWrapper(List<Circle> circles) {
        this.circles = circles;
    }
    @XmlElementWrapper(name = "circles")
    @XmlElement(name = "circle")
    public List<Circle> getCircles() {
        return circles;
    }
}
