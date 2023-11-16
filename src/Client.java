import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class Client extends Thread {
    private final int id;
    private final BlockingQueue<Client> waitingQueue;
    private volatile boolean salonOpen;
    private final Salon salon; // Reference to the Salon instance
    private final long salonStartTime; // Record the start time of the salon

    private static AtomicInteger seatedCustomers = new AtomicInteger(0);

    public Client(int id, BlockingQueue<Client> waitingQueue, boolean salonOpen, Salon salon, long salonStartTime) {
        super("Client-" + (id + 1));
        this.id = id + 1;
        this.waitingQueue = waitingQueue;
        this.salonOpen = salonOpen;
        this.salon = salon;
        this.salonStartTime = salonStartTime; // Initialize the salon start time
    }

    @Override
    public long getId() {
        return id;
    }

    // Getter for the number of seated customers
    public static int getSeatedCustomers() {
        return seatedCustomers.get();
    }

    // Method to check if the customer should stand
    private boolean shouldStand() {
        return id > 5 || seatedCustomers.get() >= 5;
    }

    @Override
    public void run() {
        try {
            if (salon.salonOpen && (System.currentTimeMillis() - salonStartTime) <= (Salon.OPERATION_TIME_SECONDS * 1000)) {
                if (waitingQueue.size() < Salon.MAX_WAITING_CLIENTS) {
                    boolean addedToQueue = waitingQueue.offer(this);

                    if (addedToQueue) {
                        if (!shouldStand()) {
                            seatedCustomers.incrementAndGet();
                            System.out.printf("%s: Client %d is sitting while waiting.%n", Thread.currentThread().getName(), id);
                        } else {
                            System.out.printf("%s: Client %d is standing while waiting.%n", Thread.currentThread().getName(), id);
                        }

                        // Increment the totalClientsServed counter
                        salon.incrementTotalClientsServed();

                        if (System.currentTimeMillis() - salonStartTime >= (Salon.OPERATION_TIME_SECONDS * 1000)) {
                            salon.salonOpen = false;
                        }
                    } else {
                        System.out.printf("%s: Salon is too full. Client %d is leaving.%n", Thread.currentThread().getName(), id);
                        salon.incrementTotalClientsLeft(); // Increment the totalClientsLeft counter
                    }
                } else {
                    System.out.printf("%s: Salon is too full. Client %d is leaving.%n", Thread.currentThread().getName(), id);
                    salon.incrementTotalClientsLeft(); // Increment the totalClientsLeft counter
                }
            } else {
                System.out.printf("%s: Salon is closed. Client %d is leaving.%n", Thread.currentThread().getName(), id);
                salon.incrementTotalClientsLeft(); // Increment the totalClientsLeft counter
            }
        } catch (Exception e) {
            // Handle exceptions during the client's wait if needed
        }
    }
}