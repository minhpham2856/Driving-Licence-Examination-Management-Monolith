package shared;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExamQueue {

    private static Queue<Integer> queueTheory = new ConcurrentLinkedQueue<>();
    private static Queue<Integer> queueLayout = new ConcurrentLinkedQueue<>();

}
