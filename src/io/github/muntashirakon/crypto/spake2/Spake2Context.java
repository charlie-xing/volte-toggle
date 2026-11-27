package io.github.muntashirakon.crypto.spake2;

import java.security.SecureRandom;
import javax.security.auth.Destroyable;

/**
 * Pure-JVM SPAKE2 bridge backed by {@link com.flyfish233.crypto.spake2.Spake2Context}
 * and {@link SecureRandom}, replacing the vulnerable native libspake2.so implementation.
 */
public final class Spake2Context implements Destroyable {

    private final com.flyfish233.crypto.spake2.Spake2Context delegate;

    public Spake2Context(Spake2Role myRole, byte[] myName, byte[] theirName) {
        com.flyfish233.crypto.spake2.Spake2Role role =
                (myRole == Spake2Role.Bob)
                        ? com.flyfish233.crypto.spake2.Spake2Role.Bob
                        : com.flyfish233.crypto.spake2.Spake2Role.Alice;
        this.delegate = new com.flyfish233.crypto.spake2.Spake2Context(
                role, myName, theirName, new SecureRandom());
    }

    public final byte[] generateMessage(byte[] password) throws IllegalStateException {
        return delegate.generateMessage(password);
    }

    public final byte[] processMessage(byte[] theirMsg) throws IllegalStateException {
        return delegate.processMessage(theirMsg);
    }

    @Override
    public boolean isDestroyed() {
        return delegate.isDestroyed();
    }

    @Override
    public void destroy() {
        delegate.destroy();
    }
}
