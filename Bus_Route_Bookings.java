import java.util.*;
import java.io.*;

public class Bus_Route_Bookings{

    static Scanner sc = new Scanner(System.in);
    static Map<String, Bus> buses = new LinkedHashMap<>();
    static Map<String, Ticket> tickets = new HashMap<>();
    static int count = 100;

    static class Stop {
        String name;
        Stop next;
        Stop(String n) { name = n; }
    }

    static class Route {
        Stop head;

        void add(String name) {
            Stop n = new Stop(name);
            if (head == null) {
                head = n;
                n.next = head;
                return;
            }
            Stop t = head;
            while (t.next != head) t = t.next;
            t.next = n;
            n.next = head;
        }

        void display() {
            if (head == null) return;
            Stop t = head;
            do {
                System.out.print(t.name + " -> ");
                t = t.next;
            } while (t != head);
            System.out.println("(Back to " + head.name + ")");
        }

        String nextStop(String name) {
            if (head == null) return "Route not found.";
            Stop t = head;
            do {
                if (t.name.equalsIgnoreCase(name))
                    return "Next stop: " + t.next.name;
                t = t.next;
            } while (t != head);
            return "Stop not found.";
        }
    }

    static class Passenger {
        String name;
        Passenger(String n) { name = n; }
    }

    static class Ticket {
        String id, name, bus, seat;

        Ticket(String i, String n, String b, String s) {
            id = i;
            name = n;
            bus = b;
            seat = s;
        }
    }

    static class Bus {
        String id;
        Route route = new Route();
        String[][] seats = new String[2][3];
        Queue<Passenger> waiting = new LinkedList<>();

        Bus(String id, String[] stops) {
            this.id = id;
            for (String s : stops) route.add(s);
        }

        String freeSeat() {
            for (int i = 0; i < 2; i++)
                for (int j = 0; j < 3; j++)
                    if (seats[i][j] == null)
                        return (i == 0 ? "A" : "B") + (j + 1);
            return null;
        }

        void occupy(String seat, String name) {
            int r = seat.charAt(0) == 'A' ? 0 : 1;
            int c = seat.charAt(1) - '1';
            seats[r][c] = name;
        }

        void release(String seat) {
            int r = seat.charAt(0) == 'A' ? 0 : 1;
            int c = seat.charAt(1) - '1';
            seats[r][c] = null;
        }

