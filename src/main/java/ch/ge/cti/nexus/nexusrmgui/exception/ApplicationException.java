package ch.ge.cti.nexus.nexusrmgui.exception;

/**
 * Une exception levee durant l'execution du batch.
 */
public class ApplicationException extends RuntimeException {

    public ApplicationException(String msg) {
        super(msg);
    }

    public ApplicationException(Exception e) {
        super(e);
    }

}
