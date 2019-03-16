import authorization.AuthService;
import authorization.AuthServiceImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java. Level 3. Lesson 3,4.
  * @version 03.03.2019
 */


/*
Lesson 3
1. Добавить в сетевой чат запись локальной истории в текстовый файл на клиенте.
2. После загрузки клиента показывать ему последние 100 строк чата.
*/

/*
Lesson 4
1. Создать три потока, каждый из которых выводит определенную букву (A, B и C) 5 раз (порядок – ABСABСABС).
 Используйте wait/notify/notifyAll.
2. На серверной стороне сетевого чата реализовать управление потоками через ExecutorService.

PS
Lesson 3
Сделал два варианта записи истории в файл. См. MessageWriterHistory и History
MessageWriterHistory записывает историю в папку: user history (предназначено для чтения истории пользователем через
блокнот c датой, переносом срок, пользователь видит отправлял он сообщение или получал явным образом)

History записывает историю в корневой каталог (запись ведется в формате для компьютера)

Lesson4
Задание №1 сделал несколько иначе, предложенного на занятии решения.
Задание №2 сделал аналогично предложенному на уроке
 */


public class ChatServer {

    //Объявляем паттерны

    private static final Pattern AUTH_PATTERN = Pattern.compile("^/auth (\\w+) (\\w+)$");


    private AuthService authService;

    //Создаем экземпляр авторизации
    {
        try {
            authService = new AuthServiceImpl();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Map<String, ClientHandler> clientHandlerMap = Collections.synchronizedMap(new HashMap<>());

    private ExecutorService execService; //Объявляем Executor Service

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.start(7777);
    }





    public void start(int port) {

        //Добавляем сервис асинхронного выполнения
        execService = Executors.newCachedThreadPool(); //не фиксированного размера так как клиентов
        //может быть несколько


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started!");
            while (true) {
                Socket socket = serverSocket.accept();
                DataInputStream inp = new DataInputStream(socket.getInputStream());
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                System.out.println("New client connected!");


                try {
                    String authMessage = inp.readUTF();
                    Matcher matcher = AUTH_PATTERN.matcher(authMessage);
                    if (matcher.matches()) {
                        String username = matcher.group(1);
                        String password = matcher.group(2);

                        if (authService.authUser(username, password)) {
                            clientHandlerMap.put(username, new ClientHandler(username, socket, this));
                            out.writeUTF("/auth successful");
                            out.flush();
                            System.out.printf("Authorization for user %s successful%n", username);

                            broadcastUserConnection();

                        } else {
                            System.out.printf("Authorization for user %s failed%n", username);

                            out.writeUTF("/auth fails");
                            out.flush();
                            socket.close();
                        }
                    } else {
                        System.out.printf("Incorrect authorization message %s%n", authMessage);
                        out.writeUTF("/auth fails");
                                      out.flush();
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            execService.shutdown(); //Закрываем
        }
    }

    public void sendMessage(String userTo, String userFrom, String msg) throws IOException {
        ClientHandler userToClientHandler = clientHandlerMap.get(userTo);
        if (userToClientHandler != null) {
            userToClientHandler.sendMessage(userFrom, msg);
        } else {
            System.out.printf("User %s not found. Message from %s is lost.%n", userTo, userFrom);
        }
    }

    public void sendUserConsistMessage(String userTo, String userFrom, String msg) throws IOException {
        ClientHandler userToClientHandler = clientHandlerMap.get(userTo);
        if (userToClientHandler != null) {
            userToClientHandler.sendUserConsistMessage(userFrom, msg);
        } else {
            System.out.printf("User %s not found. Message from %s is lost.%n", userTo, userFrom);
        }
    }



    public List<String> getUserList() {
        return new ArrayList<>(clientHandlerMap.keySet());
    }

    public void unsubscribeClient(ClientHandler clientHandler) throws IOException {

       //Удаляем из списка
        clientHandlerMap.remove(clientHandler.getUsername());
        broadcastUserConnection();

    }

    //Шлем информацию всем клиентам при подключении/отключении пользователя
    public void broadcastUserConnection() {

        System.out.println(getUserList());
        List<String> namesList;
        namesList = getUserList();

        //Преобразуем ArrayList в строку для пересылки
        StringBuilder sb = new StringBuilder();
        for (String s :  namesList)
        {
            sb.append(s);
            sb.append("//");
        }


        //Шлем всем информацию о текущих пользователях
        for (String name : namesList) {
            try {
                 sendUserConsistMessage(name,"", sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //Геттер
    public ExecutorService getExecutorService() {
        return execService;
    }


}