        void showSeats() {
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 3; j++)
                    System.out.print((seats[i][j] == null ? "Free" : "Booked") + "\t");
                System.out.println();
            }
        }
    }

    static void addBuses() {
        buses.put("B101", new Bus("B101",
                new String[]{"Main Gate", "Library", "City Mall", "Railway Station"}));

        buses.put("B102", new Bus("B102",
                new String[]{"Campus", "Bus Stand", "Hospital", "Airport"}));
    }

    static void book(Bus b, String name) {
        String seat = b.freeSeat();

        if (seat == null) {
            b.waiting.add(new Passenger(name));
            System.out.println("Bus full. " + name + " added to waiting queue.");
            return;
        }

        String id = "BT" + (++count);
        b.occupy(seat, name);
        tickets.put(id, new Ticket(id, name, b.id, seat));

        System.out.println("Ticket booked: " + id + " | " +
                name + " | Bus " + b.id + " | Seat " + seat);
    }

    static void search(String id) {
        Ticket t = tickets.get(id.toUpperCase());

        if (t == null)
            System.out.println("Ticket " + id + " not found.");
        else
            System.out.println("Ticket found: " + t.id + " | " +
                    t.name + " | " + t.bus + " | " + t.seat);
    }

    static void cancel(String id) {
        Ticket t = tickets.remove(id.toUpperCase());

        if (t == null) {
            System.out.println("Ticket not found.");
            return;
        }

        Bus b = buses.get(t.bus);
        b.release(t.seat);
        System.out.println("Ticket cancelled.");

        if (!b.waiting.isEmpty()) {
            Passenger p = b.waiting.poll();
            String newId = "BT" + (++count);
            b.occupy(t.seat, p.name);
            tickets.put(newId,
                    new Ticket(newId, p.name, b.id, t.seat));

            System.out.println("Seat assigned to " + p.name +
                    " | New Ticket: " + newId);
        }
    }

    static Bus chooseBus() {
        System.out.print("Enter Bus ID (B101/B102): ");
        Bus b = buses.get(sc.nextLine().trim().toUpperCase());

        if (b == null) System.out.println("Invalid bus ID.");
        return b;
    }

    static void saveData() {
        try (PrintWriter pw = new PrintWriter("bus_routes.txt")) {
            for (Bus b : buses.values()) {
                pw.println("R|" + b.id);
            }

            for (Ticket t : tickets.values()) {
                pw.println("T|" + t.id + "|" + t.name +
                        "|" + t.bus + "|" + t.seat);
            }

            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("File error: " + e.getMessage());
        }
    }

    static void loadData() {
        File f = new File("bus_routes.txt");

        if (!f.exists()) {
            addBuses();
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split("\\|");

                if (p[0].equals("R") && p.length == 2) {
                    if (p[1].equals("B101"))
                        buses.put("B101", new Bus("B101",
                            new String[]{"Main Gate", "Library",
                            "City Mall", "Railway Station"}));
                    else if (p[1].equals("B102"))
                        buses.put("B102", new Bus("B102",
                            new String[]{"Campus", "Bus Stand",
                            "Hospital", "Airport"}));
                }

                if (p[0].equals("T") && p.length == 5) {
                    Bus b = buses.get(p[3]);
                    if (b != null && b.freeSeat() != null) {
                        b.occupy(p[4], p[2]);
                        tickets.put(p[1],
                            new Ticket(p[1], p[2], p[3], p[4]));
                        try {
                            count = Math.max(count,
                                Integer.parseInt(p[1].substring(2)));
                        } catch (NumberFormatException ignored) {}
                    }
                }
            }

            if (buses.isEmpty()) addBuses();

        } catch (IOException e) {
            System.out.println("Loading error.");
            addBuses();
        }
    }

    static void menu() {
        System.out.println("\n===== BUS ROUTE & TICKETING SYSTEM =====");
        System.out.println("1. Show Routes");
        System.out.println("2. Book Seat");
        System.out.println("3. Search Ticket");
        System.out.println("4. Cancel Ticket");
        System.out.println("5. Show Seats");
        System.out.println("6. Waiting Queue");
        System.out.println("7. Next Stop");
        System.out.println("8. Exit");
        System.out.print("Enter choice: ");
    }

    public static void main(String[] args) {
        loadData();

        while (true) {
            menu();
            String choice = sc.nextLine();

            switch (choice) {
                case "1":
                    for (Bus b : buses.values()) {
                        System.out.println(b.id);
                        b.route.display();
                    }
                    break;

                case "2":
                    Bus b = chooseBus();
                    if (b != null) {
                        System.out.print("Passenger name: ");
                        String name = sc.nextLine().trim();
                        if (!name.isEmpty()) book(b, name);
                        else System.out.println("Name cannot be empty.");
                    }
                    break;

                case "3":
                    System.out.print("Ticket ID: ");
                    search(sc.nextLine().trim());
                    break;

                case "4":
                    System.out.print("Ticket ID: ");
                    cancel(sc.nextLine().trim());
                    break;

                case "5":
                    Bus b1 = chooseBus();
                    if (b1 != null) b1.showSeats();
                    break;

                case "6":
                    Bus b2 = chooseBus();
                    if (b2 != null) {
                        if (b2.waiting.isEmpty())
                            System.out.println("Queue is empty.");
                        else
                            for (Passenger p : b2.waiting)
                                System.out.println(p.name);
                    }
                    break;

                case "7":
                    Bus b3 = chooseBus();
                    if (b3 != null) {
                        System.out.print("Current stop: ");
                        System.out.println(
                            b3.route.nextStop(sc.nextLine().trim()));
                    }
                    break;

                case "8":
                    saveData();
                    System.out.println("Thank you!");
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}