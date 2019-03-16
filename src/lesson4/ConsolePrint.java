package lesson4;

public class ConsolePrint implements Runnable {

    private final Object monitor; //контролирует доступ к объекту
    private Thread thread;
    private static volatile char letter = 'A';
    private char letter1;
    private char letter2;


    //Конструктор
    public ConsolePrint (Object monitor, char letter1, char letter2) {
        this.monitor = monitor;
        this.letter1 = letter1;
        this.letter2 = letter2;
        thread = new Thread(this);
        thread.start();
    }


    public Thread getThread() {
        return thread;
    }


    @Override
    public void run() {
        synchronized (monitor) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (letter != letter1) {
                        monitor.wait();  //Метод wait() вынуждает вызывающий поток исполнения уступить монитор
                        // и перейти в состояние ожидания до тех пор, пока какой-нибудь другой поток
                        // исполнения не войдет в тот же монитор и не вызовет метод notify().
                    }

                    System.out.print(letter1);
                    letter = letter2;
                    monitor.notifyAll();
                    //возобновляет исполнение всех потоков, из которых был вызван метод wait() для того
                    // же самого объекта. Одному из этих потоков предоставляется доступ.
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }
}

