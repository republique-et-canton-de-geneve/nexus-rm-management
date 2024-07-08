package ch.ge.cti.nexus.nexusrmgui.exception;

/**
 * An exception thrown during the execution of the batch.
 */
public class ApplicationException extends RuntimeException {

    public ApplicationException(String msg) {
        super(msg);
    }

    public ApplicationException(Exception e) {
        super(e);
    }

}
