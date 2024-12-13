import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Класс - сервер, принимает запросы от клиентов и отдает данные
 */
public class ThreadServer {
    public static void main(String[] args) {
        // Определяем номер порта, который будет "слушать" сервер
        int port = 1777;

        try (ServerSocket servSocket = new ServerSocket(port)) {
            // Открыть серверный сокет (ServerSocket)
            while (true) {
                System.out.println("Ожидане соединения. " + port);

                // Получив соединение начинаем работать с сокетом
                Socket fromClientSocket = servSocket.accept();

                // Стартуем новый поток для обработки запроса клиента
                new SocketThread(fromClientSocket).start();
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }
}


// Этот отдельный класс для обработки запроса клиента,
// который запускается в отдельном потоке
class SocketThread extends Thread {
    private final Socket fromClientSocket;

    public SocketThread(Socket fromClientSocket) {
        this.fromClientSocket = fromClientSocket;
    }

    @Override
    public void run() {
        // Автоматически будут закрыты все ресурсы
        try (Socket localSocket = fromClientSocket;
             PrintWriter pw = new PrintWriter(localSocket.getOutputStream(), true);
             BufferedReader br = new BufferedReader(new InputStreamReader(localSocket.getInputStream()))) {

            // Читаем сообщения от клиента. Пока он не скажет "bye"
            String str;
            while ((str = br.readLine()) != null) {
                // Печатаем сообщение
                System.out.println("Состояние сервера. Было получено сообщение:" + str);
                // Сравниваем с "bye" и если это так - выходим из цикла и закрываем соединение
                if (str.equals("bye")) {
                    // Тоже говорим клиенту "bye" и выходим из цикла
                    pw.println("bye");
                    break;
                } else {
                    // Посылаем клиенту ответ
                    str = "Ответ сервера. Было получено сообщение: " + str;
                    pw.println(str);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}


class MyClient {

    public static void main(String args[]) throws Exception {
        // Определяем номер порта, на котором нас ожидает сервер для ответа
        int portNumber = 1777;
        // Подготавливаем строку для запроса - просто строка
        String str = "Тестовая строка для передачи";

        // Пишем, что стартовали клиент
        System.out.println("Client is started");

        // Открыть сокет (Socket) для обращения к локальному компьютеру
        // Сервер мы будем запускать на этом же компьютере
        // Это специальный класс для сетевого взаимодействия c клиентской стороны
        Socket socket = new Socket("127.0.0.1", portNumber);

        // Создать поток для чтения символов из сокета
        // Для этого надо открыть поток сокета - socket.getInputStream()
        // Потом преобразовать его в поток символов - new InputStreamReader
        // И уже потом сделать его читателем строк - BufferedReader
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Создать поток для записи символов в сокет
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);

        // Отправляем тестовую строку в сокет
        printWriter.println(str);

        // Входим в цикл чтения, что нам ответил сервер
        while ((str = bufferedReader.readLine()) != null) {
            // Если пришел ответ “bye”, то заканчиваем цикл
            if (str.equals("bye")) {
                break;
            }
            // Печатаем ответ от сервера на консоль для проверки
            System.out.println(str);
            // Посылаем ему "bye" для окончания "разговора"
            printWriter.println("bye");
        }

        //закрываем
        bufferedReader.close();
        printWriter.close();
        socket.close();

        System.out.println("Client is finished");
    }
}