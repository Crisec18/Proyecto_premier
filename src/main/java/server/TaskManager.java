package server;

import java.util.UUID;
import java.util.concurrent.*;

//esto es para el simular
public class TaskManager {
    private static final TaskManager INSTANCE = new TaskManager();
    private final ExecutorService executor;
    private final ConcurrentMap<String, Future<?>> tasks = new ConcurrentHashMap<>();

    private TaskManager() {
        this.executor = Executors.newFixedThreadPool(4); // configurable
    }

    public static TaskManager get() { return INSTANCE; }

    public String submit(Callable<?> task) {
        String taskId = UUID.randomUUID().toString();
        Future<?> future = executor.submit(task);
        tasks.put(taskId, future);
        return taskId;
    }

    public Future<?> getFuture(String taskId) {
        return tasks.get(taskId);
    }

    public void remove(String taskId) {
        tasks.remove(taskId);
    }
}
