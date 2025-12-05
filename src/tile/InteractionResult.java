package tile;

public class InteractionResult {
    private boolean success;
    private String message;

    public InteractionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Success: " + success + ", Message: " + message;
    }
}