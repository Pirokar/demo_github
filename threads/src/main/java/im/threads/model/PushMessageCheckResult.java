package im.threads.model;

/**
 * Created by chybakut2004 on 25.04.17.
 */

public class PushMessageCheckResult {

    private boolean detected;
    private boolean needsShowIsStatusBar;

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
