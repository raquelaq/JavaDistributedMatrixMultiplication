package org.example;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class Main {

    public static void main(String[] args) {
        HazelcastInstance hazelcastInstance = Hazelcast.newHazelcastInstance();
        Runtime runtime = Runtime.getRuntime();
        OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

        String[] headers = {"Matrix Size", "Execution Time (ms)", "Memory Used (MB)", "CPU Usage (%)", "Nodes Used", "Network Overhead (ms)", "Data Transfer Time (ms)"};

        int[] sizes = {50, 100, 400, 500, 600, 800, 1024, 2048, 4096};
        for (int size : sizes) {
            int[][] A = new int[size][size];
            int[][] B = new int[size][size];
            BasicMatrixMultiplication.generateMatrix(A, 1, 9);
            BasicMatrixMultiplication.generateMatrix(B, 1, 9);

            // Benchmark Basic
            runtime.gc();
            long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
            long startTime = System.currentTimeMillis();
            BasicMatrixMultiplication.basicMatrixMultiplication(A, B);
            long executionTime = System.currentTimeMillis() - startTime;
            long memoryUsed = runtime.totalMemory() - runtime.freeMemory() - memoryBefore;
            double cpuUsage = osBean.getSystemCpuLoad() * 100;

            CSVWriterUtility.writeToCSV("basic_results.csv", headers, new String[]{String.valueOf(size), String.valueOf(executionTime), String.valueOf(memoryUsed / (1024 * 1024.0)), String.valueOf(cpuUsage), "N/A", "N/A", "N/A"});

            // Benchmark Parallel
            runtime.gc();
            memoryBefore = runtime.totalMemory() - runtime.freeMemory();
            startTime = System.currentTimeMillis();
            ParallelMatrixMultiplication.multiplyMatricesParallel(A, B);
            executionTime = System.currentTimeMillis() - startTime;
            memoryUsed = runtime.totalMemory() - runtime.freeMemory() - memoryBefore;
            cpuUsage = osBean.getSystemCpuLoad() * 100;

            CSVWriterUtility.writeToCSV("parallel_results.csv", headers, new String[]{String.valueOf(size), String.valueOf(executionTime), String.valueOf(memoryUsed / (1024 * 1024.0)), String.valueOf(cpuUsage), "N/A", "N/A", "N/A"});

            // Benchmark Distributed
            runtime.gc();
            long startTransfer = System.currentTimeMillis();
            int nodesUsed = hazelcastInstance.getCluster().getMembers().size();
            long endTransfer = System.currentTimeMillis();
            long transferTime = endTransfer - startTransfer;

            memoryBefore = runtime.totalMemory() - runtime.freeMemory();
            startTime = System.currentTimeMillis();
            DistributedMatrixMultiplication.performDistributedMatrixMultiplication(A, B, hazelcastInstance);
            executionTime = System.currentTimeMillis() - startTime;
            memoryUsed = runtime.totalMemory() - runtime.freeMemory() - memoryBefore;

            CSVWriterUtility.writeToCSV("distributed_results.csv", headers, new String[]{String.valueOf(size), String.valueOf(executionTime), String.valueOf(memoryUsed / (1024 * 1024.0)), String.valueOf(cpuUsage), String.valueOf(nodesUsed), String.valueOf(transferTime), String.valueOf(transferTime)});
        }

        hazelcastInstance.shutdown();
    }
}
