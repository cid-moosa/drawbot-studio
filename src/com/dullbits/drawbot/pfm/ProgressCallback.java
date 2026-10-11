package com.dullbits.drawbot.pfm;

public interface ProgressCallback {
    void onProgress(int linesGenerated, float progressFraction, String stageMessage);
    boolean isCancelled();
}
