import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Random;

public class Salon {
    public static final int MAX_WAITING_CLIENTS = 10; //  Max Clients on Waiting Que
    private static final int NUM_STYLISTS = 3; //  Number of Stylists Available
    private static final int NUM_COMBS = 2; //  Number of Combs Available
    private static final int NUM_SCISSORS = 2; // Number of Scissors Available
    public static final int OPERATION_TIME_SECONDS = 10; // Time limit for salon operation
    private List<Client> clients = new ArrayList<>();
    private int totalClientsServed = 0; // Counter for total clients served
    private int totalClientsLeft = 0; // Counter for total clients left

    private Stylist[] stylists;
    private final Comb[] hairCombs = {new Comb(), new Comb()};
    private final Scissors[] scissors = {new Scissors(), new Scissors()};
    private final Semaphore combSemaphore = new Semaphore(NUM_COMBS, true);
    private final Semaphore scissorsSemaphore = new Semaphore(NUM_SCISSORS, true);
    private final BlockingQueue<Client> waitingQueue = new LinkedBlockingQueue<>(MAX_WAITING_CLIENTS);
    private final Random random = new Random();
    public volatile boolean salonOpen = true;
    private long salonStartTime; // Record the start time

    public Salon() {
        // Initialize any necessary setup or configuration
    }

    // Method to increment the totalClientsServed counter
    public synchronized void incrementTotalClientsServed() {
        totalClientsServed++;
    }

    // Method to increment the totalClientsLeft counter
    public synchronized void incrementTotalClientsLeft() {
        totalClientsLeft++;
    }

    public void startOperation() {
        stylists = new Stylist[NUM_STYLISTS];

        // Start stylist threads
        for (int i = 0; i < NUM_STYLISTS; i++) {
            stylists[i] = new Stylist(i, random, waitingQueue, salonOpen, combSemaphore, scissorsSemaphore, hairCombs, scissors);
            stylists[i].start();
        }

        salonStartTime = System.currentTimeMillis(); // Record the start time

        // Loop to simulate the arrival of clients until the time limit is reached
        while (salonOpen && (System.currentTimeMillis() - salonStartTime) <= (OPERATION_TIME_SECONDS * 1000)) {
            Client client = new Client(clients.size(), waitingQueue, salonOpen, this, salonStartTime);
            clients.add(client);
            client.start();

            try {
                // Introduce a random delay before the next client arrives to simulate real-world arrival times.
                // This delay is 0, 1, or 2 seconds, determined randomly.
                Thread.sleep(random.nextInt(3) * 1000);
            } catch (InterruptedException e) {
                // Handle interruption if needed
            }
        }

        // Once time limit is reached, SalonOpen = False
        // This indicates that no new clients should enter the salon, and the salon will close once all clients have been served.
        System.out.println("It's Closing Time! Wrap Up!");
        salonOpen = false;

        // Notify stylists that the salon is closed
        for (Stylist stylist : stylists) {
            stylist.closeSalon();
        }

        // Wait for all stylists to finish
        for (Stylist stylist : stylists) {
            try {
                stylist.join();
            } catch (InterruptedException e) {
                // Handle interruption during join
            }
        }

        // Display a closing message

        // Calculate and display the total number of clients served
        displayTotalClientsServed();
        displayTotalClientsLeft(); // Display the total number of clients left
        System.out.println("Salon Is Now Closed!");
    }

    private void displayTotalClientsServed() {
        int totalServed = 0;

        // Iterate over the stylists and sum up the clients served
        for (Stylist stylist : stylists) {
            int servedByStylist = stylist.getClientsServed();
            totalServed += servedByStylist;
            System.out.printf("Stylist %d served %d clients.%n", stylist.getId(), servedByStylist);
        }

        // Display the total number of clients served by the salon
        System.out.printf("Total clients served by the salon: %d%n", totalServed);
    }

    private void displayTotalClientsLeft() {
        // Display the total number of clients left
        System.out.printf("Total clients left: %d%n", totalClientsLeft);
    }
}
