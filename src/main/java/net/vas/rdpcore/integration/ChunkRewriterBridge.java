package net.vas.rdpcore.integration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.vas.rdpcore.mutation.MutationRequest;

/**
 * Bridge to RDP Chunk Rewriter mutation engine.
 * 
 * This is a soft-dependency adapter that uses reflection to integrate with
 * the Chunk Rewriter mod. If Chunk Rewriter is not available, mutations are
 * simply dropped with a warning.
 */
public class ChunkRewriterBridge {
    
    private static final Logger LOGGER = LogManager.getLogger("rdpcore");
    
    private static Object chunkRewriterEngine = null;
    private static boolean initialized = false;
    private static boolean available = false;
    
    /**
     * Initialize the Chunk Rewriter bridge (one-time)
     */
    public static synchronized void initialize() {
        if (initialized) return;
        initialized = true;
        
        try {
            // Load RDPChunkRewriter.getMutationEngine()
            Class<?> rdpChunkRewriterClass = Class.forName("com.rdp.chunkrewriter.RDPChunkRewriter");
            java.lang.reflect.Method getMutationEngine = rdpChunkRewriterClass.getMethod("getMutationEngine");
            chunkRewriterEngine = getMutationEngine.invoke(null);
            available = true;
            LOGGER.info("Chunk Rewriter bridge initialized successfully");
        } catch (ClassNotFoundException e) {
            LOGGER.warn("RDP Chunk Rewriter not found in classpath - world mutations will be disabled");
            available = false;
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Chunk Rewriter bridge: {}", e.getMessage());
            available = false;
        }
    }
    
    /**
     * Check if Chunk Rewriter is available
     */
    public static boolean isAvailable() {
        if (!initialized) initialize();
        return available;
    }
    
    /**
     * Submit a mutation request to Chunk Rewriter
     * Returns true if accepted, false otherwise
     */
    public static boolean submitMutation(MutationRequest rdpRequest) {
        if (!isAvailable()) {
            LOGGER.debug("Chunk Rewriter not available, ignoring mutation request");
            return false;
        }
        
        try {
            // Convert RDP request to Chunk Rewriter format
            Object crRequest = convertRequest(rdpRequest);
            if (crRequest == null) {
                LOGGER.warn("Failed to convert mutation request");
                return false;
            }
            
            // Submit via reflection
            java.lang.reflect.Method submitMethod = chunkRewriterEngine.getClass().getMethod(
                "requestRegionMutation", 
                Class.forName("com.rdp.chunkrewriter.api.MutationRequest")
            );
            Object result = submitMethod.invoke(chunkRewriterEngine, crRequest);
            
            // Check if accepted
            java.lang.reflect.Method isAcceptedMethod = result.getClass().getMethod("isAccepted");
            boolean accepted = (boolean) isAcceptedMethod.invoke(result);
            
            if (accepted) {
                java.lang.reflect.Method getQueuedMethod = result.getClass().getMethod("getQueuedChunks");
                int queuedChunks = (int) getQueuedMethod.invoke(result);
                LOGGER.debug("Mutation accepted: {} chunks queued for region ({}, {})", 
                    queuedChunks, rdpRequest.getCenterChunkX(), rdpRequest.getCenterChunkZ());
            } else {
                java.lang.reflect.Method getReasonMethod = result.getClass().getMethod("getFailureReason");
                String reason = (String) getReasonMethod.invoke(result);
                LOGGER.debug("Mutation rejected: {}", reason);
            }
            
            return accepted;
            
        } catch (Exception e) {
            LOGGER.error("Failed to submit mutation to Chunk Rewriter: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Convert RDP MutationRequest to Chunk Rewriter format via reflection
     */
    private static Object convertRequest(MutationRequest rdpRequest) {
        try {
            Class<?> crRequestBuilderClass = Class.forName(
                "com.rdp.chunkrewriter.api.MutationRequest$Builder"
            );
            Object builder = crRequestBuilderClass.newInstance();
            
            // Call builder methods in sequence
            invokeBuilderMethod(builder, crRequestBuilderClass, "center", 
                new Class<?>[] {int.class, int.class}, 
                new Object[] {rdpRequest.getCenterChunkX(), rdpRequest.getCenterChunkZ()});
            
            invokeBuilderMethod(builder, crRequestBuilderClass, "radius",
                new Class<?>[] {int.class},
                new Object[] {rdpRequest.getRadius()});
            
            invokeBuilderMethod(builder, crRequestBuilderClass, "intensity",
                new Class<?>[] {float.class},
                new Object[] {rdpRequest.getIntensity()});
            
            invokeBuilderMethod(builder, crRequestBuilderClass, "priority",
                new Class<?>[] {int.class},
                new Object[] {rdpRequest.getPriority()});
            
            invokeBuilderMethod(builder, crRequestBuilderClass, "budget",
                new Class<?>[] {int.class},
                new Object[] {rdpRequest.getBudget()});
            
            invokeBuilderMethod(builder, crRequestBuilderClass, "profile",
                new Class<?>[] {String.class},
                new Object[] {rdpRequest.getProfile()});
            
            invokeBuilderMethod(builder, crRequestBuilderClass, "cause",
                new Class<?>[] {String.class},
                new Object[] {rdpRequest.getCause()});
            
            // Call build()
            java.lang.reflect.Method buildMethod = crRequestBuilderClass.getMethod("build");
            return buildMethod.invoke(builder);
            
        } catch (Exception e) {
            LOGGER.warn("Failed to convert mutation request: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Helper to invoke builder methods via reflection
     */
    private static void invokeBuilderMethod(Object builder, Class<?> builderClass, 
                                            String methodName, Class<?>[] paramTypes, 
                                            Object[] paramValues) throws Exception {
        try {
            java.lang.reflect.Method method = builderClass.getMethod(methodName, paramTypes);
            method.invoke(builder, paramValues);
        } catch (NoSuchMethodException e) {
            // Method not available, skip silently
        }
    }
}
