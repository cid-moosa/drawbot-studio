package com.dullbits.drawbot.pfm;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.image.ImagePipeline;
import java.util.Map;

/**
 * Standard interface for all Drawbot Path Finding Modules (PFMs).
 * Decouples artistic algorithms from the UI and main lifecycle.
 */
public interface PathFindingModule {
    String getId();
    String getName();
    String getDescription();

    void preProcessing(ImagePipeline pipeline, MachineConfig config);
    void findPath(ImagePipeline pipeline, SegmentBuffer buffer, MachineConfig config, ProgressCallback callback);
    void postProcessing(SegmentBuffer buffer, PenDistribution distribution);

    Map<String, Object> getParameters();
    void setParameter(String key, Object value);
}
