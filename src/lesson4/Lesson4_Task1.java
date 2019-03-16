package lesson4;

public class Lesson4_Task1 {


    public static void main(String[] args) {

        Lesson4_Task1 monitor = new Lesson4_Task1();

        ConsolePrint print1 = new ConsolePrint(monitor, 'A', 'B');
        ConsolePrint print2 = new ConsolePrint(monitor, 'B', 'C');
        ConsolePrint print3 = new ConsolePrint(monitor, 'C', 'A');


        try {
            print1.getThread().join();
            print2.getThread().join();
            print3.getThread().join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}


