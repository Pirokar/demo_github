package im.threads.internal.model;

public class PushMessageCheckResult {

    private boolean detected = false;
    private boolean needsShowIsStatusBar = false;

    public boolean isDetected() {
        return detected;
    }

    public void setDetected(boolean detected) {
        this.detected = detected;
    }

    public boolean isNeedsShowIsStatusBar() {
        return needsShowIsStatusBar;
    }

    public void setNeedsShowIsStatusBar(boolean needsShowIsStatusBar) {
        this.needsShowIsStatusBar = needsShowIsStatusBar;
    }
}
