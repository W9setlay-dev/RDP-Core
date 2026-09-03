package net.vas.rdpcore.mutation;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.vas.rdpcore.integration.ChunkRewriterBridge;

/**
 * Coordinates all mutation requests to the RDP Chunk Rewriter.
 * Ensures proper prioritization, budgeting, and execution tracking.
 */
public class MutationCoordinator {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    private static final MutationCoordinator INSTANCE = new MutationCoordinator();
    
    private Queue<MutationRequestWrapper> priorityQueue = new PriorityQueue<>(
        (a, b) -> Integer.compare(b.priority, a.priority)
    );
    
    private Map<String, MutationResultTracking> executionStatus = new HashMap<>();
    private int totalQueued = 0;
    private int totalAccepted = 0;
    private int totalRejected = 0;
    private int totalFailed = 0;
    private long lastExecutionTime = 0L;
    
    public static MutationCoordinator getInstance() {
        return INSTANCE;
    }
    
    static {
        // Initialize Chunk Rewriter bridge on class load
        ChunkRewriterBridge.initialize();
    }
    
    /**
     * Queue a mutation request to the Chunk Rewriter
     */
    public void queueMutation(MutationRequest request, int priority) {
        String requestId = UUID.randomUUID().toString();
        MutationRequestWrapper wrapper = new MutationRequestWrapper(requestId, request, priority);
        priorityQueue.add(wrapper);
        executionStatus.put(requestId, new MutationResultTracking(requestId));
        totalQueued++;
    }
    
    /**
     * Process queued mutations up to the specified budget
     * Returns number of mutations processed
     */
    public int processQueuedMutations(int budget) {
        int processed = 0;
        long startTime = System.currentTimeMillis();
        
        while (!priorityQueue.isEmpty() && processed < budget) {
            MutationRequestWrapper wrapper = priorityQueue.poll();
            MutationResultTracking tracking = executionStatus.get(wrapper.requestId);
            
            try {
                // Submit to Chunk Rewriter via bridge
                boolean accepted = ChunkRewriterBridge.submitMutation(wrapper.request);
                
                if (accepted) {
                    tracking.status = MutationRequestStatus.ACCEPTED;
                    totalAccepted++;
                } else {
                    tracking.status = MutationRequestStatus.REJECTED;
                    totalRejected++;
                }
                
            } catch (Exception e) {
                tracking.status = MutationRequestStatus.FAILED;
                tracking.failureReason = e.getMessage();
                totalFailed++;
                LOGGER.warn("Mutation processing failed: {}", e.getMessage());
            }
            
            processed++;
        }
        
        lastExecutionTime = System.currentTimeMillis() - startTime;
        return processed;
    }
    
    /**
     * Get the status of a queued mutation
     */
    public MutationRequestStatus getStatus(String requestId) {
        MutationResultTracking tracking = executionStatus.get(requestId);
        return tracking != null ? tracking.status : MutationRequestStatus.UNKNOWN;
    }
    
    public int getQueuedCount() {
        return priorityQueue.size();
    }
    
    public int getTotalQueued() {
        return totalQueued;
    }
    
    public int getTotalAccepted() {
        return totalAccepted;
    }
    
    public int getTotalRejected() {
        return totalRejected;
    }
    
    public int getTotalFailed() {
        return totalFailed;
    }
    
    public long getLastExecutionTime() {
        return lastExecutionTime;
    }
    
    /**
     * Wrapper for tracking mutation requests
     */
    private static class MutationRequestWrapper {
        String requestId;
        MutationRequest request;
        int priority;
        
        MutationRequestWrapper(String id, MutationRequest req, int pri) {
            this.requestId = id;
            this.request = req;
            this.priority = pri;
        }
    }
    
    /**
     * Tracking data for mutation processing
     */
    private static class MutationResultTracking {
        String requestId;
        MutationRequestStatus status = MutationRequestStatus.QUEUED;
        String failureReason = "";
        long submittedTime = System.currentTimeMillis();
        
        MutationResultTracking(String id) {
            this.requestId = id;
        }
    }
    
    public enum MutationRequestStatus {
        QUEUED,
        ACCEPTED,
        REJECTED,
        FAILED,
        UNKNOWN
    }
}
