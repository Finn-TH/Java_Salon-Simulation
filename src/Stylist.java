import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
public class Stylist extends Thread {
    private final Comb[] hairCombs;
    private final Scissors[] scissors;
    private final int id;
    private int clientsServed;

    private final Random random;
    private final BlockingQueue<Client> waitingQueue;
    private volatile boolean salonOpen;
    private volatile boolean salonClosed;
    private final Semaphore combSemaphore;
    private final Semaphore scissorsSemaphore;

    public Stylist(int id, Random random, BlockingQueue<Client> waitingQueue, boolean salonOpen,
                   Semaphore combSemaphore, Semaphore scissorsSemaphore, Comb[] hairCombs, Scissors[] scissors) {
        super("Stylist-" + (id + 1));
        this.id = id + 1;
        this.clientsServed = 0;
        this.random = random;
        this.waitingQueue = waitingQueue;
        this.salonOpen = salonOpen;
        this.combSemaphore = combSemaphore;
        this.scissorsSemaphore = scissorsSemaphore;
        this.hairCombs = hairCombs;
        this.scissors = scissors;
        this.salonClosed = !salonOpen;
    }

    // Method to set the salonClosed flag
    public void closeSalon() {
        this.salonClosed = true;
    }
    public int getClientsServed() {
        return clientsServed;
    }

    @Override
    public long getId() {
        return id;
    }

    private int acquireComb() throws InterruptedException {
        combSemaphore.acquire(); // Attempt to acquire a permit from the semaphore guarding the combs.
        // Iterate over the array of combs to find one that is not currently in use.
        for (int i = 0; i < hairCombs.length; i++) {
            // If the comb at index i is available, mark it as in use and return its ID.
            if (hairCombs[i].acquire()) {
                return i + 1; // Return the ID of the comb (1-indexed for user-friendliness).
            }
        }
        // If a comb is supposed to be available but none are found, throw an exception to indicate an error in logic.
        throw new IllegalStateException("No comb available despite semaphore permit.");
    }

    private void releaseComb(int combId) {
        // Convert the 1-indexed comb ID back to 0-indexed for array access and set the comb to not in use.
        hairCombs[combId - 1].release();
        // Release a permit back to the semaphore, indicating the comb is now available for others.
        combSemaphore.release();
    }

    private int acquireScissors() throws InterruptedException {
        // Attempt to acquire a permit from the semaphore guarding the scissors.
        scissorsSemaphore.acquire();
        // Iterate over the array of scissors to find one that is not currently in use.
        for (int i = 0; i < scissors.length; i++) {
            // If the scissors at index i are available, mark them as in use and return their ID.
            if (scissors[i].acquire()) {
                return i + 1; // Return the ID of the scissors (1-indexed for user-friendliness).
            }
        }
        // If scissors are supposed to be available but none are found, throw an exception to indicate an error in logic.
        throw new IllegalStateException("No scissors available despite semaphore permit.");
    }

    private void releaseScissors(int scissorsId) {
        // Convert the 1-indexed scissors ID back to 0-indexed for array access and set the scissors to not in use.
        scissors[scissorsId - 1].release();
        // Release a permit back to the semaphore, indicating the scissors are now available for others.
        scissorsSemaphore.release();
    }

    @Override
    public void run() {
        while (!salonClosed || !waitingQueue.isEmpty()) {
            try {
                Client client = waitingQueue.poll(1, TimeUnit.SECONDS);

                if (client == null && salonClosed) {
                    // Exit the loop if the salon is closed and no clients are waiting.
                    break;
                }

                if (client != null) {
                    int combId = acquireComb();
                    int scissorsId = acquireScissors();

                    System.out.printf("%s: Stylist %d started haircut for Client-%d with comb %d and scissors %d.%n",
                            Thread.currentThread().getName(), id, client.getId(), combId, scissorsId);

                    System.out.printf("%s: Haircut in Progress for Client-%d...%n", Thread.currentThread().getName(), client.getId());
                    Thread.sleep(random.nextInt(5) + 4 * 1000);

                    System.out.printf("%s: Stylist %d finished haircut for Client-%d and released comb %d and scissors %d.%n",
                            Thread.currentThread().getName(), id, client.getId(), combId, scissorsId);

                    releaseComb(combId);
                    releaseScissors(scissorsId);

                    clientsServed++;
                }
            } catch (InterruptedException e) {
                // Handle interruption if needed
            }
        }

        System.out.printf("%s: Stylist %d has no more clients, stopping and going to sleep.%n",
                Thread.currentThread().getName(), id);
    }
}
