package com.dullbits.drawbot.async;

import com.dullbits.drawbot.color.PenDistribution;
import com.dullbits.drawbot.core.MachineConfig;
import com.dullbits.drawbot.core.SegmentBuffer;
import com.dullbits.drawbot.image.ImagePipeline;
import com.dullbits.drawbot.pfm.PathFindingModule;
import com.dullbits.drawbot.pfm.ProgressCallback;
import com.dullbits.drawbot.ui.DrawingCanvas;
import com.dullbits.drawbot.ui.StatusBarPanel;
import java.util.List;
import javax.swing.SwingWorker;

/**
 * Background worker thread decoupling path generation from the Swing Event Dispatch Thread (EDT).
 * Provides smooth 60 FPS UI responsiveness, throttled canvas repaints, and instant cancellation.
 */
public final class GenerationWorker extends SwingWorker<Void, GenerationWorker.ProgressUpdate> {

    public static final class ProgressUpdate {
        public final int lines;
        public final float fraction;
        public final String message;

        public ProgressUpdate(int lines, float fraction, String message) {
            this.lines = lines;
            this.fraction = fraction;
            this.message = message;
        }
    }

    private final PathFindingModule pfm;
    private final ImagePipeline pipeline;
    private final SegmentBuffer buffer;
    private final MachineConfig config;
    private final PenDistribution distribution;
    private final DrawingCanvas canvas;
    private final StatusBarPanel statusBar;
    private final Runnable onCompleted;

    private long lastPublishTime = 0;

    public GenerationWorker(PathFindingModule pfm, ImagePipeline pipeline,
                            SegmentBuffer buffer, MachineConfig config,
                            PenDistribution distribution, DrawingCanvas canvas,
                            StatusBarPanel statusBar, Runnable onCompleted) {
        this.pfm = pfm;
        this.pipeline = pipeline;
        this.buffer = buffer;
        this.config = config;
        this.distribution = distribution;
        this.canvas = canvas;
        this.statusBar = statusBar;
        this.onCompleted = onCompleted;
    }

    @Override
    protected Void doInBackground() throws Exception {
        buffer.clear();
        pipeline.resetWorkingToReference();

        ProgressCallback callback = new ProgressCallback() {
            @Override
            public void onProgress(int linesGenerated, float progressFraction, String stageMessage) {
                long now = System.currentTimeMillis();
                // Throttle UI update calls to ~30 FPS
                if (now - lastPublishTime >= 33) {
                    lastPublishTime = now;
                    publish(new ProgressUpdate(linesGenerated, progressFraction, stageMessage));
                }
            }

            @Override
            public boolean isCancelled() {
                return GenerationWorker.this.isCancelled();
            }
        };

        pfm.findPath(pipeline, buffer, config, callback);
        pfm.postProcessing(buffer, distribution);
        return null;
    }

    @Override
    protected void process(List<ProgressUpdate> chunks) {
        if (!chunks.isEmpty()) {
            ProgressUpdate latest = chunks.get(chunks.size() - 1);
            statusBar.setProgress(latest.fraction, true);
            statusBar.setStatus(latest.message);
            statusBar.updateStats(latest.lines);
            canvas.updateProcessingSubtitle(String.format("%,d lines | %.0f%% complete", latest.lines, latest.fraction * 100));
            canvas.setDisplayLineCount(latest.lines);
            canvas.repaint();
        }
    }

    @Override
    protected void done() {
        statusBar.setProgress(0, false);
        canvas.setProcessing(false, null, null);
        if (isCancelled()) {
            statusBar.stopProcessing("Generation cancelled by user.");
        } else {
            statusBar.stopProcessing(String.format("Generation complete: %,d lines created.", buffer.getLineCount()));
        }
        statusBar.updateStats(buffer.getLineCount());
        canvas.setDisplayLineCount(buffer.getLineCount());
        canvas.repaint();

        if (onCompleted != null) {
            onCompleted.run();
        }
    }
}
