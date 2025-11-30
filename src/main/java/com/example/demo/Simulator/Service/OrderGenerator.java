package com.example.demo.Simulator.Service;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class OrderGenerator {

    private final Random random = new Random();
    private final String[] names = { "RAHUL K", "PRIYA S", "AMIT P", "NEHA M", "RAVI T", "SITA D", "KIRAN J",
            "MAYA R" };
    private final String dummyData = "SOURCEPLATFORMBATCH20250130PROCESSORDERLOADSTATUSPENDINGCHECKSUM7C4A8D09";

    public List<String> generateRandomOrders(int count) {
        List<String> orders = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            StringBuilder order = new StringBuilder();

            order.append("0");
            order.append(String.format("%04d", random.nextInt(9999) + 1));
            order.append(String.format("%04d", random.nextInt(9999) + 1));
            order.append(random.nextBoolean() ? "B" : "S");
            order.append(String.format("TXN%013d", Math.abs(random.nextLong()) % 1000000000000L));

            LocalDateTime now = LocalDateTime.now();
            order.append(now.format(DateTimeFormatter.ofPattern("ddMMyyyyHHmmss")));

            long amount = random.nextInt(999999) + 10000;
            order.append(String.format("%016d", amount));
            order.append(String.format("ACCT%016d", Math.abs(random.nextLong()) % 10000000000000000L));

            String name = names[random.nextInt(names.length)];
            order.append(String.format("%-20s", name));
            order.append(String.format("%09d", random.nextInt(999999999)));

            int year = 1970 + random.nextInt(35);
            int month = 1 + random.nextInt(12);
            int day = 1 + random.nextInt(28);
            order.append(String.format("%02d%02d%d", day, month, year));

            order.append(String.format("Y%016d", random.nextInt(9999) + 1));
            order.append("   ");

            StringBuilder dummy = new StringBuilder();
            while (dummy.length() < 632) {
                dummy.append(dummyData);
            }
            order.append(dummy.substring(0, 632));
            order.append("|");

            orders.add(order.toString());
        }

        return orders;
    }
}