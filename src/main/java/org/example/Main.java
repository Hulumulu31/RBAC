package org.example;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    
    // Количество потоков и длина расчёта (можно менять)
    private static final int THREAD_COUNT = 4;
    private static final int CALCULATION_LENGTH = 50;
    private static final int SYMBOL_DELAY_MS = 100; // задержка между символами прогресс-бара

    public static void main(String[] args) throws InterruptedException {
        // Очищаем экран и перемещаем курсор в начало
        System.out.print("\033[2J\033[H");
        System.out.flush();
        
        // Скрываем курсор для красивого вывода
        System.out.print("\033[?25l");
        System.out.flush();
        
        System.out.println("Запуск многопоточного расчёта...");
        System.out.println("Потоков: " + THREAD_COUNT + ", Длина расчёта: " + CALCULATION_LENGTH);
        System.out.println("=".repeat(80));
        
        // Резервируем место под прогресс-бары (THREAD_COUNT строк)
        for (int i = 0; i < THREAD_COUNT; i++) {
            System.out.println();
        }
        // Резервируем место под сообщения о завершении
        for (int i = 0; i < THREAD_COUNT; i++) {
            System.out.println();
        }
        
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(THREAD_COUNT);
        ReentrantLock printLock = new ReentrantLock();
        
        Thread[] threads = new Thread[THREAD_COUNT];
        
        // Создаём и запускаем потоки
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadNumber = i + 1;
            threads[i] = new Thread(() -> {
                try {
                    // Ждём сигнала старта для одновременного начала
                    startLatch.await();
                    
                    long startTime = System.currentTimeMillis();
                    StringBuilder progressBar = new StringBuilder();
                    
                    // Имитация расчёта с прогресс-баром
                    for (int j = 0; j < CALCULATION_LENGTH; j++) {
                        // Имитация работы
                        try {
                            Thread.sleep(SYMBOL_DELAY_MS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                        
                        progressBar.append("=");
                        
                        // Синхронизированный вывод: каждый поток на своей строке
                        printLock.lock();
                        try {
                            // Перемещаем курсор на строку прогресс-бара
                            // Строки: 1-заголовок1, 2-заголовок2, 3-заголовок3, 4..(3+THREAD_COUNT)-прогрессбары
                            int progressBarLine = 4 + threadNumber - 1;
                            System.out.print("\033[" + progressBarLine + ";1H");
                            
                            // Очищаем строку и выводим прогресс-бар
                            System.out.print("\033[K");
                            System.out.print("[" + threadNumber + "] Поток-" + threadNumber + 
                                " (ID: " + Thread.currentThread().getId() + ") |" + 
                                progressBar + " ".repeat(CALCULATION_LENGTH - progressBar.length()) + "|");
                            System.out.flush(); // Немедленный вывод
                            
                        } finally {
                            printLock.unlock();
                        }
                        
                        // После завершения потока - вывод времени
                        if (j == CALCULATION_LENGTH - 1) {
                            long elapsed = System.currentTimeMillis() - startTime;
                            printLock.lock();
                            try {
                                int doneLine = 4 + THREAD_COUNT + threadNumber - 1;
                                System.out.print("\033[" + doneLine + ";1H");
                                System.out.print("\033[K");
                                System.out.println("[" + threadNumber + "] Завершено за: " + elapsed + " мс");
                                System.out.flush();
                            } finally {
                                printLock.unlock();
                            }
                        }
                    }
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("\nПоток " + threadNumber + " прерван");
                } finally {
                    doneLatch.countDown();
                }
            });
            
            threads[i].start();
        }
        
        // Даём потокам подготовиться и сигнализируем старт
        Thread.sleep(100);
        startLatch.countDown();
        
        // Ждём завершения всех потоков
        doneLatch.await();
        
        // Позиционируем курсор после всех строк
        int finalLine = 4 + THREAD_COUNT * 2;
        System.out.print("\033[" + finalLine + ";1H");
        System.out.println("=".repeat(80));
        System.out.println("Все потоки завершили работу!");
        
        // Показываем курсор обратно
        System.out.print("\033[?25h");
        System.out.flush();
        System.out.println();
    }
}
